//
//  BasicTabbarController+WrappedNavigationController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 20/9/2023.
//

import UIKit
import RTRootNavigationController

extension BasicTabbarController {


    /// 由于 BasicTabbarController.viewControllers 都是 UINavigationController 类型的 (为了 FamilyViewController / GuardianViewController / MineViewController 这几个控制器有独立可配置的 navigationBar. 否则这三个就会共享 tabbarController.navigationController)
    /// 所以设计此 NavigationController, 专门用来包装 FamilyViewController / GuardianViewController / MineViewController.
    /// 使 FamilyViewController / GuardianViewController / MineViewController 实例执行 push / pop 操作时, 执行 tabbarController.navigationController 的 push / pop 操作
    class WrappedNavigationController: BaseNavigationController {

        let wrappableControllerClasses: [AnyClass] = [FamilyViewController2.self, GuardianViewController.self, MineViewController.self]

        override func pushViewController(_ viewController: UIViewController, animated: Bool) {
            // 由于使用了 RTRootNavigationController, 所以 初始化时传入的 viewController 是 RTContainerController 类型
            if let viewController = viewController as? RTContainerController,
               // 取出其中的 contentViewController, 检查 contentViewController 是不是 FamilyViewController / GuardianViewController / MineViewController 这几种类型
               let contentViewController = viewController.contentViewController,
               self.wrappableControllerClasses.contains(where: { contentViewController.isKind(of: $0) }) {
                super.pushViewController(viewController, animated: animated)
                return
            }
            self.tabBarController?.navigationController?.pushViewController(viewController, animated: animated)
        }

        override func popViewController(animated: Bool) -> UIViewController? {
            self.tabBarController?.navigationController?.popViewController(animated: true)
        }

        override func popToViewController(_ viewController: UIViewController, animated: Bool) -> [UIViewController]? {
            self.tabBarController?.navigationController?.popToViewController(viewController, animated: animated)
        }

        override func popToRootViewController(animated: Bool) -> [UIViewController]? {
            self.tabBarController?.navigationController?.popToRootViewController(animated: animated)
        }
    }

}

