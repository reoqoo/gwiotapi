//
//  UserProfileTableViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 13/9/2023.
//

import UIKit
import RQCore
import RQCoreUI

extension UserProfileTableViewController {
    struct CellItem {
        let title: String
        let indicatorImage: UIImage?
    }
}

class UserProfileTableViewController: BaseTableViewController {

    static func fromStoryBoard() -> UserProfileTableViewController {
        let sb = UIStoryboard.init(name: R.storyboard.userProfile.name, bundle: nil)
        return sb.instantiateViewController(withIdentifier: String.init(describing: UserProfileTableViewController.self)) as! UserProfileTableViewController
    }

    let vm: ViewModel = .init()

    @IBOutlet weak var headerImageView: UIImageView!
    @IBOutlet weak var nickNameLabel: UILabel!
    @IBOutlet weak var userIDLabel: UILabel!
    @IBOutlet weak var telephoneNumberLabel: UILabel!
    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var regionLabel: UILabel!

    weak var modifyNameAlert: AlertViewController?

    lazy var logoutButton: UIButton = .init(type: .custom).then {
        $0.setBackgroundColor(R.color.background_FFFFFF_white()!, for: .normal)
        $0.setBackgroundColor(R.color.background_000000_5()!, for: .highlighted)
        $0.setTitle(String.localization.localized("AA0283", note: "退出登录"), for: .normal)
        $0.setTitleColor(R.color.text_000000_90(), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
    }

    lazy var cellItems: [[CellItem]] = [
        [.init(title: String.localization.localized("AA0276", note: "头像"), indicatorImage: nil)],
        [.init(title: String.localization.localized("AA0277", note: "修改昵称"), indicatorImage: R.image.commonArrowRightStyle1()),
         .init(title: String.localization.localized("AA0596", note: "账户ID"), indicatorImage: R.image.commonCopy()),
         .init(title: String.localization.localized("AA0278", note: "修改密码"), indicatorImage: R.image.commonArrowRightStyle1()),
         .init(title: String.localization.localized("AA0279", note: "手机"), indicatorImage: R.image.commonArrowRightStyle1()),
         .init(title: String.localization.localized("AA0280", note: "邮箱"), indicatorImage: R.image.commonArrowRightStyle1()),
         .init(title: String.localization.localized("AA0281", note: "注册地区"), indicatorImage: nil),
         .init(title: String.localization.localized("AA0282", note: "注销账号"), indicatorImage: R.image.commonArrowRightStyle1())]
    ]

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0275", note: "账户信息")

        self.tableView.separatorColor = R.color.lineSeparator()
        self.tableView.separatorInset = .init(top: 0, left: 12, bottom: 0, right: 12)
        self.tableView.tableHeaderView = .init(frame: .init(x: 0, y: 0, width: 0, height: 16))

        let footerView: UIView = .init()
        footerView.addSubview(self.logoutButton)
        self.logoutButton.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.height.equalTo(46)
        }
        
        self.tableView.tableFooterView = footerView
        footerView.frame = .init(x: 0, y: 0, width: 0, height: 80)
        
        // 数据绑定
        AccountCenter.shared.$currentUser.flatMap {
            $0?.$profileInfo.eraseToAnyPublisher() ?? Just<RQCore.ProfileInfo?>(nil).eraseToAnyPublisher()
        }.sink { [weak self] in
            guard let headerURL = $0?.headUrl else { return }
            self?.headerImageView.kf.setImage(with: headerURL, placeholder: R.image.userHeaderDefault())
        }.store(in: &self.anyCancellables)
        
        AccountCenter.shared.currentUser?.$profileInfo
            .sink(receiveValue: { [weak self] userProfile in
                self?.nickNameLabel.text = userProfile?.nick
                self?.telephoneNumberLabel.text = !(userProfile?.hasBindTelephone ?? false) ? String.localization.localized("AA0296", note: "未绑定") : userProfile?.mobile
                self?.emailLabel.text = !(userProfile?.hasBindEmail ?? false) ? String.localization.localized("AA0296", note: "未绑定") : userProfile?.email
                self?.userIDLabel.text = userProfile?.showId
                self?.tableView.performBatchUpdates{}
            }).store(in: &self.anyCancellables)
        
        AccountCenter.shared.currentUser?.$basicInfo.sink(receiveValue: { [weak self] basicInfo in
            self?.regionLabel.text = basicInfo.regionInfo?.countryName
        }).store(in: &self.anyCancellables)

        // 点击了 退出登录 按钮
        self.logoutButton.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.presentLogoutAlert()
        }).store(in: &self.anyCancellables)

        self.vm.$status.sink(receiveValue: {[weak self] status in
            switch status {
            case let .didCompleteModifyUserInfo(result):
                self?.didCompleteModifyUserInfo(result)
            default:
                break
            }
        }).store(in: &self.anyCancellables)
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = self.cellItems[indexPath.section][indexPath.row]
        let cell = super.tableView(tableView, cellForRowAt: indexPath)
        cell.textLabel?.font = .systemFont(ofSize: 16)
        cell.textLabel?.textColor = R.color.text_000000_90()
        cell.textLabel?.text = item.title
        cell.detailTextLabel?.font = .systemFont(ofSize: 14)
        cell.detailTextLabel?.textColor = R.color.text_000000_60()
        if let indicatorImage = item.indicatorImage {
            cell.accessoryView = UIImageView.init(image: indicatorImage)
        }else{
            cell.accessoryView = nil
        }
        return cell
    }

    override func tableView(_ tableView: UITableView, shouldHighlightRowAt indexPath: IndexPath) -> Bool {
        if indexPath.row == 5 && indexPath.section == 1 { return false }
        return true
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        // 修改头像
        if indexPath.row == 0 && indexPath.section == 0 {
            let vc = UserSelectHeaderViewController.init()
            self.present(vc, animated: true)
            vc.$selectedHeaderURL.compactMap({ $0 })
                .sink { [weak self] in
                    self?.headerImageView.kf.setImage(with: $0, placeholder: R.image.userHeaderDefault())
                }.store(in: &self.anyCancellables)
        }
        // 复制 userId
        if indexPath.row == 1 && indexPath.section == 1 {
            UIPasteboard.general.string = AccountCenter.shared.currentUser?.profileInfo?.showId
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0268", note: "复制成功"))
        }
        // 修改昵称
        if indexPath.row == 0 && indexPath.section == 1 {
            self.presentRenameAlert()
        }
        // 修改密码
        if indexPath.row == 2 && indexPath.section == 1 {
            let vc = ModifyPasswordViewController.init()
            self.navigationController?.pushViewController(vc, animated: true)
        }
        // 修改手机
        if indexPath.row == 3 && indexPath.section == 1 {
            self.telephoneCellDidSelected()
        }
        // 修改邮箱
        if indexPath.row == 4 && indexPath.section == 1 {
            self.emailCellDidSelected()
        }
        // 注销账号
        if indexPath.row == 6 && indexPath.section == 1 {
            let vc = CloseAccountReasonSelectionViewController.init()
            self.present(vc, animated: true)
            vc.$flowItem.sink(receiveValue: { [weak self] flowItem in
                    guard let flowItem = flowItem else { return }
                    let vc = CloseAccountIntroductionViewController.init(flowItem: flowItem)
                    self?.navigationController?.pushViewController(vc, animated: true)
                }).store(in: &self.anyCancellables)
        }
    }

    override func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat { 12 }

    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 0.1
    }

    // MARK: Helper
    func presentRenameAlert() {
        let toast = String.localization.localized("AA0289", note: "昵称必须少于24个字")
        let alert = RQCoreUI.AlertViewController.init(title: String.localization.localized("AA0286", note: "请输入昵称"), content: nil, actions: [
            .init(title: String.localization.localized("AA0059", note: "取消"), style: .cancel),
            .init(title: String.localization.localized("AA0058", note: "确定"), style: .default, handler: { [weak self] _, alert in
                MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true, tag: 100)
                self?.vm.processEvent(.modifyUserInfo(header: nil, nick: alert.textFields?.first?.text, oldPassword: nil, newPassword: nil))
            })
        ])
        alert.addTextFiled(String.localization.localized("AA0286", note: "请输入昵称"), textFieldLimit: .init(limit: 24, limitWarning: toast), editingChangedHandler: { text, textField, alert in
            guard let alert = alert as? AlertViewController else { return }
            alert.actions.last?.enable = !text.isEmpty
        })
        self.modifyNameAlert = alert
        self.present(alert, animated: true)
    }

    // 点击了手机号码
    func telephoneCellDidSelected() {
        // 如果已绑定手机, 跳到更换绑定提示页
        // 如果未绑定手机, 跳转到输入手机号码页面
        if AccountCenter.shared.currentUser?.profileInfo?.hasBindTelephone ?? false {
            let vc = RebindTipsViewController(bindType: .changeTelephoneBind)
            self.navigationController?.pushViewController(vc, animated: true)
        }else{
            let vc = RequestOneTimeCodeForBindingViewController(bindType: .bindTelephone)
            self.navigationController?.pushViewController(vc, animated: true)
        }
    }

    // 点击了邮箱
    func emailCellDidSelected() {
        // 如果已绑定邮箱, 跳到更换绑定提示页
        // 如果未绑定邮箱, 跳转到输入邮箱地址页面
        if AccountCenter.shared.currentUser?.profileInfo?.hasBindEmail ?? false {
            let vc = RebindTipsViewController(bindType: .changeEmailBind)
            self.navigationController?.pushViewController(vc, animated: true)
        }else{
            let vc = RequestOneTimeCodeForBindingViewController(bindType: .bindEmail)
            self.navigationController?.pushViewController(vc, animated: true)
        }
    }

    // MARK: VM Status Handling
    func didCompleteModifyUserInfo(_ result: Result<Void, Swift.Error>) {
        MBProgressHUD.fromTag(100)?.hideDispatchOnMainThread()
        if case let .failure(err) = result {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
        }
        if case .success = result {
            self.modifyNameAlert?.dismiss(animated: true)
        }
    }
    
    // 弹出退出登录提示
    func presentLogoutAlert() {
        let cancelAction: RQCoreUI.AlertViewController.Action = .init(title: String.localization.localized("AA0059", note: "取消"), style: .cancel)
        let sureAction: RQCoreUI.AlertViewController.Action = .init(title: String.localization.localized("AA0058", note: "确定"), style: .destructive, handler: { [weak self] _, alert in
            self?.logout()
            alert.dismiss(animated: true)
        })
        let alert = RQCoreUI.AlertViewController.init(title: nil, content: .string(String.localization.localized("AA0309", note: "确定退出登录吗？")), actions: [cancelAction, sureAction])
        self.present(alert, animated: true)
    }

    func logout() {
        // 退出登录发布者
        AccountCenter.shared.logoutCurrentUser()
    }
}
