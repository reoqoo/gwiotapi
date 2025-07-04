//
//  KeyWindow.swift
//  Reoqoo
//
//  Created by xiaojuntao on 25/7/2023.
//

import UIKit
import RQCore
import GWIoTApi

/// 负责 摇一摇 弹出 debug 助手功能
/// 在多屏幕需求出现之前, 此 Window 实例仅有一份, 且生命周期由 AppDelegate 或 SceneDelegate 管理, AppEntranceManager 也会持有此 Window 实例, 掌握生命周期
class KeyWindow: UIWindow {
    
    private lazy var debugFlagImageView: UIImageView = .init(image: R.image.commonDebugFlag())

    var anyCancellables: Set<AnyCancellable> = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    override init(windowScene: UIWindowScene) {
        super.init(windowScene: windowScene)
        self.setup()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    func setup() {
        // 监听请求地址变化, 以显示 "DEBUG" flag 在右上角
        GWPlugin.shared.hostConfig.observe(weakRef: self) { [weak self] config in
            DispatchQueue.main.async {
                guard let config else { return }
                if config.env == .test {
                    self?.showDebugFlag()
                }else{
                    self?.hideDebugFlag()
                }
            }
        }
    }
    
    /// 显示 debug flag
    private func showDebugFlag() {
        self.addSubview(self.debugFlagImageView)
        self.debugFlagImageView.snp.makeConstraints { make in
            make.trailing.top.equalToSuperview()
        }
    }

    private func hideDebugFlag() {
        self.debugFlagImageView.removeFromSuperview()
    }

}
