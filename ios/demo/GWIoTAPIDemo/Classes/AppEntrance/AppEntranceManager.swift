//
//  AppEntranceManager.swift
//  Reoqoo
//
//  Created by xiaojuntao on 18/7/2023.
//

import UIKit
import RQWebServices
import GWIoTApi

/// 由于 app 需要支援 iOS13 以下版本, 而 iOS13 采用的是 SceneDelegate 管理 UI 生命周期, iOS13以下系统直接由 AppDelegate.window 管理 根 window 生命周期, 所以设计此单例以集中管理两种情况的存在. 也以便应对日后可能出现的 ipad 适配 及 多窗口需求
/// 提供的功能:
/// 1. 创建 及 管理两种兼容场景的 window.rootViewController
/// 2. 判断情况以对需要展示的 rootViewController 进行切换. 例如用户未登录, 就显示 登录页, 例如需要播放广告, 播放特定启动动画 都在这里处理
class AppEntranceManager {
    
    static let shared: AppEntranceManager = .init()

    /// 在某些需求场景下, 仅仅对键盘显示/隐藏 事件监听以获取 keyboard frame 是不足够的, 如果键盘当前已经显示, 就取不到当前 frame 了, 所以设置此值供外部监听使用
    @DidSetPublished private(set) var currentKeyboardStatus: KeyboardStatus = .init(frame: .zero, isShow: false)

    /// Application Status 发布者
    @DidSetPublished private(set) var applicationState: ApplicationState = .didFinishLaunching

    private var anyCancellables: Set<AnyCancellable> = []

    /// AppEntranceManager 掌握 keyWindow 的生命周期. 两个 createKeyWindow 方法创建的Window会被持有
    var keyWindow: KeyWindow?

    private init() {
        // 对 键盘 状态进行监听
        self.bindingKeyboardStatusNotification()

        // 对 application state 进行监听
        self.bindingApplicaitonState()

        // 监听手动设置语言事件通知
        NotificationCenter.default.publisher(for: Notification.Name.init(LanguageCode.didChanngeLanguageNotificaitonName.rawValue)).sink(receiveValue: { [weak self] _ in
            self?.changeInterfaceLanguage()
        }).store(in: &self.anyCancellables)

        // 监听账号被注销事件, 弹个 MBPHUD
        NotificationCenter.default.publisher(for: User.accountHasBeenDeletedNotification)
            .sink(receiveValue: { _ in
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0522", note: "账号已注销"))
            }).store(in: &self.anyCancellables)
    }

    @available(iOS 13.0, *)
    func createKeyWindow(with scene: UIWindowScene) -> KeyWindow {
        let window = KeyWindow.init(windowScene: scene)
//        // 强制关闭暗黑模式
//        window.overrideUserInterfaceStyle = .light
        window.makeKeyAndVisible()
        self.keyWindow = window
        return window
    }

    // 计算属性: 创建登录ViewController, 不选择持有该控制器是为了确保切换了 rootViewController 后, 登录页面可以销毁
    func createLoginViewControllerWrapped() -> BaseNavigationController {
        let loginViewController = LoginViewController.init()
        return BaseNavigationController.init(rootViewController: loginViewController)
    }

    // 计算属性: 创建主业务视图
    func createMainViewController() -> BaseNavigationController {
        let tabbarController = BasicTabbarController.init()
        return BaseNavigationController.init(rootViewController: tabbarController)
    }

    // 绑定 User 及 User登录状态, 以控制 rootViewController
    func rootViewControllerSetup() {
        // 创建 User 登录状态的发布者
        let userLoginStatusObservable = AccountCenter.shared.$currentUser.flatMap({
            // 当 AccountCenter.shared.currentUser == nil, 表示用户未登录
            guard let user = $0 else { return Just.init(false).eraseToAnyPublisher() }
            return user.$isLogin.eraseToAnyPublisher()
        })

        // 对当前 User 是否登录状态进行监听, 以控制 rootViewController
        userLoginStatusObservable.sink(receiveValue: { [weak self] loggedIn in
            logDebug("App启动, 进入设置 window.rootViewController 环节, loggedIn = \(loggedIn)")
            if loggedIn {
                self?.keyWindow?.rootViewController = self?.createMainViewController()
            }else{
                self?.keyWindow?.rootViewController = self?.createLoginViewControllerWrapped()
            }
        }).store(in: &self.anyCancellables)
        
        // 1秒后尝试弹出用户协议
        Timer.publish(every: 1, on: .main, in: .common).autoconnect().first().sink { [weak self] _ in
            self?.presentUsageAgreementIfNeed()
        }.store(in: &self.anyCancellables)
    }

    // 对 键盘显示/隐藏 进行监听
    private func bindingKeyboardStatusNotification() {
        NotificationCenter.default
            .publisher(for: UIApplication.keyboardWillShowNotification)
            .sink(receiveValue: { [weak self] notification in
                guard let keyboardFrame = notification.userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? CGRect,
                      let self = self else { return }
                self.currentKeyboardStatus = .init(frame: keyboardFrame, isShow: true)
            }).store(in: &self.anyCancellables)

        NotificationCenter.default.publisher(for: UIApplication.keyboardWillHideNotification)
            .sink(receiveValue: { [weak self] notification in
                guard let keyboardFrame = notification.userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? CGRect,
                      let self = self else { return }
                self.currentKeyboardStatus = .init(frame: keyboardFrame, isShow: false)
            }).store(in: &self.anyCancellables)
    }

    // 对 app 状态进行绑定
    private func bindingApplicaitonState() {
        [NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification).map({ _ in ApplicationState.didBecomeActive }),
         NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification).map({ _ in ApplicationState.didEnterBackground }),
         NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification).map({ _ in ApplicationState.willEnterForeground }),
         NotificationCenter.default.publisher(for: UIApplication.didFinishLaunchingNotification).map({ _ in ApplicationState.didFinishLaunching }),
         NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification).map({ _ in ApplicationState.willResignActive }),
         NotificationCenter.default.publisher(for: UIApplication.willTerminateNotification).map({ _ in ApplicationState.willTerminate })]
            .merge()
            .sink { [weak self] state in
                self?.applicationState = state
            }.store(in: &self.anyCancellables)
    }

    private func presentUsageAgreementIfNeed() {
        // 在用户曾经同意过`使用协议`, `隐私协议` 后, 存储当前 app version 到 UserDefaults
        // 如果该值为空, 就弹出协议弹窗
        if let _ = UserDefaults.standard.object(forKey: UserDefaults.GlobalKey.Reoqoo_AgreeToUsageAgreementOnAppVersion.rawValue) as? String { return }
        guard let rootViewController = self.keyWindow?.rootViewController else { return }
        ReoqooAlertViewController.presentFullEditionUsageAgreement(withPresentedViewController: rootViewController) { url in
            let vc = WebViewController.init(url: url)
            let nav = BaseNavigationController.init(rootViewController: vc)
            Self.topMostViewController?.present(nav, animated: true)
        }
    }

    /// 当 NSBundle.setLanguate() 方法被调用, 接受到通知后, 会触发此方法
    /// 为 window 创建新的 RootViewController, 以达到刷新app语言的目的
    private func changeInterfaceLanguage() {

        let hud = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(inView: self.keyWindow, isMask: true)
        /// 让菊花转 1 秒后再换 rootViewController, 否则太快了, 用户点了没什么成就感
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {

            let tabbarController = BasicTabbarController.init()
            let mainViewController = BaseNavigationController.init(rootViewController: tabbarController)
            tabbarController.selectedIndex = 2
            let changeLangVC = ChangeLanguageViewController.init()
            mainViewController.pushViewController(changeLangVC, animated: false)

            hud.hideDispatchOnMainThread()
            self.keyWindow?.rootViewController = mainViewController
        }
    }

    static var topMostViewController: UIViewController? {
        guard let rootViewController = AppEntranceManager.shared.keyWindow?.rootViewController else { return nil }
        func topFromRoot(_ root: UIViewController?) -> UIViewController? {
            if let presentedVC = root?.presentedViewController {
                return topFromRoot(presentedVC)
            }
            if let nav = root as? UINavigationController, let lastVC = nav.topViewController {
                return topFromRoot(lastVC)
            }
            if let tab = root as? UITabBarController, let selectedVC = tab.selectedViewController {
                return topFromRoot(selectedVC)
            }
            return root
        }
        return topFromRoot(rootViewController)
    }

    var tabbarViewController: BasicTabbarController? {
        guard let nav = self.keyWindow?.rootViewController as? BaseNavigationController else { return nil }
        return nav.rt_viewControllers.first as? BasicTabbarController
    }

}
