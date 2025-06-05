//
//  DebugConfigurationViewModel.swift
//  RQCore
//
//  Created by xiaojuntao on 22/11/2024.
//

import Foundation
import Combine
import IVDevTools
import GWIoTApi
import RQCore

class DebugConfigurationViewModel: Combine.ObservableObject {

    enum Environment: String, CaseIterable, Identifiable {

        var id: Environment { self }
        
        case release = "Release"
        case debug = "Debug"
    }

    var h5DebugMode: Bool = UIApplication.UserDefaults.isH5DebugMode {
        didSet {
            UIApplication.UserDefaults.isH5DebugMode = h5DebugMode
        }
    }

    var appName: String = UIApplication.UserDefaults.assginedAppName ?? UIApplication.appName
    var appPkgName: String = UIApplication.UserDefaults.assginedAppPkgName ?? (Bundle.main.bundleIdentifier ?? "")
    var appID: String = UIApplication.UserDefaults.assginedAppID ?? UIApplication.appID
    var appToken: String = UIApplication.UserDefaults.assginedAppToken ?? UIApplication.appToken

    var openDebugAssistant: Bool = UIApplication.UserDefaults.isAppDebugModeEnable {
        didSet {
            if openDebugAssistant {
                // 打开 DEBUG 小蜜蜂
                UIApplication.shared.keyWindow?.addSubview(IVDevToolsAssistant.shared)
            }else{
                IVDevToolsAssistant.shared.removeFromSuperview()
            }
            UIApplication.UserDefaults.isAppDebugModeEnable = openDebugAssistant
        }
    }

    var sdkVersion: String {
        (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? ""
    }

    func saveConfiguration(appName: String, appPkgName: String, appID: String, appToken: String, isTestEnv: Bool) {

        // reoqoo 的改动在这里
        // 保存值
        UIApplication.UserDefaults.assginedAppName = appName.isEmpty ? nil : appName
        UIApplication.UserDefaults.assginedAppPkgName = appPkgName.isEmpty ? nil : appPkgName
        UIApplication.UserDefaults.assginedAppID = appID.isEmpty ? nil : appID
        UIApplication.UserDefaults.assginedAppToken = appToken.isEmpty ? nil : appToken
        UIApplication.UserDefaults.isTestEnv = isTestEnv

        GWIoTApi.GWIoT.shared.configComponent.setEnv(env: isTestEnv ? .test : .prod)

        // 删掉配置表
        RQCore.Agent.clearStandardConfiguration()
        
        // 发这个通知会让用户登出, 但是有点非主流, 如果宿主App不接这个 Notification 就无效了
        NotificationCenter.default.post(name: RQCore.accessTokenDidExpiredNotification, object: nil)
    }

    func restoreConfiguration() {
        
        // 保存值
        UIApplication.UserDefaults.assginedAppName = nil
        UIApplication.UserDefaults.assginedAppPkgName = nil
        UIApplication.UserDefaults.assginedAppID = nil
        UIApplication.UserDefaults.assginedAppToken = nil
        UIApplication.UserDefaults.isTestEnv = false

        // 删掉配置表
        // 删掉配置表
        RQCore.Agent.clearStandardConfiguration()

        // 发这个通知会让用户登出, 但是有点非主流, 如果宿主App不接这个 Notification 就无效了
        NotificationCenter.default.post(name: RQCore.accessTokenDidExpiredNotification, object: nil)
    }
}
