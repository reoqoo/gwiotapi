//
//  MessageCenterSubLevelViewController+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/9/2023.
//

import Foundation
import IVMessageMgr
import RQCore

extension MessageCenterSubLevelViewController.ViewModel {
    enum Status {
        case idle
        // 刷新数据后, 通知视图是否还有更多数据, 以便UI更新上拉加载更多的控件
        case refreshHasMoreDataStatus(noMoreData: Bool)
    }

    enum Event {
        case viewDidLoad
        case refresh
        case loadMore
    }
}

extension MessageCenterSubLevelViewController {
    class ViewModel {

        let firstLevelMessageItem: MessageCenter.FirstLevelMessageItem
        init(firstLevelMessageItem: MessageCenter.FirstLevelMessageItem) {
            self.firstLevelMessageItem = firstLevelMessageItem
        }

        /// 二级消息模型
        @DidSetPublished var secondLevenMessageItems: [MessageCenter.SecondLevelMessageItem] = []

        @DidSetPublished var status: Status = .idle

        private var anyCancellables: Set<AnyCancellable> = []

        func processEvent(_ event: Event) {
            switch event {
            case .viewDidLoad, .refresh:
                self.loadData(lastId: nil)
            case .loadMore:
                self.loadData(lastId: self.secondLevenMessageItems.last?.id)
            }
        }

        func loadData(lastId: Int64?, size: Int = 15) {
            // 如果是固件升级消息, 从 MessageCenter 获取
            if self.firstLevelMessageItem.tag == MessageCenter.MessageTag.firmwareUpdate {
                self.requestFirmwareUpgradeMessageListPublisher()
                    .sink(receiveValue: { [weak self] messageItems in
                        self?.status = .refreshHasMoreDataStatus(noMoreData: true)
                        self?.secondLevenMessageItems = messageItems
                    }).store(in: &self.anyCancellables)
            }else{
                self.requestSecondLevelMessageListPublisher(self.firstLevelMessageItem.tag, lastId: lastId).sink { [weak self] completion in
                    guard case .failure = completion else { return }
                    self?.status = .refreshHasMoreDataStatus(noMoreData: false)
                } receiveValue: { [weak self] messageItems in
                    // lastId == nil 表示 refresh 操作
                    if lastId == nil {
                        self?.secondLevenMessageItems = []
                    }
                    self?.secondLevenMessageItems.append(contentsOf: messageItems)
                    self?.status = .refreshHasMoreDataStatus(noMoreData: messageItems.isEmpty)
                }.store(in: &self.anyCancellables)
            }
        }

        // MARK: 发布者封装
        /// 从 MessageCenter 获取 固件升级发布者
        func requestFirmwareUpgradeMessageListPublisher() -> AnyPublisher<[MessageCenter.SecondLevelMessageItem], Never> {
            MessageCenter.shared.$deviceFirmwareMessages.map({
                $0.sorted(by: { $0.messageItem.time > $1.messageItem.time }).map({ $0.messageItem })
            }).eraseToAnyPublisher()
        }

        /// 从服务器获取二级消息模型 发布者
        func requestSecondLevelMessageListPublisher(_ tag: MessageCenter.MessageTag, lastId: Int64?, size: Int = 15) -> AnyPublisher<[MessageCenter.SecondLevelMessageItem], Swift.Error> {
            Deferred {
                Future { promise in
                    IVMessageCenterMgr.share.secondMessageList(tag: tag.rawValue, lastId: lastId, size: size) {
                        let result = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                        promise(result)
                    }
                }.tryMap { json -> [MessageCenter.SecondLevelMessageItem] in
                    let res = try json["data"]["list"].decoded(as: [MessageCenter.SecondLevelMessageItem].self)
                    return res
                }
            }.eraseToAnyPublisher()
        }
    }
}
