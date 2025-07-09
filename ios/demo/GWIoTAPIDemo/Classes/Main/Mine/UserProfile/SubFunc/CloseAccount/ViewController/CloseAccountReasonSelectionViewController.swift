//
//  CloseAccountReasonSelectionViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit
import RQCore
import RQCoreUI

extension CloseAccountReasonSelectionViewController {
    class TableViewCell: UITableViewCell {

        var reason: CloseAccountReasonSelectionViewController.CloseAccountReason? {
            didSet {
                self.textLabel?.text = reason?.description
            }
        }

        lazy var checkBoxImageView: UIImageView = .init().then {
            $0.contentMode = .center
        }

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: .subtitle, reuseIdentifier: reuseIdentifier)

            self.selectionStyle = .none
            
            self.textLabel?.font = .systemFont(ofSize: 16)
            self.textLabel?.textColor = R.color.text_000000_90()

            self.contentView.addSubview(self.checkBoxImageView)
            self.checkBoxImageView.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-16)
                make.centerY.equalToSuperview()
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        override func setSelected(_ selected: Bool, animated: Bool) {
            self.checkBoxImageView.image = selected ? R.image.commonCheckbox_1Selected() : R.image.commonCheckbox_1Deselect()
        }
    }
}

class CloseAccountReasonSelectionViewController: PageSheetStyleViewController {

    // 内部监听
    @DidSetPublished private var selectedReason: CloseAccountReasonSelectionViewController.CloseAccountReason?

    // 外部监听
    @DidSetPublished var flowItem: CloseAccountReasonSelectionViewController.CloseAccountFlowItem?

    private var tableViewDataSources: [CloseAccountReason] = CloseAccountReasonSelectionViewController.CloseAccountReason.allCases

    private lazy var titleLabel: UILabel = .init().then {
        $0.text = String.localization.localized("AA0310", note: "注销原因")
        $0.font = .systemFont(ofSize: 18, weight: .medium)
        $0.textAlignment = .center
    }

    private lazy var tableView: UITableView = .init(frame: .zero, style: .plain).then {
        $0.rowHeight = 56
        $0.delegate = self
        $0.dataSource = self
        $0.showsVerticalScrollIndicator = false
        $0.sectionHeaderHeight = 0.1
        $0.sectionFooterHeight = 0.1
        $0.separatorInset = .init(top: 0, left: 16, bottom: 0, right: 16)
        $0.register(TableViewCell.self, forCellReuseIdentifier: String.init(describing: TableViewCell.self))
    }

    private lazy var suggestionTextViewInputAccessoryView: InputAccessoryView = .init()
    private lazy var suggestionTextViewPlaceholder: UILabel = .init().then {
        $0.text = String.localization.localized("AA0316", note: "其他意见或建议")
        $0.font = .systemFont(ofSize: 14)
        $0.textColor = R.color.text_000000_38()
    }
    private lazy var suggestionTextView: UITextView = .init().then {
        $0.textColor = R.color.text_000000_90()
        $0.font = .systemFont(ofSize: 14)
        $0.inputAccessoryView = self.suggestionTextViewInputAccessoryView
        $0.layer.cornerRadius = 6
        $0.layer.masksToBounds = true
        $0.layer.borderWidth = 1
        $0.layer.borderColor = R.color.background_000000_5()?.cgColor
        $0.delegate = self
    }

    private lazy var btnsContainer: UIStackView = .init().then {
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.axis = .horizontal
        $0.distribution = .fillEqually
    }

    private lazy var cancelBtn: UIButton = .init(type: .system).then {
        $0.setTitle(String.localization.localized("AA0059", note: "取消"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        $0.setTitleColor(R.color.text_link_4A68A6(), for: .normal)
    }

    private lazy var continueBtn: UIButton = .init(type: .system).then {
        $0.setTitle(String.localization.localized("AA0317", note: "继续"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        $0.setTitleColor(R.color.button_destructive_FA2A2D(), for: .normal)
        $0.setTitleColor(R.color.button_destructive_FA2A2D()!.withAlphaComponent(0.38), for: .disabled)
    }

    private var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.contentView.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(12)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(52)
        }

        self.contentView.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(300)
        }

        self.contentView.addSubview(self.suggestionTextView)
        self.suggestionTextView.snp.makeConstraints { make in
            make.top.equalTo(self.tableView.snp.bottom).offset(12)
            make.leading.equalToSuperview().offset(12)
            make.trailing.equalToSuperview().offset(-12)
            make.height.equalTo(60)
        }

        self.contentView.addSubview(self.suggestionTextViewPlaceholder)
        self.suggestionTextViewPlaceholder.snp.makeConstraints { make in
            make.leading.equalTo(self.suggestionTextView).offset(8)
            make.top.equalTo(self.suggestionTextView).offset(8)
        }

        let separator = UIView.init()
        separator.backgroundColor = R.color.background_F2F3F6_thinGray()
        self.contentView.addSubview(separator)
        separator.snp.makeConstraints { make in
            make.top.equalTo(self.suggestionTextView.snp.bottom).offset(4)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(8)
        }

        self.contentView.addSubview(self.btnsContainer)
        self.btnsContainer.snp.makeConstraints { make in
            make.leading.trailing.equalToSuperview()
            make.top.equalTo(separator).offset(12)
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-12)
        }

        self.btnsContainer.addArrangedSubview(self.cancelBtn)
        self.cancelBtn.snp.makeConstraints { make in
            make.height.equalTo(56)
        }

        let midLine = UIView.init()
        midLine.backgroundColor = R.color.background_000000_5()
        self.btnsContainer.addSubview(midLine)
        midLine.snp.makeConstraints { make in
            make.width.equalTo(1)
            make.center.equalToSuperview()
            make.height.equalTo(30)
        }

        self.btnsContainer.addArrangedSubview(self.continueBtn)
        self.continueBtn.snp.makeConstraints { make in
            make.height.equalTo(56)
        }

        // 监听键盘 frame 以避免遮挡 suggestionTextView
        NotificationCenter.default.publisher(for: UIApplication.keyboardWillShowNotification).compactMap({
            return $0.userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? CGRect
        }).sink(receiveValue: { [weak self] keyboardFrame in
            UIView.animate(withDuration: 0.3) {
                let transformY = -keyboardFrame.height
                self?.contentView.transform = .init(translationX: 0, y: transformY)
            }
        }).store(in: &self.anyCancellables)

        NotificationCenter.default.publisher(for: UIApplication.keyboardWillHideNotification)
            .sink(receiveValue: { [weak self] _ in
                UIView.animate(withDuration: 0.3) {
                    self?.contentView.transform = .identity
                }
            }).store(in: &self.anyCancellables)

        self.tableView.publisher(for: \.contentSize).map({ $0.height })
            .sink(receiveValue: { [weak self] height in
                self?.tableView.snp.updateConstraints({ make in
                    make.height.equalTo(height)
                })
            }).store(in: &self.anyCancellables)

        self.suggestionTextViewInputAccessoryView.doneButtonItem.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                self?.view.endEditing(true)
            }).store(in: &self.anyCancellables)

        self.cancelBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.dismiss(animated: true)
        }).store(in: &self.anyCancellables)

        self.continueBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.dismiss(animated: true, completion: {
                self?.flowItem = CloseAccountReasonSelectionViewController.CloseAccountFlowItem.init(reason: self?.selectedReason, otherSuggestion: self?.suggestionTextView.text)
            })
        }).store(in: &self.anyCancellables)

        self.$selectedReason.map({ $0 != nil }).sink(receiveValue: { [weak self] flag in
            self?.continueBtn.isEnabled = flag
        }).store(in: &self.anyCancellables)
    }

    override func layoutContentView() {
        self.contentView.snp.makeConstraints { make in
            make.leading.trailing.equalToSuperview()
            make.bottom.equalToSuperview().offset(16)
        }
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }

}

// MARK: TableViewDataSource, Delegate
extension CloseAccountReasonSelectionViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { self.tableViewDataSources.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: TableViewCell.self), for: indexPath) as! TableViewCell
        cell.reason = CloseAccountReasonSelectionViewController.CloseAccountReason.allCases[indexPath.row]
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.selectedReason = CloseAccountReasonSelectionViewController.CloseAccountReason.allCases[safe_: indexPath.row]
    }
}

extension CloseAccountReasonSelectionViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        let new = (textView.text as NSString).replacingCharacters(in: range, with: text)
        self.suggestionTextViewPlaceholder.isHidden = new.count != 0
        return true
    }
}
