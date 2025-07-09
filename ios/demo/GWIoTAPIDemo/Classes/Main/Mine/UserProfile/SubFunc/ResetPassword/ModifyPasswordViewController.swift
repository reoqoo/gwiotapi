//
//  ModifyPasswordViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit
import RQCore

class ModifyPasswordViewController: BaseViewController, ScrollBaseViewAndKeyboardMatchable {

    var scrollable: UIScrollView { self.scrollView }

    var anyCancellables: Set<AnyCancellable> = []

    lazy var scrollView: UIScrollView = .init().then {
        $0.showsVerticalScrollIndicator = true
        $0.alwaysBounceVertical = true
        $0.keyboardDismissMode = .interactive
    }

    lazy var oldPasswordInputTextView: PasswordInputView = .init().then {
        $0.textField.placeholder = String.localization.localized("AA0291", note: "旧密码")
    }

    lazy var newPasswordInputTextView: PasswordInputView = .init().then {
        $0.textField.placeholder = String.localization.localized("AA0292", note: "新密码")
    }

    lazy var confirmPasswordInputTextView: PasswordInputView = .init().then {
        $0.textField.placeholder = String.localization.localized("AA0293", note: "确认新密码")
    }

    lazy var tipLabel: UILabel = .init().then {
        $0.numberOfLines = 0
        $0.font = .systemFont(ofSize: 12)
        $0.textColor = R.color.text_000000_60()
        $0.text = String.localization.localized("AA0025", note: "密码为8～30位包含字⺟、数字的字符")
    }

    lazy var sureBtn: UIButton = .init(type: .custom).then {
        $0.setTitle(String.localization.localized("AA0058", note: "确定"), for: .normal)
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0278", note: "修改密码")

        self.dismissKeyboardWhenTapOnNonInteractiveArea()
        self.adjustScrollViewContentInsetWhenKeyboardFrameChanged()

        self.view.backgroundColor = R.color.background_FFFFFF_white()
        self.rq.setNavigationBarBackground(R.color.background_FFFFFF_white()!, tintColor: R.color.text_000000_90()!)

        self.view.addSubview(self.scrollView)
        self.scrollView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.scrollView.addSubview(self.oldPasswordInputTextView)
        self.oldPasswordInputTextView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(48)
            make.trailing.equalToSuperview().offset(-28)
            make.leading.equalToSuperview().offset(28)
            make.height.equalTo(56)
        }

        self.scrollView.addSubview(self.newPasswordInputTextView)
        self.newPasswordInputTextView.snp.makeConstraints { make in
            make.top.equalTo(self.oldPasswordInputTextView.snp.bottom).offset(12)
            make.trailing.equalToSuperview().offset(-28)
            make.leading.equalToSuperview().offset(28)
            make.height.equalTo(56)
        }

        self.scrollView.addSubview(self.confirmPasswordInputTextView)
        self.confirmPasswordInputTextView.snp.makeConstraints { make in
            make.top.equalTo(self.newPasswordInputTextView.snp.bottom).offset(12)
            make.trailing.equalToSuperview().offset(-28)
            make.leading.equalToSuperview().offset(28)
            make.height.equalTo(56)
        }

        self.scrollView.addSubview(self.tipLabel)
        self.tipLabel.snp.makeConstraints { make in
            make.top.equalTo(self.confirmPasswordInputTextView.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.scrollView.addSubview(self.sureBtn)
        self.sureBtn.snp.makeConstraints { make in
            make.top.equalTo(self.tipLabel.snp.bottom).offset(56)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(46)
            make.width.equalTo(self.view.snp.width).offset(-56)
            make.bottom.equalToSuperview().offset(-16)
        }

        Publishers.CombineLatest3(self.oldPasswordInputTextView.$text, self.newPasswordInputTextView.$text, self.confirmPasswordInputTextView.$text).map { old, new, confirm in
            if (old?.isEmpty ?? true) || (new?.isEmpty ?? true) || (confirm?.isEmpty ?? true) { return false }
            return true
        }.sink { [weak self] flag in
            self?.sureBtn.isEnabled = flag
        }.store(in: &self.anyCancellables)

        self.sureBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.modifyPassword()
        }).store(in: &self.anyCancellables)
    }

    func modifyPassword() {
        let hud = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        self.modifyPasswordPublisher(old: self.oldPasswordInputTextView.text, new: self.newPasswordInputTextView.text, confirm: self.confirmPasswordInputTextView.text)
            .sink(receiveCompletion: {
                hud.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { _ in
                AccountCenter.shared.logoutCurrentUser()
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0295", note: "密码修改成功，请重新登录"))
            }).store(in: &self.anyCancellables)
    }

    func modifyPasswordPublisher(old: String?, new: String?, confirm: String?) -> AnyPublisher<RQCore.ProfileInfo, Swift.Error> {
        // 两次输入不一致
        if new != confirm {
            return Fail(error: (ReoqooError.accountError(reason: .confirmPasswordError))).eraseToAnyPublisher()
        }

        guard let new = new else {
            return Fail(error: (ReoqooError.accountError(reason: .passwordFormatError))).eraseToAnyPublisher()
        }

        // 密码必须为 数字 + 字母 组合
        if !new.isValidPassword {
            return Fail(error: (ReoqooError.accountError(reason: .passwordFormatError))).eraseToAnyPublisher()
        }

        guard let modifyUserInfoObservable = AccountCenter.shared.currentUser?.modifyUserInfoPublisher(header: nil, nick: nil, oldPassword: old, newPassword: new) else {
            return Fail(error: (ReoqooError.generalError(reason: .optionalTypeUnwrapped))).eraseToAnyPublisher()
        }
        // 修改密码后, 调登出接口
        return modifyUserInfoObservable
    }

}
