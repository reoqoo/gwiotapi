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

        // 迁移操作自动按需执行
        MigrateHelper.migrate()

        // Override point for customization after application launch.
        self.sdkInit(withAppliction: application, withLaunchingOptions: launchOptions)
        
        // 旧固件一键呼叫调试
//        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//            let alarmType: DHAlarmType = .typeKeyCall
//            let targetViewType: DHTargetViewType = .voip
//
//            let event_id = "10831359249910868011697766034498"
//            let alarmMsg = DHAlarmMessage.init()
//            alarmMsg.alarmType = alarmType
//            alarmMsg.alarmId = event_id
//            // 如果是一键呼叫事件, 需要传 keyCallContent 参数, 否则会跳转到事件页
//            alarmMsg.keyCallContent = "alarmId=" + event_id + "&alarmType=" + String(alarmType.rawValue) + "&code=" + "" + "&pts=" + String(Int(Date().timeIntervalSince1970))
//            
//            guard let dev = DeviceManager2.fetchDevice("12885484243"), let vc = AppEntranceManager.shared.tabbarViewController else { return }
//            DophiGoApiManager.noticeDophigoDevice(dev, alarm: alarmMsg, targetVC: vc, targetViewType: targetViewType)
//        }

        // 模拟收到一键呼叫推送
//        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//            let dict = ["push_data":
//                            ["deviceId": "38654705667",
//                             "pushContent": ["Type": "event", "alarmId": "128855036031717155102", "alarmType": 2, "flag": 0, "value": "",],
//                             "pushTime": Date().timeIntervalSince1970 * 1000 - 500,
//                             "pushType": 274877906944,]
//            ]
//            // 前台收到推送
//            GWIoT.shared.receivePushNotification(noti: .init(userInfo: dict))
//            // 点击推送
//            GWIoT.shared.clickPushNotification(noti: .init(userInfo: dict))
//        }

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

        // 配置颜色
        RQCore.Agent.shared.apperanceConfiguration = UIApplication.apperanceConfiguration

        // GWIoTApi init
        let opts = GWIoTApi.InitOptions(appConfig: .init(appId: UIApplication.appID, appToken: UIApplication.appToken, appName: UIApplication.appName))
        // 有需要时配置
        opts.albumConfig = nil
        // 取出当前 App 语言, 设置到 opts 中
        let gwLang = GWIoTApi.LanguageCode.current
        opts.language = gwLang
        
        let servicesEnv: HostConfig.Env = UIApplication.UserDefaults.isTestEnv ? .test : .prod
        opts.hostConfig = .init(
            env: servicesEnv,
            prodHost: HostConfig.Host(
                iot: "|list.iotvideo.cloudlinks.cn",
                base: "https://openapi.reoqoo.com",
                plugin: "https://openapi-plugin.reoqoo.com",
                h5: "https://trade.reoqoo.com"
            ),
            testHost: HostConfig.Host(
                iot: "|list.iotvideo.cloudlinks.cn",
                base: "https://openapi-test.reoqoo.com",
                plugin: "https://openapi-plugin-test.reoqoo.com",
                h5: "https://trade-test.reoqoo.com"
            )
        )

        if let watermarkPath = Bundle.main.path(forResource: "watermark_reo_logo", ofType: "png") {
            opts.albumConfig = .init(watermarkConfig: .init(filePath: watermarkPath, position: nil, horizontalMargin: 0, verticalMargin: 0, widthScale: 0, heightScale: 0))
        }

        GWIoT.shared.initialize(opts: opts)

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
