//
//  LiveView+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 17/3/2025.
//

import Foundation
import IoTVideo
import GWPlayer
import RQCore

extension LiveView.ViewModel {
    enum Status {
        case idle
        // 播放状态变更
        case playerStateDidChanged(state: GWPlayerState)
        // 连接状态变更
        case playerConnectionStateDidChanged(state: IVConnectionState)
        // 播放画面数量变化
        case numberOfPlayerViewsDidChanged(views: [GWPlayerView])
    }

    enum Event {
        case play
        case stop
        case updateSelectedCameraIndex(Int)
    }
}

extension LiveView {
    class ViewModel: NSObject {

        var anyCancellables: Set<AnyCancellable> = []

        override init() {
            super.init()

            /// 监听播放器播放状态和画面数量变化状态, 播放中时会执行一次截图, 画面数量变化时会执行一次截图
            Publishers.CombineLatest(self.playStateChangedPublisher, self.numberOfPlayerViewsChangedPublisher.prepend([]))
                .debounce(for: 0.3, scheduler: DispatchQueue.main)
                .sink { [weak self] status, _ in
                    guard case .playing = status else { return }
                    // 让 player 截图
                    self?.takeSnapshot()
                }.store(in: &self.anyCancellables)
        }

        // 数据
        public var dataItem: LiveViewContainer.CollectionViewDataItem? {
            didSet {
                guard let device = self.device else {
                    self.reset()
                    return
                }

                // 如果是多画面设备, 当前选中哪个画面
                let indexOfSelectedCamUserDefaultKey: String = UserDefaults.UserKey.Reoqoo_LiveViewMulitCameraSelectedIndexKeyPrefix.rawValue + "_" + device.deviceId
                self.indexOfSelectedCam = AccountCenter.shared.currentUser?.userDefault?.integer(forKey: indexOfSelectedCamUserDefaultKey)

                self.player = .init(deviceId: device.deviceId, delegate: self)
                self.player?.mute = true
                self.player?.decodePriority = .hardwarePriority
                self.player?.scalingMode = .fill
                self.player?.setDefinition(.mid, completionHandler: { err in
                    guard let err = err else { return }
                    logError("[ReoqooLiveView] 设置清晰度时出现错误: ", err)
                })
                self.numOfViews = device.numberOfViews
            }
        }

        var device: DeviceEntity? {
            guard let dataItem = self.dataItem else {
                return nil
            }
            guard case let .device(dev) = dataItem else {
                return nil
            }
            return dev
        }

        // player
        private var player: IVLivePlayer? {
            didSet {
                self.playerChangingPublisher.send((oldValue: oldValue, newValue: self.player))
            }
        }

        // 供 View 监听 player 变化, 以便插入/移除 playerView
        public let playerChangingPublisher: PassthroughSubject<(oldValue: IVLivePlayer?, newValue: IVLivePlayer?), Never> = .init()

        /// 播放器画面截图
        /// 播放中时会执行一次截图
        /// 画面数量变化时会执行一次截图
        /// 然后赋值到这个数组
        @DidSetPublished public var snapshotImages: [UIImage] = []

        @DidSetPublished public var numOfViews: Int = 1

        // 状态, 供 View 监听
        @DidSetPublished public var status: Status = .idle

        /// 当前选中的摄像头索引
        /// 不允许写, 要写调方法写
        @DidSetPublished public private(set) var indexOfSelectedCam: Int?

        /// 播放状态改变时触发这个发布者
        private let playStateChangedPublisher: PassthroughSubject<GWPlayerState, Never> = .init()
        /// 播放画面数量改变时触发这个发布者
        private let numberOfPlayerViewsChangedPublisher: PassthroughSubject<Void, Never> = .init()

        func processEvent(_ event: Event) {
            switch event {
            case .play:
                self.play()
            case .stop:
                self.stop()
            case .updateSelectedCameraIndex(let i):
                guard let deviceId = self.device?.deviceId else { return }
                self.indexOfSelectedCam = i
                // 存到 UserDefaults
                let userDefaultKey: String = UserDefaults.UserKey.Reoqoo_LiveViewMulitCameraSelectedIndexKeyPrefix.rawValue + "_" + deviceId
                AccountCenter.shared.currentUser?.userDefault?.set(i, forKey: userDefaultKey)
                AccountCenter.shared.currentUser?.userDefault?.synchronize()
            }
        }

        private func play() {
            self.player?.play()
        }

        private func stop() {
            self.player?.stop()
        }

        /// 重置所有状态
        /// 更换模型, cell 被 reuse 时需要执行
        private func reset() {
            self.stop()
            self.player = nil
            self.numOfViews = 0
            self.snapshotImages = []
            self.indexOfSelectedCam = 0
        }

        deinit {
            logDebug("[ReoqooLiveView] ====\(self) dealloc====")
        }
    }
}

// MARK: Helper
extension LiveView.ViewModel {
    /// 执行截图
    func takeSnapshot() {
        let attr = GWVideoViewAttr.videoAttr(withPathPattern: nil, viewIndices: nil)

        self.player?.takeSnapshot(attr, onSuccess: { [weak self] mapping in
            let keys = mapping.keys.sorted { Int($0) < Int($1) }
            var images: [UIImage] = []
            keys.forEach {
                guard let img = mapping[$0] as? UIImage else { return }
                images.append(img)
            }
            self?.snapshotImages = images
        }, onError: { err in
            logError("[ReoqooLiveView] 截图时出现错误", err)
        })
    }

    /// 先尝试从 UserDefaults 中取出用户指定的 view, 如果用户没指定, 则取 videoIndex 值最小的那个
    /// 从 videoViews 中取出 videoIndex 值最小的一个 playerView
    var firstPriorityPlayerView: GWPlayerView? {
        let views = self.player?.videoViews.sorted { $0.videoIndex < $1.videoIndex }
        if let idx = self.indexOfSelectedCam, let target = views?[safe_: idx] {
            return target
        }
        return views?.first
    }

    var firstPlayerView: GWPlayerView? {
        let views = self.player?.videoViews.sorted { $0.videoIndex < $1.videoIndex }
        return views?.first
    }
}

extension LiveView.ViewModel: IVLivePlayerDelegate {
    // 播放器连接状态变更
    func player(_ player: IVPlayer, onConnStateChange state: IVConnectionState) {
        logInfo(["[ReoqooLiveView] player: \(player) device: \(player.deviceId) connectStateDidChanged: \(state.log_description)"])
        self.status = .playerConnectionStateDidChanged(state: state)
    }

    // 播放器播放状态变更
    func player(_ player: GWBasePlayer, onStateChange state: GWPlayerState) {
        guard let player = player as? IVPlayer else { return }
        logInfo(["[ReoqooLiveView] player: \(player) device: \(player.deviceId) onStateChange: \(state.log_description)"])
        self.status = .playerStateDidChanged(state: state)
        self.playStateChangedPublisher.send(state)
    }

    // 画面数量变化
    func player(_ player: GWBasePlayer, onIncreaseVideoView videoView: GWPlayerView) -> Bool {
        guard let player = player as? IVPlayer else { return true }
        let numOfViews = player.videoViews.count
        logInfo(["[ReoqooLiveView] player: \(player) device: \(player.deviceId) onIncreaseVideoView: \(numOfViews)"])
        self.status = .numberOfPlayerViewsDidChanged(views: player.videoViews)
        self.numberOfPlayerViewsChangedPublisher.send(())
        return true
    }
    
    // 画面数量变化
    func player(_ player: GWBasePlayer, onDecreaseVideoView videoView: GWPlayerView) {
        guard let player = player as? IVPlayer else { return }
        let numOfViews = player.videoViews.count
        logInfo(["[ReoqooLiveView] player: \(player) device: \(player.deviceId) onDecreaseVideoView: \(numOfViews)"])
        self.status = .numberOfPlayerViewsDidChanged(views: player.videoViews)
        self.numberOfPlayerViewsChangedPublisher.send(())
    }
}

extension IVConnectionState {
    var log_description: String {
        switch self {
        case .assign_chn:
            return "通道分配成功"
        case .wake_up_dev:
            return "服务器已收到连接请求，正在唤醒设备"
        case .connecting:
            return "连接中...，设备已收到唤醒通知，开始握手过程"
        case .connected:
            return "已连接，握手过程完成，连接通道已就绪"
        case .disconnecting:
            return "断开中..."
        case .disconnected:
            return "已断开"
        @unknown default:
            return ""
        }
    }
}

extension GWPlayerState {
    var log_description: String {
        switch self {
        case .uninit:
            return "未初始化"
        case .inited:
            return "已初始化"
        case .preparing:
            return "资源准备中"
        case .prepared:
            return "已准备好播放"
        case .loading:
            return "加载中"
        case .playing:
            return "播放中"
        case .paused:
            return "已暂停"
        case .stopping:
            return "停止中"
        case .stopped:
            return "已停止"
        case .completed:
            return "已完成"
        case .error:
            return "出错了, 通过-getError获取详情"
        @unknown default:
            return ""
        }
    }
}
