//
//  RegisterRequestVerificationCodeViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 27/7/2023.
//

import Foundation
import RQWebServices
import RQCore
import RQApi

extension RequestOneTimeCodeViewController {
    struct AccountType: OptionSet, CaseIterable, CustomStringConvertible {

        static var allCases: [AccountType] = [.email, .telephone]

        var rawValue: Int
        var description: String = ""

        init(rawValue: Int) {
            self.rawValue = rawValue
            self.description = ""
        }

        init(rawValue: Int, description: String) {
            self.rawValue = rawValue
            self.description = description
        }

        static let email: AccountType = .init(rawValue: 1 << 0, description: String.localization.localized("AA0280", note: "邮箱"))
        static let telephone: AccountType = .init(rawValue: 1 << 1, description: String.localization.localized("AA0279", note: "手机"))

        func descriptionCombine() -> String {
            let descriptions = Self.allCases.filter { self.contains($0) }.map({ $0.description })
            return descriptions.joined(separator: "/")
        }
    }
}

// 输入账号获取验证码功能
class RequestOneTimeCodeViewController: BaseViewController {

    @DidSetPublished var supportedAccountType: AccountType

    init(accountType: AccountType = [.email, .telephone]) {
        self.supportedAccountType = accountType
        super.init(nibName: nil, bundle: nil)
        self.title = ""
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    private lazy var titleLabel: UILabel = UILabel.init().then {
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.text = String.localization.localized("AA0016", note: "账号注册")
    }

    private lazy var introductionLabel: UILabel = UILabel.init().then {
        $0.textColor = R.color.text_000000_60()!
        $0.font = .systemFont(ofSize: 14)
        $0.text = self.supportedAccountType.contains(.telephone) ? String.localization.localized("AA0019", note: "输入您可用于XXXXXXXX账号注册的邮箱或⼿机号") : String.localization.localized("AA0481", note: "输入您可用于XXXXXXXX账号注册的邮箱")
        $0.numberOfLines = 0
    }

    private lazy var accountInputView: AccountInputView = .init()

    private lazy var tipsLabel: UILabel = UILabel.init().then {
        $0.font = .systemFont(ofSize: 12)
        $0.textColor = .red
    }

    private lazy var confirmButton: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0020", note: "获取验证码"), for: .normal)
    }

    private lazy var agreementView: AgreementView = .init(frame: .zero)

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.rq.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()

        self.view.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(24)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.view.addSubview(self.introductionLabel)
        self.introductionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.view.addSubview(self.accountInputView)
        self.accountInputView.snp.makeConstraints { make in
            make.top.equalTo(self.introductionLabel.snp.bottom).offset(50)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(56)
        }

        self.view.addSubview(self.tipsLabel)
        self.tipsLabel.snp.makeConstraints { make in
            make.top.equalTo(self.accountInputView.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
        }

        self.view.addSubview(self.confirmButton)
        self.confirmButton.snp.makeConstraints { make in
            make.top.equalTo(self.accountInputView.snp.bottom).offset(45)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(46)
        }

        self.view.addSubview(self.agreementView)
        self.agreementView.snp.makeConstraints { make in
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-24)
            make.centerX.equalToSuperview()
            make.leading.greaterThanOrEqualToSuperview().offset(16)
            make.trailing.lessThanOrEqualToSuperview().offset(-16)
        }

        // 确认按钮点击
        self.confirmButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                self?.tryRequestOneTimeCode()
            }).store(in: &self.anyCancellables)

        // 监听 AggrementView URL 点击
        self.agreementView.linkDidTapObservable.sink(receiveValue: { [weak self] url in
            self?.navigationController?.pushViewController(WebViewController.init(url: url), animated: true)
        }).store(in: &self.anyCancellables)

        // 支持的登录方式 绑定到 accountInputView.textField.placeholder
        self.$supportedAccountType
            .map({ $0.descriptionCombine() })
            .sink(receiveValue: { [weak self] text in
                self?.accountInputView.textField.placeholder = text
            }).store(in: &self.anyCancellables)

        // 当前选定的地区信息绑定到 accountInputView
        RegionInfoProvider.shared.$selectedRegion.sink(receiveValue: { [weak self] regionInfo in
            self?.accountInputView.regionInfo = regionInfo
        }).store(in: &self.anyCancellables)

        // 确认按钮 enable 状态
        self.accountInputView.$text.map({ !($0?.isEmpty ?? true) })
            .sink(receiveValue: { [weak self] flag in
                self?.confirmButton.isEnabled = flag
            }).store(in: &self.anyCancellables)
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
}

// MARK: Action
extension RequestOneTimeCodeViewController {
    func tryRequestOneTimeCode() {

        self.view.endEditing(true)

        // 邮箱手机号判断
        guard let account = self.accountInputView.textField.text else {
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
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0562", note: "请输入正确的手机号或邮箱"))
            return
        }

        // 未同意协议
        if !self.agreementView.isAgree {
            self.showAgreementAlert()
            return
        }

        // 发送请求
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        self.confirmButton.isEnabled = false
        AccountCenter.shared.getOneTimeCodeForRegisterRequestPublisher(accountType: accountType)
            .sink(receiveCompletion: { [weak self] in
                loadingHUD.hideDispatchOnMainThread()
                self?.confirmButton.isEnabled = true
                guard case let .failure(err) = $0 else { return }
                // 如果账号已被注册, self.tipsLabel 要显示
                if (err as NSError).code == ReoqooError.AccountErrorReason.telephoneHaveBeenRegistered.code || (err as NSError).code == ReoqooError.AccountErrorReason.emailHaveBeenRegistered.code {
                    self?.tipsLabel.text = err.localizedDescription
                }else{
                    MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
                }
            }, receiveValue: { [weak self] _ in
                // 发送获取验证码请求成功, 成功后跳转 OneTimeCodeInputViewController
                self?.jump2OneTimeCodeInput(accountType: accountType)
            }).store(in: &self.anyCancellables)
    }
}

// MARK: Helper
extension RequestOneTimeCodeViewController {
    func showAgreementAlert() {
        ReoqooAlertViewController.showUsageAgreementAlert(withPresentedViewController: self, agreeClickHandler: { [weak self] in
            // 点击了同意按钮
            self?.agreementView.isAgree = true
            self?.tryRequestOneTimeCode()
        }, urlClickHandler: { [weak self] url in
            // 用户点击了 协议链接, 打开网页
            let webViewController = WebViewController(url: url)
            let nav = BaseNavigationController.init(rootViewController: webViewController)
            self?.presentedViewController?.present(nav, animated: true)
        })
    }

    func jump2OneTimeCodeInput(accountType: RQApi.AccountType) {
        let vc = OneTimeCodeInputViewController.init(for: .register, accountType: accountType)
        self.navigationController?.pushViewController(vc, animated: true)

        // 监听验证码验证结果
        vc.resultPublisher.sink(receiveValue: { [weak self] in
            if case let .success(code) = $0 {
                // 成功了, 跳转到设置密码环节
                let vc = PasswordSettingViewController.init(for: .register(accountType: accountType, oneTimeCode: code))
                self?.navigationController?.pushViewController(vc, animated: true)
            }
        }).store(in: &self.anyCancellables)

        // 监听 重发验证码 按钮点击
        vc.$status.sink(receiveValue: { [weak self] status in
            if case let .resendVerifyCode(accountType) = status {
                self?.resendOneTimeCode(accountType: accountType)
            }
        }).store(in: &self.anyCancellables)
    }

    func resendOneTimeCode(accountType: RQApi.AccountType) {
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread()
        AccountCenter.shared.getOneTimeCodeForRegisterRequestPublisher(accountType: accountType)
            .sink(receiveCompletion: {
                loadingHUD.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { _ in
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0040", note: "验证码已发送"))
            }).store(in: &self.anyCancellables)
    }
}
