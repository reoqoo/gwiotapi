//
//  MessageCenterSubLevelViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/9/2023.
//

import UIKit
import MJRefresh
import RQCore
import RQDeviceShared
import RQWebServices
import RQFirmwareUpgrade

class MessageCenterSubLevelViewController: BaseViewController {

    let vm: ViewModel
    init(item: MessageCenter.FirstLevelMessageItem) {
        self.vm = .init(firstLevelMessageItem: item)
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    lazy var tableView: UITableView = .init(frame: .zero, style: .insetGrouped).then {
        $0.rowHeight = UITableView.automaticDimension
        $0.estimatedRowHeight = 100
        $0.sectionHeaderHeight = 0.1
        $0.sectionFooterHeight = 12
        $0.delegate = self
        $0.dataSource = self
        $0.showsVerticalScrollIndicator = false
        $0.separatorColor = R.color.background_000000_5()
        $0.backgroundColor = R.color.background_F2F3F6_thinGray()
        $0.separatorInset = .init(top: 0, left: 74, bottom: 0, right: 16)
        $0.tableHeaderView = UIView.init(frame: .init(x: 0, y: 0, width: 0, height: 16))
        $0.register(SubLevelMessageTableViewCell.self, forCellReuseIdentifier: String.init(describing: SubLevelMessageTableViewCell.self))
    }
    
    /// 设备分享弹窗
    lazy private var sharePopupHelper: DeviceSharePopupHelper = .init()

    private var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = self.vm.firstLevelMessageItem.title

        self.view.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.vm.processEvent(.viewDidLoad)
        
        self.vm.$secondLevenMessageItems.sink(receiveValue: { [weak self] _ in
            self?.tableView.reloadData()
            self?.tableView.mj_header?.endRefreshing()
        }).store(in: &self.anyCancellables)

        self.tableView.mj_header = MJCommonHeader.init()
        self.tableView.mj_header?.refreshingBlock = { [weak self] in
            self?.vm.processEvent(.refresh)
        }

        self.tableView.mj_footer = MJCommonFooter.init()
        self.tableView.mj_footer?.refreshingBlock = { [weak self] in
            self?.vm.processEvent(.loadMore)
        }

        self.vm.$status.sink(receiveValue: { [weak self] status in
            switch status {
            case let .refreshHasMoreDataStatus(noMoreData):
                if noMoreData {
                    self?.tableView.mj_footer?.endRefreshingWithNoMoreData()
                }else{
                    self?.tableView.mj_footer?.endRefreshing()
                }
            default:
                break
            }
        }).store(in: &self.anyCancellables)
    }

}

extension MessageCenterSubLevelViewController: UITableViewDataSource, UITableViewDelegate {

    func numberOfSections(in tableView: UITableView) -> Int { self.vm.secondLevenMessageItems.count }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { 1 }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: SubLevelMessageTableViewCell.self), for: indexPath) as! SubLevelMessageTableViewCell
        cell.item = self.vm.secondLevenMessageItems[indexPath.section]
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = self.vm.secondLevenMessageItems[indexPath.section]
        // 跳转到设备升级
        if item.tag == .firmwareUpdate {
            let vc = DeviceFirmwareUpgradeViewController.init()
            self.navigationController?.pushViewController(vc, animated: true)
        }
        // 设备分享
        // 跳转到目前已分享的设备列表
        if item.tag == .shareGuest && (item.type == 17 || item.type == 16) { //16：主人邀请访客（需主动确认），主人收到的访客确认消息   17：主人删除访客通知，访客删除设备通知
            guard let device = DeviceManager.fetchDevice(String(item.deviceId)) else {
                let vc = ReoqooAlertViewController(alertContent: .string(String.localization.localized("AA0563", note: "您已解绑该设备")), actions: [.init(title: String.localization.localized("AA0131", note: "知道了"), style: .custom, color: R.color.text_link_4A68A6())])
                vc.alertView.titleLabel.textAlignment = .center
                self.present(vc, animated: true)
                return;
            }

            let vc = ShareToManagedViewController(deviceId: device.deviceId)
            self.navigationController?.pushViewController(vc, animated: true)
            return
        }
        // 弹出是否接受设备弹窗
        if item.tag == .shareGuest && item.type == 14 { //14：主人邀请访客（需主动确认），访客接收的邀请消息
            if item.time + 86400 < Date().timeIntervalSince1970 { // 邀请已超过24小时
                self.presentShareOutdateAlert()
                return
            }
            
            // 邀请未超过24小时
            //"redirectUrl" = "AppNativeUrl?Type=2&InviteCode=xxx&DeviceID=xxx&Permission=117&SharerName=xxx"
            guard let url = URL(string: item.redirectUrl),
                  let deviceID   = url.getParam(for: "DeviceID"),   //设备id
                  let inviteCode = url.getParam(for: "InviteCode"), //邀请码
                  //let permission = url.getParam(for: "Permission"), //权限
                  let sharerName = url.getParam(for: "SharerName")  //分享者
            else {
                logDebug("show share popup view fail, \(item.redirectUrl)")
                self.presentShareOutdateAlert()
                return
            }
            
            // 传递分享model
            var inviteModel = MessageCenter.DeviceShareInviteModel()
            inviteModel.deviceId = deviceID
            inviteModel.showWay = 1
            inviteModel.shareToken = inviteCode
            inviteModel.inviteAccount = sharerName
            
            self.sharePopupHelper.checkShare(inviteModel: inviteModel)
            return
        }
        // 带网页, 跳转到网页
        if !item.redirectUrl.isEmpty {
            let webvc = WebViewController(url: URL.init(string: item.redirectUrl))
            self.navigationController?.pushViewController(webvc, animated: true)
//            if item.tag == .vss {
//                let webvc = VASServiceWebViewController(url: URL.init(string: item.redirectUrl), device: nil)
//                self.navigationController?.pushViewController(webvc, animated: true)
//            }else{
//            }
        }
    }
}

// MARK: Helper
extension MessageCenterSubLevelViewController {
    func presentShareOutdateAlert() {
        let vc = ReoqooAlertViewController(alertContent: .string(String.localization.localized("AA0168", note: "分享失效，可让主人重新分享")), actions: [.init(title: String.localization.localized("AA0131", note: "知道了"), style: .custom, color: R.color.text_link_4A68A6())])
        self.present(vc, animated: true)
    }
}
