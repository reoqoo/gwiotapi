//
//  LiveViewCollectionViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import UIKit
import RQCore

/// LiveViewContainer.collectionView 的 cell
/// 层级结构
/// LiveViewContainer
///     - collectionView
///         - collectionViewCell
///             - LiveView
class LiveViewCollectionViewCell: UICollectionViewCell {

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    override func prepareForReuse() {
        super.prepareForReuse()
        self.liveView.reset()
    }

    var dataItem: LiveViewContainer.CollectionViewDataItem? {
        didSet {
            
            self.liveView.dataItem = self.dataItem

            self.deviceStatusLabel.isHidden = true

            // 丢弃上一个disposeBag
            self.deviceStatusMonitorCancellables = .init()

            // 空设备占位
            if case .placeholder = self.dataItem {
                self.liveView.isHidden = true
                self.deviceNameLabel.isHidden = true
                self.deviceStatusLabel.isHidden = false
                self.deviceStatusLabel.text = String.localization.localized("AA0464", note: "暂无设备")
            }

            if case let .device(device) = self.dataItem {
                // 设置状态
                self.deviceNameLabel.isHidden = false
                self.deviceStatusLabel.isHidden = false
                device.publisher(\.remarkName, whenErrorOccur: "")
                    .sink(receiveValue: { [weak self] name in
                        self?.deviceNameLabel.text = name
                    }).store(in: &self.deviceStatusMonitorCancellables)

                self.deviceNameLabel.text = device.remarkName
                // 监听设备状态
                device.publisher(\.status, whenErrorOccur: .offline)
                    .receive(on: DispatchQueue.main)
                    .sink(receiveValue: { [weak self, weak device] status in
                        if device?.isInvalidated ?? true { return }
                        self?.deviceStatusLabel.text = device?.liveDescription
                        self?.liveView.isHidden = status != .online
                    }).store(in: &self.deviceStatusMonitorCancellables)
            }
        }
    }

    var layoutMode: LiveViewContainer.LayoutMode? {
        didSet {
            self.liveView.layoutMode = self.layoutMode
        }
    }

    private var deviceStatusMonitorCancellables: Set<AnyCancellable> = []

    /// 实时画面
    private(set) lazy var liveView: LiveView = .init()

    /// 设备名称
    private(set) lazy var deviceNameLabel: UILabel = .init().then {
        $0.textColor = R.color.text_FFFFFF()
        $0.font = .systemFont(ofSize: 12)
        $0.numberOfLines = 1
    }

    // MARK: 空设备/无画面占位
    /// "暂无设备" / "设备已下线"
    private(set) lazy var deviceStatusLabel: UILabel = .init().then {
        $0.textColor = R.color.text_FFFFFF()!.withAlphaComponent(0.6)
        $0.font = .systemFont(ofSize: 14)
        $0.numberOfLines = 0
        $0.textAlignment = .center
    }

    override init(frame: CGRect) {
        super.init(frame: frame)

        self.contentView.backgroundColor = R.color.background_placeholder0_4D4D4D()

        self.contentView.addSubview(self.liveView)
        self.liveView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.contentView.addSubview(self.deviceNameLabel)
        self.deviceNameLabel.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(8)
            make.trailing.equalToSuperview().offset(-8)
            make.top.equalToSuperview().offset(8)
        }

        self.contentView.addSubview(self.deviceStatusLabel)
        self.deviceStatusLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.leading.greaterThanOrEqualToSuperview().offset(12)
            make.trailing.greaterThanOrEqualToSuperview().offset(-12)
        }
    }

    deinit {
        logDebug("====\(self) dealloc====")
    }

}

extension RQCore.DeviceEntity {
    // 状态描述: "智能看家" -> "实时画面"
    fileprivate var liveDescription: String {
        if self.status == .offline {
            return String.localization.localized("AA0200", note: "设备已离线")
        }
        if self.status == .shutdown {
            return String.localization.localized("AA0598", note: "设备已关机")
        }
        if self.isLiveClose {
            return String.localization.localized("AA0199", note: "画面已关闭")
        }
        return ""
    }
}
