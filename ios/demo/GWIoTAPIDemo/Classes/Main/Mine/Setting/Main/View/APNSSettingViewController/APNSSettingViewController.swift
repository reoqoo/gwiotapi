//
//  APNSSettingViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 7/6/2024.
//

import Foundation

class APNSSettingViewController: BaseTableViewController {

    let vm: ViewModel = .init()

    init() {
        super.init(style: .insetGrouped)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = String.localization.localized("AA0613", note: "消息推送设置")
        self.tableView.rowHeight = UITableView.automaticDimension
        self.tableView.separatorColor = R.color.lineSeparator()
        self.tableView.separatorInset = .init(top: 0, left: 12, bottom: 0, right: 12)
        self.tableView.register(SettingViewController.AuthorizationStatusTableViewCell.self, forCellReuseIdentifier: String.init(describing: SettingViewController.AuthorizationStatusTableViewCell.self))
        self.tableView.tableHeaderView = UIView(frame: .init(x: 0, y: 0, width: 0.1, height: 0.1))
        self.tableView.sectionHeaderHeight = 16
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.vm.tableViewDataSources.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: SettingViewController.AuthorizationStatusTableViewCell.self), for: indexPath) as! SettingViewController.AuthorizationStatusTableViewCell
        cell.item = self.vm.tableViewDataSources[indexPath.row]
        return cell
    }

    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat { 16 }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: false)
        UIApplication.shared.open(URL.appSetting)
    }
}
