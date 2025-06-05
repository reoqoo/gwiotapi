//
//  RQSDKDemoAlertViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 21/8/2023.
//

import Foundation

extension ReoqooAlertViewController {

    typealias ActionHandler = () -> ()

    enum Content {
        case none
        case string(_ content: String)
        case attributedString(_ content: NSAttributedString)
    }
}

/// 对 IVPopupView 进行封装, 提供文本编辑功能
class ReoqooAlertViewController: BaseViewController {

    private(set) lazy var alertView: IVPopupView = .init(property: self.property, actions: [])
    private(set) lazy var property: IVPopupViewProperty = .init().then {
        $0.messageAlign = .left
    }

    private let alertTitle: ReoqooAlertViewController.Content
    private let alertContent: ReoqooAlertViewController.Content
    
    // attribute text 链接被点击回调
    var attributeTextLinkOnClickHandler: ((URL)->())?
    
    /// title 字体
    var titleFont: UIFont = .systemFont(ofSize: 18, weight: .medium) {
        didSet {
            self.alertView.titleLabel.font = self.titleFont
        }
    }

    /// content 字体
    var messageFont: UIFont = .systemFont(ofSize: 16) {
        didSet {
            self.messageTextView.font = self.messageFont
        }
    }

    lazy var messageTextView: UITextView = .init().then {
        $0.delegate = self
        $0.isScrollEnabled = false
        $0.isEditable = false
        $0.font = self.messageFont
        $0.textColor = R.color.text_000000_90()
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.linkTextAttributes = [.foregroundColor: R.color.text_link_4A68A6()!]
    }

    init(alertTitle: ReoqooAlertViewController.Content = .none, alertContent: ReoqooAlertViewController.Content = .none, input: [String] = [], inputPlaceholder: [String] = [], inputLimit: Int = 24, inputLimitText: String = "", actions: [IVPopupAction] = [], attributeTextLinkOnClickHandler: ((URL)->())? = nil) {
        self.alertTitle = alertTitle
        self.alertContent = alertContent
        self.attributeTextLinkOnClickHandler = attributeTextLinkOnClickHandler
        super.init(nibName: nil, bundle: nil)
        self.modalTransitionStyle = .crossDissolve
        self.modalPresentationStyle = .custom

        self.property.cornerRadius = 16
        self.property.separatorStyle = .middle
        self.property.separatorColor = R.color.text_000000_10()!

        self.property.inputText = input
        self.property.inputPlaceholder = inputPlaceholder
        self.property.inputLimits = [inputLimit]
        self.property.inputLimitBlock = { [weak self] index in
            if !inputLimitText.isEmpty {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: inputLimitText)
            }
        }
        self.alertView.addActions(actions)
        ToastManager.shared.position = .center //解决拉起键盘时toast被遮挡问题

        if !(self.property.inputText?.isEmpty ?? true) {
            // 如果是输入型 alert, title 靠左
            self.alertView.titleLabel.textAlignment = .left
            self.property.position = .center
        }else{
            self.alertView.titleLabel.textAlignment = .center
            self.property.position = .bottom
        }

        // title 设置
        if case let .attributedString(title) = self.alertTitle {
            self.property.title = title.string
        }

        if case let .string(title) = self.alertTitle {
            self.property.title = title
        }

        // message 设置
        if case let .string(message) = self.alertContent {
            self.property.message = message
        }

        // 如果 message 是 attributed string, 将展示的容器更改为 customView
        if case let .attributedString(message) = self.alertContent {
            self.property.message = ""
            self.property.customView = self.messageTextView
            self.messageTextView.attributedText = message
        }
        
        self.alertView.titleLabel.textAlignment = .left
    }

    deinit {
        ToastManager.shared.position = .bottom //回归旧的显示位置
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        UIView.animate(withDuration: 0.3) {
            self.view.backgroundColor = R.color.background_000000_40()!
            self.alertView.contentView.transform = .identity
        }
        self.alertView.inputFields.first?.becomeFirstResponder()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIView.animate(withDuration: 0.3) {
            self.view.backgroundColor = .clear
            self.alertView.contentView.transform = .init(translationX: 0, y: self.view.height * 0.5)
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.backgroundColor = .clear

        // 在 show 方法调用前先将 Handler 保存起来, 以便 show 方法调用完毕后更换. 否则 show 方法会往 handler 里加入我不需要的 dismiss 动画
        let originalHandlers = self.alertView.actions.map({ $0.handler })

        self.alertView.show(in: self.view)

        // show 方法调用完了, 替换 handlers
        for (idx, act) in self.alertView.actions.enumerated() {
            guard let original = originalHandlers[safe_: idx] else { return }
            let autoDismiss = act.autoDismissAfterHandling
            act.handler = { [weak self] in
                original?()
                if autoDismiss {
                    self?.dismiss(animated: true)
                }
            }
        }

        self.alertView.backgroundColor = .clear
        self.alertView.contentView.transform = .init(translationX: 0, y: self.view.bounds.height * 0.5)
        // 如果 self.messageTextView.attributedText 非空, 设置 messageTextView 的高度
        if let attributedText = self.messageTextView.attributedText {
            let textSize = attributedText.boundingRect(with: .init(width: self.view.bounds.width - 72, height: CGFloat.greatestFiniteMagnitude), options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
            var height = textSize.height + 20
            // 如果内容过多, 调整 textView 高度, 调整可滚动
            if UIScreen.main.bounds.height * 0.5 < height {
                height = UIScreen.main.bounds.height * 0.5
                self.messageTextView.isScrollEnabled = true
            }
            self.messageTextView.snp.remakeConstraints({ make in
                make.height.equalTo(height)
            })
        }
    }

    func addAction(_ action: IVPopupAction) {
        self.alertView.addAction(action)
    }

    func addActions(_ actions: [IVPopupAction]) {
        self.alertView.addActions(actions)
    }

    var textFieldContents: [String?] {
        self.alertView.inputFields.map({ $0.text })
    }
}

extension ReoqooAlertViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange, interaction: UITextItemInteraction) -> Bool {
        self.attributeTextLinkOnClickHandler?(URL)
        return false
    }
}
