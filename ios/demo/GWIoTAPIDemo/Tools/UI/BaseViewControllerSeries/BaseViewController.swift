//
//  CommonViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 26/7/2023.
//

import Foundation

extension BaseViewController {
    enum ViewStatus {
        case unknown
        case willAppear
        case didAppear
        case willDisAppear
        case didDisappear
    }
}

// 根控制器. 目前主要负责 生命周期事件捕获后输出log
class BaseViewController: UIViewController {

    @DidSetPublished var viewStatus: ViewStatus = .unknown
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = R.color.background_F2F3F6_thinGray()!
        logInfo(self, #function)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.viewStatus = .willAppear
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.viewStatus = .didAppear
        logInfo(self, #function)
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.viewStatus = .willDisAppear
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        self.viewStatus = .didDisappear
        logInfo(self, #function)
    }

    // 因为使用了 RTRootNavigationController, 返回按钮在此处定义
    override func rt_customBackItem(withTarget target: Any!, action: Selector!) -> UIBarButtonItem! {
        .init(image: R.image.commonNavigationBack(), style: .done, target: target, action: action)
    }

    deinit {
        logInfo(self, #function)
    }

    override var supportedInterfaceOrientations: UIInterfaceOrientationMask { .portrait }

}
