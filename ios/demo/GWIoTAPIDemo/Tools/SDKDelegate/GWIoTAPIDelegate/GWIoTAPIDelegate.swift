//
//  GWIoTAPIDelegate.swift
//  GWIoTAPIDemo
//
//  Created by xiaojuntao on 27/2/2025.
//

import Foundation
import GWIoTBridgeYooseeKit
import GWIoTApi
import RQCore

class GWUserInfo: IUserAccessInfo {
    var accessId: String
    
    var accessToken: String
    
    var area: String
    
    var expireTime: String
    
    var terminalId: String
    
    var userId: String
    
    var regRegion: String
    
    init(accessId: String, accessToken: String, area: String, expireTime: String, terminalId: String, userId: String, regRegion: String) {
        self.accessId = accessId
        self.accessToken = accessToken
        self.area = area
        self.expireTime = expireTime
        self.terminalId = terminalId
        self.userId = userId
        self.regRegion = regRegion
    }
}

class GWDevice: IDevice {
    var solution: GWIoTApi.Solution
    var deviceId: String
    
    init(solution: GWIoTApi.Solution, deviceId: String) {
        self.solution = solution
        self.deviceId = deviceId
    }
}

/// 负责对接 YooseeKit 相关业务及充当代理
class GWIoTAPIDelegate {
    static let shared: GWIoTAPIDelegate = .init()
    
    var anyCancellables: Set<AnyCancellable> = []
    
    private init() {}
    
    func startObserve() {
        // Observe user login / logout
        AccountCenter.shared.$currentUser.sink(
            receiveValue: { user in
                if let user = user {
                    let userInfo = GWUserInfo.init(
                        accessId: user.basicInfo.accessId,
                        accessToken: user.basicInfo.accessToken,
                        area: user.basicInfo.area,
                        expireTime: String(
                            user.basicInfo.expireTime
                        ),
                        terminalId: user.basicInfo.terminalId,
                        userId: user.basicInfo.userId,
                        regRegion: user.basicInfo.regRegion
                    )
                    GWIoT.shared.login(accessInfo: userInfo)
                } else {
                    GWIoT.shared.logout { res, err in
                        
                    }
                }
            }).store(in: &self.anyCancellables)
    }
    
    
    /// 打开 Yoosee 方案设备监控页
    static func openMonitor(deviceId: String, solution: GWIoTApi.Solution) {
        let device = GWDevice(solution: solution, deviceId: deviceId)
        device.deviceId = deviceId
        
        let opts = OpenPluginOption(device: device)
        
        GWIoT.shared.openHome(opts: opts) { result, err in
            switch(gwiot_handleCb(result, err)) {
            case .success(_): break
            case .failure(let err): logError("openHome failed", err)
            }
        }
    }
    
    /// 打开 Yoosee 方案设备回放
    static func openPlayback(deviceId: String, solution: GWIoTApi.Solution) {
        let device = GWDevice(solution: .yoosee, deviceId: deviceId)
        device.deviceId = deviceId
        
        let opts = PlaybackOption(device: device)
        
        GWIoT.shared.openPlayback(opts: opts) { result, err in
            switch(gwiot_handleCb(result, err)) {
            case .success(_): break
            case .failure(let err): logError("openPlayback failed", err)
            }
        }
    }
}
