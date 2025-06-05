//
//  SystemMessageViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/2/2024.
//

import Foundation
import RQWebServices

class SystemMessageViewController: BaseTableViewController, MessageCenterViewControllerChildren {
    
    let vm: ViewModel = .init()

    private var anyCancellables: Set<AnyCancellable> = []

    // 提示没开启系统推送权限的
    lazy var header: Header = .init()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.emptyDataSetSource = self
        self.tableView.emptyDataSetDelegate = self
        self.tableView.rowHeight = 86
        self.tableView.sectionHeaderHeight = 0.1
        self.tableView.separatorStyle = .none
        self.tableView.delegate = self
        self.tableView.dataSource = self
        self.tableView.emptyDataSetSource = self
        self.tableView.emptyDataSetDelegate = self
        self.tableView.showsVerticalScrollIndicator = false
        self.tableView.separatorColor = R.color.lineSeparator()
        self.tableView.backgroundColor = R.color.background_FFFFFF_white()
        self.tableView.tableHeaderView = .init(frame: .init(x: 0, y: 0, width: 0, height: 16))
        self.tableView.register(FirstLevelMessageTableViewCell.self, forCellReuseIdentifier: String.init(describing: FirstLevelMessageTableViewCell.self))
        
        self.vm.processEvent(.viewDidLoad)

        self.vm.$firstLevelMessageItems
            .sink(receiveValue: { [weak self] _ in
                self?.tableView.reloadData()
            }).store(in: &self.anyCancellables)

        self.vm.$status
            .sink(receiveValue: { [weak self] status in
                switch status {
                case .messageItemUnreadStatusDidRefresh:
                    self?.tableView.reloadData()
                case .didSweepAllUnread:
                    self?.tableView.reloadData()
                default:
                    break
                }
            }).store(in: &self.anyCancellables)

        // 如果用户没有开启通知权限
        UNUserNotificationCenter.getNotificationSettingsPublisher()
            .sink(receiveValue: { [weak self] settings in
                if settings.authorizationStatus != .authorized {
                    self?.tableView.tableHeaderView = self?.header
                    self?.header.snp.makeConstraints { make in
                        make.width.equalToSuperview()
                    }
                }
            }).store(in: &self.anyCancellables)

        // 点击了 TableView header
        self.header.tap.tapPublisher.sink(receiveValue: { _ in
            UIApplication.shared.open(URL.appSetting)
        }).store(in: &self.anyCancellables)
    }

    // MARK: UITableViewDataSource, UITableViewDelegate
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { self.vm.firstLevelMessageItems.count }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: FirstLevelMessageTableViewCell.self), for: indexPath) as! FirstLevelMessageTableViewCell
        cell.item = self.vm.firstLevelMessageItems[indexPath.row]
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let item = self.vm.firstLevelMessageItems[indexPath.row]
        // 请求接口, 更新已读状态
        self.vm.processEvent(.refreshUnread(item: item))
        // app 升级消息
        if item.tag == .appUpdate {
            UIApplication.shared.open(URL.init(string: item.redirectUrl)!)
            return
        }
        // 子级菜单 || 设备升级消息
        if item.isHeap || item.tag == .firmwareUpdate {
            let vc = MessageCenterSubLevelViewController.init(item: item)
            self.navigationController?.pushViewController(vc, animated: true)
            return
        }
        // 跳转 h5
        if !item.redirectUrl.isEmpty {
            let vc = WebViewController(url: URL.init(string: item.redirectUrl))
            self.navigationController?.pushViewController(vc, animated: true)
            return
        }
    }
}

extension SystemMessageViewController: EmptyDataSetSource, EmptyDataSetDelegate {
    func customView(forEmptyDataSet scrollView: UIScrollView) -> UIView? {
        self.emptyDataPlaceholder
    }
}
