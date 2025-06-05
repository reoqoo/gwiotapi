//
//  ShareManagedViewController+ViewModel.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 13/6/2024.
//

import Foundation
import RQCore

extension ShareManagedViewController {
    class ViewModel {

        enum Event {
            case idle
            case refreshDeviceList
        }

        enum State {
            case idle
            case refreshing
            case refreshDeviceListResult(Result<Void, Swift.Error>)
        }

        var event: Event = .idle {
            didSet {
                switch event {
                case .idle:
                    break
                case .refreshDeviceList:
                    // 主动发起请求设备, 否则无法准确获取设备是否被分享的情况
                    self.state = .refreshing
                    DeviceManager.shared.requestDevices { [weak self] in
                        if case let .failure(err) = $0 {
                            self?.state = .refreshDeviceListResult(.failure(err))
                        }
                        if case .success = $0 {
                            self?.state = .refreshDeviceListResult(.success(()))
                        }
                    }
                }
            }
        }

        @DidSetPublished var state: State = .idle

        /// 分享出去的设备
        @DidSetPublished var sharedToDevices: [DeviceEntity] = []

        /// 接受分享的设备
        @DidSetPublished var sharedFromDevices: [DeviceEntity] = []

        typealias TableViewSection = (title: String, devices: [DeviceEntity])
        @DidSetPublished var tableViewDataSources: [TableViewSection] = []
        
        var anyCancellables: Set<AnyCancellable> = []

        init() {
            // 对 DeviceManager 进行监听
            DeviceManager.shared.generateDevicesPublisher(keyPaths: [\.role, \.cloudStatus])
                .sink { [weak self] in
                    guard let res = $0 else { return }
                    self?.sharedToDevices = res.filter({ $0.role == .master && $0.hasShared })
                    self?.sharedFromDevices = res.filter({ $0.role == .shared })
                }.store(in: &self.anyCancellables)

            Publishers.CombineLatest(self.$sharedToDevices, self.$sharedFromDevices).sink { [weak self] shareTo, shareFrom in
                let sections = [(String.localization.localized("AA0178", note: "分享的设备"), shareTo), (String.localization.localized("AA0179", note: "来自分享的设备"), shareFrom)]
                self?.tableViewDataSources = sections.filter({ !$1.isEmpty })
            }.store(in: &self.anyCancellables)
        }
    }
}
