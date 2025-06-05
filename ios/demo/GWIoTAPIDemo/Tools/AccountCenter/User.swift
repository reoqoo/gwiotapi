//
//  User.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 18/7/2023.
//

import Foundation
import CryptoSwift
import GWIoTApi
import RQCore
import RQApi

extension User {
    /// 审核期间此 user 提供给 apple 登录, 隐藏云服务入口
    static var SUPERVIP_userId: String = ""
}

class User: Codable {

    /// 数据持久化盐. 16位字符串, 采用 128 AES 加密. ECB模式
    static let storeSalt: String = "_ReOQoO_IsgReat_"

    @DidSetPublished var isLogin: Bool = false
    /// 登录/注册接口返回的用户信息, 例如 userid, token 等
    @DidSetPublished var basicInfo: RQCore.LoginInfo
    /// 用户信息 /app/user/infoQuery
    @DidSetPublished var profileInfo: RQCore.ProfileInfo?

    /// 是否超级VIP
    var isSuperVip: Bool { self.basicInfo.userId == User.SUPERVIP_userId }

    /// 该用户专属 UserDefaults
    lazy var userDefault: UserDefaults? = .init(suiteName: self.basicInfo.userId)

    init(userInfo: LoginInfo) {
        self.basicInfo = userInfo
    }

    required init(from decoder: Decoder) throws {
        self.isLogin = (try? decoder.decode("isLogin")) ?? false
        do {
            self.basicInfo = try decoder.decode("basicInfo")
        } catch let err {
            logError("[AccountCenter] decode User.basicInfo 时遇到错误", err)
            throw err
        }
        do {
            self.profileInfo = try decoder.decode("profileInfo")
        } catch let err {
            logError("[AccountCenter] decode User.basicInfo 时遇到错误", err)
            throw err
        }
    }

    func encode(to encoder: Encoder) throws {
        try? encoder.encode(self.isLogin, for: "isLogin")
        try? encoder.encode(self.basicInfo, for: "basicInfo")
        try? encoder.encode(self.profileInfo, for: "profileInfo")
    }

    private var anyCancellables: Set<AnyCancellable> = []

    /// 用户信息文件夹路径: ~/Documents/ReoqooUsers/"UserID"/
    lazy var userFolderPath: String = {
        let path = AccountCenter.usersInfoFolder + "/" + self.basicInfo.userId
        if !FileManager.default.fileExists(atPath: path) {
            try! FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: true)
        }
        return path
    }()

    func store() {
        let infFilePath = self.userFolderPath + "/" + AccountCenter.userInfoFileName
        do {
            let jsonData = try self.encoded()
            let jsonStr = String.init(data: jsonData, encoding: .utf8)
            // 加密
            let encrypt = try jsonStr?.encryptToBase64(cipher: AES(key: Self.storeSalt.bytes, blockMode: ECB()))
            // 写入
            try encrypt?.write(toFile: infFilePath, atomically: true, encoding: .utf8)
        } catch let err {
            logError("[AccountCenter] 持久化 User 时发生错误, User: \(self)\n, Error: \(err)")
        }
    }

    /// 删除持久化在本地的用户信息
    func deleteStored() {
        do {
            try FileManager.default.removeItem(atPath: self.userFolderPath)
        } catch let err {
            logInfo("[AccountCenter] 删除本地用户信息失败", err)
        }
    }

    // 同步 APNS token
    func syncAPNSToken(_ apnsToken: String) {
        self.syncAPNSTokenObservable(apnsToken: apnsToken)
            .sink(receiveCompletion: {
                guard case let .failure(err) = $0 else { return }
                logError("[AccountCenter] 同步 APNS Token 到服务器失败: ", err)
            }, receiveValue: {
                logInfo("[AccountCenter] 同步 APNS Token 到服务器成功")
            }).store(in: &self.anyCancellables)
    }

    // 尝试刷新登录 token
    func tryRefreshToken() {
        let now = Date().timeIntervalSince1970
        // 检查上次更新时间距今是否超过1天
        if let latestUpdateTime: TimeInterval = self.userDefault?.object(forKey: UserDefaults.UserKey.Reoqoo_LatestUpdateAccessTokenTime.rawValue) as? TimeInterval,
            latestUpdateTime + 86400 > now {
            return
        }
        /// 2023/12/25: 由于 StandardConfiguration.shared 在 init 时 会触发此方法调用, 但是 iotVideo 的 host 还没有被设置, 这将导致 获取刷新用户Token接口请求了错误的 host, 所以稍微延后此方法的调用
        self.refreshTokenObservable()
            .delaySubscription(for: 1)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {
                guard case let .failure(err) = $0 else { return }
                logError("[AccountCenter] 刷新 accesstoken 接口请求失败了", err)
            }, receiveValue: { [weak self] result in
                self?.didUpdateAccessTokenSuccess(result.accessToken, expireTime: result.expireTime)
                // 更新更新时间
                self?.userDefault?.set(Date().timeIntervalSince1970, forKey: UserDefaults.UserKey.Reoqoo_LatestUpdateAccessTokenTime.rawValue)
                self?.userDefault?.synchronize()
                logInfo("[AccountCenter] 刷新 accesstoken 成功")
            }).store(in: &self.anyCancellables)
    }
    
    /// 在网路请求 tryRefreshToken 成功后执行
    func didUpdateAccessTokenSuccess(_ accessToken: String, expireTime: TimeInterval) {
        self.basicInfo.accessToken = accessToken
        self.basicInfo.expireTime = expireTime
        // 更新token后, 持久化用户数据
        self.store()
        /// 更新 Agent.shared.loginInfo
        RQCore.Agent.shared.updateAccessToken(accessToken, expireTime: expireTime)
    }

    /// 更新用户 profile info
    /// 如遇失败, 自动重试, 间隔 5秒
    func updateUserProfileInfo() {
        /// 2023/12/5: 由于 StandardConfiguration.shared 在 init 时 会触发此方法调用, 但是 iotVideo 的 host 还没有被设置, 这将导致 获取用户信息接口请求了错误的 host, 所以稍微延后此方法的调用
        self.queryUserProfileInfoPublisher()
            .delaySubscription(for: 0.2)
            .catch({ err in
                Fail(error: err).delay(for: 5, scheduler: DispatchQueue.main)
            })
            .retry(30)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {
                guard case let .failure(err) = $0 else { return }
                logError("[AccountCenter] 刷新 User.ProfileInfo 接口请求失败了", err)
            }, receiveValue: { [weak self] profileInfo in
                self?.profileInfo = profileInfo
            }).store(in: &self.anyCancellables)
    }

}

// MARK: Observable 封装
extension User {

    // MARK: 刷新登录 token
    typealias RefreshTokenResult = (accessToken: String, expireTime: TimeInterval)
    private func refreshTokenObservable() -> AnyPublisher<RefreshTokenResult, Swift.Error> {
        Deferred {
            Future { promise in
                RQApi.Api.updateAccessToken(uniqueId: UIDevice.current.identifierForVendor?.uuidString) {
                    let res = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                    if case let .success(json) = res {
                        guard let accessToken = json["data"]["accessToken"].string, let expireTime = json["data"]["expireTime"].double else {
                            promise(.failure(ReoqooError.generalError(reason: .optionalTypeUnwrapped)))
                            return
                        }
                        promise(.success((accessToken, expireTime)))
                    }
                    if case .failure(let failure) = res {
                        promise(.failure(failure))
                    }
                }
            }
        }
        .eraseToAnyPublisher()
    }

    // MARK: 同步推送TOKEN
    private func syncAPNSTokenObservable(apnsToken: String) -> AnyPublisher<Void, Swift.Error> {
        let termId = self.basicInfo.terminalId
        return Future { promise in
            RQApi.Api.syncAPNSToken(terminalId: termId, apnsToken: apnsToken) {
                let res = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                if case .success = res {
                    promise(.success(()))
                }
                if case .failure(let failure) = res {
                    promise(.failure(failure))
                }
            }
        }.eraseToAnyPublisher()
    }

    // MARK: 用户信息
    // 获取用户信息
    private func queryUserProfileInfoPublisher() -> AnyPublisher<ProfileInfo, Swift.Error> {
        Deferred {
            Future<JSON, Swift.Error> { promise in
                RQApi.Api.queryUserProfileInfo {
                    let res = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                    if case let .success(json) = res {
                        promise(.success(json))
                    }
                    if case .failure(let error) = res {
                        promise(.failure(error))
                    }
                }
            }.tryMap {
                try $0["data"].decoded(as: ProfileInfo.self)
            }
        }.eraseToAnyPublisher()
    }

    /// 修改用户信息发布者
    /// 修改完毕后 flatMap 更新 user profile 的 发布者, 以更新 self.userProfile 的值
    func modifyUserInfoPublisher(header: String?, nick: String?, oldPassword: String?, newPassword: String?) -> AnyPublisher<ProfileInfo, Swift.Error> {
        Deferred {
            Future<Void, Swift.Error> { promise in
                RQApi.Api.modifyUserInfo(header: header, nick: nick, oldPassword: oldPassword, newPassword: newPassword, uniqueId: UIDevice.current.identifierForVendor?.uuidString) {
                    let res = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                    if case .success = res {
                        promise(.success(()))
                    }
                    if case .failure(let error) = res {
                        promise(.failure(error))
                    }
                }
            }
        }.flatMap { [weak self] _ in
            guard let queryUserProfileInfoPublisher = self?.queryUserProfileInfoPublisher() else {
                return Fail<ProfileInfo, Swift.Error>(error: ReoqooError.generalError(reason: .optionalTypeUnwrapped)).eraseToAnyPublisher()
            }
            return queryUserProfileInfoPublisher
        }.handleEvents(receiveOutput: { [weak self] profile in
            self?.profileInfo = profile
        })
        .eraseToAnyPublisher()
    }
    
    /// 绑定 邮箱 / 手机号 发布者
    func bindMobileOrEmailPublisher(accountType: RQApi.AccountType, oneTimeCode: String) -> AnyPublisher<Void, Swift.Error> {
        Deferred {
            Future { promise in
                RQApi.Api.bindAccount(accountType, oneTimeCode: oneTimeCode) {
                    let res = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                    if case .success = res {
                        promise(.success(()))
                    }
                    if case .failure(let err) = res {
                        promise(.failure(err))
                    }
                }
            }
        }.handleEvents(receiveOutput: { [weak self] _ in
            // 请求成功后更新用户信息
            self?.updateUserProfileInfo()
        }).eraseToAnyPublisher()
    }
}
