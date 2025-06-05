//
//  ShareManagedViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 13/6/2024.
//

import Foundation
import RQDeviceShared

class ShareManagedViewController: BaseViewController {

    let vm: ViewModel = .init()

    lazy var tableView: UITableView = .init(frame: .zero, style: .insetGrouped).then {
        $0.delegate = self
        $0.dataSource = self
        $0.emptyDataSetSource = self
        $0.emptyDataSetDelegate = self
        $0.separatorInset = .init(top: 0, left: 60, bottom: 0, right: 12)
        $0.separatorColor = R.color.lineSeparator()
        $0.register(DeviceTableViewCell.self, forCellReuseIdentifier: String.init(describing: DeviceTableViewCell.self))
        $0.register(DeviceTableViewHeader.self, forHeaderFooterViewReuseIdentifier: String.init(describing: DeviceTableViewHeader.self))
        $0.mj_header = MJCommonHeader.init(refreshingTarget: self, refreshingAction: #selector(self.tableViewHeaderOnRefresh(_:)))
    }

    private lazy var emptyView = CommonEmptyView(image: R.image.empty()!, text: String.localization.localized("AA0177", note: "暂无分享的设备"))

    var anyCancellables: Set<AnyCancellable> = []

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    init() {
        super.init(nibName: nil, bundle: nil)
        self.title = String.localization.localized("AA0147", note: "共享管理")

        self.view.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.vm.$tableViewDataSources.sink(receiveValue: { [weak self] collector in
            self?.tableView.reloadData()
        }).store(in: &self.anyCancellables)

        self.vm.$state.sink { [weak self] state in
            switch state {
            case .refreshDeviceListResult:
                self?.tableView.mj_header?.endRefreshing()
            default:
                break
            }
        }.store(in: &self.anyCancellables)

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            self.tableView.mj_header?.beginRefreshing()
        }
    }

    @objc func tableViewHeaderOnRefresh(_ sender: MJCommonHeader) {
        self.vm.event = .refreshDeviceList
    }
}

extension ShareManagedViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int { self.vm.tableViewDataSources.count }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { self.vm.tableViewDataSources[section].devices.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: DeviceTableViewCell.self), for: indexPath) as! DeviceTableViewCell
        cell.device = self.vm.tableViewDataSources[indexPath.section].devices[indexPath.row]
        return cell
    }

    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: String.init(describing: DeviceTableViewHeader.self)) as! DeviceTableViewHeader
        header.text = self.vm.tableViewDataSources[section].title
        return header
    }

    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat { 16 }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let section = self.vm.tableViewDataSources[indexPath.section]
        let device = section.devices[indexPath.row]
        if device.isInvalidated { return }
        if section.title == String.localization.localized("AA0178", note: "分享的设备") {
            let vc = ShareToManagedViewController.init(deviceId: device.deviceId)
            self.navigationController?.pushViewController(vc, animated: true)
        }
        if section.title == String.localization.localized("AA0179", note: "来自分享的设备") {
            let vc = ShareFromManagedViewController.init(deviceId: device.deviceId)
            self.navigationController?.pushViewController(vc, animated: true)
        }
    }
}

extension ShareManagedViewController: EmptyDataSetSource, EmptyDataSetDelegate {
    func customView(forEmptyDataSet scrollView: UIScrollView) -> UIView? { self.emptyView }
}
