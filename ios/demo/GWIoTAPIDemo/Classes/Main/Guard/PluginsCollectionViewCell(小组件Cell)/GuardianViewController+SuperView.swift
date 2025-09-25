//
//  GuardianViewController+PluginCollectionViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import Foundation

extension GuardianViewController {

    /// 小组件Cell的父类
    class PluginCollectionViewCell: UICollectionViewCell {

        /// 是否可折叠的描述
        enum Expandable {
            case yes(isExpanded: Bool)
            case no
        }

        // 提供给子类重写
        var pluginContentView: PluginCollectionViewCellContent = .init()
        
        /// 小组件Cell 对外提供很多交互事件(例如按钮点击)发布者, 外部(一般是ViewController)对事件进行订阅的操作一般发生在 willDisplayCell 中,
        /// 由于 Cell 是可复用的, 当 Cell 在视图上滑动时, willDisplayCell 会调用多次, 就会造成重复订阅的情况发生
        /// 所以, 将这类 事件交互订阅关系(disposable) 交给 Cell 管理, 外部在 didEndDisplayCell 代理方法中重置此 disposeBag 即可解决重复订阅的问题
        var externalCancellables: Set<AnyCancellable> = []

        override init(frame: CGRect) {
            super.init(frame: frame)
            self.backgroundColor = .clear
            self.contentView.backgroundColor = R.color.background_FFFFFF_white()!
            self.contentView.layer.cornerRadius = 12
            self.contentView.layer.masksToBounds = true
        }

        /// 当 pluginContentView 被展开时, 会暂时移出 cell.contentView, 添加到新的父视图
        /// 当 pluginContentView 被折叠时, 又会重新被添加回 cell.contentView
        /// /// 所以, 需要提供一个方法供子类重写, 将 pluginContentView 添加
        func addPluginContentViewIfNeed() {}

        /// 当 pluginContentView 被展开时, 会暂时移出 cell.contentView, 添加到新的父视图
        /// 当 pluginContentView 被折叠时, 又会重新被添加回 cell.contentView
        /// 所以, 需要提供一个方法供子类重写, 对 pluginContentView 进行 Autolayout 布局
        func layoutPluginContentView() {}

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        
        /// 观察到一个现象, performBatchUpdates / reloadItemAtIndexPaths 会触发 cell 重用机制, 旧的cell不会被销毁
        /// 所以, 当 cellDidEndDisplay, 应该移除业务操作
        func didEndDisplay() {
            // 解除按钮点击监听
            self.externalCancellables = []
        }
    }

    /// 根据看家需求, 小组件(Cell) 中的子视图是可以被 "展开" 的, 所以便有了这个父类
    /// 当视图被展开时, 会从原本的 superView(cell) 中抽出, 加到新的 superView 上重设 Autolayout
    /// 此视图分为 topArea, contentView, bottomArea 三个部分
    /// topArea: 顶部 "折叠" 按钮(父类已添加), 供子类添加 title, 更多按钮 等元素
    /// contentView: 提供给子类加入元素, 当视图处于 "折叠" 时, 可快速禁止其中的用户交互
    /// bottomArea: 供子类提供 "查看更多"按钮
    class PluginCollectionViewCellContent: UIView {
        
        /// 对 "是否可以折叠" 的描述
        var expandable: PluginCollectionViewCell.Expandable = .yes(isExpanded: false) {
            didSet {
                // 不可扩展, 不显示扩展按钮
                self.topAreaAccessoryViewContainerTop2SuperViewConstraint?.update(priority: .init(999))
                self.topAreaAccessoryViewContainerTop2FlodBtnBottomConstraint?.update(priority: .init(99))
                self.flodBtn.isHidden = true

                // 如果 "扩展" 了, 要显示顶部扩展按钮
                if case let .yes(isExpanded) = self.expandable, isExpanded {
                    self.topAreaAccessoryViewContainerTop2SuperViewConstraint?.update(priority: .init(99))
                    self.topAreaAccessoryViewContainerTop2FlodBtnBottomConstraint?.update(priority: .init(999))
                    self.flodBtn.isHidden = false
                }

                self.layoutIfNeeded()
            }
        }

        /// 是否展开中
        var isExpanded: Bool {
            guard case let .yes(isExpanded) = self.expandable else { return false }
            return isExpanded
        }

        /// 用于记录"展开"前的 frame
        var frameThatBeforeExpanded: CGRect?

        /// 顶部区域
        /// 放置:
        /// - "折叠按钮",
        /// - topContainer
        /// 拖拽手势也是放在这个区域, 仅仅拖拽这个区域可触发拖拽 "收起" 的操作
        private lazy var topArea: UIView = .init().then {
            $0.backgroundColor = .clear
        }

        /// 折叠按钮
        private(set) lazy var flodBtn: UIButton = .init(type: .custom).then {
            $0.setImage(R.image.commonArrowBottomStyle0(), for: .normal)
            $0.isHidden = true
        }

        /// 添加在 expandArea, 当子类对 topContainer 赋值时, topContainer 被添加到 此处
        private var topAreaAccessoryViewContainer: UIView = .init()

        private var topAreaAccessoryViewContainerTop2FlodBtnBottomConstraint: SnapKit.Constraint?

        private var topAreaAccessoryViewContainerTop2SuperViewConstraint: SnapKit.Constraint?

        /// 主要内容视图
        private(set) lazy var contentView: UIView = .init().then {
            $0.backgroundColor = .clear
        }

        /// 底部区域
        /// 供外部放置子视图
        private(set) lazy var bottomArea: UIView = .init().then {
            $0.backgroundColor = R.color.background_FFFFFF_white()
        }

        private var anyCancellables: Set<AnyCancellable> = []

        override init(frame: CGRect) {
            super.init(frame: frame)

            self.clipsToBounds = true
            
            self.addSubview(self.topArea)
            self.topArea.snp.makeConstraints { make in
                make.top.leading.trailing.equalToSuperview()
            }

            self.topArea.addSubview(self.flodBtn)
            self.flodBtn.snp.makeConstraints { make in
                make.top.equalToSuperview()
                make.centerX.equalToSuperview()
            }

            self.topArea.addSubview(self.topAreaAccessoryViewContainer)
            self.topAreaAccessoryViewContainer.snp.makeConstraints { make in
                self.topAreaAccessoryViewContainerTop2FlodBtnBottomConstraint = make.top.equalTo(self.flodBtn.snp.bottom).priority(.init(99)).constraint
                self.topAreaAccessoryViewContainerTop2SuperViewConstraint = make.top.equalToSuperview().priority(.init(999)).constraint
                make.leading.trailing.bottom.equalToSuperview()
            }

            self.addSubview(self.contentView)
            self.contentView.snp.makeConstraints { make in
                make.top.equalTo(self.topArea.snp.bottom)
                make.leading.trailing.bottom.equalToSuperview()
            }

            self.addSubview(self.bottomArea)
            self.bottomArea.snp.makeConstraints { make in
                make.leading.trailing.bottom.equalToSuperview()
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        /// 设置顶部内容视图
        /// 供子类调用. 将放置于顶部, 传入的 View 需具备固定高度 或 自主通过Autolayout设置 height
        func setTopAccessoryView(_ view: UIView?) {
            self.topAreaAccessoryViewContainer.removeAllSubviews()
            guard let view = view else { return }
            self.topAreaAccessoryViewContainer.addSubview(view)
            view.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }
        }

        /// 设置底部辅助视图
        func setBottomAccessoryView(_ view: UIView?) {
            self.bottomArea.removeAllSubviews()
            guard let view = view else { return }
            self.bottomArea.addSubview(view)
            view.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }
        }

    }
}
