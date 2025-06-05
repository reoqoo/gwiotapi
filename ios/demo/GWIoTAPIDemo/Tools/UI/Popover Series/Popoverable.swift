//
//  Popable.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 19/4/2024.
//

import Foundation

/// 任何继承此协议的 UIView, 都可快速使用 self.popoverView 进行泡泡弹框
/// 可参考 PopoverListView 的做法:
/// PopoverListView 源码中几乎只管自身内容的显示, 由于其继承自 Popoverable 协议, 所以, 当其需要以泡泡样式弹出时, 只需要 listView.popoverView.show(...) 即可
protocol Popoverable: UIView {
    
    var popoverView: Popover { get }
    var popoverOptions: [PopoverOption] { get }

    var willShowHandler: (() -> ())? { get }
    var willDismissHandler: (() -> ())? { get }
    var didShowHandler: (() -> ())? { get }
    var didDismissHandler: (() -> ())? { get }

    func show(point: CGPoint, inView: UIView)
    func show(fromView: UIView, inView: UIView)
    func showAsDialog(inView: UIView)
}

extension Popoverable {

    /// 为了避免 self 和 popoverView 相互引用无法销毁, 将其设计为计算属性
    /// 因此在show方法调用前, 主动访问此属性并做出更改都是无效的
    var popoverView: Popover {
        get {
            if let popoverView = self.superview as? Popover {
                return popoverView
            }else{
                let popoverView = Popover.init(options: self.popoverOptions)
                popoverView.willShowHandler = self.willShowHandler
                popoverView.willDismissHandler = self.willDismissHandler
                popoverView.didShowHandler = self.didShowHandler
                popoverView.didDismissHandler = self.didDismissHandler
                return popoverView
            }
        }
    }

    var willShowHandler: (() -> ())? { nil }
    var willDismissHandler: (() -> ())? { nil }
    var didShowHandler: (() -> ())? { nil }
    var didDismissHandler: (() -> ())? { nil }

    func show(point: CGPoint, inView: UIView) {
        self.popoverView.show(self, point: point, inView: inView)
    }

    func show(fromView: UIView, inView: UIView) {
        self.popoverView.show(self, fromView: fromView, inView: inView)
    }

    func showAsDialog(inView: UIView) {
        self.popoverView.showAsDialog(self, inView: inView)
    }
}
