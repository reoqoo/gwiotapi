//
//  GuardianViewController+LiveViewContainer.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import Foundation
import RQCore

extension LiveViewContainer {
    /// 按照需求, 显示布局分为 1个 或 4个 画面, 当画面 大于 1 且 小于 4 时, 也是 4宫格布局, 没有显示的格应该显示 "暂无设备" 占位
    /// placeholder 就是占位表达
    enum CollectionViewDataItem {
        case placeholder
        case device(DeviceEntity)
    }

    /// 布局方式
    enum LayoutMode: Int, Codable, CaseIterable {
        // 单个
        case single
        // 四宫格
        case fourGird

        var icon: UIImage {
            switch self {
            case .single:
                return R.image.guardLiveLayoutModeSingle()!
            case .fourGird:
                return R.image.guardLiveLayoutModeFourGird()!
            }
        }
        
        /// 列数
        var numOfColumn: Int {
            switch self {
            case .single:
                return 1
            case .fourGird:
                return 2
            }
        }

        /// 行数
        var numOfRow: Int {
            switch self {
            case .single:
                return 1
            case .fourGird:
                return 2
            }
        }
    }
}

/// 实时画面容器. 放置多个 LiveView, 负责 LiveView 的布局
/// 结构为:
/// LiveViewContainer
///     - collectionView
///         - collectionViewCell
///             - LiveView
class LiveViewContainer: UIView {

    /// 有多少页
    @DidSetPublished var numberOfPages: Int = 0

    /// Current page
    @DidSetPublished var currentPage: Int = 0

    /// 被选中的设备
    /// 供外部监听
    @DidSetPublished var selectedDevice: DeviceEntity?

    /// 供 cell 监听 cellectionView 是否已经停止滑动交互, 当滑动结束后, 才开始画面播放
    @DidSetPublished private var collectionViewIsDragging: Bool = false

    @DidSetPublished var isCollectionViewReloadComplete: Bool = false

    // 布局模式
    var layoutMode: LayoutMode {
        get { self.vm.layoutMode }
        set {
            self.vm.layoutMode = newValue
            self.collectionViewFlowLayout.layoutMode = newValue
        }
    }

    private let vm: ViewModel = .init()

    private var anyCancellables: Set<AnyCancellable> = []

    private lazy var collectionViewFlowLayout: LiveViewCollectionViewFlowLayout = .init().then {
        $0.scrollDirection = .horizontal
    }

    private lazy var collectionView: UICollectionView! = .init(frame: .zero, collectionViewLayout: self.collectionViewFlowLayout).then {
        $0.bounces = false
        $0.dataSource = self
        $0.delegate = self
        $0.isPagingEnabled = true
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.backgroundColor = R.color.background_placeholder0_4D4D4D()
        $0.isPrefetchingEnabled = false
        $0.register(LiveViewCollectionViewCell.self, forCellWithReuseIdentifier: String.init(describing: LiveViewCollectionViewCell.self))
    }

    init(devices: [DeviceEntity]) {

        super.init(frame: .zero)

        self.vm.devices = devices
        
        self.collectionView.backgroundColor = R.color.background_FFFFFF_white()

        self.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        // numberOfPage 由 vm 计算, view 不负责此功能, 所以将 vm.numberPage 和 self.numberPage 绑定
        self.vm.$numberOfPages.sink(receiveValue: { [weak self] i in
            self?.numberOfPages = i
        }).store(in: &self.anyCancellables)

        // 当 datasource 或 layout模式 发生改变, 刷新视图
        self.vm.$dataSources.combineLatest(self.vm.$layoutMode)
            .debounce(for: 0.2, scheduler: DispatchQueue.main)
            .sink { [weak self] _, _ in
                self?.stopAll(immediately: false)
                self?.isCollectionViewReloadComplete = false
                self?.collectionView.reloadData {
                    self?.isCollectionViewReloadComplete = true
                }
            }.store(in: &self.anyCancellables)

        // 监听 viewController viewStatus / App become active, 以影响 isDisplaying 属性
        let isAppActiveObservable = AppEntranceManager.shared.$applicationState.map { $0 == .didBecomeActive || $0 == .willEnterForeground || $0 == .didFinishLaunching }
        let viewStateObservable = NotificationCenter.default.publisher(for: GuardianViewController.viewStatusDidChangedNotificationName).compactMap({ notification -> Bool? in
            guard let viewStatus = notification.userInfo?[GuardianViewController.viewStatusUserInfoKey] as? BaseViewController.ViewStatus else { return nil }
            return viewStatus == .willAppear || viewStatus == .didAppear
        })
        // App进入前台 且 视图正在显示
        let isDisplayingObservable = isAppActiveObservable.combineLatest(viewStateObservable).map({ $0 && $1 })

        // 将几个播放/暂停相关属性 combine 起来
        isDisplayingObservable.removeDuplicates().combineLatest(self.$collectionViewIsDragging.removeDuplicates(), self.$isCollectionViewReloadComplete.removeDuplicates())
            .debounce(for: 0.2, scheduler: DispatchQueue.main)
            .sink { [weak self] isDisplaying, isDragging, isCollectionViewReloadComplete in
                if !isDisplaying {
                    self?.stopAll(immediately: false)
                    return
                }
                if !isDragging && isDisplaying && isCollectionViewReloadComplete {
                    self?.playAll()
                    return
                }
            }.store(in: &self.anyCancellables)
    }

    private override init(frame: CGRect) { fatalError("init(frame:) has not been implemented") }
    
    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    // 更新
    public func updateDevcies(_ devices: [DeviceEntity]) {
        self.vm.devices = devices
    }

    // 更新当前显示页 (外部 pageControl 可能会控制)
    public func updateCurrentPage(_ page: Int, animated: Bool = false) {
        if page == self.currentPage { return }
        if page >= self.vm.dataSources.count { return }
        let offsetX = CGFloat(page) * self.collectionView.bounds.width
        self.collectionView.setContentOffset(.init(x: offsetX, y: 0), animated: animated)
    }

    private func playAll() {
        for cell in self.collectionView.visibleCells {
            guard let cell = cell as? LiveViewCollectionViewCell else { return }
            cell.liveView.play()
        }
    }

    private func stopAll(immediately: Bool) {
        for cell in self.collectionView.visibleCells {
            guard let cell = cell as? LiveViewCollectionViewCell else { return }
            cell.liveView.stop()
        }
    }
}

extension LiveViewContainer: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.vm.dataSources.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: LiveViewCollectionViewCell.self), for: indexPath) as! LiveViewCollectionViewCell
        cell.dataItem = self.vm.dataSources[safe_: indexPath.item]
        cell.layoutMode = self.layoutMode
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let cell = cell as? LiveViewCollectionViewCell else { return }
        cell.liveView.reset()
    }

}

extension LiveViewContainer: UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let item = self.vm.dataSources[indexPath.item]
        guard case let .device(device) = item else { return }
        self.selectedDevice = device
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        if self.vm.dataSources.count == 1 || self.vm.layoutMode == .single {
            return .init(width: collectionView.bounds.width - 1, height: collectionView.bounds.height)
        }
        let w = collectionView.bounds.width * 0.5 - 1
        let h = collectionView.bounds.height * 0.5 - 0.5
        return .init(width: w, height: h)
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat { 1 }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat { 1 }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        .init(top: 0, left: 0.5, bottom: 0, right: 0.5)
    }

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        self.currentPage = Int((scrollView.contentOffset.x / scrollView.bounds.width).rounded())
    }

    func scrollViewWillBeginDecelerating(_ scrollView: UIScrollView) {
        self.collectionViewIsDragging = true
    }

    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        self.collectionViewIsDragging = false
    }
    
}
