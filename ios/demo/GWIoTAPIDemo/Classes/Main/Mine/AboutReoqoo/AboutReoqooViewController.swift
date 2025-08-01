//
//  AboutReoqooViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 13/9/2023.
//

import UIKit
import RTRootNavigationController
import RQCore
import RQWebServices

extension AboutReoqooViewController {
    class TableViewCell: UITableViewCell {
        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: .subtitle, reuseIdentifier: reuseIdentifier)
            self.accessoryView = UIImageView.init(image: R.image.commonArrowRightStyle1())
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        var cellItem: CellItem? {
            didSet {
                self.textLabel?.text = self.cellItem?.title
            }
        }
    }

    class CellItem {
        var title: String
        var handler: () -> Void

        init(title: String, handler: @escaping ()->Void) {
            self.title = title
            self.handler = handler
        }
    }
}

class AboutReoqooViewController: BaseTableViewController {

    override func loadView() {
        let tableView = UITableView.init(frame: .zero, style: .insetGrouped)
        tableView.dataSource = self
        tableView.delegate = self
        self.view = tableView
    }

    lazy var dataSource: [CellItem] = [
        .init(title: String.localization.localized("AA0274", note: "版本更新"), handler: { [weak self] in
            UIApplication.shared.open(URL.AppStoreURL)
        }),
        .init(title: String.localization.localized("AA0044", note: "用户协议"), handler: { [weak self] in
            let vc = WebViewController.init(url: StandardConfiguration.shared.usageAgreementURL)
            self?.rt_navigationController.pushViewController(vc, animated: true)
        }),
        .init(title: String.localization.localized("AA0368", note: "隐私政策"), handler: { [weak self] in
            let vc = WebViewController.init(url: StandardConfiguration.shared.privacyPolicyURL)
            self?.rt_navigationController.pushViewController(vc, animated: true)
        })
    ]

    lazy var logoImageView: UIImageView = .init(image: R.image.appLogo216())


    lazy var appNameLabel: UILabel = .init().then {
        $0.text = String.localization.localized("AA0447", note: "reoqoo")
        $0.font = .systemFont(ofSize: 20, weight: .medium)
        $0.textColor = R.color.text_000000_90()
    }

    lazy var appVersionLabel: UILabel = .init().then {
        $0.text = "Version" + " " + Bundle.majorVersion + "(\(Bundle.buildVersion))"
        $0.font = .systemFont(ofSize: 16)
        $0.textColor = R.color.text_000000_60()
        $0.isUserInteractionEnabled = true
    }

    lazy var tableViewHeader: UIView = .init().then {
        $0.backgroundColor = .clear
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0224", note: "关于reoqoo")

        self.tableView.rowHeight = 56
        self.tableView.backgroundColor = R.color.background_F2F3F6_thinGray()
        self.tableView.separatorColor = R.color.text_000000_10()
        self.tableView.separatorInset = .init(top: 0, left: 12, bottom: 0, right: 12)
        self.tableView.register(TableViewCell.self, forCellReuseIdentifier: String.init(describing: TableViewCell.self))

        self.tableViewHeader.addSubview(self.logoImageView)
        self.logoImageView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(60)
            make.centerX.equalToSuperview()
        }

        self.tableViewHeader.addSubview(self.appNameLabel)
        self.appNameLabel.snp.makeConstraints { make in
            make.top.equalTo(self.logoImageView.snp.bottom).offset(12)
            make.centerX.equalToSuperview()
        }

        self.tableViewHeader.addSubview(self.appVersionLabel)
        self.appVersionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.appNameLabel.snp.bottom).offset(12)
            make.centerX.equalToSuperview()
            make.bottom.equalToSuperview().offset(-30)
        }

        self.tableView.tableHeaderView = self.tableViewHeader
        self.tableViewHeader.snp.makeConstraints { make in
            make.width.equalTo(self.view.snp.width)
        }

        self.view.layoutIfNeeded()
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { self.dataSource.count }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: TableViewCell.self), for: indexPath) as! TableViewCell
        cell.cellItem = self.dataSource[indexPath.row]
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let item = self.dataSource[indexPath.row]
        item.handler()
    }
}
