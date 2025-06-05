//
//  SceneDelegate.swift
//  Reoqoo
//
//  Created by xiaojuntao on 17/7/2023.
//

import UIKit
import AppTrackingTransparency

@available(iOS 13.0, *)
class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
        guard let windowScene = (scene as? UIWindowScene) else { return }
        self.window = AppEntranceManager.shared.createKeyWindow(with: windowScene)
        AppEntranceManager.shared.rootViewControllerSetup()
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // 重置角标, 设置为 -1 表示清空角标且不清除推送
        UIApplication.shared.applicationIconBadgeNumber = -1
        // 开启 ATT 追踪, 需要等待到appBecomeActive后再等待2秒后再请求 ATT 权限, 否则不会弹出来
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            if #available(iOS 14, *) {
                ATTrackingManager.requestTrackingAuthorization { status in }
            }
        }

        // 通过 UIAppearance 设置一些全局的 UI样式
//        let selectedBackgroundView = UIView.init()
//        selectedBackgroundView.backgroundColor = .black.withAlphaComponent(0.1)
//        UITableViewCell.appearance().selectedBackgroundView = selectedBackgroundView
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }
}

