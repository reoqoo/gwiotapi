//
//  CloseAccountPasswordInputViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit
import IVAccountMgr
import RQCore

class CloseAccountPasswordInputViewController: BaseViewController, ScrollBaseViewAndKeyboardMatchable {

    let flowItem: CloseAccountReasonSelectionViewController.CloseAccountFlowItem
    init(flowItem: CloseAccountReasonSelectionViewController.CloseAccountFlowItem) {
        self.flowItem = flowItem
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var scrollable: UIScrollView { self.scrollView }

    var anyCancellables: Set<AnyCancellable> = []

    lazy var scrollView: UIScrollView = .init().then {
        $0.showsVerticalScrollIndicator = true
        $0.alwaysBounceVertical = true
    }

    private lazy var titleLabel: UILabel = UILabel.init().then {
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.text = String.localization.localized("AA0324", note: "验证身份")
    }

    private lazy var subTitleLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_60()!
        $0.font = .systemFont(ofSize: 14)
        $0.text = String.localization.localized("AA0325", note: "为了账户安全，请先验证登录密码")
        $0.numberOfLines = 0
    }

    private lazy var passwordInputView: PasswordInputView = .init().then {
        $0.textField.placeholder = String.localization.localized("AA0326", note: "登录密码")
    }

    private lazy var tipsLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_60()!
        $0.font = .systemFont(ofSize: 14)
        $0.text = String.localization.localized("AA0327", note: "如果忘记原密码，请退出登录，点击“忘记密码”，使用验证码重新设置密码")
        $0.numberOfLines = 0
    }

    lazy var nextStepButton: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0377", note: "下一步"), for: .normal)
        $0.setTitleColor(R.color.text_FFFFFF(), for: .normal)
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

        self.scrollView.addSubview(self.subTitleLabel)
        self.subTitleLabel.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.passwordInputView)
        self.passwordInputView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(56)
            make.top.equalTo(self.subTitleLabel.snp.bottom).offset(24)
        }

        self.scrollView.addSubview(self.tipsLabel)
        self.tipsLabel.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.top.equalTo(self.passwordInputView.snp.bottom).offset(8)
        }

        self.scrollView.addSubview(self.nextStepButton)
        self.nextStepButton.snp.makeConstraints { make in
            make.top.equalTo(self.tipsLabel.snp.bottom).offset(44)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.width.equalTo(self.view.snp.width).offset(-56)
            make.height.equalTo(46)
            make.bottom.equalToSuperview().offset(-12)
        }

        self.nextStepButton.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.presentWranningAlert()
        }).store(in: &self.anyCancellables)

        self.passwordInputView.$text.map({ !($0?.isEmpty ?? true) })
            .sink(receiveValue: { [weak self] flag in
                self?.nextStepButton.isEnabled = flag
            }).store(in: &self.anyCancellables)
    }

    func presentWranningAlert() {
        let vc = ReoqooAlertViewController(alertTitle: .string(String.localization.localized("AA0328", note: "您真的要注销账号吗？")), alertContent: .string(String.localization.localized("AA0329", note: "账号注销后，将清空所有信息和数据")))
        vc.addAction(.init(title: String.localization.localized("AA0059", note: "取消"), style: .custom, color: R.color.text_link_4A68A6(), font: .systemFont(ofSize: 16, weight: .medium)))
        vc.addAction(.init(title: String.localization.localized("AA0058", note: "确定"), style: .custom, color: R.color.button_destructive_FA2A2D(), font: .systemFont(ofSize: 16, weight: .medium), handler: { [weak self] in
            self?.closeAccount()
        }))
        self.present(vc, animated: true)
    }

    // 发送注销请求
    func closeAccount() {
        guard let reasonType = self.flowItem.reason?.rawValue, let password = self.passwordInputView.text else { return }
        let hud = MBProgressHUD.showLoadingHUD_DispatchOnMainThread()
        AccountCenter.shared.closeAccountPublisher(password: password, reasonType: reasonType, reasonDesc: nil)
            .sink(receiveCompletion: { 
                hud.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { [weak self] _ in
                let vc = CloseAccountSucceedViewController.init()
                let nav = BaseNavigationController.init(rootViewController: vc)
                nav.modalPresentationStyle = .fullScreen
                self?.present(nav, animated: true)
            }).store(in: &self.anyCancellables)
    }
}
