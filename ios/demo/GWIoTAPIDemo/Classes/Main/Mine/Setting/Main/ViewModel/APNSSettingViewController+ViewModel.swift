//
//  APNSSettingViewController+VM.swift
//  Reoqoo
//
//  Created by xiaojuntao on 11/6/2024.
//

import Foundation

extension APNSSettingViewController {
    class ViewModel {

        var tableViewDataSources: [SettingViewController.AuthorizationStatusItem] {
            [self.notificationAuthorization]
        }

        /// 推送权限
        var notificationAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0615", note: "消息推送通知"), description: String.localization.localized("AA0616", note: "开启后，可及时获得reoqoo APP的最新消息"))

        var anyCancellables: Set<AnyCancellable> = []

        init() {
            // 推送权限
            UNUserNotificationCenter.current().getNotificationSettings { [weak self] settings in
                self?.notificationAuthorization.isValid = settings.authorizationStatus == .authorized
            }
        }
    }
}
