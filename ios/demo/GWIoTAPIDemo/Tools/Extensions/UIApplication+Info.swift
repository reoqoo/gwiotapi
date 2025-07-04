//
//  UIApplication+Key.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/7/2023.
//

import Foundation

extension UIApplication {
    enum Key {}
    enum UserDefaults {}
}

extension UIApplication.Key {
    enum Bugly {
        static let appID = "c4c7e120ae"
        static let appKey = "af233192-1ae2-442d-844d-d47895ee7af5"
    }
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
