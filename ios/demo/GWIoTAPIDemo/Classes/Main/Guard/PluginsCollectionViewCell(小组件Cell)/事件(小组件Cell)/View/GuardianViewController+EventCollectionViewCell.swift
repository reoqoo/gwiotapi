//
//  GuardianViewController+EventCollectionCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import Foundation

extension GuardianViewController {

    /// 视图结构:
    ///     - self
    ///         - contentView (系统提供)
    ///             - eventContentView: EventContentView
    /// 当需要展开时, 会对 eventContentView 从 contentView 中取出, 放置到新的父视图中, 重新布局
    /// 当需要折叠时, 会重新加入到 contentView
    class EventCollectionCell: PluginCollectionViewCell {

        // vm 和 self.pluginContentView 共享
        let vm: ViewModel = .init()

        override init(frame: CGRect) {
            super.init(frame: frame)
            self.pluginContentView = EventContentView.init(vm: self.vm)
            self.addPluginContentViewIfNeed()
            self.layoutPluginContentView()
        }

        override func layoutPluginContentView() {
            let width: CGFloat = UIScreen.main.bounds.width - 32
            let height: CGFloat = 330
            self.contentView.addSubview(self.pluginContentView)
            self.pluginContentView.snp.remakeConstraints { make in
                make.edges.equalToSuperview()
                make.width.equalTo(width)
                make.height.equalTo(height)
            }
        }

        override func addPluginContentViewIfNeed() {
            if self.contentView.subviews.contains(where: { $0 == self.pluginContentView }) { return }
            self.contentView.addSubview(self.pluginContentView)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
    
}
