//
//  TabBar.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 2/2/2024.
//

import Foundation

protocol TabBarDelegate: AnyObject {
    func reoqooTabBar(_ tabbar: ScrollableTabBar, didSelectItem item: ScrollableTabBar.Item, atIndex: Int)
}

extension ScrollableTabBar {
    struct Item {
        let title: String

        var font: UIFont = .systemFont(ofSize: 18, weight: .medium)
        var selectedColor: UIColor = R.color.text_000000_90()!
        var disableColor: UIColor = R.color.text_000000_60()!
        var zoomMax: Double = 1.2
    }
    
    enum BottomLineWidthDescription {
        // 固定宽度
        case fixed(_: Double)
        // 跟随Item宽度
        case followItem

        var width: Double? {
            if case let .fixed(w) = self {
                return w
            }
            return nil
        }
    }

    enum BottomLineStyle {
        // 不显示
        case none
        /// 显示:
        /// Color 颜色
        /// width 宽度
        /// height: 高度
        /// radius 圆角
        case show(color: UIColor, widthDescription: BottomLineWidthDescription, height: Double, radius: Double)
    }
}

class ScrollableTabBar: UIView {

    public var contentInset: UIEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: 8) {
        didSet {
            self.scrollView.contentInset = self.contentInset
        }
    }

    public var spacing: Double = 8 {
        didSet {
            self.stackView.spacing = self.spacing
        }
    }

    public var bottomLineStyle: BottomLineStyle = .none {
        didSet {
            if case .none = self.bottomLineStyle {
                self.bottomLine.isHidden = true
                self.bottomLineColor = nil
                self.bottomLineWidthDescription = nil
            }
            if case let .show(color, widthDescription, height, radius) = self.bottomLineStyle {
                self.bottomLine.isHidden = false
                self.bottomLine.layer.cornerRadius = radius
                self.bottomLineColor = color
                self.bottomLineHeight = height
                self.bottomLine.backgroundColor = color
                self.bottomLineWidthDescription = widthDescription
            }
        }
    }

    private var bottomLineWidthDescription: BottomLineWidthDescription? {
        didSet {
            guard let widthDescription = self.bottomLineWidthDescription else { return }
            self.bottomLine.snp.remakeConstraints { make in
                make.width.equalTo(widthDescription.width ?? 48)
                make.bottom.equalToSuperview()
                make.height.equalTo(self.bottomLineHeight)
                if let targetBtn = self.stackView.arrangedSubviews[safe_: self.selectedIndex] {
                    make.centerX.equalTo(targetBtn)
                }else{
                    make.leading.equalToSuperview()
                }
            }
        }
    }

    private var bottomLineHeight: Double = 5
    
    private var bottomLineColor: UIColor?

    private(set) lazy var scrollView: UIScrollView = .init().then {
        $0.delegate = self
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.contentInset = self.contentInset
    }

    private(set) lazy var stackView: UIStackView = .init().then {
        $0.axis = .horizontal
        $0.alignment = .center
        $0.distribution = .fillProportionally
        $0.spacing = self.spacing
    }

    private(set) lazy var bottomLine: UIView = .init().then {
        $0.isHidden = true
        $0.backgroundColor = R.color.brand()
    }
    
    /// badge 存储
    private var idx_badge_mapping: [Int: UIView] = [:]

    weak var delegate: TabBarDelegate?

    public var items: [Item] = [] {
        didSet {
            self.setupItems()
        }
    }
    
    /// 用于记录 item们 本来的宽度, 在形变发生时, 同时更新Autolayout, 控制按钮宽度的变化
    private var itemsOriginalWidth: [CGFloat] = []
    
    weak var linkageScrollView: UIScrollView? {
        didSet {
            // 监听 ScrollView, 进行滑动联动
            guard let scrollView = self.linkageScrollView else {
                self.scrollViewObserveToken?.invalidate()
                return
            }
            self.scrollViewObserveToken = scrollView.observe(\.contentOffset) { [weak self] scrollView , offsetWrapped in
                if scrollView.isDragging {
                    self?.linkageScrollViewOffsetDidChange(scrollView.contentOffset)
                }
                if self?.linkageScrollViewIsDragging ?? false && !scrollView.isDragging {
                    self?.linkageScrollViewDidEndDecelerating()
                }
                self?.linkageScrollViewIsDragging = scrollView.isDragging
            }
            // 先执行一次主动选中的操作
            self.linkageScrollViewDidEndDecelerating()
        }
    }

    private var scrollViewObserveToken: NSKeyValueObservation?
    private var linkageScrollViewIsDragging: Bool = false
    private var startOffsetX: Double = 0
    private var selectedIndex: Int = 0
    private var isSelectManualy: Bool = false

    init(items: [Item] = [], linkageScrollView: UIScrollView? = nil) {
        super.init(frame: .zero)
        self.setupUI()
        self.items = items
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setupUI()
    }

    override func willMove(toSuperview newSuperview: UIView?) {
        super.willMove(toSuperview: newSuperview)
        if newSuperview == nil {
            self.scrollViewObserveToken?.invalidate()
        }
    }

    deinit {
        self.scrollViewObserveToken?.invalidate()
    }

    /// 显示 Badge
    public func showBadgeAtIndex(_ idx: Int, diameter: Double = 6) {
        // 如果idx对应的item不存在
        guard let _ = self.items[safe_: idx] else { return }
        // 对应的btn不存在
        guard let btn = self.stackView.arrangedSubviews[safe_: idx] else { return }
        // 如果已经有了, return
        if let _ = self.idx_badge_mapping[idx] { return }
        let badge = UIView.init()
        badge.backgroundColor = .red
        badge.layer.cornerRadius = diameter * 0.5
        badge.layer.masksToBounds = true
        self.stackView.addSubview(badge)
        badge.snp.makeConstraints { make in
            make.top.equalTo(btn.snp.top)
            make.trailing.equalTo(btn.snp.trailing).offset(6)
            make.height.width.equalTo(diameter)
        }
        self.idx_badge_mapping[idx] = badge
    }

    /// 隐藏 Badge
    public func hideBadge(_ idx: Int) {
        guard let badge = self.idx_badge_mapping[idx] else { return }
        badge.removeFromSuperview()
        self.idx_badge_mapping[idx] = nil
    }

    // MARK: Helper
    private func setupUI() {
        self.addSubview(self.scrollView)
        self.scrollView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.scrollView.addSubview(self.stackView)
        self.stackView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
            make.height.equalTo(self.snp.height)
        }

        self.stackView.addSubview(self.bottomLine)
    }

    private func setupItems() {
        self.itemsOriginalWidth = []
        self.stackView.arrangedSubviews.forEach {
            $0.removeFromSuperview()
        }
        for (idx, item) in self.items.enumerated() {
            let btn = self.createButtonWithItem(item)
            self.stackView.addArrangedSubview(btn)
            btn.tag = idx
            let btnWidth = btn.systemLayoutSizeFitting(.zero, withHorizontalFittingPriority: .defaultHigh, verticalFittingPriority: .defaultHigh).width
            btn.snp.makeConstraints { make in
                make.width.equalTo(btnWidth)
            }
            self.itemsOriginalWidth.append(btnWidth)
        }
        if let firstItemBtn = self.stackView.arrangedSubviews.first {
            self.bottomLine.snp.remakeConstraints { make in
                make.bottom.equalToSuperview()
                make.height.equalTo(self.bottomLineHeight)
                make.width.equalTo(self.bottomLineWidthDescription?.width ?? 48)
                make.centerX.equalTo(firstItemBtn)
            }
        }
    }

    private func createButtonWithItem(_ item: Item) -> UIButton {
        let btn = UIButton.init(type: .custom)
        btn.setTitle(item.title, for: .normal)
        btn.titleLabel?.font = item.font
        btn.setTitleColor(item.selectedColor, for: .normal)
        btn.addTarget(self, action: #selector(self.btnOnClick(_:)), for: .touchUpInside)
        return btn
    }

    private func linkageScrollViewDidEndDecelerating() {
        guard let linkageScrollViewOffsetX = self.linkageScrollView?.contentOffset.x, let width = self.linkageScrollView?.bounds.width else { return }
        self.startOffsetX = linkageScrollViewOffsetX
        var idx = (linkageScrollViewOffsetX + 0.5 * width) / width
        // 如遇 idx 为 nan, 置为0
        idx = idx.isNaN ? 0 : idx
        self.selectItemAtIndex(Int(idx))
    }

    private func linkageScrollViewOffsetDidChange(_ offset: CGPoint) {

        if self.isSelectManualy { return }
        guard let linkageScrollView = self.linkageScrollView else { return }
        if linkageScrollView.contentOffset.x < 0 || linkageScrollView.contentOffset.x > linkageScrollView.contentSize.width { return }

        let x = offset.x
        var sourceBtn: UIButton?
        var targetBtn: UIButton?
        var sourceIdx: Int = 0
        var targetIdx: Int = 0

        // 滑动进度
        let progress = fabs(x - self.startOffsetX) / max(linkageScrollView.bounds.width, 1)
        
        // 向右
        if x > self.startOffsetX {
            if progress != 0 {
                sourceIdx = self.selectedIndex
                targetIdx = sourceIdx + 1
                if progress >= 1 {
                    self.selectedIndex += 1
                    self.startOffsetX += linkageScrollView.bounds.width
                }
            }
            if targetIdx >= self.items.count { return }
            sourceBtn = self.stackView.arrangedSubviews.filter({ $0.tag == sourceIdx }).first as? UIButton
            targetBtn = self.stackView.arrangedSubviews.filter({ $0.tag == targetIdx }).first as? UIButton
        }

        // 向左
        if x < self.startOffsetX {
            if progress != 0 {
                sourceIdx = self.selectedIndex
                targetIdx = sourceIdx - 1
                if progress >= 1 {
                    self.selectedIndex -= 1
                    self.startOffsetX -= linkageScrollView.bounds.width
                }
            }
            if targetIdx >= self.items.count { return }
            sourceBtn = self.stackView.arrangedSubviews.filter({ $0.tag == sourceIdx }).first as? UIButton
            targetBtn = self.stackView.arrangedSubviews.filter({ $0.tag == targetIdx }).first as? UIButton
        }

        if x == self.startOffsetX { return }

        // 改变颜色
        guard let sourceItem = self.items[safe_: sourceIdx],
        let targetItem = self.items[safe_: targetIdx]
        else { return }

        let sourceCurrentColor = sourceItem.selectedColor
        let sourceToColor = sourceItem.disableColor
        let targetCurrentColor = targetItem.disableColor
        let targetToColor = targetItem.selectedColor

        guard let sourceCurrentColor_R = sourceCurrentColor.rgb_r,
        let sourceCurrentColor_G = sourceCurrentColor.rgb_g,
        let sourceCurrentColor_B = sourceCurrentColor.rgb_b
        else { return }

        guard let sourceToColor_R = sourceToColor.rgb_r,
        let sourceToColor_G = sourceToColor.rgb_g,
        let sourceToColor_B = sourceToColor.rgb_b
        else { return }

        guard let targetCurrentColor_R = targetCurrentColor.rgb_r,
        let targetCurrentColor_G = targetCurrentColor.rgb_g,
        let targetCurrentColor_B = targetCurrentColor.rgb_b
        else { return }

        guard let targetToColor_R = targetToColor.rgb_r,
        let targetToColor_G = targetToColor.rgb_g,
        let targetToColor_B = targetToColor.rgb_b
        else { return }
        
        let sourceColor_R = sourceCurrentColor_R + (sourceToColor_R - sourceCurrentColor_R) * progress
        let sourceColor_G = sourceCurrentColor_G + (sourceToColor_G - sourceCurrentColor_G) * progress
        let sourceColor_B = sourceCurrentColor_B + (sourceToColor_B - sourceCurrentColor_B) * progress
        let sourceColor: UIColor = .init(r: sourceColor_R, g: sourceColor_G, b: sourceColor_B)
        sourceBtn?.setTitleColor(sourceColor, for: .normal)

        let targetColor_R = targetCurrentColor_R + (targetToColor_R - targetCurrentColor_R) * progress
        let targetColor_G = targetCurrentColor_G + (targetToColor_G - targetCurrentColor_G) * progress
        let targetColor_B = targetCurrentColor_B + (targetToColor_B - targetCurrentColor_B) * progress
        let targetColor: UIColor = .init(r: targetColor_R, g: targetColor_G, b: targetColor_B)
        targetBtn?.setTitleColor(targetColor, for: .normal)

        // 形变
        let targetZoomMax = targetItem.zoomMax
        let targetTransform_factor = 1 + (targetZoomMax - 1) * progress
        targetBtn?.transform = .init(scaleX: targetTransform_factor, y: targetTransform_factor)
        if let originalWidth = self.itemsOriginalWidth[safe_: targetIdx] {
            targetBtn?.snp.updateConstraints { make in
                make.width.equalTo(originalWidth * targetTransform_factor)
            }
        }

        let sourceZoomMax = sourceItem.zoomMax
        let sourceTransform_factor = sourceZoomMax + (1 - sourceZoomMax) * progress
        sourceBtn?.transform = .init(scaleX: sourceTransform_factor, y: sourceTransform_factor)
        if let originalWidth = self.itemsOriginalWidth[safe_: sourceIdx] {
            sourceBtn?.snp.updateConstraints { make in
                make.width.equalTo(originalWidth * sourceTransform_factor)
            }
        }

        // Bottom Line 定位 / 宽度
        let bottomLineTranslationX = ((targetBtn?.centerX ?? 0) - (sourceBtn?.centerX ?? 0)) * progress
        self.bottomLine.transform = .init(translationX: bottomLineTranslationX, y: 0)
    }

    private func selectItemAtIndex(_ idx: Int) {
        self.makeAllBtnDeselected()
        guard let btn = self.stackView.arrangedSubviews.filter({ $0.tag == idx }).first as? UIButton else { return }

        let targetColor = self.items[idx].selectedColor
        let zoomMax = self.items[idx].zoomMax
        btn.transform = .init(scaleX: zoomMax, y: zoomMax)
        btn.setTitleColor(targetColor, for: .normal)

        if let originalWidth = self.itemsOriginalWidth[safe_: idx] {
            btn.snp.updateConstraints { make in
                make.width.equalTo(originalWidth * zoomMax)
            }
        }

        self.bottomLine.snp.remakeConstraints { make in
            make.bottom.equalToSuperview()
            make.height.equalTo(self.bottomLineHeight)
            make.width.equalTo(self.bottomLineWidthDescription?.width ?? 48)
            make.centerX.equalTo(btn)
        }
        self.bottomLine.transform = .identity
        self.bottomLine.layoutIfNeeded()

        self.isSelectManualy = true
        self.selectedIndex = idx
        self.startOffsetX = (self.linkageScrollView?.bounds.width ?? 0) * Double(idx)
        self.delegate?.reoqooTabBar(self, didSelectItem: self.items[idx], atIndex: idx)
        self.isSelectManualy = false
    }

    private func makeAllBtnDeselected() {
        self.stackView.arrangedSubviews.enumerated().forEach {
            guard let btn = $1 as? UIButton else { return }
            let item = self.items[$0]
            btn.setTitleColor(item.disableColor, for: .normal)
            btn.transform = .identity
            if let originalWidth = self.itemsOriginalWidth[safe_: $0] {
                btn.snp.updateConstraints { make in
                    make.width.equalTo(originalWidth)
                }
            }
        }
    }

    // MARK: Action
    @objc func btnOnClick(_ sender: UIButton) {
        guard let idx = self.stackView.arrangedSubviews.firstIndex(of: sender) else { return }
        self.selectItemAtIndex(idx)
    }
}

extension ScrollableTabBar: UIScrollViewDelegate {
    
}
