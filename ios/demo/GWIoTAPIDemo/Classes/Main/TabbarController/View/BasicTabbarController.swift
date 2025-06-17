//
//  BaseTabbarController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 24/7/2023.
//

import UIKit
import RTRootNavigationController
import RQCore
import RQWebServices
import IVAccountMgr

class BasicTabbarController: UITabBarController {
    
    let vm: ViewModel = .init()

    var anyCancellables: Set<AnyCancellable> = []

    lazy var familyViewController: FamilyViewController2 = .init()
    lazy var mineViewController: MineViewController = .fromStoryboard()

    override init(nibName nibNameOrNil: String? = nil, bundle nibBundleOrNil: Bundle? = nil) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        // 为确保这几个 child 的 navigationController 都是独立的, 所以 child 需要是 WrappedNavigationController 类型
        self.viewControllers = [WrappedNavigationController.init(rootViewController: self.familyViewController), 
                                WrappedNavigationController.init(rootViewController: self.mineViewController)]
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        /// 保持 NavigationBar 隐藏
        self.navigationController?.setNavigationBarHidden(true, animated: false)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.backgroundColor = .white

        self.tabBar.tintColor = R.color.brand()
        self.tabBar.unselectedItemTintColor = R.color.text_000000_50()
        self.tabBar.barTintColor = R.color.text_FFFFFF()
        self.tabBar.backgroundColor = self.tabBar.barTintColor

        self.tabBar.layer.shadowOffset = .init(width: 0, height: -2)
        self.tabBar.layer.shadowOpacity = 0.05
        self.tabBar.layer.shadowRadius = 8
        self.tabBar.layer.shadowColor = UIColor.black.cgColor
        self.tabBar.shadowImage = UIImage()
        self.tabBar.backgroundImage = UIImage()
    }

    // 因为使用了 RTRootNavigationController, 返回按钮在此处定义
    override func rt_customBackItem(withTarget target: Any!, action: Selector!) -> UIBarButtonItem! {
        .init(image: R.image.commonNavigationBack(), style: .done, target: target, action: action)
    }

    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        self.selectedViewController?.supportedInterfaceOrientations ?? .portrait
    }
}
