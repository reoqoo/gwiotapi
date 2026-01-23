//
//  UIApplication+Key.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/7/2023.
//

import Foundation

extension UIApplication {
    enum UserDefaults {}
}

extension UIApplication.UserDefaults {

    /// 是否测试环境
    static var isTestEnv: Bool {
        get {
            UserDefaults.standard.bool(forKey: UserDefaults.GlobalKey.Reoqoo_isTestEnv.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_isTestEnv.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

}
