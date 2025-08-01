//
//  MineViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/8/2023.
//

import Foundation
import GWIoTApi
import RQWebServices
import RQCore
import RQCoreUI
import RQFirmwareUpgrade
import RQIssueFeedback
import RQMessageCenter

extension MineViewController {
    enum EquityBtnType: Int {
        case placeholder = 0
        case vas = 1
        case flux = 2
    }
}

/// tabBar2 - 我的页，包括设置页、分享管理等
class MineViewController: BaseTableViewController {

    lazy var msgButton = UIButton.init(type: .system).then {
        $0.setImage(R.image.mine_message(), for: .normal)
        $0.contentEdgeInsets = .init(top: 0, left: 6, bottom: 0, right: 6)
    }

    lazy var settingButton = UIButton.init(type: .system).then {
        $0.setImage(R.image.mine_setting(), for: .normal)
        $0.contentEdgeInsets = .init(top: 0, left: 6, bottom: 0, right: 6)
    }

    lazy var tableViewBackground: UIView = .init().then {
        let headerImageView = UIImageView(image: R.image.mine_header_bg())
        headerImageView.contentMode = .scaleAspectFill
        $0.addSubview(headerImageView)
        headerImageView.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
        }
    }

    @IBOutlet weak var userProfileLoadingView: UIActivityIndicatorView!
    @IBOutlet weak var headerImageView: UIImageView!
    @IBOutlet weak var userNameLabel: UILabel!
    @IBOutlet weak var functionsStackView: UIStackView!
    @IBOutlet weak var equitiesStackView: UIStackView!
    @IBOutlet weak var equitiesTitleLabel: UILabel!
    
    var anyCancellables: Set<AnyCancellable> = []

    /// 设备升级, 我的相册, 共享管理 三个按钮
    lazy var functionsButtons: [ServiceButton] = {
        var res: [ServiceButton] = []

        let devicesUpgradeBtn: ServiceButton = .init(title: String.localization.localized("AA0219", note: "设备升级"), image: R.image.mine_dev_upgrade()!)
        devicesUpgradeBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            let vc = DeviceFirmwareUpgradeViewController.init()
            self?.navigationController?.pushViewController(vc, animated: true)
        }).store(in: &self.anyCancellables)

        let myAlbumBtn: ServiceButton = .init(title: String.localization.localized("AA0218", note: "我的相册"), image: R.image.mine_album()!)
        myAlbumBtn.tapPublisher.sink { [weak self] _ in
            guard let self = self else { return }
            GWIoT.shared.openAlbum(device: nil, asset: nil) { _, _ in }
        }.store(in: &self.anyCancellables)

        let shareManagementBtn: ServiceButton = .init(title: String.localization.localized("AA0147", note: "共享管理"), image: R.image.mine_share()!)
        shareManagementBtn.tapPublisher.sink { [weak self] _ in
            Task {
                try await GWIoT.shared.openShareManagerPage()
            }
        }.store(in: &self.anyCancellables)

        res.append(devicesUpgradeBtn)
        res.append(myAlbumBtn)
        res.append(shareManagementBtn)
        return res
    }()

    /// 云服务, 流量 两个按钮
    lazy var equityButtons: [ServiceButton] = {
        var res: [ServiceButton] = []
        let vasServicesBtn: ServiceButton = .init(title: String.localization.localized("AA0247", note: "云服务"), image: R.image.mine_cloud()!)
        vasServicesBtn.tag = EquityBtnType.vas.rawValue
        vasServicesBtn.tapPublisher.sink { [weak self] _ in
            let vc = VASServiceWebViewController.init(url: StandardConfiguration.shared.vasH5URL2, device: nil)
            vc.entrySource = "6"
            self?.navigationController?.pushViewController(vc, animated: true)
        }.store(in: &self.anyCancellables)

        // 流量按钮
        let fluxServicesBtn: ServiceButton = .init(title: String.localization.localized("AA0652", note: "流量"), image: R.image.mine_flux()!)
        fluxServicesBtn.tag = EquityBtnType.flux.rawValue
        fluxServicesBtn.tapPublisher.sink { [weak self] _ in
            let vc = VASServiceWebViewController.init(url: StandardConfiguration.shared.fourGFluxH5URL2, device: nil)
            vc.entrySource = "6"
            self?.navigationController?.pushViewController(vc, animated: true)
        }.store(in: &self.anyCancellables)

        res.append(vasServicesBtn)
        res.append(fluxServicesBtn)
        return res
    }()

    /// 我的订单
    lazy var cellItem_myTransactions: CellItem = .init(image: R.image.mine_transication()!, title: String.localization.localized("AA0653", note: "我的订单"), action: { [weak self] in
        let vc = VASServiceWebViewController.init(url: StandardConfiguration.shared.mineTransactionsH5URL, device: nil)
        vc.entrySource = "6"
        self?.navigationController?.pushViewController(vc, animated: true)
    })

    /// 我的卡券
    lazy var cellItem_myTickets: CellItem = .init(image: R.image.mine_walllet()!, title: String.localization.localized("AA0654", note: "我的卡券"), action: { [weak self] in
        let vc = VASServiceWebViewController.init(url: StandardConfiguration.shared.mineWalletH5URL, device: nil)
        vc.entrySource = "6"
        self?.navigationController?.pushViewController(vc, animated: true)
    })

    /// 帮助与反馈
    lazy var cellItem_feedback: CellItem = .init(image: R.image.mine_feedback()!, title: String.localization.localized("AA0223", note: "帮助与反馈"), action: { [weak self] in
        let vc = IssueFeedbackH5ViewController.init()
        self?.navigationController?.pushViewController(vc, animated: true)
    })

    /// 关于reoqoo
    lazy var cellItem_about: CellItem = .init(image: R.image.mine_about()!, title: String.localization.localized("AA0224", note: "关于reoqoo"), action: { [weak self] in
        let vc = AboutReoqooViewController.init()
        self?.navigationController?.pushViewController(vc, animated: true)
    })

    lazy var cellItems: [CellItem] = []

    static func fromStoryboard() -> MineViewController {
        let sb = UIStoryboard.init(name: "Mine", bundle: nil)
        let vc = sb.instantiateViewController(withIdentifier: String.init(describing: MineViewController.self)) as! MineViewController
        return vc
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.tabBarItem = .init(title: String.localization.localized("AA0043", note: "我的"), image: R.image.tab_mine_unselected()?.withRenderingMode(.alwaysOriginal), selectedImage: R.image.tab_mine_selected()?.withRenderingMode(.alwaysOriginal))
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.rq.setNavigationBarBackground(.clear)

        self.navigationItem.rightBarButtonItems = [UIBarButtonItem.init(customView: self.settingButton), UIBarButtonItem.init(customView: self.msgButton)]

        self.tableView.separatorColor = R.color.lineSeparator()
        self.tableView.separatorInset = .init(top: 0, left: 52, bottom: 0, right: 8)
        self.tableView.backgroundColor = R.color.background_F2F3F6_thinGray()
        self.tableView.sectionHeaderHeight = 0.1
        self.tableView.sectionFooterHeight = 16
        self.tableView.showsVerticalScrollIndicator = false
        self.tableView.showsHorizontalScrollIndicator = false
        self.tableView.backgroundView = self.tableViewBackground

        self.equitiesTitleLabel.text = String.localization.localized("AA0651", note: "权益服务")

        // "设备升级" "相册" "设备共享"
        self.functionsButtons.forEach {
            self.functionsStackView.addArrangedSubview($0)
        }

        // 是否需要显示云服务, 流量入口
        // 如果设备列表为空 或 user 是 SUPERVIP, "我的订单" 和 "我的卡券" 不需要显示
        Publishers.CombineLatest4(DeviceManager.shared.$needShowVasEntrance, DeviceManager.shared.$needShow4GFluxEntrance, DeviceManager.shared.generateDevicesPublisher(), RQCore.Agent.shared.$profileInfo)
            .sink { [weak self] showVas, show4G, devicesFetchResult, currentUser in
                let numOfDev = devicesFetchResult?.count ?? 0
                let isSuperVip = currentUser?.isSuperVip ?? false
                let hide = numOfDev == 0 || isSuperVip
                self?.reloadTableView(showVas: showVas, showFlux: show4G, hideMyTransactions: hide, hideMyTickets: hide)
            }.store(in: &self.anyCancellables)

        // 消息中心按钮点击
        self.msgButton.tapPublisher.sink { _ in
            Task {
                try await GWIoT.shared.openMessageCenterPage()
            }
        }.store(in: &self.anyCancellables)

        // 设置按钮点击
        self.settingButton.tapPublisher.sink { [weak self] _ in
            let vc = SettingViewController.init(style: .insetGrouped)
            self?.navigationController?.pushViewController(vc, animated: true)
        }.store(in: &self.anyCancellables)

        // 用户信息绑定
        AccountCenter.shared.$currentUser.flatMap {
            $0?.$profileInfo.eraseToAnyPublisher() ?? Just<RQCore.ProfileInfo?>(nil).eraseToAnyPublisher()
        }.sink(receiveValue: { [weak self] profileInfo in
            if profileInfo == nil {
                self?.userProfileLoadingView.startAnimating()
            }else{
                self?.userProfileLoadingView.stopAnimating()
            }
            self?.headerImageView.kf.setImage(with: profileInfo?.headUrl, placeholder: R.image.userHeaderDefault())
            self?.userNameLabel.text = profileInfo?.nickNamePresentation
        }).store(in: &self.anyCancellables)

        // 监听定时刷新未读消息
        [MessageCenter.shared.$numberOfNewFirmwareMessages,
        MessageCenter.shared.$numberOfUnreadSystemMessages,
        MessageCenter.shared.$numberOfUnreadWelfareActivityMessages,
         MessageCenter.shared.$numberOfUnreadAppNewVersionMessages]
            .combineLatest()
            .receive(on: DispatchQueue.main)
            .map {
                $0.reduce(0, +)
            }.sink { [weak self] count in
                if count > 0 {
                    self?.msgButton.showBadge(.icon, position: .topRight, isSetBorder: false, offset: .init(x: 0, y: 0), diameter: 5)
                }else{
                    self?.msgButton.hideBadge()
                }
            }.store(in: &self.anyCancellables)
        
        // 是否有设备可升级
        FirmwareUpgradeCenter.shared.$tasks.map({
            $0.filter({ !$0.upgradeStatus.isSuccess }).isEmpty
        })
        .sink(receiveValue: { [weak self] in
            self?.functionsButtons.first?.showBadge = !$0
        }).store(in: &self.anyCancellables)
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 3 {
            return self.cellItems.count
        }
        if section == 2, self.equitiesStackView.arrangedSubviews.count == 0 {
            return 0
        }
        return super.tableView(tableView, numberOfRowsInSection: section)
    }

    override func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.backgroundColor = R.color.background_FFFFFF_white()!
        if indexPath.section == 3 {
            self.setupCellStyle(cell: cell)
            cell.imageView?.image = self.cellItems[safe_: indexPath.row]?.image
            cell.textLabel?.text = self.cellItems[safe_: indexPath.row]?.title
        }
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if indexPath.section == 0 {
            let vc = UserProfileTableViewController.fromStoryBoard()
            self.navigationController?.pushViewController(vc, animated: true)
        }
        if indexPath.section == 3 {
            self.cellItems[safe_: indexPath.row]?.action()
        }
    }

    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 2, !self.needShowEquitiesCell() {
            return 0.1
        }
        return super.tableView(tableView, heightForHeaderInSection: section)
    }

    override func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if section == 2, !self.needShowEquitiesCell() {
            return 0.1
        }
        return super.tableView(tableView, heightForFooterInSection: section)
    }

    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if indexPath.section == 2, !self.needShowEquitiesCell() {
            return 0.1
        }
        return super.tableView(tableView, heightForRowAt: indexPath)
    }
}

extension MineViewController {

    /// 根据显示条件刷新Tableview
    /// - Parameters:
    ///   - showVas: 是否显示云服务入口
    ///   - showFlux: 是否显示流量入口
    ///   - hideMyTransactions: 隐藏我的订单
    ///   - hideMyTickets: 隐藏我的卡券
    func reloadTableView(showVas: Bool, showFlux: Bool, hideMyTransactions: Bool, hideMyTickets: Bool) {
        self.equitiesStackView.removeAllSubviews()
        // "云服务" "流量"
        self.equityButtons.forEach { self.equitiesStackView.addArrangedSubview($0) }
        // 移除云服务入口
        if !showVas {
            let btn = self.equitiesStackView.arrangedSubviews.first(where: { $0.tag == EquityBtnType.vas.rawValue })
            btn?.removeFromSuperview()
        }
        // 移除4G服务入口
        if !showFlux {
            let btn = self.equitiesStackView.arrangedSubviews.first(where: { $0.tag == EquityBtnType.flux.rawValue })
            btn?.removeFromSuperview()
        }
        // UI 要求要靠左, 所以需要按需补位
        let numOfEquitiesBtn = self.equitiesStackView.arrangedSubviews.count
        for _ in 0..<(3 - numOfEquitiesBtn) {
            let placeholder = ServiceButton.placeholderButton()
            self.equitiesStackView.addArrangedSubview(placeholder)
        }

        // 组合 cellItems
        if hideMyTickets {
            self.cellItems = [self.cellItem_feedback, self.cellItem_about]
        }else{
            self.cellItems = [self.cellItem_myTransactions, self.cellItem_myTickets, self.cellItem_feedback, self.cellItem_about]
        }
        
        self.tableView.reloadData()
    }

    func setupCellStyle(cell: UITableViewCell) {
        cell.textLabel?.font = .systemFont(ofSize: 16)
        cell.textLabel?.textColor = R.color.text_000000_90()
        cell.accessoryView = UIImageView.init(image: R.image.commonArrowRightStyle1()!)
    }

    func needShowEquitiesCell() -> Bool {
        return self.equitiesStackView.arrangedSubviews.contains { $0.tag != EquityBtnType.placeholder.rawValue }
    }
}
