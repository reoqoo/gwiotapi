//
//  FindPasswordViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 31/7/2023.
//

import UIKit
import RQCore
import RQApi

class FindPasswordViewController: BaseViewController {

    private lazy var titleLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.text = String.localization.localized("AA0031", note: "找回密码")
    }

    private lazy var introductionLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_60()!
        $0.font = .systemFont(ofSize: 14)
        $0.text = String.localization.localized("AA0149", note: "请输入XXXXXXXX账号")
    }

    private lazy var accountInputView: AccountInputView = .init()

    private lazy var tipsLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 12)
        $0.textColor = .red
    }

    private lazy var getOneTimeCodeBtn: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0020", note: "获取验证码"), for: .normal)
    }

    // regionSelectionBarButtonItem 的 custom view
    lazy var regionSelectionButton: UIButton = .init(type: .system).then {
        $0.setTitleColor(R.color.text_000000()!, for: .normal)
        $0.setTitle(nil, for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16)
    }

    lazy var regionSelectionBarButtonItem: UIBarButtonItem = .init(customView: self.regionSelectionButton)

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.rq.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()

        self.navigationItem.rightBarButtonItem = self.regionSelectionBarButtonItem

        self.view.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(24)
            make.leading.equalToSuperview().offset(28)
        }

        self.view.addSubview(self.introductionLabel)
        self.introductionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
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

        self.view.addSubview(self.getOneTimeCodeBtn)
        self.getOneTimeCodeBtn.snp.makeConstraints { make in
            make.top.equalTo(self.accountInputView.snp.bottom).offset(45)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(46)
        }

        // regionSelectionBarButtonItem.title 绑定 当前选择的地区
        RegionInfoProvider.shared.$selectedRegion.map({ $0.countryName }).sink(receiveValue: { [weak self] countryName in
            self?.regionSelectionButton.setTitle(countryName, for: .normal)
        }).store(in: &self.anyCancellables)

        RegionInfoProvider.shared.$selectedRegion.sink(receiveValue: { [weak self] regionInfo in
            self?.accountInputView.regionInfo = regionInfo
        }).store(in: &self.anyCancellables)

        // 地区选择按钮点击
        self.regionSelectionButton.tapPublisher.sink(receiveValue: { [weak self] _ in
            let vc = RegionSelectionViewController.init()
            self?.navigationController?.pushViewController(vc, animated: true)
        }).store(in: &self.anyCancellables)

        // 如果输入框内容非 邮箱/手机号, 禁止"获取验证码"按钮点击
        self.accountInputView.$text.map({
            guard let text = $0 else { return false }
            return text.isValidEmail || text.isValidTelephoneNumber
        })
        .sink(receiveValue: { [weak self] flag in
            self?.getOneTimeCodeBtn.isEnabled = flag
        }).store(in: &self.anyCancellables)

        // 监听 regionSelectionButton.titleLabel.text 发生改变
        self.regionSelectionButton.publisher(for: \.titleLabel).compactMap({ $0 }).flatMap({ $0.publisher(for: \.text) }).delay(for: 0.1, scheduler: DispatchQueue.main)
            .sink(receiveValue: { [weak self] _ in
                self?.regionSelectionButton.sizeToFit()
                // 如果不刷新 navigationBar 布局, 会出现 button 被长文案拉长后切换到短文案的显示异常问题
                self?.navigationController?.navigationBar.layoutIfNeeded()
            }).store(in: &self.anyCancellables)

        // 获取验证码按钮点击
        self.getOneTimeCodeBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.getOneTimeCodeBtnOnClick()
        }).store(in: &self.anyCancellables)
    }

}

// MARK: Action
extension FindPasswordViewController {
    func getOneTimeCodeBtnOnClick() {

        guard let account = self.accountInputView.textField.text else {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0149", note: "请输入XXXXXXXX账号"))
            return
        }

        var accountType: RQApi.AccountType?

        // 检查 手机号码 / 邮箱 是否有误
        if self.accountInputView.textField.text?.isValidEmail ?? false {
            accountType = .email(account)
        }

        if self.accountInputView.textField.text?.isValidTelephoneNumber ?? false {
            accountType = .mobile(account, mobileArea: RegionInfoProvider.shared.selectedRegion.countryCode)
        }

        guard let accountType = accountType else {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0369", note: "请输入手机号或邮箱"))
            return
        }

        self.requestOneTimeCodeForFindPassword(accountType: accountType)
    }
}

// MARK: Requests
extension FindPasswordViewController {
    func requestOneTimeCodeForFindPassword(accountType: RQApi.AccountType) {
        // 发起请求
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        AccountCenter.shared.getVerificationCodeForFindPasswordRequestPublisher(accountType: accountType)
            .sink(receiveCompletion: {
                loadingHUD.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                // 如果收到 "账号不存在" 错误, self.tipsLabel 需要显示
                if (err as? ReoqooError)?.isReason(ReoqooError.AccountErrorReason.accountIsNotExist) ?? false {
                    self.tipsLabel.text = ReoqooError.AccountErrorReason.accountIsNotExist.description
                }else{
                    MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
                }
            }, receiveValue: { [weak self] in
                self?.jump2OneTimeCodeInput(accountType: accountType)
            }).store(in: &self.anyCancellables)
    }
}

// MARK: Helper
extension FindPasswordViewController {
    func jump2OneTimeCodeInput(accountType: RQApi.AccountType) {
        // 成功, 跳转到输入验证码页面
        let vc = OneTimeCodeInputViewController(for: .findPassword, accountType: accountType)
        self.navigationController?.pushViewController(vc, animated: true)

        vc.resultPublisher.sink(receiveValue: { [weak self] result in
            if case let .success(code) = result {
                // 成功了, 跳转到设置密码环节
                let vc = PasswordSettingViewController.init(for: .forgotPassword(accountType: accountType, oneTimeCode: code))
                self?.navigationController?.pushViewController(vc, animated: true)
            }
        }).store(in: &self.anyCancellables)

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
