//
//  SystemMessageViewController+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/2/2024.
//

import Foundation
import IVMessageMgr
import RQCore

extension SystemMessageViewController.ViewModel {
    enum Status {
        case idle
        case messageItemUnreadStatusDidRefresh(item: MessageCenter.FirstLevelMessageItem)
        case didSweepAllUnread
    }

    enum Event {
        case viewDidLoad
        case refresh    // 暂时没有下拉刷新这个需求
        case refreshUnread(item: MessageCenter.FirstLevelMessageItem)    // 刷新某设备某类型的未读消息数量
        // 清除所有 unread 状态
        case sweepAllUnread
    }
}

extension SystemMessageViewController {
    class ViewModel {
        /// 一级消息模型
        @DidSetPublished var firstLevelMessageItems: [MessageCenter.FirstLevelMessageItem] = []

        @DidSetPublished var status: Status = .idle

        private var anyCancellables: Set<AnyCancellable> = []

        func processEvent(_ event: Event) {
            switch event {
            case .viewDidLoad, .refresh:
                self.reloadData()
            case let .refreshUnread(item):
                self.refreshUnread(item: item)
            case .sweepAllUnread:
                self.sweepAllUnread()
            }
        }

        func reloadData() {
            [MessageCenter.shared.$appNewVersionMessageMapping.map({ $0.values.reversed() }).eraseToAnyPublisher(),
             self.requestMessageListPublisher()
             , self.requestDeviceUpdateInfoPublisher()]
                .merge()
                .receive(on: DispatchQueue.main)
                .sink { [weak self] items in
                    // 设备升级消息去重
                    if self?.firstLevelMessageItems.contains(where: { $0.tag == MessageCenter.MessageTag.firmwareUpdate }) ?? false && items.contains(where: { $0.tag == MessageCenter.MessageTag.firmwareUpdate }) {
                        self?.firstLevelMessageItems.removeAll(where: { $0.tag == MessageCenter.MessageTag.firmwareUpdate })
                    }
                    self?.firstLevelMessageItems += items
                }.store(in: &self.anyCancellables)
        }

        /// 调用消息已读接口
        func refreshUnread(item: MessageCenter.FirstLevelMessageItem) {
            item.unreadCnt = 0
            MessageCenter.shared.syncMsgHaveBeenRead(tag: item.tag.rawValue, deviceId: nil)
            self.status = .messageItemUnreadStatusDidRefresh(item: item)
        }

        func sweepAllUnread() {
            self.firstLevelMessageItems.forEach { $0.unreadCnt = 0 }
            MessageCenter.shared.syncMsgHaveBeenRead(tag: nil, deviceId: nil)
            MessageCenter.shared.syncWelfareActivityBeenReaded(MessageCenter.shared.welfareActivityMessages)
            self.status = .didSweepAllUnread
        }

        // MARK: 发布者
        /// 获取服务器消息发布者 (一级)
        /// 通过接口 "msgcenter/msgtype/list" 获取一级消息
        func requestMessageListPublisher() -> AnyPublisher<[MessageCenter.FirstLevelMessageItem], Never> {
            Deferred {
                Future { promise in
                    IVMessageCenterMgr.share.getMessageList {
                        let result = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                        promise(result)
                    }
                }.tryMap { json -> [MessageCenter.FirstLevelMessageItem] in
                    let res = try json["data"]["list"].decoded(as: [MessageCenter.FirstLevelMessageItem].self)
                    return res
                }.replaceError(with: [])
            }.eraseToAnyPublisher()
        }

        /// 获取设备固件升级通知发布者 (一级)
        func requestDeviceUpdateInfoPublisher() -> AnyPublisher<[MessageCenter.FirstLevelMessageItem], Never> {
            // 从 MessageCenter.shared.$deviceFirmwareMessages 组建一级消息
            MessageCenter.shared.$deviceFirmwareMessages.map {
                guard let firmwareMessage = $0.sorted(by: { $0.messageItem.time > $1.messageItem.time }).first else {
                    return []
                }
                let msgTime = firmwareMessage.messageItem.time
                let version = firmwareMessage.newVersion
                let summary =  String.localization.localized("AA0497", note: "【%@】升级提醒：%@上线啦。查看更新内容>>", args: [firmwareMessage.deviceName, version])
                let item = MessageCenter.FirstLevelMessageItem.init(tag: .firmwareUpdate, title: String.localization.localized("AA0219", note: "设备升级"), deviceId: 0, msgTime: msgTime, isHeap: true, summary: summary, unreadCnt: firmwareMessage.unreadCount, redirectUrl: "")
                return [item]
            }.eraseToAnyPublisher()
        }
    }
}
