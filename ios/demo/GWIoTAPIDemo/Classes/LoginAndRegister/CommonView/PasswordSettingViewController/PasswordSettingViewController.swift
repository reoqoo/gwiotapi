//
//  PasswordSettingViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/7/2023.
//

import UIKit
import IVAccountMgr
import RQCore
import RQApi

extension PasswordSettingViewController {
    enum For {
        case register(accountType: RQApi.AccountType, oneTimeCode: String)
        case forgotPassword(accountType: RQApi.AccountType, oneTimeCode: String)
    }
}

class PasswordSettingViewController: BaseViewController, ScrollBaseViewAndKeyboardMatchable {

    let `for`: PasswordSettingViewController.For
    init(`for`: For) {
        self.`for` = `for`
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    // ScrollBaseViewAndKeyboardMatchable
    var scrollable: UIScrollView { self.scrollView }
    var anyCancellables: Set<AnyCancellable> = []
    
    lazy var scrollView: UIScrollView = .init().then {
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.alwaysBounceVertical = true
        $0.keyboardDismissMode = .interactive
    }

    private lazy var titleLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.text = String.localization.localized("AA0024", note: "设置密码")
        $0.numberOfLines = 0
    }

    private lazy var introductionLabel: UILabel = UILabel.init().then {
        $0.textColor = R.color.text_000000_60()!
        $0.font = .systemFont(ofSize: 14)
        $0.text = String.localization.localized("AA0025", note: "密码为8～30位包含字⺟、数字的字符")
        $0.numberOfLines = 0
    }

    private lazy var passwordInputView: PasswordInputView = .init(frame: .zero).then {
        $0.textField.placeholder = String.localization.localized("AA0003", note: "密码")
    }

    // 确认密码输入框
    private lazy var repeatPasswordInputView: PasswordInputView = .init(frame: .zero).then {
        $0.textField.placeholder = String.localization.localized("AA0026", note: "确认密码")
    }

    // 输入密码提示: "两次输入不一致"
    private lazy var passwordInputStatusLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 12)
        $0.text = String.localization.localized("AA0373", note: "两次输入不一致")
        $0.textColor = .systemRed
    }

    private lazy var okBtn: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.setTitleColor(.white, for: .normal)
        $0.setTitle(String.localization.localized("AA0058", note: "确定"), for: .normal)
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()
        
        self.view.addSubview(self.scrollView)
        self.scrollView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.scrollView.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(24)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.introductionLabel)
        self.introductionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.passwordInputView)
        self.passwordInputView.snp.makeConstraints { make in
            make.top.equalTo(self.introductionLabel.snp.bottom).offset(38)
            make.height.equalTo(56)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.repeatPasswordInputView)
        self.repeatPasswordInputView.snp.makeConstraints { make in
            make.top.equalTo(self.passwordInputView.snp.bottom).offset(24)
            make.height.equalTo(56)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.passwordInputStatusLabel)
        self.passwordInputStatusLabel.snp.makeConstraints { make in
            make.top.equalTo(self.repeatPasswordInputView.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.okBtn)
        self.okBtn.snp.makeConstraints { make in
            make.top.equalTo(self.passwordInputStatusLabel.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.bottom.equalToSuperview().offset(-24)
            make.height.equalTo(46)
            // 将 scrollView 的 contentSize 宽度撑起来
            make.width.equalTo(self.view.snp.width).offset(-56)
        }

        self.dismissKeyboardWhenTapOnNonInteractiveArea()
        self.adjustScrollViewContentInsetWhenKeyboardFrameChanged()

        // 将 两个密码输入框 的输入事件 combine 起来
        self.passwordInputView.textField.textPublisher.combineLatest(self.repeatPasswordInputView.textField.textPublisher)
            .sink { [weak self] (p0, p1) in
                // 两次输入密码对比结果文案提示
                // 当 确认密码输入框 为空时, 隐藏文案
                // 当 两个输入框 文案相同时, 隐藏文案
                self?.passwordInputStatusLabel.isHidden = (p1?.isEmpty ?? true) || p0 == p1
                // 确定按钮 是否可被点击
                self?.okBtn.isEnabled = p0 == p1 && !(p0?.isEmpty ?? true) && !(p1?.isEmpty ?? true)
            }.store(in: &self.anyCancellables)

        self.okBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            let password = self?.passwordInputView.textField.text ?? ""
            if !password.isValidPassword {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0396", note: "密码为8-30位包含字母、数字的字符"))
                return
            }
            if case let .register(accountType, oneTimeCode) = self?.for {
                let regionNameCode = RegionInfoProvider.shared.selectedRegion.regionCode
                self?.sendingSignUpRequest(accountType: accountType, regionNameCode: regionNameCode, oneTimeCode: oneTimeCode, password: password)
            }
            if case let .forgotPassword(accountType, oneTimeCode) = self?.for {
                self?.sendingFindPasswordRequest(accountType: accountType, password: password, code: oneTimeCode)
            }
        }).store(in: &self.anyCancellables)
    }

}

// MARK: Requests
extension PasswordSettingViewController {
    // 发送注册请求
    func sendingSignUpRequest(accountType: RQApi.AccountType, regionNameCode: String, oneTimeCode: String, password: String) {
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        AccountCenter.shared.registerRequestPublisher(accountType: accountType, regionNameCode: regionNameCode, password: password, oneTimeCode: oneTimeCode)
            .sink(receiveCompletion: {
                loadingHUD.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { _ in
            }).store(in: &self.anyCancellables)
    }

    // 发送找回密码请求
    func sendingFindPasswordRequest(accountType: RQApi.AccountType, password: String, code: String) {
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        AccountCenter.shared.findPasswordRequestPublisher(accountType: accountType, password: password, code: code)
            .sink(receiveCompletion: {
                loadingHUD.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { [weak self] _ in
                // 跳转到成功
                let vc = PasswordSettingSucceedViewController.init()
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)
    }
}
