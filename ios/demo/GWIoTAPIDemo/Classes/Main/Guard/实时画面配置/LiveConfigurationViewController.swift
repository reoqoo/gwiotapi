//
//  LiveConfigurationViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 29/8/2023.
//

import UIKit
import RQCore
import RQCoreUI

extension LiveConfigurationViewController {

    // 设备排序Cell
    class DeviceSortCell: UITableViewCell {

        var device: DeviceEntity? {
            didSet {
                self.deviceNameLabel.text = self.device?.remarkName
                self.viewableBtn.isSelected = (self.device?.isLiveClose ?? false)
            }
        }

        lazy var viewableBtnObservable: Combine.PassthroughSubject<Bool, Never> = .init()

        lazy var viewableBtn: UIButton = .init(type: .custom).then {
            $0.tintColor = R.color.text_000000_90()
            $0.setImage(R.image.commonEyeOpen(), for: .normal)
            $0.setImage(R.image.commonEyeClose(), for: .selected)
        }

        lazy var deviceNameLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 16)
            $0.textColor = R.color.text_000000_90()
        }

        lazy var moveIconImageView: UIImageView = .init(image: R.image.commonSort())

        var externalCancellables: Set<AnyCancellable> = []

        private var btnTapCancellables: Set<AnyCancellable> = []

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.isUserInteractionEnabled = false

            self.addSubview(self.viewableBtn)
            self.viewableBtn.snp.makeConstraints { make in
                make.top.leading.bottom.equalToSuperview()
                make.width.equalTo(56)
            }

            self.addSubview(self.deviceNameLabel)
            self.deviceNameLabel.snp.makeConstraints { make in
                make.centerY.equalToSuperview()
                make.leading.equalTo(self.viewableBtn.snp.trailing)
                make.trailing.equalToSuperview().offset(-88)
            }

            let separator = UIView.init()
            separator.backgroundColor = R.color.background_000000_5()
            self.addSubview(separator)
            separator.snp.makeConstraints { make in
                make.height.equalTo(1)
                make.leading.trailing.bottom.equalToSuperview()
            }

            self.viewableBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
                self?.viewableBtn.isSelected.toggle()
                self?.viewableBtnObservable.send(self?.viewableBtn.isSelected ?? false)
            }).store(in: &self.btnTapCancellables)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}

class LiveConfigurationViewController: PageSheetStyleViewController {

    typealias ConfigurationResult = (layoutMode: LiveViewContainer.LayoutMode, devices: [DeviceEntity])
    let didFinishConfigurationObservable: Combine.PassthroughSubject<ConfigurationResult, Never> = .init()
    /// 这样的设计是为了给外部提供一个 DISPOSEBAG, 以便在 self 销毁时自动解除绑定
    var externalCancellables: Set<AnyCancellable> = []

    // tableview 数据源, 从 DeviceManager 中获取
    private var devices: [DeviceEntity] = []

    private var layoutMode: LiveViewContainer.LayoutMode = .fourGird

    init(layoutMode: LiveViewContainer.LayoutMode) {
        super.init(nibName: nil, bundle: nil)
        self.layoutMode = layoutMode
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

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

    // 选择布局模式
    lazy var tableViewHeader: UIView = .init().then {
        $0.backgroundColor = R.color.background_FFFFFF_white()
    }

    lazy var layoutModeTitleLabel: UILabel = .init().then {
        $0.text = String.localization.localized("AA0196", note: "视窗")
        $0.font = .systemFont(ofSize: 14)
        $0.textColor = R.color.text_000000_90()
    }

    lazy var layoutModeStackView: UIStackView = .init().then {
        $0.axis = .horizontal
        $0.distribution = .equalSpacing
        $0.spacing = 14
    }

    lazy var tableView: UITableView = .init(frame: .zero, style: .grouped).then {
        $0.delegate = self
        $0.dataSource = self
        $0.isEditing = true
        $0.estimatedRowHeight = 56
        $0.allowsSelection = false
        $0.showsVerticalScrollIndicator = false
        $0.contentInset = .init(top: 0, left: 0, bottom: 16, right: 0)
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.separatorStyle = .none
        $0.delaysContentTouches = true
        $0.register(DeviceSortCell.self, forCellReuseIdentifier: String.init(describing: DeviceSortCell.self))
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func layoutContentView() {
        super.layoutContentView()
    }

    override func viewDidLoad() {
        super.viewDidLoad()

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

        self.contentView.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.top.equalTo(self.topContainer.snp.bottom)
            make.leading.trailing.bottom.equalToSuperview()
        }

        self.tableViewHeader.addSubview(self.layoutModeTitleLabel)
        self.layoutModeTitleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(12)
            make.leading.equalToSuperview().offset(16)
        }

        self.tableViewHeader.addSubview(self.layoutModeStackView)
        self.layoutModeStackView.snp.makeConstraints { make in
            make.top.equalTo(self.layoutModeTitleLabel.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(16)
            make.bottom.equalToSuperview().offset(-8)
        }

        // 布局模式按钮
        LiveViewContainer.LayoutMode.allCases.forEach {
            let btn = UIButton.init(type: .custom)
            btn.setImage($0.icon, for: .normal)
            btn.setBackgroundColor(R.color.background_000000_5()!, for: .normal)
            btn.setBackgroundColor(R.color.brand()!.withAlphaComponent(0.2), for: .selected)
            btn.titleLabel?.font = .systemFont(ofSize: 14)
            btn.layer.cornerRadius = 18
            btn.layer.masksToBounds = true
            btn.contentEdgeInsets = .init(top: 0, left: 38, bottom: 0, right: 38)
            btn.addTarget(self, action: #selector(self.layoutModeBtnClicked(sender:)), for: .touchUpInside)
            btn.snp.makeConstraints { make in
                make.height.equalTo(36)
            }
            self.layoutModeStackView.addArrangedSubview(btn)
        }

        // 奇技淫巧: tableViewHeader 高度自适应
        self.tableView.tableHeaderView = self.tableViewHeader
        self.tableViewHeader.snp.makeConstraints { make in
            make.width.equalTo(self.tableView)
        }
        self.tableViewHeader.layoutIfNeeded()

        let idxOfSelectMode = LiveViewContainer.LayoutMode.allCases.firstIndex(of: self.layoutMode) ?? 0
        if let btn = self.layoutModeStackView.arrangedSubviews[safe_: idxOfSelectMode] as? UIButton {
            btn.isSelected = true
        }

        self.cancelBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.dismiss(animated: true)
        }).store(in: &self.anyCancellables)

        self.okBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            guard let layoutMode = self?.layoutMode else { return }
            guard let devices = self?.devices else { return }
            // 更新 device.liveSortIdx
            DeviceManager.db_updateDevicesWithContext { _ in
                devices.enumerated().forEach { $1.liveViewSortID = $0 }
            }
            self?.didFinishConfigurationObservable.send((layoutMode, devices))
            self?.didFinishConfigurationObservable.send(completion: .finished)
            self?.dismiss(animated: true)
        }).store(in: &self.anyCancellables)
        
        DeviceManager.shared.generateDevicesPublisher(keyPaths: [\.liveViewSortID]).first()
            .sink(receiveValue: { [weak self] results in
                if let devs = results?.sorted(by: \.liveViewSortID, ascending: true) {
                    self?.devices = Array(devs)
                }else{
                    self?.devices = []
                }
                self?.tableView.reloadData()
            }).store(in: &self.anyCancellables)
    }

    @objc func layoutModeBtnClicked(sender: UIButton) {
        self.layoutModeStackView.arrangedSubviews.forEach {
            guard let btn = $0 as? UIButton else { return }
            btn.isSelected = false
        }
        sender.isSelected = true
        let idxOfBtn = self.layoutModeStackView.arrangedSubviews.firstIndex(of: sender) ?? 0
        self.layoutMode = LiveViewContainer.LayoutMode.allCases[safe_: idxOfBtn] ?? .single
    }
}

extension LiveConfigurationViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.devices.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: DeviceSortCell.self), for: indexPath) as! DeviceSortCell
        cell.device = self.devices[indexPath.row]
        return cell
    }

    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? { String.localization.localized("AA0197", note: "排序") }

    func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        guard let header = view as? UITableViewHeaderFooterView else { return }
        header.textLabel?.font = .systemFont(ofSize: 14)
        header.textLabel?.textColor = R.color.text_000000_90()
        header.textLabel?.text = String.localization.localized("AA0197", note: "排序")
    }

    func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        guard let cell = cell as? DeviceSortCell else { return }
        // 解除按钮点击监听
        cell.externalCancellables = .init()
    }

    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        guard let cell = cell as? DeviceSortCell else { return }
        // 监听 "画面可见" 按钮点击
        cell.viewableBtnObservable.sink(receiveValue: { [weak self] isClose in
            let dev = self?.devices[safe_: indexPath.row]
            DeviceManager.db_updateDevicesWithContext { _ in
                dev?.isLiveClose = isClose
            }
        }).store(in: &self.anyCancellables)
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat { 44 }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat { 56 }

    func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool { true }

    func tableView(_ tableView: UITableView, moveRowAt sourceIndexPath: IndexPath, to destinationIndexPath: IndexPath) {
        // 更新数据源
        let targetDevice = self.devices.remove(at: sourceIndexPath.row)
        self.devices.insert(targetDevice, at: destinationIndexPath.row)
    }

    // 如果没有这一行, 编辑模式下的tableview左边默认会出现一个删除icon
    func tableView(_ tableView: UITableView, editingStyleForRowAt indexPath: IndexPath) -> UITableViewCell.EditingStyle { .none }

}
