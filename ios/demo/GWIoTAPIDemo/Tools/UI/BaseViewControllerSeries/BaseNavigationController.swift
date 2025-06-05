//
//  CommonNavigationController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 24/7/2023.
//

import UIKit
import RTRootNavigationController

/// app 通用 NavigationController
class BaseNavigationController: RTRootNavigationController, UIGestureRecognizerDelegate {

    override var shouldAutorotate: Bool { true }

    // MARK: 屏幕旋转控制
    // 默认方向
    override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation {
        return self.topViewController?.preferredInterfaceOrientationForPresentation ?? .portrait
    }

    // 支持的方向
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        self.topViewController?.supportedInterfaceOrientations ?? [.portrait]
    }

    override func viewDidLoad() {
        super.viewDidLoad()
    }

    // TODO: 如果需要拦截左滑返回手势, 从这里入手
    func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        guard let containerViewController = self.topViewController as? RTContainerController, let viewController = containerViewController.contentViewController else { return true }
        // 
        return !viewController.rt_disableInteractivePop
    }
}
