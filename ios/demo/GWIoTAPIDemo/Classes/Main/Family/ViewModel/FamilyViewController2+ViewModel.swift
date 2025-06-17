//
//  FamilyViewController2+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 17/2/2024.
//

import Foundation
import RQCore
import GWIoTBridgeReoqooKit

extension FamilyViewController2 {
    class ViewModel {

        /// 设备分享请求发布者
        public lazy var deviceShareInviteObservable: PassthroughSubject<MessageCenter.DeviceShareInviteModel, Never> = .init()

        /// 供 View 监听, 以弹出 浮窗推广
        public let floatingPromotionSubject: PassthroughSubject<IVBBSMgr.Banner, Never> = .init()

        /// 供 View 监听, 以弹出 首页顶部Banner
        public let headerBannerSubject: PassthroughSubject<IVBBSMgr.Banner, Never> = .init()

        /// 供 View 监听, 以弹出 首页重要公告
        @DidSetPublished public var importantBannerItem: ImportantNoticeItem?

        /// 当分享邀请被处理(接受/拒绝)后, 会触发此发布者
        public let shareInviteHandlingResultObservable: PassthroughSubject<(String, Bool), Never> = .init()

        private var anyCancellables: Set<AnyCancellable> = []

        init() {
            /// 这里监听p2p的在线消息（设备分享）
            // 为了使 APP 启动后取一次消息, 以 NOTIFY_USER_MSG_UPDATE 作为首发元素
            let p2pMsg = P2POnlineMsg.init(topic: P2POnlineMsg.TopicType.NOTIFY_USER_MSG_UPDATE.rawValue)
            Publishers.CombineLatest(RQCore.Agent.shared.$linkStatus, RQCore.Agent.shared.$p2pOnlineMsg.prepend(p2pMsg))
                .sink(receiveValue: { [weak self] linkStatus, onlineMsg in
                    if linkStatus != .online { return }
                    switch onlineMsg.topicType {
                    case .NOTIFY_USER_MSG_UPDATE, .UNKNOWN:
                        // 刷新邀请消息
                        self?.checkOutInviteMessage()
                        // 刷新公告消息
                        self?.fetchNotices()
                    case .NOTIFY_SYS: //消息中心刷新通知
                        if let deviceId = onlineMsg.isGuestDidBind {
                            // 接受设备分享通知
                            self?.shareInviteHandlingResultObservable.send((deviceId, true))
                        } else if let deviceId = onlineMsg.isGuestDidUnbind {
                            // 移除访客通知
                            self?.shareInviteHandlingResultObservable.send((deviceId, false))
                        }
                    default: break
                    }
                }).store(in: &self.anyCancellables)
        }

        // 展示浮窗/顶部banner/重要
        func fetchNotices() {
            let bbsMsgMgr = RQCore.Agent.shared.ivBBSMgr
            bbsMsgMgr.checkOut { [weak bbsMsgMgr, weak self] suc in
                if let banner = bbsMsgMgr?.getBannerInfo(of: .floatingIcon) {
                    logInfo("展示公告浮窗: ", banner.picUrl as Any, banner.url as Any)
                    self?.floatingPromotionSubject.send(banner)
                }
                if let banner = bbsMsgMgr?.getBannerInfo(of: .home) {
                    logInfo("展示首页顶部Banner: ", banner.picUrl as Any, banner.url as Any)
                    self?.headerBannerSubject.send(banner)
                }

                if let banners = bbsMsgMgr?.getBannerInfo(showOpt: .deviceList),
                      let bbsNotices = bbsMsgMgr?.getNotices(of: .none),
                      let targetBanner = banners.filter({ $0.tag == "important_Home" }).first,
                      let targetNotice = bbsNotices.filter({ $0.tag == targetBanner.noticeTag }).first {

                    logInfo("展示首页重要公告: ", targetNotice.title as Any, targetBanner.url as Any)

                    var item = ImportantNoticeItem()
                    item.noticeTag = targetBanner.noticeTag ?? ""
                    item.url = targetBanner.url ?? ""
                    item.showOpt = targetBanner.showOpt ?? .deviceList
                    item.tag = targetBanner.tag
                    item.title = targetNotice.title ?? ""

                    self?.importantBannerItem = item
                }

            }
        }

        // 获取用户邀请消息
        func checkOutInviteMessage() {
            let userMsgMgr = RQCore.Agent.shared.ivUserMsgMgr
            userMsgMgr.checkOut { [weak userMsgMgr, weak self] success in
                if !success { return }
                let messages = userMsgMgr?.getMessages(isRead: .unread, isExpired: .notExpire) ?? []
                // 取出分享消息
                guard let inviteMsg = messages.filter({ $0.type == .share }).first, let jsonStr = inviteMsg.data else { return }
                let json = JSON.init(parseJSON: jsonStr)
                // 组件模型
                var inviteModel = MessageCenter.DeviceShareInviteModel()
                inviteModel.msgId = inviteMsg.msgId
                inviteModel.url = json["url"].stringValue
                inviteModel.deviceId = json["deviceId"].stringValue
                inviteModel.showWay = json["showWay"].intValue
                inviteModel.shareToken = json["shareToken"].stringValue
                inviteModel.inviteAccount = json["inviteAccount"].stringValue
                // 发布者发布邀请
                self?.deviceShareInviteObservable.send(inviteModel)
            }
        }

        // 重要公告已读
        public func readImportantNotice() {
            guard let tag = self.importantBannerItem?.tag else { return }
            RQCore.Agent.shared.ivBBSMgr.updateNoticeStatus(true, tag: tag) { jsonStr, err in }
        }
    }
}
