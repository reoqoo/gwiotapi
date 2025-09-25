//
//  GuardianViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/8/2023.
//

import Foundation
import MJRefresh
import GWIoTApi

import RQCore
import RQCoreUI

/// tabBar1 - 看家页，包括多路同屏、看家事件记录列表
class GuardianViewController: BaseViewController {
    
    // View 状态改变通知
    static let viewStatusDidChangedNotificationName: Notification.Name = .init("GuardianViewController.viewStatusDidChangedNotificationName")
    // MJHeader 下拉刷新通知
    static let headerRefreshingNotificationName: Notification.Name = .init("GuardianViewController.headerRefreshingNotificationName")
    static let viewStatusUserInfoKey: String = "viewStatus"

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        self.tabBarItem = .init(title: String.localization.localized("AA0042", note: "看家"), image: R.image.tab_guard_unselected()?.withRenderingMode(.alwaysOriginal), selectedImage: R.image.tab_guard_selected()?.withRenderingMode(.alwaysOriginal))
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    let vm: ViewModel = .init()

    // 记录 "正在使用流量" 提示是否已显示过
    var isCelluarTipsDidShow: Bool = false

    // 用于记录上一次设备数量, 避免太频繁刷新 CollectionView.
    // 仅在数量从 0 -> N, N -> 0 这样变化时才刷新 CollectionView
    // 临时解决 https://www.tapd.cn/tapd_fe/42436043/bug/detail/1142436043001051549 这个问题, 是由于频繁刷新 CollectionView, Cell短时间内重复创建多个导致
    var previousNumberOfDevs: Int?

    private var anyCancellables: Set<AnyCancellable> = []

    lazy var titleLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.textColor = R.color.text_000000_90()!
        $0.text = String.localization.localized("AA0192", note: "智能看家")
    }

    lazy var titleContainer: UIView = .init().then {
        $0.backgroundColor = .clear
    }

    /// 当用户没有设备时显示
    lazy var emptyDevicePlaceholder: EmptyDevicesPlaceholder = .init()

    lazy var collectionViewFlowLayout: UICollectionViewFlowLayout = .init().then {
        $0.scrollDirection = .vertical
        // 开启 size 自适应
        $0.estimatedItemSize = .init(width: self.view.bounds.width - 32, height: 400)
    }

    lazy var collectionView: UICollectionView! = .init(frame: .zero, collectionViewLayout: self.collectionViewFlowLayout).then {
        $0.dataSource = self
        $0.delegate = self
        $0.alwaysBounceVertical = true
        $0.emptyDataSetSource = self
        $0.emptyDataSetDelegate = self
        $0.backgroundColor = .clear
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.allowsSelection = false
        $0.contentInset = .init(top: 0, left: 0, bottom: 16, right: 0)
        $0.isPrefetchingEnabled = false
        $0.register(LiveCollectionCell.self, forCellWithReuseIdentifier: String.init(describing: LiveCollectionCell.self))
        $0.register(EventCollectionCell.self, forCellWithReuseIdentifier: String.init(describing: EventCollectionCell.self))
    }

    lazy var mj_header: MJCommonHeader = .init().then {
        $0.refreshingBlock = {
            NotificationCenter.default.post(name: Self.headerRefreshingNotificationName, object: self, userInfo: nil)
        }
    }

    /// 当组件展开时, 被展开的视图会被放置在此 View 上
    lazy var expandedContainer: UIView = .init().then {
        $0.backgroundColor = .clear
        $0.layer.cornerRadius = 12
        $0.layer.masksToBounds = true
        $0.isHidden = true
    }

    /// 新手引导显示时, 指向这个 collectionCell 上的 moreBtn
    weak var beginnerGuidanceTarget_0: UIView?

    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController?.setNavigationBarHidden(true, animated: false)
        
        self.view.addSubview(self.titleContainer)
        self.titleContainer.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(10)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(44)
        }

        self.titleContainer.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(16)
            make.top.bottom.trailing.equalToSuperview()
        }

        self.view.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.top.equalTo(self.titleContainer.snp.bottom).offset(16)
            make.leading.trailing.bottom.equalToSuperview()
        }

        self.view.addSubview(self.expandedContainer)
        self.expandedContainer.snp.makeConstraints { make in
            make.top.equalTo(self.titleContainer.snp.bottom).offset(16)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-16)
        }

        // 添加设备点击
        self.emptyDevicePlaceholder.button.tapPublisher
            .sink(receiveValue: { _ in
                Task {
                    try await QRCodeScanningHandler.shared.openScanningWithTitle(String.localization.localized("AA0049", note: "添加设备"), description: String.localization.localized("AA0062", note: "扫描设备机身二维码添加设备"))
                }
            }).store(in: &self.anyCancellables)

        // 监听设备列表变化
        DeviceManager.shared.$devices
//            .debounce(for: 1, scheduler: DispatchQueue.main)    // 这样会必现崩溃
            .sink(receiveValue: { [weak self] devs in
                let previousNumOfDevs = self?.previousNumberOfDevs ?? -1
                if (previousNumOfDevs == 0 && devs.count > 0) || (devs.count == 0 && previousNumOfDevs > 0) {
                    self?.collectionView.reloadData()
                }
                self?.previousNumberOfDevs = devs.count
                // CollectionView MJHeader 刷新
                self?.collectionView.mj_header = devs.isEmpty ? nil : self?.mj_header
            }).store(in: &self.anyCancellables)

        // 监听 viewStatus, 发送 通知, 以便子视图能监听 viewWillAppear / disappear 系列事件
        self.$viewStatus.sink(receiveValue: { [weak self] status in
            NotificationCenter.default.post(name: Self.viewStatusDidChangedNotificationName, object: self, userInfo: [Self.viewStatusUserInfoKey: status])
        }).store(in: &self.anyCancellables)

        // 监听网络变化, 如遇非 wifi 环境, totas提示一次
        AFNetworkReachabilityManager.publisher.combineLatest(DeviceManager.shared.$devices).sink { [weak self] networkState, devs in
            guard case .reachableViaWWAN = networkState else { return }
            if devs.isEmpty { return }
            if (self?.isCelluarTipsDidShow ?? true) { return }
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0198", note: "正在使用3G/4G流量,可能会产生流量费用"))
            self?.isCelluarTipsDidShow = true
        }.store(in: &self.anyCancellables)

        self.vm.layoutModeDidChangedObservable.delay(for: 0.05, scheduler: DispatchQueue.main).sink(receiveValue: { [weak self] layout in
            // 更新了布局, 刷新一下 LiveCollectionviewCell 的高度
            // 23/12/21 发现在使用 performBatchUpdates 方法后, 检查 Debug Memory Graph 会发现有额外的Cell被创建了, 导致播放画面显示不出来
            //            self?.collectionView.performBatchUpdates(nil, completion: nil)
            // 接上文, 所以为了使布局被刷新后, Cell的高度可以刷新, 最终采取重置布局的方式
            self?.collectionView.collectionViewLayout.invalidateLayout()
        }).store(in: &self.anyCancellables)
    }

}

extension GuardianViewController: UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        // 如果 用户没有任何设备绑定, 显示占位图
        if DeviceManager.shared.devices.count == 0 { return 0 }
        return self.vm.pluginTypes.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let pluginType = self.vm.pluginTypes[indexPath.item]
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: pluginType.collectionCellClass), for: indexPath)
        if let cell = cell as? GuardianViewController.LiveCollectionCell {
            self.beginnerGuidanceTarget_0 = cell.moreBtn
        }
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        // 实时画面组件Cell
        if let cell = cell as? LiveCollectionCell {
            self.vm.layoutModeDidChangedObservable
                .sink(receiveValue: { [weak cell] layoutMode in
                    cell?.layoutMode = layoutMode
                }).store(in: &cell.externalCancellables)

            // 交互事件绑定
            // more按钮 点击
            cell.moreBtnOnClickObservable.sink(receiveValue: { [weak self] in
                self?.liveCellMoreBtnClicked()
            }).store(in: &cell.externalCancellables)

            // 设备选择
            cell.deviceSelectedObservable
                .sink(receiveValue: { [weak self] dev in
                    self?.liveViewOnClickedWithDevice(dev)
                }).store(in: &cell.externalCancellables)
        }

        // 事件组件Cell
        if let cell = cell as? EventCollectionCell, let pluginContentView = cell.pluginContentView as? EventContentView {
            // 过滤条件绑定
            self.vm.$devicesFilter.sink(receiveValue: { [weak cell] filter in
                cell?.vm.devicesFilter = filter
            }).store(in: &cell.externalCancellables)

            self.vm.$eventTypesFilter.sink(receiveValue: { [weak cell] filter in
                cell?.vm.eventTypesFilter = filter
            }).store(in: &cell.externalCancellables)

            // 事件 Cell 交互事件绑定
            pluginContentView.controlEventObservable
                .sink(receiveValue: { [weak self] eventContentViewControlEvent in
                    if case .moreBtnClicked = eventContentViewControlEvent {
                        self?.eventCellMoreBtnClicked()
                    }
                    if case .expandBtnClicked = eventContentViewControlEvent {
                        self?.eventCellExpandBtnClicked()
                    }
                    if case .flodBtnClicked = eventContentViewControlEvent {
                        self?.eventCellFlodBtnClicked()
                    }
                    if case let .eventCellClicked(event: event) = eventContentViewControlEvent {
                        self?.eventCellTableViewCellClicked(event)
                    }
                    if case .eventDidRefreshComplete = eventContentViewControlEvent {
                        self?.collectionView.mj_header?.endRefreshing()
                    }
                }).store(in: &cell.externalCancellables)
        }
    }

    func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        if let cell = cell as? PluginCollectionViewCell {
            cell.didEndDisplay()
        }
    }
}

extension GuardianViewController: UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat { 12 }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat { 12 }
}

extension GuardianViewController: EmptyDataSetSource, EmptyDataSetDelegate {
    func customView(forEmptyDataSet scrollView: UIScrollView) -> UIView? { self.emptyDevicePlaceholder }
    func verticalOffset(forEmptyDataSet scrollView: UIScrollView) -> CGFloat { -44 }
    func emptyDataSetShouldAllowScroll(_ scrollView: UIScrollView) -> Bool { true }
    func emptyDataSetWillAppear(_ scrollView: UIScrollView) {
        self.view.backgroundColor = R.color.background_FFFFFF_white()
    }
    func emptyDataSetWillDisappear(_ scrollView: UIScrollView) {
        self.view.backgroundColor = R.color.background_F2F3F6_thinGray()
    }
}

// MARK: 组件交互事件处理
extension GuardianViewController {
    /// 实时画面: More btn 点击
    func liveCellMoreBtnClicked() {
        let vc = LiveConfigurationViewController(layoutMode: self.vm.layoutMode)
        vc.didFinishConfigurationObservable
            .sink(receiveValue: { [weak self] result in
                // 更新布局
                self?.vm.layoutMode = result.layoutMode
                // 弹提示
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0201", note: "保存成功"))
            }).store(in: &self.anyCancellables)

        self.present(vc, animated: true)
    }

    /// 实时画面: 设备被点击, 进入插件
    func liveViewOnClickedWithDevice(_ device: DeviceEntity?) {
        guard let device = device else { return }
        let gwDev = GWDevice.init(solution: device.solution == .yoosee ? .yoosee : .reoqoo, deviceId: device.deviceId)
        GWIoT.shared.openHome(opts: OpenPluginOption.init(device: gwDev), completionHandler: { _, _ in })
    }

    /// 事件Cell: More Btn 点击
    func eventCellMoreBtnClicked() {
        let vc = EventConfigurationViewController.init(eventType: self.vm.eventTypesFilter, devices: self.vm.devicesFilter)
        self.present(vc, animated: true)
        vc.didFinishConfigObservable
            .sink(receiveValue: { [weak self] result in
                // 提交过滤内容到vm
                self?.vm.eventTypesFilter = result.eventsFilter
                self?.vm.devicesFilter = result.devicesFilter
                // 弹提示🤮
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0201", note: "保存成功"))
            }).store(in: &vc.externalCancenllables)
    }

    /// 事件Cell: 展开按钮 点击
    func eventCellExpandBtnClicked() {
        guard let cell = self.collectionViewCellFromPluginType(.event) as? EventCollectionCell else { return }
        self.expand(cell)
    }

    /// 事件Cell: 折叠按钮点击
    func eventCellFlodBtnClicked() {
        guard let cell = self.collectionViewCellFromPluginType(.event) as? EventCollectionCell else { return }
        self.flod(cell)
    }

    /// 事件Cell: tableView cell 点击
    func eventCellTableViewCellClicked(_ event: GuardianViewController.Event) {

        guard let device = DeviceManager.fetchDevice(String(event.devId)) else { return }

        let alarm = DHAlarmMessage()
        alarm.alarmType = event.alarmType.convert2DHAlarmType()
        alarm.alarmId = event.alarmId
        alarm.startTime = Int(event.startTime)
        alarm.endTime = Int(event.endTime)

        let gwdev = GWDevice.init(solution: device.solution == .yoosee ? .yoosee : .reoqoo, deviceId: device.deviceId)
        // TODO: 点击去看回放
        GWIoT.shared.openPlayback(opts: PlaybackOption.init(device: gwdev, alarmId: event.alarmId, startTime: .init(value: Int64(event.startTime)))) { _, _ in }
    }
}

// MARK: Helper
extension GuardianViewController {

    // 通过 plugin 类型快速获取 对应的 collectionViewCell
    func collectionViewCellFromPluginType(_ type: PluginType) -> PluginCollectionViewCell? {
        guard let idx = self.vm.pluginTypes.firstIndex(of: type) else { return nil }
        guard let cell = self.collectionView.cellForItem(at: .init(item: idx, section: 0)) as? PluginCollectionViewCell else { return nil }
        return cell
    }
}

// MARK: Expand / Flod 小组件
extension GuardianViewController {

    /// 展开 Cell 小组件
    /// 将 Cell 小组件 中 的 pluginContentView 从 原视图(cell) 中移除,
    /// 再添加到 self.expandedContainer 中
    /// - Parameter cell: PluginCollectionViewCell
    func expand(_ cell: PluginCollectionViewCell) {

        let pluginContentView = cell.pluginContentView
        // 计算当前 pluginContentView 于 self.expandedContainer 上的位置
        let pluginContentViewRectOnExpandedContainer = pluginContentView.convert(pluginContentView.bounds, to: self.expandedContainer)
        // 将该 rect 记录起来, 以便折叠时使用
        pluginContentView.frameThatBeforeExpanded = pluginContentViewRectOnExpandedContainer

        // 从 cell 中移除
        pluginContentView.removeFromSuperview()
        // 添加到 self.expandedContainer
        self.expandedContainer.isHidden = false
        self.expandedContainer.addSubview(pluginContentView)
        pluginContentView.frame = pluginContentViewRectOnExpandedContainer

        pluginContentView.snp.remakeConstraints { make in
            make.edges.equalToSuperview()
        }

        UIView.animate(withDuration: 0.3) {
            self.view.layoutIfNeeded()
            self.collectionView.alpha = 0
        }completion: { _ in
            self.collectionView.isHidden = true
            self.collectionView.alpha = 1
        }
    }

    /// 折叠 Cell 小组件
    /// - Parameter cell: PluginCollectionViewCell
    func flod(_ cell: PluginCollectionViewCell) {

        let pluginContentView = cell.pluginContentView
        self.collectionView.isHidden = false

        // 动画
        let frameThatBeforeExpanded: CGRect = pluginContentView.frameThatBeforeExpanded ?? .zero
        pluginContentView.snp.remakeConstraints { make in
            make.top.equalTo(frameThatBeforeExpanded.minY)
            make.height.equalTo(frameThatBeforeExpanded.height)
            make.leading.trailing.equalToSuperview()
        }

        UIView.animate(withDuration: 0.3) {
            self.view.layoutIfNeeded()
        }completion: { _ in
            self.expandedContainer.isHidden = true
            cell.pluginContentView.removeFromSuperview()
            cell.addPluginContentViewIfNeed()
            cell.layoutPluginContentView()
        }
    }
}
