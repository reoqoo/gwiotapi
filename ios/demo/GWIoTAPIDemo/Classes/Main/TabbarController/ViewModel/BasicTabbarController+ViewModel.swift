//
//  BasicTabbarController+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 14/12/2023.
//

import Foundation
import RQCore
import RQDeviceAddition
import GWIoTBridgeReoqooKit
import IVAccountMgr

extension BasicTabbarController {
    class ViewModel {
        
        var anyCancellables: Set<AnyCancellable> = []
        
        /// 供 View 监听, 发布 用户消息, 以弹出 H5 推广页面
        let usrMsgEventSubject: Combine.PassthroughSubject<(IVUserMessageMgr.Message), Never> = .init()
        
        // 供 View 监听, 发布 公告消息, 以弹出 H5 推广页面
        let promotionH5PopupEventSubject: Combine.PassthroughSubject<IVBBSMgr.Notice, Never> = .init()
        
        init() {
            // 为了使 APP 启动后取一次消息, 以 NOTIFY_USER_MSG_UPDATE 作为首发元素
            let p2pMsg = P2POnlineMsg.init(topic: P2POnlineMsg.TopicType.NOTIFY_USER_MSG_UPDATE.rawValue)
            Publishers.CombineLatest(RQCore.Agent.shared.$linkStatus, GWIoTBridgeReoqooKit.Bridge.shared.$p2pOnlineMsg.prepend(p2pMsg))
                .sink(receiveValue: { [weak self] linkStatus, msg in
                    if linkStatus != .online { return }
                    if msg.topicType == P2POnlineMsg.TopicType.NOTIFY_USER_MSG_UPDATE || msg.topicType == P2POnlineMsg.TopicType.UNKNOWN {
                        self?.fetchH5Msg(ignoreDeviceFirstBind: true)
                        self?.fetchH5Banner()
                    }
                }).store(in: &self.anyCancellables)
            
            // 监听设备绑定成功通知, 查询用户消息, 以弹出首绑推广H5
            NotificationCenter.default.publisher(for: RQDeviceAddition.Agent.didFinishBindNotification).sink { [weak self] _ in
                self?.fetchH5Msg(ignoreDeviceFirstBind: false)
            }.store(in: &self.anyCancellables)
        }
        
        // 展示用户消息H5弹窗 notice/usrmsg
        func fetchH5Msg(ignoreDeviceFirstBind: Bool) {
            let userMsgMgr = RQCore.Agent.shared.ivUserMsgMgr
            userMsgMgr.checkOut { [weak userMsgMgr, weak self] msgs_list in
                let msgList = userMsgMgr?.getMessages(of: [.h5], isRead: .unread, isExpired: .notExpire)
                // 取出消息
                guard let msg = msgList?.first, let data = msg.model as? IVUserMessageMgr.PopupH5TagData else { return }
                // 如果是首绑消息, 按 ignoreDeviceFirstBind 参数过滤一下
                if ignoreDeviceFirstBind && data.msgType == "VasPromotion" { return }
                self?.usrMsgEventSubject.send(msg)
            }
        }
        
        // 展示公告H5弹窗
        func fetchH5Banner() {
            let bbsMsgMgr = RQCore.Agent.shared.ivBBSMgr
            bbsMsgMgr.checkOut { [weak bbsMsgMgr, weak self] suc in
                let notice = bbsMsgMgr?.getNotices(of: .deviceList)?.filter({ $0.showOpt != .none }).first
                guard let notice = notice else { return }
                self?.promotionH5PopupEventSubject.send(notice)
            }
        }
    }
}
