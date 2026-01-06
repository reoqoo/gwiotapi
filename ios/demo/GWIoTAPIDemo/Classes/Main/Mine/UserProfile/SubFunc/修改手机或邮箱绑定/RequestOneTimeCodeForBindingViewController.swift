//
//  EmailInputViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit
import RQCore
import RQApi

extension RequestOneTimeCodeForBindingViewController {
    enum BindType {
        // 新绑定邮箱
        case bindEmail
        // 更换邮箱绑定
        case changeEmail
        // 新绑定手机号
        case bindTelephone
        // 更换手机号绑定
        case changeTelephone

        var title: String {
            switch self {
            case .bindEmail, .changeEmail:
                return String.localization.localized("AA0305", note: "请输入邮箱")
            case .bindTelephone, .changeTelephone:
                return String.localization.localized("AA0299", note: "请输入手机号")
            }
        }

        var successTips: String {
            switch self {
            case .bindEmail:
                return String.localization.localized("AA0307", note: "绑定邮箱成功")
            case .changeEmail:
                return String.localization.localized("AA0308", note: "更换邮箱成功")
            case .bindTelephone:
                return String.localization.localized("AA0302", note: "绑定手机号成功")
            case .changeTelephone:
                return String.localization.localized("AA0303", note: "更换手机号成功")
            }
        }
    }
}

class RequestOneTimeCodeForBindingViewController: BaseViewController, ScrollBaseViewAndKeyboardMatchable {

    let bindType: BindType
    init(bindType: BindType) {
        self.bindType = bindType
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    var scrollable: UIScrollView { self.scrollView }

    var anyCancellables: Set<AnyCancellable> = []

    /// 在验证码发送完毕后将账号信息记录起来
    var accountType: RQApi.AccountType?

    lazy var scrollView: UIScrollView = .init().then {
        $0.showsVerticalScrollIndicator = true
        $0.alwaysBounceVertical = true
    }

    private lazy var titleLabel: UILabel = UILabel.init().then {
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.text = self.bindType.title
        $0.numberOfLines = 0
    }

    private lazy var accountInputView: AccountInputView = .init().then {
        $0.textField.placeholder = self.bindType.title
    }

    lazy var regionSelectionButton: RegionSelectionButton = .init().then { [weak self] btn in
        btn.isEnabled = self?.bindType == .bindTelephone
    }

    lazy var getOneTimeCodeButton: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0020", note: "获取验证码"), for: .normal)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.dismissKeyboardWhenTapOnNonInteractiveArea()
        self.adjustScrollViewContentInsetWhenKeyboardFrameChanged()

        self.view.addSubview(self.scrollView)
        self.scrollView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.scrollView.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(24)
            make.leading.equalToSuperview().offset(28)
        }

        if self.bindType == .bindTelephone || self.bindType == .changeTelephone {
            self.scrollView.addSubview(self.regionSelectionButton)
            self.regionSelectionButton.snp.makeConstraints { make in
                make.top.equalTo(self.titleLabel.snp.bottom).offset(52)
                make.leading.equalToSuperview().offset(28)
                make.trailing.equalToSuperview().offset(-28)
                make.height.equalTo(56)
            }

            self.scrollView.addSubview(self.accountInputView)
            self.accountInputView.snp.makeConstraints { make in
                make.top.equalTo(self.regionSelectionButton.snp.bottom).offset(12)
                make.leading.equalToSuperview().offset(28)
                make.trailing.equalToSuperview().offset(-28)
                make.height.equalTo(56)
            }
        }else{
            self.scrollView.addSubview(self.accountInputView)
            self.accountInputView.snp.makeConstraints { make in
                make.top.equalTo(self.titleLabel.snp.bottom).offset(52)
                make.leading.equalToSuperview().offset(28)
                make.trailing.equalToSuperview().offset(-28)
                make.height.equalTo(56)
            }
        }

        self.scrollView.addSubview(self.getOneTimeCodeButton)
        self.getOneTimeCodeButton.snp.makeConstraints { make in
            make.top.equalTo(self.accountInputView.snp.bottom).offset(40)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.bottom.equalToSuperview().offset(40)
            make.height.equalTo(46)
            make.width.equalTo(self.view.snp.width).offset(-32)
        }

        self.titleLabel.text = self.bindType.title
        self.accountInputView.textField.placeholder = self.bindType.title

        self.accountInputView.$text.map({ !($0?.isEmpty ?? true) })
            .sink(receiveValue: { [weak self] flag in
                self?.getOneTimeCodeButton.isEnabled = flag
            }).store(in: &self.anyCancellables)

        self.getOneTimeCodeButton.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.tryRequestOneTimeCode()
        }).store(in: &self.anyCancellables)

        RegionInfoProvider.shared.$selectedRegion.map({ $0.countryName }).sink(receiveValue: { [weak self] countryName in
            self?.regionSelectionButton.currentRegionLabel.text = countryName
        }).store(in: &self.anyCancellables)

        RegionInfoProvider.shared.$selectedRegion.sink(receiveValue: { [weak self] regionInfo in
            if self?.bindType == .bindEmail || self?.bindType == .changeEmail {
                self?.accountInputView.regionInfo = nil
            }else{
                self?.accountInputView.regionInfo = regionInfo
            }
        }).store(in: &self.anyCancellables)

        self.regionSelectionButton.tapPublisher.sink(receiveValue: { [weak self] _ in
            let vc = RegionSelectionViewController.init()
            self?.navigationController?.pushViewController(vc, animated: true)
        }).store(in: &self.anyCancellables)
    }

}

// MARK: Helper
extension RequestOneTimeCodeForBindingViewController {
    func tryRequestOneTimeCode() {

        self.view.endEditing(true)

        // 邮箱手机号判断
        guard let account = self.accountInputView.textField.text else {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0369", note: "请输入手机号或邮箱"))
            return
        }

        var accountType: RQApi.AccountType?

        // 检查 手机号码 / 邮箱 是否有误
        if self.accountInputView.textField.text?.isValidEmail ?? false {
            accountType = .email(account)
        }

        if self.accountInputView.textField.text?.isValidTelephoneNumber ?? false {
            accountType = .mobile(account, mobileArea: RegionInfoProvider.shared.selectedRegion.districtCode)
        }

        guard let accountType = accountType else {
            // 判断错误类型, 弹出适当的提示语
            if self.bindType == .bindEmail || self.bindType == .changeEmail {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0267", note: "邮箱格式错误"))
            }
            if self.bindType == .bindTelephone || self.bindType == .changeTelephone {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0266", note: "手机号格式错误"))
            }
            return
        }

        // 发送请求
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        self.getOneTimeCodeButton.isEnabled = false
        AccountCenter.shared.getOneTimeCodeForRegisterRequestPublisher(accountType: accountType)
            .sink(receiveCompletion: { [weak self] in
                loadingHUD.hideDispatchOnMainThread()
                self?.getOneTimeCodeButton.isEnabled = true
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { [weak self] _ in
                // 发送验证码请求成功, 将 account 信息记录起来, 以便下一步请求使用
                self?.accountType = accountType
                // 发送获取验证码请求成功, 成功后跳转 VerificationCodeInputViewController
                self?.go2OneTimeCodeInput(accountType: accountType)
            }).store(in: &self.anyCancellables)
    }
}

// MARK: Helper
extension RequestOneTimeCodeForBindingViewController {
    func go2OneTimeCodeInput(accountType: RQApi.AccountType) {
        let vc = OneTimeCodeInputViewController.init(for: .binding, accountType: accountType)
        self.navigationController?.pushViewController(vc, animated: true)
        vc.resultPublisher.sink { [weak self] result in
            if case let .success(code) = result {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: self?.bindType.successTips ?? "")
                // 发起绑定请求
                self?.bindRequest(oneTimeCode: code)
            }
        }.store(in: &self.anyCancellables)

        vc.$status.sink { [weak self] status in
            if case let .resendVerifyCode(accountType) = status {
                self?.resendOneTimeCode(accountType: accountType)
            }
        }.store(in: &self.anyCancellables)
    }

    func resendOneTimeCode(accountType: RQApi.AccountType) {
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        AccountCenter.shared.getOneTimeCodeForRegisterRequestPublisher(accountType: accountType)
            .sink(receiveCompletion: {
                loadingHUD.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { _ in
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0040", note: "验证码已发送"))
            }).store(in: &self.anyCancellables)
    }

    func bindRequest(oneTimeCode: String) {
        guard let accountType = self.accountType else { return }
        let hud = MBProgressHUD.showLoadingHUD_DispatchOnMainThread()
        AccountCenter.shared.currentUser?.bindMobileOrEmailPublisher(accountType: accountType, oneTimeCode: oneTimeCode)
            .sink(receiveCompletion: { [weak self] in
                hud.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else {
                    self?.pop2ProfileViewController()
                    return
                }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { [weak self] _ in
                self?.pop2ProfileViewController()
            }).store(in: &self.anyCancellables)
    }

    func pop2ProfileViewController() {
        guard let target = self.navigationController?.viewControllers.filter({ $0 is UserProfileTableViewController }).first else { return }
        self.navigationController?.popToViewController(target, animated: true)
    }
}
