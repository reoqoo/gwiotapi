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

    /// H5 DEBUG 模式
    static var isH5DebugMode: Bool {
        get {
            UserDefaults.standard.bool(forKey: UserDefaults.GlobalKey.Reoqoo_H5DebugMode.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_H5DebugMode.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// APP DEBUG 模式
    static var isAppDebugModeEnable: Bool {
        get {
            UserDefaults.standard.bool(forKey: UserDefaults.GlobalKey.Reoqoo_DebugMode.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_DebugMode.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// 指定 AppID
    static var assginedAppID: String? {
        get {
            UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_AppID.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_AppID.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// 指定 AppToken
    static var assginedAppToken: String? {
        get {
            UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_AppToken.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_AppToken.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// 指定 AppName
    static var assginedAppName: String? {
        get {
            UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_AppName.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_AppName.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// 指定 AppPkgName
    static var assginedAppPkgName: String? {
        get {
            UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_AppPkgName.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_AppPkgName.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// 指定 privacy policy URL
    static var assginedPrivacyPolicyURL: String? {
        get {
            UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_PrivacyPolicyURL.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_PrivacyPolicyURL.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

    /// 指定 user agreement  URL
    static var assginedUserAgreementURL: String? {
        get {
            UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_UserAgreementURL.rawValue)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: UserDefaults.GlobalKey.Reoqoo_UserAgreementURL.rawValue)
            UserDefaults.standard.synchronize()
        }
    }

}
