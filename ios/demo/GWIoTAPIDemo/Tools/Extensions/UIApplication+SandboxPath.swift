//
//  UIApplication+SandboxPath.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 14/8/2024.
//

import Foundation

extension UIApplication {
    /// reoqoo 持久化信息根目录     .../Library/Application Support/com.reoqoo
    static let reoqooDirectoryPath: String = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true).first! + "/" + "com.reoqoo"
    /// reoqoo 用户信息目录   .../Library/Application Support/com.reoqoo/users
    static let usersInfoDirectoryPath: String = UIApplication.reoqooDirectoryPath + "/" + "users"
}
