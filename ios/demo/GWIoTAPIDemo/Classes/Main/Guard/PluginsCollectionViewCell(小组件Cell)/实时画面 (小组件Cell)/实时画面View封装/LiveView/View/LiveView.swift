//
//  LiveView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import Foundation
import Lottie
import IoTVideo
import GWPlayer
import RQCore

/// 实时画面视图
/// 层级关系:
/// LiveViewContainer
///     - collectionView
///         - collectionViewCell
///             - LiveView
class LiveView: UIView {

    // 数据
    var dataItem: LiveViewContainer.CollectionViewDataItem? {
        set {
            self.vm.dataItem = newValue
        }
        get {
            self.vm.dataItem
        }
    }

    private let vm: ViewModel = .init()

    /// 放置主playerView的容器
    lazy var mainPlayerViewContainer: UIView = .init().then {
        $0.backgroundColor = .clear
        $0.isUserInteractionEnabled = false
    }

    // 放置副 playerView 的容器
    lazy var subPlayerViewsContainer: SubPlayerViewsContainer = .init(vm: self.vm).then {
        $0.backgroundColor = .clear
        $0.isUserInteractionEnabled = false
        $0.isHidden = true
    }

    // LayoutMode 为 single 时的切换操作视图
    lazy var camSwitchView: CamSwitchView = .init(vm: self.vm).then {
        $0.isHidden = true
    }

    /// 布局方式
    /// 不同的布局决定了 重试按钮 的样式
    @DidSetPublished var layoutMode: LiveViewContainer.LayoutMode? {
        didSet {
            guard let layoutMode = self.layoutMode else { return }
            self.refreshButton.titleLabel?.font = layoutMode == .single ? .systemFont(ofSize: 14) : .systemFont(ofSize: 12)
            let refreshIcon: UIImage = layoutMode == .single ? R.image.guardLiveRefresh()! : R.image.guardLiveRefreshSmall()!
            self.refreshButton.setImage(refreshIcon, for: .normal)
        }
    }

    private var loadingAnimate: LottieAnimationView = .init(name: R.file.loading_whiteJson.name).then {
        $0.backgroundBehavior = .pauseAndRestore
        $0.loopMode = .loop
        $0.isHidden = true
    }

    private var refreshButton: IVButton = .init(.top, space: 8).then {
        $0.isHidden = true
        $0.titleLabel?.font = .systemFont(ofSize: 14)
        $0.setImage(R.image.guardLiveRefresh(), for: .normal)
        $0.setTitle(String.localization.localized("AA0428", note: "加载失败"), for: .normal)
        $0.setTitleColor(R.color.text_FFFFFF()?.withAlphaComponent(0.6), for: .normal)
    }

    private var anyCancellables: Set<AnyCancellable> = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    deinit {
        logDebug("[ReoqooLiveView] ====\(self) dealloc====", "====\(self.vm) should dealloc====")
    }

    // MARK: Helper
    private func setup() {

        self.addSubview(self.mainPlayerViewContainer)
        self.mainPlayerViewContainer.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.addSubview(self.subPlayerViewsContainer)
        self.subPlayerViewsContainer.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.addSubview(self.camSwitchView)
        self.camSwitchView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.addSubview(self.loadingAnimate)
        self.loadingAnimate.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.height.equalTo(24)
        }

        self.addSubview(self.refreshButton)
        self.refreshButton.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview()
        }

        self.refreshButton.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.play()
        }).store(in: &self.anyCancellables)

        // 监听播放器对象
        self.vm.playerChangingPublisher.sink { [weak self] (oldValue: IVLivePlayer?, newValue: IVLivePlayer?) in
            // 先移除旧的 playerView
            self?.mainPlayerViewContainer.subviews.forEach({ $0.removeFromSuperview() })
            // 将 player view 取出来加入到播放视图中
            if let playerView = self?.vm.firstPlayerView {
                self?.mainPlayerViewContainer.addSubview(playerView)
                playerView.snp.makeConstraints { make in
                    make.edges.equalToSuperview()
                }
            }
        }.store(in: &self.anyCancellables)

        // 监听 vm status
        self.vm.$status
            .receive(on: DispatchQueue.main)
            .sink { [weak self] in
                switch $0 {
                case .idle:
                    break
                // 播放状态变化
                case .playerStateDidChanged(let state):
                    self?.playerStateDidChanged(state)
                // 连接状态变化
                case .playerConnectionStateDidChanged(state: let state):
                    self?.playerConnectStateDidChanged(state)
                default:
                    break
                }
            }.store(in: &self.anyCancellables)

        // Combine .numberOfPlayerViewsDidChanged(views) 以及 self.layoutMode, 以便控制视图UI
        let playerViewsDidChangedPublisher = self.vm.$status.filter({
            if case .numberOfPlayerViewsDidChanged = $0 { return true }
            return false
        }).map({ status -> [GWPlayerView] in
            guard case let .numberOfPlayerViewsDidChanged(views) = status else { return [] }
            return views
        }).prepend([])

        // 监听 player 画面, layoutMode, 设备画面数量, 当前选中的视图索引, 以便控制辅助视图的显示
        Publishers.CombineLatest4(playerViewsDidChangedPublisher, self.$layoutMode.compactMap({ $0 }), self.vm.$numOfViews, self.vm.$indexOfSelectedCam)
        .debounce(for: 0.1, scheduler: DispatchQueue.main)
        .sink { [weak self] views, layoutMode, numberOfViews, indexOfSelectedCam in
            self?.handleNumberOfPlayerViewsDidChanged(views, layoutMode: layoutMode, numberOfViews: numberOfViews)
        }.store(in: &self.anyCancellables)
    }

    func play() {
        if case let .device(dev) = self.dataItem, dev.isLiveClose {
            self.vm.processEvent(.stop)
            self.showLoading(false)
            self.refreshButton.isHidden = true
            return
        }
        self.vm.processEvent(.play)
    }

    func stop() {
        self.vm.processEvent(.stop)
    }

    func reset() {
        self.dataItem = nil
    }

}

// MARK: Helper
extension LiveView {

    /// 画面变化
    func handleNumberOfPlayerViewsDidChanged(_ views: [GWPlayerView], layoutMode: LiveViewContainer.LayoutMode, numberOfViews: Int) {
        let isMultiViews = numberOfViews > 1
        // 如果没有多画面显示, 直接 return
        if !isMultiViews {
            self.camSwitchView.isHidden = true
            self.subPlayerViewsContainer.isHidden = true
            return
        }
        // 根据当前 layoutMode 布局视图
        if layoutMode == .fourGird {
            self.camSwitchView.isHidden = true
            self.subPlayerViewsContainer.isHidden = false

            // 先移除旧的 playerView
            self.mainPlayerViewContainer.subviews.forEach({ $0.removeFromSuperview() })
            // 加入指定视图
            if let playerView = self.vm.firstPlayerView {
                self.mainPlayerViewContainer.addSubview(playerView)
                playerView.snp.makeConstraints { make in
                    make.edges.equalToSuperview()
                }
            }
        }
        if layoutMode == .single {
            self.camSwitchView.isHidden = false
            self.subPlayerViewsContainer.isHidden = true

            // 先移除旧的 playerView
            self.mainPlayerViewContainer.subviews.forEach({ $0.removeFromSuperview() })
            // 加入指定视图
            if let playerView = self.vm.firstPriorityPlayerView {
                self.mainPlayerViewContainer.addSubview(playerView)
                playerView.snp.makeConstraints { make in
                    make.edges.equalToSuperview()
                }
            }
        }
    }

    func playerConnectStateDidChanged(_ state: IVConnectionState) {
        switch state {
        case .assign_chn, .wake_up_dev, .connecting, .disconnecting:
            self.showLoading(true)
            self.refreshButton.isHidden = true
        case .connected:
            self.showLoading(false)
            self.refreshButton.isHidden = true
        case .disconnected:
            self.showLoading(false)
            self.refreshButton.isHidden = false
        @unknown default:
            break
        }
    }

    func playerStateDidChanged(_ state: GWPlayerState) {
        switch state {
        case .playing:
            self.showLoading(false)
            self.refreshButton.isHidden = true
        case .preparing, .uninit, .inited, .prepared, .loading:
            self.showLoading(true)
            self.refreshButton.isHidden = true
        case .paused, .stopping, .stopped, .completed, .error:
            self.showLoading(false)
            self.refreshButton.isHidden = false
        @unknown default:
            break
        }
    }

    private func showLoading(_ flag: Bool) {
        if flag {
            self.refreshButton.isHidden = true
            self.loadingAnimate.isHidden = false
            self.loadingAnimate.play()
        }else{
            self.loadingAnimate.isHidden = true
            self.loadingAnimate.pause()
        }
    }
}
