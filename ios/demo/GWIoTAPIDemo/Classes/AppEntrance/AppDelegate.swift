//
//  AppDelegate.swift
//  Reoqoo
//
//  Created by xiaojuntao on 17/7/2023.
//

import UIKit
import RQImagePicker
import GWIoTApi
import RQCore

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var anyCancellables: Set<AnyCancellable> = []

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        // Override point for customization after application launch.
        self.sdkInit(withAppliction: application, withLaunchingOptions: launchOptions)

        return true
    }

    // MARK: UISceneSession Lifecycle
    @available(iOS 13.0, *)
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    @available(iOS 13.0, *)
    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }

    func application(_ application: UIApplication, handleOpen url: URL) -> Bool {
        return self.handleOpenURL(url)
    }

    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        return self.handleOpenURL(url)
    }

    func application(_ application: UIApplication, open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        return self.handleOpenURL(url)
    }
}

// MARK: Helper
extension AppDelegate {
    
    func sdkInit(withAppliction application: UIApplication, withLaunchingOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) {

        // 推送注册
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, err in
            if granted {
                logInfo("用户允许APNS远程推送服务")
            }else{
                logInfo("用户拒绝APNS远程推送服务")
            }
        }

        ImagePickerViewController.localizableStringSetter = {
            switch $0 {
            case .camera:
                return String.localization.localized("AA0546", note: "相机")
            case .photo:
                return String.localization.localized("AA0547", note: "相片")
            case .video:
                return String.localization.localized("AA0548", note: "视频")
            case .select:
                return String.localization.localized("AA0230", note: "选择")
            case .authorizationToAccessMoreAssets:
                return String.localization.localized("AA0609", note: "访问更多")
            case .jump2SystemSettingCauseAuthorizationLimit:
                return String.localization.localized("AA0610", note: "reoqoo只能存取相册部分相片, 建议允许存取`所有相片`, 点击去设置")
            case .jump2SystemSettingCauseAuthorizationDeined:
                return String.localization.localized("AA0611", note: "reoqoo没有相册访问权限, 点击去设置")
            @unknown default:
                return ""
            }
        }

        // GWIoTApi init
        let opts = GWIoTApi.InitOptions(appConfig: .init(appId: UIApplication.appID, appToken: UIApplication.appToken, appName: UIApplication.appName, cId: UIApplication.cid))
        // 生成设备二维码时需要这个值, 例如: https://brandDomain/d/?u=xxx...
        opts.brandDomain = "reoqoo.com"
        // 取出当前 App 语言, 设置到 opts 中
        let gwLang = GWIoTApi.LanguageCode.current
        opts.language = gwLang
        
        let servicesEnv: HostConfig.Env = UIApplication.UserDefaults.isTestEnv ? .test : .prod
        opts.hostConfig = .init(env: servicesEnv)

        if let watermarkPath = Bundle.main.path(forResource: "watermark_reo_logo", ofType: "png") {
            opts.albumConfig = .init(watermarkConfig: .init(filePath: watermarkPath, position: nil, horizontalMargin: 0, verticalMargin: 0, widthScale: 0, heightScale: 0))
        }

        GWIoT.shared.initialize(opts: opts)
        GWIoT.shared.setUIConfiguration(configuration: UIApplication.apperanceConfiguration)

        GWBasePlayer.setLogLevel(.verbose)

        // Start Observe
        GWIoTAPIDelegate.shared.startObserve()

        // 网络监听
        // 这个不靠谱, 换了 wifi 也不会有通知
        IVNetworkHelper.sharedInstance.startMonitor()

    }

    func handleOpenURL(_ url: URL) -> Bool {
        return true
    }
    
    /// 成功取得远程推送token
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let token = deviceToken.map({ String.init(format: "%02.2hhx", $0) }).joined()
        logInfo("取得远程推送token: ", token)
        // 同步 APNS token 到服务器
        AccountCenter.shared.currentUser?.syncAPNSToken(token)
    }

    /// 获取远程推送token失败
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        logError("获取远程推送token失败: ", error)
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    // 前台接收到通知
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        logInfo("前台收到推送", userInfo)
        // completionHandler 参数决定是否播放声音和展示推送通知于系统通知栏中
//        completionHandler([.alert, .sound])
        completionHandler([])
        // 推送跳转处理
        GWIoT.shared.receivePushNotification(noti: .init(userInfo: userInfo))
    }

    // 点击通知进入app
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        logInfo("点击推送进入app", userInfo)
        completionHandler()
        // 推送跳转处理
        GWIoT.shared.clickPushNotification(noti: .init(userInfo: userInfo))
    }
}
