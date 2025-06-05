//
//  BaseTableViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 4/9/2023.
//

import Foundation

class BaseTableViewController: UITableViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = R.color.background_F2F3F6_thinGray()!
        logInfo(self, #function)
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        logInfo(self, #function)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        logInfo(self, #function)
    }

    // 因为使用了 RTRootNavigationController, 返回按钮在此处定义
    override func rt_customBackItem(withTarget target: Any!, action: Selector!) -> UIBarButtonItem! {
        .init(image: R.image.commonNavigationBack(), style: .done, target: target, action: action)
    }

    override var supportedInterfaceOrientations: UIInterfaceOrientationMask { .portrait }

    deinit { logInfo(self, #function) }
}
