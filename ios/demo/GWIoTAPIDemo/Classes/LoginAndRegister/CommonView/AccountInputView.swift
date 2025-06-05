//
//  AccountInputView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 26/7/2023.
//

import UIKit
import RQCore

class AccountInputView: UIView {

    @DidSetPublished var text: String?
    @DidSetPublished var regionInfo: RegionInfo?

    private(set) lazy var regionLabelContainer: UIView = .init().then {
        $0.backgroundColor = .clear
    }

    private(set) lazy var regionLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 14)
        $0.textColor = R.color.text_000000_90()
    }

    private(set) lazy var textField: UITextField = .init().then {
        $0.textContentType = .username
        $0.keyboardType = .asciiCapable
        $0.returnKeyType = .done
        $0.clearButtonMode = .whileEditing
        $0.font = .systemFont(ofSize: 16)
        $0.textColor = R.color.text_000000_90()!
        $0.delegate = self
        $0.placeholder = String.localization.localized("AA0002", note: "邮箱/⼿机号")
        $0.setContentHuggingPriority(.init(249), for: .horizontal)
        $0.setContentCompressionResistancePriority(.init(249), for: .horizontal)
    }

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

    private func setup() {

        self.addSubview(self.textField)
        self.textField.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.addSubview(self.bottomLine)
        self.bottomLine.snp.makeConstraints { make in
            make.leading.trailing.bottom.equalToSuperview()
            make.height.equalTo(0.5)
        }

        // 监听 textfield 被 setText 的情况
        self.textField.publisher(for: \.text).sink(receiveValue: { [weak self] str in
            self?.text = str
        }).store(in: &self.anyCancellables)

        /// 监听 regionInfo 变化
        self.$regionInfo.map({ $0?.countryCode ?? "" })
            .sink(receiveValue: {[weak self] code in
                if code.isEmpty {
                    self?.removeRegionCodeLabel()
                    return
                }
                self?.insertRegionCodeLabel()
                self?.regionLabel.text = "+" + code
            }).store(in: &self.anyCancellables)
    }
    
    /// 插入区号 label
    func insertRegionCodeLabel() {
        if self.regionLabelContainer.superview == self { return }

        self.addSubview(self.regionLabelContainer)
        self.regionLabelContainer.snp.makeConstraints { make in
            make.leading.top.bottom.equalToSuperview()
        }

        let separator = UIView.init()
        separator.backgroundColor = R.color.lineInputDisable()
        self.regionLabelContainer.addSubview(separator)
        separator.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.height.equalTo(20)
            make.width.equalTo(0.5)
            make.trailing.equalToSuperview()
        }

        self.regionLabelContainer.addSubview(self.regionLabel)
        self.regionLabel.snp.makeConstraints { make in
            make.leading.top.bottom.equalToSuperview()
            make.trailing.equalTo(separator.snp.leading).offset(-16)
        }

        self.textField.snp.remakeConstraints { make in
            make.trailing.top.bottom.equalToSuperview()
            make.leading.equalTo(self.regionLabelContainer.snp.trailing).offset(16)
        }
    }

    /// 移除区号 label
    func removeRegionCodeLabel() {
        self.regionLabelContainer.removeFromSuperview()
        self.textField.snp.remakeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
}

extension AccountInputView: UITextFieldDelegate {
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        self.bottomLine.backgroundColor = R.color.lineInputEnable()
        self.bottomLine.snp.updateConstraints { make in
            make.height.equalTo(1)
        }
        return true
    }

    func textFieldShouldEndEditing(_ textField: UITextField) -> Bool {
        self.bottomLine.backgroundColor = R.color.lineInputDisable()
        self.bottomLine.snp.updateConstraints { make in
            make.height.equalTo(0.5)
        }
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
