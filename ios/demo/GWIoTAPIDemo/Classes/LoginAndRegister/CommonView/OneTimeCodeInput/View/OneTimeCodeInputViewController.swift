//
//  VerificationCodeInputViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 27/7/2023.
//

import UIKit
import RQApi
import RQCore

extension OneTimeCodeInputViewController {
    enum For {
        case register
        case findPassword
        case binding
    }

    enum Status {
        case idle
        // 要求外部监听者(通常是上层ViewController), 重发验证码
        case resendVerifyCode(accountType: RQApi.AccountType)
    }
}

class OneTimeCodeInputViewController: BaseViewController {

    // 功能目的
    let `for`: For
    // 验证码长度, 此值决定了输入格的数量, 可输入的长度
    var lengthOfCode: Int

    // 供外部监听结果
    var resultPublisher: Combine.PassthroughSubject<Result<String, Swift.Error>, Never> = .init()

    // 供外部监听状态
    @DidSetPublished var status: Status = .idle

    // 内部监听以影响 self.introductionLabel 的显示
    @DidSetPublished private var accountType: RQApi.AccountType

    init(for: For, accountType: RQApi.AccountType, lengthOfCode: Int = 6) {
        self.`for` = `for`
        self.accountType = accountType
        self.lengthOfCode = lengthOfCode
        super.init(nibName: nil, bundle: nil)
    }

    private override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) { fatalError("init(nibName nibNameOrNil:, bundle nibBundleOrNil:) has not been implemented") }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    private lazy var titleLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.text = String.localization.localized("AA0034", note: "请输入验证码")
        $0.numberOfLines = 0
    }

    private lazy var introductionLabel: UILabel = .init().then {
        $0.textColor = R.color.text_000000_60()!
        $0.font = .systemFont(ofSize: 14)
        $0.numberOfLines = 0
    }

    private lazy var tapOnCodeStackView: UITapGestureRecognizer = .init()

    private lazy var codeStackView: UIStackView = .init().then {
        $0.axis = .horizontal
        $0.spacing = 12
        $0.distribution = .equalSpacing
        $0.alignment = .center
        $0.addGestureRecognizer(self.tapOnCodeStackView)
    }

    private lazy var sendVerifyCodeBtn: UIButton = .init(type: .custom).then {
        $0.setLinkTextStyle()
        $0.setTitle(String.localization.localized("AA0036", note: "重新发送"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 14)
    }

    /// 用于接收验证码输入的操作
    /// 作为第一响应者
    /// 隐藏
    /// 当输入事件发生时影响 codeStackView 的内容改变
    private lazy var codeTextField: UITextField = .init().then {
        $0.isHidden = true
        $0.delegate = self
        $0.textContentType = .oneTimeCode
    }

    private lazy var listOfCodeView: [OneTimeCodeInputViewController.CodeView] = {
        var res: [OneTimeCodeInputViewController.CodeView] = []
        for _ in 1...self.lengthOfCode {
            let codeView = OneTimeCodeInputViewController.CodeView.init(frame: .zero)
            res.append(codeView)
        }
        return res
    }()

    private var anyCancellables: Set<AnyCancellable> = []

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.codeTextField.becomeFirstResponder()
    }

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

        self.view.addSubview(self.codeStackView)
        self.codeStackView.snp.makeConstraints { make in
            make.top.equalTo(self.introductionLabel.snp.bottom).offset(38)
            make.leading.greaterThanOrEqualToSuperview().offset(28)
            make.trailing.lessThanOrEqualToSuperview().offset(-28)
            make.centerX.equalToSuperview()
        }

        self.listOfCodeView.forEach {
            self.codeStackView.addArrangedSubview($0)
            $0.snp.makeConstraints { make in
                make.height.equalTo(52)
                make.width.equalTo(42)
            }
        }

        self.view.addSubview(self.sendVerifyCodeBtn)
        self.sendVerifyCodeBtn.snp.makeConstraints { make in
            make.top.equalTo(self.codeStackView.snp.bottom).offset(8)
            make.centerX.equalToSuperview()
        }

        self.view.addSubview(self.codeTextField)
        self.codeTextField.snp.makeConstraints { make in
            make.bottom.equalToSuperview()
            make.centerX.equalToSuperview()
        }

        // 点击了 CodeStackView
        self.tapOnCodeStackView.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.codeTextField.becomeFirstResponder()
        }).store(in: &self.anyCancellables)

        // 将 accountType.account 绑定到 introductionLabel
        self.$accountType
            .map({ String.localization.localized("AA0035", note: "验证码已发送⾄") + " " + $0.account })
            .sink(receiveValue: { [weak self] text in
                self?.introductionLabel.text = text
            }).store(in: &self.anyCancellables)

        // "重新发送" 按钮点击
        self.sendVerifyCodeBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            // 开启倒数
            self?.startCountDown()
            // 要求外部重新发送验证码
            guard let accountType = self?.accountType else { return }
            self?.status = .resendVerifyCode(accountType: accountType)
        }).store(in: &self.anyCancellables)

        // 开启倒数
        self.startCountDown()
    }

}

// MARK: Helper
extension OneTimeCodeInputViewController {
    // 开启倒数
    func startCountDown() {
        self.sendVerifyCodeBtn.isEnabled = false
        var countDownSeconds: Double = 60
        Timer.countdownPublisher(countDownSeconds, every: 1)
            .sink { [weak self] _ in
                self?.sendVerifyCodeBtn.setTitle(String.localization.localized("AA0036", note: "重新发送"), for: .normal)
                self?.sendVerifyCodeBtn.isEnabled = true
            } receiveValue: { [weak self] i in
                countDownSeconds -= 1
                let title = String.localization.localized("AA0036", note: "重新发送") + "(\(Int(countDownSeconds)))"
                self?.sendVerifyCodeBtn.setTitle(title, for: .normal)
            }.store(in: &self.anyCancellables)
    }
}

// MARK: Request
extension OneTimeCodeInputViewController {

    // 发起验证验证码请求
    func verifyOneTimeCode(code: String) {
        self.codeTextField.endEditing(true)
        let loadingHUD = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        AccountCenter.shared.verifyOneTimeCodeRequestPublisher(accountType: self.accountType, code: code)
            .sink { [weak self] in
                loadingHUD.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
                // 请求出错了, 置空验证码输入框
                self?.codeTextField.text = ""
                self?.codeTextField.becomeFirstResponder()
            } receiveValue: { [weak self] in
                self?.resultPublisher.send(.success(code))
            }.store(in: &self.anyCancellables)
    }
}

extension OneTimeCodeInputViewController: UITextFieldDelegate {

    // self.codeTextFeild 的输入内容 映射 self.listOfCodeView 中显示的内容
    func resetListOfCodeViewsContent(content: String) {
        // 重置所有 codeView 的输入内容 及 isActive 状态
        self.listOfCodeView.forEach {
            $0.isActive = false
            $0.character = nil
        }
        // 赋值 codeViews 的 label 内容
        content.enumerated().forEach { (idx, char) in
            self.listOfCodeView[safe_: idx]?.character = char
        }
    }

    func textFieldDidBeginEditing(_ textField: UITextField) {
        let targetIdx = textField.text?.count ?? 0
        // 重置内容
        self.resetListOfCodeViewsContent(content: textField.text ?? "")
        // 使空置位光标显示
        self.listOfCodeView[safe_: targetIdx]?.isActive = true
    }

    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        // 修改后的结果
        let result = ((textField.text ?? "") as NSString).replacingCharacters(in: range, with: string)
        // 重置内容
        self.resetListOfCodeViewsContent(content: result)
        // 当 result 长度等于限制, 发起请求
        if result.count == self.lengthOfCode {
            // 可以发起验证码请求了
            self.verifyOneTimeCode(code: result)
        }
        // 当 result 长度超出限制, 输入无效
        if result.count > self.lengthOfCode {
            return false
        }
        // 让下一个 codeView active
        let nextCodeView = self.listOfCodeView[safe_: result.count]
        nextCodeView?.isActive = true
        return true
    }
}
