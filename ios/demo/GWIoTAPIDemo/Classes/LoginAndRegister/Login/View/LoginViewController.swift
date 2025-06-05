//
//  LoginViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 18/7/2023.
//

import UIKit
import IVAccountMgr
import RQCore
import RQCoreUI
import RQApi
import RQWebServices

class LoginViewController: BaseViewController, ScrollBaseViewAndKeyboardMatchable {

    var scrollable: UIScrollView { self.scrollView }
    var anyCancellables: Set<AnyCancellable> = []
    
    @IBOutlet weak var helloLabel: UILabel?
    lazy var tapOnHelloLabel: UITapGestureRecognizer = .init().then {
        $0.numberOfTapsRequired = 5
    }

    @IBOutlet weak var scrollView: UIScrollView!
    /// 输入框部分的容器底部 到 其他登录方式label 的顶部的距离约束
    @IBOutlet weak var topContainerBottom_to_otherLoginWayLabelTop_constraint: NSLayoutConstraint!
    /// "同意协议..." 容器
    @IBOutlet weak var bottomContainer: UIView!
    // "我已同意协议..."
    @IBOutlet weak var agreementView: AgreementView!
    @IBOutlet weak var accountInputView: AccountInputView!
    @IBOutlet weak var passwordInputView: PasswordInputView!
    
    @IBOutlet weak var loginBtn: UIButton!
    @IBOutlet weak var forgotPasswordBtn: UIButton!
    @IBOutlet weak var registerBtn: UIButton!

    // regionSelectionBarButtonItem 的 custom view
    lazy var regionSelectionButton: UIButton = .init(type: .system).then {
        $0.setTitleColor(R.color.text_000000()!, for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16)
    }

    lazy var regionSelectionBarButtonItem: UIBarButtonItem = .init(customView: self.regionSelectionButton)

    override func viewSafeAreaInsetsDidChange() {
        super.viewSafeAreaInsetsDidChange()
        // 为了使 "其他登录方式" 及其以下的视图在大屏幕设备上仍然保持在最底部且不超出屏幕.
        // 将 bottomContainer 的 frame 转换为相对于 self.view 的 frame
        let bottomContainerFrameOnView = self.view.convert(self.bottomContainer.bounds, from: self.bottomContainer)
        var diff = self.view.bounds.maxY - self.view.safeAreaInsets.top - self.view.safeAreaInsets.bottom - bottomContainerFrameOnView.maxY
        diff = max(0, diff)
        self.topContainerBottom_to_otherLoginWayLabelTop_constraint.constant = diff
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        // ScrollBaseViewAndKeyboardMatchable
        self.dismissKeyboardWhenTapOnNonInteractiveArea()
        self.adjustScrollViewContentInsetWhenKeyboardFrameChanged()

        self.navigationItem.rightBarButtonItem = self.regionSelectionBarButtonItem

        self.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()

        self.helloLabel?.text = String.localization.localized("AA0001", note: "Hello , reoqoo ！")
        self.registerBtn.setTitle(String.localization.localized("AA0006", note: "快速注册"), for: .normal)
        self.forgotPasswordBtn.setTitle(String.localization.localized("AA0005", note: "忘记密码？"), for: .normal)

        self.loginBtn.layer.cornerRadius = 23
        self.loginBtn.layer.masksToBounds = true
        self.loginBtn.setStyle_0()
        self.loginBtn.setTitle(String.localization.localized("AA0004", note: "登录"), for: .normal)

        self.forgotPasswordBtn.setTitleColor(R.color.text_000000_60()!, for: .normal)
        self.registerBtn.setTitleColor(R.color.text_000000_60()!, for: .normal)

        self.helloLabel?.isUserInteractionEnabled = true
        self.helloLabel?.addGestureRecognizer(self.tapOnHelloLabel)

        // regionSelectionBarButtonItem.title 绑定 当前选择的地区
        RegionInfoProvider.shared.$selectedRegion.map({ $0.countryName })
            .sink(receiveValue: { [weak self] title in
                self?.regionSelectionButton.setTitle(title, for: .normal)
            }).store(in: &self.anyCancellables)

        RegionInfoProvider.shared.$selectedRegion
            .sink(receiveValue: { [weak self] regionInfo in
                self?.accountInputView.regionInfo = regionInfo
            }).store(in: &self.anyCancellables)

        // 地区选择按钮点击
        self.regionSelectionButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                let vc = RegionSelectionViewController.init()
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)

        // 监听 regionSelectionButton.titleLabel.text 发生改变
        self.regionSelectionButton.publisher(for: \.titleLabel).compactMap({ $0 }).flatMap({ $0.publisher(for: \.text) }).delay(for: 0.1, scheduler: DispatchQueue.main)
            .sink(receiveValue: { [weak self] _ in
                self?.regionSelectionButton.sizeToFit()
                // 如果不刷新 navigationBar 布局, 会出现 button 被长文案拉长后切换到短文案的显示异常问题
                self?.navigationController?.navigationBar.layoutIfNeeded()
            }).store(in: &self.anyCancellables)

        // 监听 AggrementView URL 点击
        self.agreementView.linkDidTapObservable
            .sink(receiveValue: { [weak self] url in
                self?.navigationController?.pushViewController(WebViewController.init(url: url), animated: true)
            }).store(in: &self.anyCancellables)

        // 登录按钮点击
        self.loginBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.tryLogin()
        }).store(in: &self.anyCancellables)

        // 注册按钮点击
        self.registerBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            let vc = RegionConfirmationViewController.init()
            self?.navigationController?.pushViewController(vc, animated: true)
        }).store(in: &self.anyCancellables)

        // 忘记密码按钮点击
        self.forgotPasswordBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            let vc = FindPasswordViewController.init()
            self?.navigationController?.pushViewController(vc, animated: true)
        }).store(in: &self.anyCancellables)

        // 没有内容时不让点击
//        Observable.combineLatest(self.accountInputView.$text, self.passwordInputView.$text).map({
//            ($0?.isEmpty ?? true) || ($1?.isEmpty ?? true)
//        }).bind { [weak self] isDisabled in
//            self?.loginBtn.isEnabled = !isDisabled
//        }.disposed(by: self.disposeBag)

        self.tapOnHelloLabel.tapPublisher.sink { [weak self] in
            guard case .ended = $0.state else { return }
            // 判断是否需要进入反馈页
            guard self?.accountInputView.text?.lowercased() == "reoqoo" && self?.passwordInputView.text?.lowercased() == "reoqoo" else { return }
            let vc = IssueFeedbackViewController.fromStoryboard()
            vc.isUserLogin = false
            self?.navigationController?.pushViewController(vc, animated: true)
        }.store(in: &self.anyCancellables)
    }

}

// MARK: Helper
extension LoginViewController {
    func showAgreementAlert() {
        ReoqooAlertViewController.showUsageAgreementAlert(withPresentedViewController: self, agreeClickHandler: { [weak self] in
            // 点击了同意按钮
            self?.agreementView.isAgree = true
            self?.tryLogin()
        }, urlClickHandler: { [weak self] url in
            // 用户点击了 协议链接, 打开网页
            let webViewController = WebViewController(url: url)
            let nav = BaseNavigationController.init(rootViewController: webViewController)
            self?.presentedViewController?.present(nav, animated: true)
        })
    }

    func tryLogin() {

        self.view.endEditing(true)

        guard let account = self.accountInputView.textField.text, let password = self.passwordInputView.textField.text else {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0562", note: "请输入正确的手机号或邮箱"))
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
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0013", note: "账号或密码错误"))
            return
        }

        // 未同意协议
        if !self.agreementView.isAgree {
            self.showAgreementAlert()
            return
        }

        // 发起请求
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        self.loginBtn.isEnabled = false
        AccountCenter.shared.loginRequestPublisher(accountType: accountType, password: password)
            .sink(receiveCompletion: { [weak self] in
                loadingHUD.hideDispatchOnMainThread()
                self?.loginBtn.isEnabled = true
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: { _ in

            }).store(in: &self.anyCancellables)
    }
}
