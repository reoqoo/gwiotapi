//
//  GuardianViewController+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 29/8/2023.
//

import Foundation
import RQCore

extension GuardianViewController {

    class ViewModel {
        
        // 布局方式
        var layoutMode: LiveViewContainer.LayoutMode = {
            let rawValue = (AccountCenter.shared.currentUser?.userDefault?.value(forKey: UserDefaults.UserKey.Reoqoo_LiveViewLayoutMode.rawValue) as? Int) ?? 1
            let res = LiveViewContainer.LayoutMode.init(rawValue: rawValue) ?? .fourGird
            return res
        }()
        {
            didSet {
                self.layoutModeDidChangedObservable.send(self.layoutMode)
                // 发生变化, 马上持久化到本地
                AccountCenter.shared.currentUser?.userDefault?.set(self.layoutMode.rawValue, forKey: UserDefaults.UserKey.Reoqoo_LiveViewLayoutMode.rawValue)
                AccountCenter.shared.currentUser?.userDefault?.synchronize()
            }
        }

        // 布局方式变化Observable, 供 View 监听变化
        lazy var layoutModeDidChangedObservable: Combine.CurrentValueSubject<LiveViewContainer.LayoutMode, Never> = .init(self.layoutMode)
        
        var pluginTypes: [PluginType] = [.live, .event]

        // MARK: 事件过滤器
        /// 事件过滤
        @DidSetPublished var eventTypesFilter: DITCloudEventType?
        /// 设备过滤
        @DidSetPublished var devicesFilter: [DeviceEntity] = []

    }
}
