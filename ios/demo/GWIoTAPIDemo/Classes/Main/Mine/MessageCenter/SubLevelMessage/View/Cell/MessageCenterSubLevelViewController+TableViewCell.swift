//
//  MessageCenterSubLevelViewController+TableViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/9/2023.
//

import Foundation
import RQCore

extension MessageCenterSubLevelViewController {
    class SubLevelMessageTableViewCell: UITableViewCell {

        var item: MessageCenter.SecondLevelMessageItem? {
            didSet {
                self.titleLabel.text = self.item?.title
                self.summaryLabel.text = self.item?.body
                self.timeLabel.text = Date.init(timeIntervalSince1970: self.item?.time ?? 0).friendlyPresented
            }
        }

        lazy var titleLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 15, weight: .medium)
            $0.textColor = R.color.text_000000_90()
        }

        lazy var timeLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 12)
            $0.textColor = R.color.text_000000_38()
            $0.setContentHuggingPriority(.init(rawValue: 999), for: .horizontal)
        }

        lazy var titleLabelAndTimeLabelContainer: UIView = .init()

        lazy var summaryLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 13, weight: .regular)
            $0.textColor = R.color.text_000000_60()
            $0.numberOfLines = 0
        }

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.backgroundColor = R.color.background_FFFFFF_white()

            self.contentView.addSubview(self.titleLabelAndTimeLabelContainer)
            self.titleLabelAndTimeLabelContainer.snp.makeConstraints { make in
                make.top.trailing.leading.equalToSuperview()
            }

            self.titleLabelAndTimeLabelContainer.addSubview(self.titleLabel)
            self.titleLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.top.equalToSuperview().offset(14)
                make.bottom.equalToSuperview().offset(-14)
            }

            self.titleLabelAndTimeLabelContainer.addSubview(self.timeLabel)
            self.timeLabel.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.leading.greaterThanOrEqualTo(self.titleLabel.snp.trailing).offset(8)
                make.centerY.equalToSuperview()
            }

            let separator: UIView = .init()
            separator.backgroundColor = R.color.background_000000_5()
            self.contentView.addSubview(separator)
            separator.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.trailing.equalToSuperview().offset(-12)
                make.height.equalTo(1)
                make.top.equalTo(self.titleLabelAndTimeLabelContainer.snp.bottom)
            }

            self.contentView.addSubview(self.summaryLabel)
            self.summaryLabel.snp.makeConstraints { make in
                make.top.equalTo(self.titleLabelAndTimeLabelContainer.snp.bottom).offset(12)
                make.leading.equalToSuperview().offset(12)
                make.trailing.equalToSuperview().offset(-12)
                make.bottom.equalToSuperview().offset(-12)
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}
