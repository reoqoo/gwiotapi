//
//  BeginnerGuidanceView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 25/9/2023.
//

import Foundation

/// 新手引导弹出管理器
class BeginnerGuidance {

    static let shared: BeginnerGuidance = .init()

    private init() {}
    
    // 引导任务栈
    var items: [Item] = []

    // 插入新的新手引导弹出请求
    func append(_ item: Item) {
        self.items.append(item)
        self.popBubbleIfNeed()
    }
    
    var currentPopover: Popover?

    var anyCancellables: Set<AnyCancellable> = []

    private func popBubbleIfNeed() {
        if let _ = self.currentPopover { return }
        guard let item = self.items.first else { return }
        guard let target = item.target else { return }
        guard let keyWindow = AppEntranceManager.shared.keyWindow else { return }

        // 移除第一个
        self.items.removeFirst()
        
        // 创建新手引导 bubbleView
        let bubbleView = BubbleView.init(item: item)
        let size = bubbleView.systemLayoutSizeFitting(.init(width: keyWindow.bounds.width * 0.5, height: keyWindow.bounds.size.height), withHorizontalFittingPriority: .required, verticalFittingPriority: .fittingSizeLevel)
        bubbleView.frame = .init(origin: .zero, size: size)
        bubbleView.actionBtn.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                self?.currentPopover?.dismiss()
            }).store(in: &self.anyCancellables)
        
        let opts: [PopoverOption] = [
            .animationIn(0.3),
            .animationOut(0.3),
            .color(.black.withAlphaComponent(0.75)),
            .cornerRadius(12),
            .dismissOnBlackOverlayTap(false)
        ]
        let popover = Popover.init(options: opts, dismissHandler: { [weak self] in
            self?.currentPopover = nil
            self?.popBubbleIfNeed()
        })
        self.currentPopover = popover
        
        DispatchQueue.main.asyncAfter(deadline: .now() + item.showAfterDelay) {
            if let inView = item.inView {
                popover.show(bubbleView, fromView: target, inView: inView)
            }else{
                popover.show(bubbleView, fromView: target)
            }
        }
    }
}
