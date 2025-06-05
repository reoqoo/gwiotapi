//
//  PasswordInputView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 26/7/2023.
//

import UIKit

class PasswordInputView: UIView {

    @DidSetPublished var isEditing = false

    @DidSetPublished var text: String?

    private(set) lazy var textField: UITextField = .init().then {
        $0.textContentType = .password
        $0.keyboardType = .asciiCapable
        $0.returnKeyType = .done
        $0.clearButtonMode = .whileEditing
        $0.isSecureTextEntry = true
        $0.delegate = self
        $0.textColor = R.color.text_000000_90()!
        $0.font = .systemFont(ofSize: 16)
        $0.placeholder = String.localization.localized("AA0003", note: "密码")
    }

    private(set) lazy var hidePasswordBtn: UIButton = {
        let res = UIButton.init(type: .custom)
        res.setContentHuggingPriority(.init(999), for: .horizontal)
        res.setImage(R.image.commonIsHidePasswordTrue(), for: .normal)
        res.setImage(R.image.commonIsHidePasswordFalse(), for: .selected)
        res.contentEdgeInsets = .init(top: 8, left: 16, bottom: 8, right: 24)
        return res
    }()

    /// 底部线条. 进入输入状态后高亮
    private(set) lazy var bottomLine: UIView = {
        let res = UIView.init()
        res.backgroundColor = R.color.lineInputDisable()!
        return res
    }()

    private var anyCancellables: Set<AnyCancellable> = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    func setup() {
        self.addSubview(self.textField)
        self.textField.setContentCompressionResistancePriority(.init(249), for: .horizontal)
        self.textField.setContentHuggingPriority(.init(249), for: .horizontal)
        self.textField.snp.makeConstraints { make in
            make.top.bottom.leading.equalToSuperview()
        }

        self.addSubview(self.hidePasswordBtn)
        self.hidePasswordBtn.setContentCompressionResistancePriority(.init(999), for: .horizontal)
        self.hidePasswordBtn.setContentHuggingPriority(.init(999), for: .horizontal)
        self.hidePasswordBtn.snp.makeConstraints { make in
            make.leading.equalTo(self.textField.snp.trailing).offset(8)
            make.centerY.equalToSuperview()
            make.trailing.equalToSuperview()
        }

        self.addSubview(self.bottomLine)
        self.bottomLine.snp.makeConstraints { make in
            make.leading.trailing.bottom.equalToSuperview()
            make.height.equalTo(0.5)
        }

        self.hidePasswordBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.hidePasswordBtn.isSelected.toggle()
        }).store(in: &self.anyCancellables)

        // 使 self.textField.isSecureTextEntry 监听 self.hidePasswordBtn.isSelected
        self.hidePasswordBtn.publisher(for: \.isSelected).map({ !$0 })
            .sink(receiveValue: { [weak self] flag in
                self?.textField.isSecureTextEntry = flag
            }).store(in: &self.anyCancellables)

        // 监听 textfield 被 setText 的情况
        self.textField.publisher(for: \.text).sink(receiveValue: { [weak self] str in
            self?.text = str
        }).store(in: &self.anyCancellables)
    }
}

extension PasswordInputView: UITextFieldDelegate {
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        self.bottomLine.backgroundColor = R.color.lineInputEnable()
        self.bottomLine.snp.updateConstraints { make in
            make.height.equalTo(1)
        }
        self.isEditing = true
        return true
    }

    func textFieldShouldEndEditing(_ textField: UITextField) -> Bool {
        self.bottomLine.backgroundColor = R.color.lineInputDisable()
        self.bottomLine.snp.updateConstraints { make in
            make.height.equalTo(0.5)
        }
        self.isEditing = false
        return true
    }

    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        self.text = (textField.text as? NSString)?.replacingCharacters(in: range, with: string)
        return true
    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.endEditing(true)
        return true
    }

    func textFieldShouldClear(_ textField: UITextField) -> Bool {
        self.text = nil
        return true
    }
}
