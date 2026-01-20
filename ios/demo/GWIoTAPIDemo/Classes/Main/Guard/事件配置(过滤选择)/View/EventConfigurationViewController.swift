//
//  EventConfigurationViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 31/8/2023.
//

import Foundation
import RQCore
import RQCoreUI

extension EventConfigurationViewController {
    class CollectionViewCell: UICollectionViewCell {

        var item: CollectionViewCellItem? {
            didSet {
                guard let item = self.item else { return }
                self.label.text = item.type.description
                self.iconImageView.image = item.type.icon
                self.stackView.spacing = item.type.icon == nil ? 0 : 6
                if item.type.description.sizeWithFont(.systemFont(ofSize: 14)).width > UIScreen.main.bounds.width - 48 {
                    self.labelWidthConstraint?.isActive = true
                }else{
                    self.labelWidthConstraint?.isActive = false
                }
                self.contentView.backgroundColor = item.isSelected ? R.color.brand()?.withAlphaComponent(0.2) : R.color.background_000000_5()
            }
        }

        private lazy var stackView: UIStackView = .init().then {
            $0.axis = .horizontal
            $0.alignment = .center
            $0.spacing = 6
            $0.distribution = .fillProportionally
        }

        private lazy var iconImageView: UIImageView = .init().then {
            $0.contentMode = .center
        }

        private lazy var label: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_90()
            $0.textAlignment = .center
        }

        private var labelWidthConstraint: Constraint?

        override init(frame: CGRect) {
            super.init(frame: frame)
            
            self.backgroundColor = .clear
            self.contentView.layer.cornerRadius = 18
            self.contentView.layer.masksToBounds = true
            self.contentView.backgroundColor = R.color.background_000000_5()

            self.contentView.addSubview(self.stackView)
            self.stackView.snp.makeConstraints { make in
                make.edges.equalTo(UIEdgeInsets.init(top: 0, left: 10, bottom: 0, right: 10))
                make.height.equalTo(36)
                make.width.greaterThanOrEqualTo(72)
            }
            
            self.stackView.addArrangedSubview(self.iconImageView)
            self.stackView.addArrangedSubview(self.label)
            let labelMaxWidth = UIScreen.main.bounds.width - 48
            self.label.snp.makeConstraints { make in
                self.labelWidthConstraint = make.width.equalTo(labelMaxWidth).constraint
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }

    class CollectionViewHeader: UICollectionReusableView {
        
        let expandBtnClickedObservable: Combine.PassthroughSubject<Bool, Never> = .init()
        var expandBtnClickedCancellables: Set<AnyCancellable> = []

        var section: Section? {
            didSet {
                self.titleLabel.text = section?.filterCase.title
                self.expandBtn.isHidden = !(section?.isExpandable ?? true)
                if case let .device(isExpanded) = section?.filterCase {
                    self.expandBtn.isSelected = isExpanded
                }
            }
        }

        private(set) lazy var titleLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_90()
        }

        private(set) lazy var expandBtn: IVButton = .init(.right, space: 0, alignment: .center, padding: 0).then {
            $0.titleLabel?.font = .systemFont(ofSize: 12)
            $0.setTitleColor(R.color.text_000000_60(), for: .normal)
            $0.setTitle(String.localization.localized("AA0503", note: "展开"), for: .normal)
            $0.setTitle(String.localization.localized("AA0504", note: "收起"), for: .selected)
            $0.setImage(R.image.commonArrowBottomStyle1(), for: .normal)
            $0.setImage(R.image.commonArrowTopStyle1(), for: .selected)
        }

        private var anyCancellables: Set<AnyCancellable> = []

        override init(frame: CGRect) {
            super.init(frame: frame)

            self.addSubview(self.titleLabel)
            self.titleLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(16)
                make.centerY.equalToSuperview()
            }

            self.addSubview(self.expandBtn)
            self.expandBtn.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-16)
                make.centerY.equalToSuperview()
            }

            self.expandBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
                self?.expandBtn.isSelected.toggle()
                guard let isExpanded = self?.expandBtn.isSelected else { return }
                self?.expandBtnClickedObservable.send(isExpanded)
            }).store(in: &self.anyCancellables)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}

class EventConfigurationViewController: PageSheetStyleViewController {

    typealias ConfigurationResult = (eventsFilter: DITCloudEventType?, devicesFilter: [DeviceEntity])
    let didFinishConfigObservable: Combine.PassthroughSubject<ConfigurationResult, Never> = .init()
    var externalCancenllables: Set<AnyCancellable> = []

    // 当前选中的过滤器记录
    private var eventsFilter: DITCloudEventType?
    private var devicesFilter: [DeviceEntity] = []

    /// 构造器
    /// - Parameters:
    ///   - eventType: 事件类型过滤, 如为空, 表示不筛选(全部)
    ///   - device: 设备筛选, 如为空, 表示不筛选(全部)
    init(eventType: DITCloudEventType?, devices: [DeviceEntity]) {
        self.eventsFilter = eventType
        self.devicesFilter = devices
        super.init(nibName: nil, bundle: nil)
    }

    private override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) { fatalError("init(coder:) has not been implemented") }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    // MARK: collectionView 数据源
    /// 决定 header 显示, 是否折叠, 是否可折叠
    lazy var sections: [EventConfigurationViewController.Section] = [
        .init(filterCase: .event, isExpandable: false),
        .init(filterCase: .device(isExpanded: false), isExpandable: true)
    ]

    /// 决定 cell 显示
    /// 可选的事件类型
    var eventFilterSelections: [CollectionViewCellItem] = DITCloudEventType.validityTypes
        .filter { $0 != .face }
        .reduce(into: [.init(type: .all)]) { partialResult, type in
            partialResult.append(.init(type: .event(type)))
        }
    /// 可选的设备
    var deviceFilterSelections: [CollectionViewCellItem] = []

    // MARK: UI
    lazy var topContainer: UIView = .init().then {
        $0.backgroundColor = .clear
    }

    lazy var cancelBtn: UIButton = .init(type: .system).then {
        $0.tintColor = R.color.text_link_4A68A6()!
        $0.setTitle(String.localization.localized("AA0059", note: "取消"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        $0.contentEdgeInsets = .init(top: 0, left: 16, bottom: 0, right: 16)
    }

    lazy var okBtn: UIButton = .init(type: .system).then {
        $0.tintColor = R.color.text_link_4A68A6()!
        $0.setTitle(String.localization.localized("AA0058", note: "确定"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        $0.contentEdgeInsets = .init(top: 0, left: 16, bottom: 0, right: 16)
    }

    lazy var collectionViewFlowLayout: AlignedCollectionViewFlowLayout = .init(horizontalAlignment: .left, verticalAlignment: .center).then {
        $0.scrollDirection = .vertical
    }
    lazy var collectionView: UICollectionView = .init(frame: .zero, collectionViewLayout: self.collectionViewFlowLayout).then {
        $0.dataSource = self
        $0.delegate = self
        $0.alwaysBounceVertical = true
        $0.allowsSelection = true
        $0.allowsMultipleSelection = true
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.contentInset = .init(top: 0, left: 0, bottom: 16, right: 0)
        $0.register(CollectionViewHeader.self, forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader, withReuseIdentifier: String.init(describing: CollectionViewHeader.self))
        $0.register(CollectionViewCell.self, forCellWithReuseIdentifier: String.init(describing: CollectionViewCell.self))
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func layoutContentView() {
        super.layoutContentView()
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.collectionViewFlowLayout.estimatedItemSize = .init(width: 88, height: 36)
        self.collectionViewFlowLayout.headerReferenceSize = .init(width: self.view.bounds.width, height: 40)
        self.collectionViewFlowLayout.minimumLineSpacing = 12
        self.collectionViewFlowLayout.minimumInteritemSpacing = 10
        self.collectionViewFlowLayout.sectionInset = .init(top: 0, left: 16, bottom: 12, right: 16)
        
        self.contentView.addSubview(self.topContainer)
        self.topContainer.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
            make.height.equalTo(64)
        }

        self.topContainer.addSubview(self.okBtn)
        self.okBtn.snp.makeConstraints { make in
            make.trailing.equalToSuperview()
            make.top.bottom.equalToSuperview()
        }

        self.topContainer.addSubview(self.cancelBtn)
        self.cancelBtn.snp.makeConstraints { make in
            make.leading.equalToSuperview()
            make.top.bottom.equalToSuperview()
        }

        self.contentView.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.top.equalTo(self.topContainer.snp.bottom)
            make.leading.trailing.equalToSuperview()
            make.bottom.equalToSuperview()
        }

        self.cancelBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.dismiss(animated: true)
        }).store(in: &self.anyCancellables)

        self.okBtn.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                guard let self = self else { return }
                let result: ConfigurationResult = self.configurationResultFromCollectionViewSelection()
                self.didFinishConfigObservable.send(result)
                self.didFinishConfigObservable.send(completion: .finished)
                self.dismiss(animated: true)
            }).store(in: &self.anyCancellables)

        // 获取设备回放查看权限, 只显示具备权限的设备
        self.getPlaybackPermissionDevicesObservable().flatMap { deviceIds in
            Combine.Publishers.CombineLatest(DeviceManager.shared.generateDevicesPublisher(keyPaths: [\.deviceId]), Combine.Just(deviceIds))
        }.sink(receiveCompletion: { completion in

        }, receiveValue: { [weak self] elem in
            // 目标设备们的ID
            let deviceIds = elem.1
            // 设备
            let devs = elem.0
            self?.deviceFilterSelections = [.init(type: .all)]
            let targets = devs?.filter({ deviceIds.contains($0.deviceId) })
            targets?.forEach({
                self?.deviceFilterSelections.append(.init(type: .device($0)))
            })

            // 判断设备数量决定section是否可折叠
            self?.sections.last?.isExpandable = (self?.deviceFilterSelections.count ?? 0) > 3

            // 如果当前设备过滤条件非空, 默认不折叠设备section
            if !(self?.devicesFilter.isEmpty ?? false) {
                self?.sections.last?.filterCase = .device(isExpanded: true)
            }

            // 配置CollectionView的选中情况
            self?.setupCollecitonViewSelection()
            self?.collectionView.reloadData()
        }).store(in: &self.anyCancellables)
    }

}

extension EventConfigurationViewController: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        self.sections.count
    }

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        let section = self.sections[section]
        if case .event = section.filterCase {
            return self.eventFilterSelections.count
        }
        if case let .device(isExpanded) = section.filterCase {
            // 不可展开
            if !section.isExpandable {
                return self.deviceFilterSelections.count
            }
            // 可展开, 但未展开
            if !isExpanded {
                return self.deviceFilterSelections.count >= 3 ? 3 : self.deviceFilterSelections.count
            }
            // 展开
            if isExpanded {
                return self.deviceFilterSelections.count
            }
        }
        fatalError("没有匹配的filterCase")
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: CollectionViewCell.self), for: indexPath) as! CollectionViewCell
        let section = self.sections[indexPath.section]
        if case .event = section.filterCase {
            cell.item = self.eventFilterSelections[indexPath.item]
        }
        if case .device = section.filterCase {
            cell.item = self.deviceFilterSelections[indexPath.item]
        }
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {

        // 允许多选
        // 如果选中第0个 indexPath.item(选中了"全部"), 取消该 section 中其他 item 的选择
        // 如果选中的不是第0个, 取消该 section 中的第一个 item 的选中

        // 事件
        if indexPath.section == 0 {
            if indexPath.item == 0 {
                // "全部" 不允许被手动主动取消选择
                self.eventFilterSelections.forEach({ $0.isSelected = false })
                self.eventFilterSelections[safe_: 0]?.isSelected = true
            }else{
                self.eventFilterSelections[safe_: indexPath.item]?.isSelected.toggle()
                let isTargetBeenSelected = self.eventFilterSelections[safe_: indexPath.item]?.isSelected ?? false
                // 检查目前数组中的选中状态, 如果没有任何一个项目被选中, 则选中第一个
                if !isTargetBeenSelected && self.eventFilterSelections.filter({ $0.isSelected }).isEmpty {
                    self.eventFilterSelections[safe_: 0]?.isSelected = !isTargetBeenSelected
                }else{
                    self.eventFilterSelections[safe_: 0]?.isSelected = false
                }
            }
        }

        // 设备
        if indexPath.section == 1 {
            if indexPath.item == 0 {
                // "全部" 不允许被手动主动取消选择
                self.deviceFilterSelections.forEach({ $0.isSelected = false })
                self.deviceFilterSelections[safe_: 0]?.isSelected = true
            }else{
                self.deviceFilterSelections[safe_: indexPath.item]?.isSelected.toggle()
                let isTargetBeenSelected = self.deviceFilterSelections[safe_: indexPath.item]?.isSelected ?? false
                if !isTargetBeenSelected && self.deviceFilterSelections.filter({ $0.isSelected }).isEmpty {
                    self.deviceFilterSelections[safe_: 0]?.isSelected = !isTargetBeenSelected
                }else{
                    self.deviceFilterSelections[safe_: 0]?.isSelected = false
                }
            }
        }

        collectionView.reloadData()
    }

    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        let header = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: String.init(describing: CollectionViewHeader.self), for: indexPath) as! CollectionViewHeader
        header.section = self.sections[indexPath.section]
        return header
    }

    func collectionView(_ collectionView: UICollectionView, willDisplaySupplementaryView view: UICollectionReusableView, forElementKind elementKind: String, at indexPath: IndexPath) {
        guard let view = view as? CollectionViewHeader else { return }
        view.expandBtnClickedCancellables = .init()
        // 点击了展开按钮
        view.expandBtnClickedObservable
            .sink(receiveValue: { [weak self] isExpanded in
                guard let section = self?.sections.last else { return }
                section.filterCase = .device(isExpanded: isExpanded)
                self?.collectionView.reloadData()
            }).store(in: &view.expandBtnClickedCancellables)
    }

    func collectionView(_ collectionView: UICollectionView, didEndDisplayingSupplementaryView view: UICollectionReusableView, forElementOfKind elementKind: String, at indexPath: IndexPath) {
        guard let view = view as? CollectionViewHeader else { return }
        view.expandBtnClickedCancellables = []
    }
}

// MARK: Helper
extension EventConfigurationViewController {
    /// 根据 self.timeFilter / self.eventFilter / self.deviceFilter 这几个值 设置 collectionView 的选中项目
    func setupCollecitonViewSelection() {
        // 设置默认选择项

        // 事件
        // 计算 .event 类型在 self.sections 中的索引
        if let eventsFilter = self.eventsFilter {
            eventsFilter.asArray.forEach { event in
                self.eventFilterSelections.first(where: { $0.type == .event(event) })?.isSelected = true
            }
        }else{
            self.eventFilterSelections.first?.isSelected = true
        }

        // 设备
        // 计算 .device 类型在 self.sections 中的索引
        if !self.devicesFilter.isEmpty {
            self.devicesFilter.forEach { dev in
                self.deviceFilterSelections.first(where: { $0.type == .device(dev) })?.isSelected = true
            }
        }else{
            self.deviceFilterSelections.first?.isSelected = true
        }
    }

    /// 根据 Collectionview 的选中情况, 输出 ConfigurationResult
    func configurationResultFromCollectionViewSelection() -> ConfigurationResult {
        // 事件
        var eventFilters: DITCloudEventType? = nil
        let selectedEvents = self.eventFilterSelections.compactMap {
            if $0.isSelected {
                return $0
            }
            return nil
        }
        if selectedEvents.contains(where: { $0.type == .all }) {
            // 包含了 "全部"
            eventFilters = nil
        }else{
            // 不包含 "全部"
            let eventFiltersRawValue = selectedEvents.reduce(into: UInt(0)) { partialResult, item in
                if case let .event(ditCloudEventType) = item.type {
                    partialResult |= ditCloudEventType.rawValue
                }
            }
            eventFilters = DITCloudEventType.init(rawValue: eventFiltersRawValue)
        }

        // 设备
        var deviceFilters: [DeviceEntity] = []
        let selectedDevices = self.deviceFilterSelections.compactMap {
            if $0.isSelected {
                return $0
            }
            return nil
        }
        if selectedDevices.contains(where: { $0.type == .all }) {
            // 包含了 "全部"
            deviceFilters = []
        }else{
            // 不包含 "全部"
            selectedDevices.forEach {
                guard case let .device(dev) = $0.type else { return }
                deviceFilters.append(dev)
            }
        }

        return (eventFilters, deviceFilters)
    }

    // 获取具备查看回放权限的设备 的发布者
    func getPlaybackPermissionDevicesObservable() -> AnyPublisher<[String], Swift.Error> {
        let devices = DeviceManager.shared.devices
        return DeviceEntity.getDevicesPlaybackPermissionPublisher(devices).map {
            $0.compactMap({ $1 ? $0 : nil })
        }.eraseToAnyPublisher()
    }
}
