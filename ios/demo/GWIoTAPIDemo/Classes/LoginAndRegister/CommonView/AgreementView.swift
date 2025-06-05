//
//  IVAgreementView.swift
//  Yoosee
//
//  Created by hongxiaobin on 2022/7/20.
//  Copyright © 2022 Gwell. All rights reserved.
//

import UIKit

/// [checkBox] + "我已阅读...." View
/// size 自适应, 多行适配
class AgreementView: UIView {

    public let linkDidTapObservable: Combine.PassthroughSubject<URL, Never> = .init()

    public var isAgree: Bool {
        set {
            self.checkBoxButton.isSelected = newValue
        }
        get {
            return self.checkBoxButton.isSelected
        }
    }

    private(set) lazy var textView: UITextView = {
        let res = UITextView.init()
        res.delegate = self
        res.isScrollEnabled = false
        res.isEditable = false
        res.backgroundColor = .clear
        res.linkTextAttributes = [.foregroundColor: R.color.text_link_4A68A6()!]
        return res
    }()

    private lazy var checkBoxButton: UIButton = {
        let res = UIButton.init(type: .custom)
        res.setImage(R.image.commonCheckbox_0Deselect(), for: .normal)
        res.setImage(R.image.commonCheckbox_0Selected(), for: .selected)
        res.setContentHuggingPriority(.init(999), for: .horizontal)
        return res
    }()

    private var anyCancellables: Set<AnyCancellable> = []

    // MARK: Lifecycle
    override init(frame: CGRect) {
        super.init(frame: .zero)
        self.setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    private func setup() {


        self.addSubview(self.checkBoxButton)
        self.checkBoxButton.snp.makeConstraints { make in
            make.leading.top.bottom.equalToSuperview()
        }

        self.addSubview(self.textView)
        self.textView.snp.makeConstraints { make in
            make.top.bottom.trailing.equalToSuperview()
            make.leading.equalTo(self.checkBoxButton.snp.trailing)
        }

        // 监听 textView.text 改变
        self.textView.textPublisher.sink(receiveValue: { [weak self] text in
            self?.layoutIfNeeded()
        }).store(in: &self.anyCancellables)

        self.checkBoxButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                self?.checkBoxButton.isSelected.toggle()
            }).store(in: &self.anyCancellables)

        self.textView.attributedText = ReoqooAlertViewController.agreementAttributedContent
    }

    override var intrinsicContentSize: CGSize {
        var res: CGSize = .zero
        let textMaxSize: CGSize = .init(width: UIScreen.main.bounds.width - 64, height: CGFloat.greatestFiniteMagnitude)
        let textSize = self.textView.attributedText.string.sizeWithFont(.systemFont(ofSize: 12), maxSize: textMaxSize)
        res.height = max(self.checkBoxButton.bounds.height, textSize.height)
        res.width = textSize.width + self.checkBoxButton.width
        return res
    }

}

// MARK: UITextViewDelegate
extension AgreementView: UITextViewDelegate {
    func textView(_: UITextView, shouldInteractWith url: URL, in _: NSRange, interaction _: UITextItemInteraction) -> Bool {
        self.linkDidTapObservable.send(url)
        return false
    }
}
