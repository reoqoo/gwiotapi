//
//  SystemAuthorizationViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 7/6/2024.
//

import Foundation

class SystemAuthorizationViewController: BaseTableViewController {

    let vm: ViewModel = .init()

    init() {
        super.init(style: .insetGrouped)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = String.localization.localized("AA0614", note: "系统权限设置")
        self.tableView.rowHeight = UITableView.automaticDimension
        self.tableView.separatorColor = R.color.lineSeparator()
        self.tableView.separatorInset = .init(top: 0, left: 12, bottom: 0, right: 12)
        self.tableView.register(SettingViewController.AuthorizationStatusTableViewCell.self, forCellReuseIdentifier: String.init(describing: SettingViewController.AuthorizationStatusTableViewCell.self))
        self.tableView.register(SettingViewController.AuthorizationStatusTableViewHeader.self, forHeaderFooterViewReuseIdentifier: String.init(describing: SettingViewController.AuthorizationStatusTableViewHeader.self))
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.vm.tableViewDataSources.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: SettingViewController.AuthorizationStatusTableViewCell.self), for: indexPath) as! SettingViewController.AuthorizationStatusTableViewCell
        cell.item = self.vm.tableViewDataSources[indexPath.row]
        return cell
    }

    override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: String.init(describing: SettingViewController.AuthorizationStatusTableViewHeader.self)) as! SettingViewController.AuthorizationStatusTableViewHeader
        header.label.text = String.localization.localized("AA0633", note: "为了向您提供更好的用户体验，reoqoo 会在特定场景下向您申请以下手机系统权限")
        return header
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: false)
        UIApplication.shared.open(URL.appSetting)
    }
}
