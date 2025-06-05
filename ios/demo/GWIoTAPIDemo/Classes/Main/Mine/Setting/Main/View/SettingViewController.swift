//
//  SettingViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 6/6/2024.
//

import Foundation

class SettingViewController: BaseTableViewController {

    typealias CellDidSelectedHandler = () -> ()
    typealias CellItem = (title: String, handler: CellDidSelectedHandler)
    lazy var items: [CellItem] = [
        (String.localization.localized("AA0613", note: "消息推送设置"), { [weak self] in
            guard let self else { return }
            let vc = APNSSettingViewController.init()
            self.navigationController?.pushViewController(vc, animated: true)
        }),
        (String.localization.localized("AA0614", note: "系统权限设置"), { [weak self] in
            guard let self else { return }
            let vc = SystemAuthorizationViewController.init()
            self.navigationController?.pushViewController(vc, animated: true)
        }),
        (String.localization.localized("AA0221", note: "语言"), { [weak self] in
            guard let self else { return }
            let vc = ChangeLanguageViewController.init()
            self.navigationController?.pushViewController(vc, animated: true)
        })
    ]

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = String.localization.localized("AA0103", note: "设置")
        self.tableView.separatorColor = R.color.lineSeparator()
        self.tableView.separatorInset = .init(top: 0, left: 12, bottom: 0, right: 12)
        self.tableView.rowHeight = 56
        self.tableView.tableHeaderView = UIView(frame: .init(x: 0, y: 0, width: 0.1, height: 0.1))
        self.tableView.sectionHeaderHeight = 16
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { self.items.count }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: UITableViewCell.self))
        if cell == nil {
            cell = UITableViewCell.init(style: .default, reuseIdentifier: String.init(describing: UITableViewCell.self))
            cell!.accessoryType = .disclosureIndicator
        }
        cell!.textLabel?.text = self.items[indexPath.row].title
        return cell!
    }

    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat { 16 }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.items[indexPath.row].handler()
    }
}
