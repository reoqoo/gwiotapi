//
//  WelfareActivityViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/2/2024.
//

import Foundation
import RQCore
import RQWebServices

class WelfareActivityViewController: BaseTableViewController, MessageCenterViewControllerChildren {

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        // 进入了页面, 将 福利活动标记为已读
        MessageCenter.shared.syncWelfareActivityBeenReaded(MessageCenter.shared.welfareActivityMessages)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.emptyDataSetSource = self
        self.tableView.emptyDataSetDelegate = self
        self.tableView.register(WelfareActivityTableViewCell.self, forCellReuseIdentifier: String.init(describing: WelfareActivityTableViewCell.self))
        self.tableView.rowHeight = UITableView.automaticDimension
        self.tableView.separatorStyle = .none
        self.tableView.showsVerticalScrollIndicator = false

        MessageCenter.shared.$welfareActivityMessages.sink(receiveValue: { [weak self] _ in
            self?.tableView.reloadData()
        }).store(in: &self.anyCancellables)
    }

    // MARK: TableViewDataSource, TableViewDelegate
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        MessageCenter.shared.welfareActivityMessages.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: WelfareActivityTableViewCell.self), for: indexPath) as! WelfareActivityTableViewCell
        cell.item = MessageCenter.shared.welfareActivityMessages[indexPath.row]
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = MessageCenter.shared.welfareActivityMessages[indexPath.row]
        guard let url = URL.init(string: item.url) else { return }
        if item.expireTime < Date().timeIntervalSince1970 {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0603", note: "活动已结束~"))
        }else{
            let vc = WebViewController(url: url)
            self.navigationController?.pushViewController(vc, animated: true)
        }
    }
}

extension WelfareActivityViewController: EmptyDataSetSource, EmptyDataSetDelegate {
    func customView(forEmptyDataSet scrollView: UIScrollView) -> UIView? {
        self.emptyDataPlaceholder
    }
}
