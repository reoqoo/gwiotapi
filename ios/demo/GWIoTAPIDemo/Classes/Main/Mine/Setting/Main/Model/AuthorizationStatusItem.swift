//
//  AuthorizationStatusItem.swift
//  Reoqoo
//
//  Created by xiaojuntao on 11/6/2024.
//

import Foundation

extension SettingViewController {
    class AuthorizationStatusItem {
        var title: String
        var description: String
        @Published var isValid: Bool = false
        init(title: String, description: String) {
            self.title = title
            self.description = description
        }
    }
}
