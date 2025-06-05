//
//  ScrollBaseViewAndKeyboardMatchable.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 24/7/2023.
//

import UIKit

/// 提供给一些 "带输入功能的", "以 scrollView 作为主要视图的" ViewController 一些通用功能
/// 功能:
/// 1. 键盘遮挡时控制 scrollView contentInset 使内容可见
/// 2. 点击空白处收起键盘的功能
protocol ScrollBaseViewAndKeyboardMatchable: UIViewController {
    var scrollable: UIScrollView { get }
    var anyCancellables: Set<AnyCancellable> { get set }
}

// MARK: 提供的功能, 默认实现, 遵守方只需直接调用即可
extension ScrollBaseViewAndKeyboardMatchable {
    // 点击空白处可收起键盘
    @discardableResult
    func dismissKeyboardWhenTapOnNonInteractiveArea() -> UITapGestureRecognizer {
        let tap = UITapGestureRecognizer.init()
        tap.tapPublisher.sink { [weak self] gesture in
            self?.view.endEditing(true)
        }.store(in: &self.anyCancellables)
        self.scrollable.addGestureRecognizer(tap)
        return tap
    }

    // 键盘要 显示/消失 时, 调整 scrolView.contentInsets.bottom 以避免键盘遮挡整个可显示区域
    func adjustScrollViewContentInsetWhenKeyboardFrameChanged() {
        NotificationCenter.default.publisher(for: UIApplication.keyboardWillHideNotification).sink { [weak self] _ in
            self?.scrollable.contentInset = .zero
        }.store(in: &self.anyCancellables)

        NotificationCenter.default.publisher(for: UIApplication.keyboardWillShowNotification).sink { [weak self] notification in
            guard let keyboardFrame = notification.userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? CGRect else { return }
            self?.scrollable.contentInset = .init(top: 0, left: 0, bottom: keyboardFrame.height, right: 0)
        }.store(in: &self.anyCancellables)
    }
}
