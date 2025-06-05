//
//  FirstLevelMessageTableViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/9/2023.
//

import Foundation
import RQCore

extension SystemMessageViewController {
    class FirstLevelMessageTableViewCell: UITableViewCell {
        var item: MessageCenter.FirstLevelMessageItem? {
            didSet {
                self.iconImageView.image = self.item?.icon
                self.titleLabel.text = self.item?.title
                self.summaryLabel.text = self.item?.summary
                self.timeLabel.text = Date.init(timeIntervalSince1970: self.item?.msgTime ?? 0).friendlyPresented
            }
        }

        lazy var iconImageView: UIImageView = .init().then {
            $0.contentMode = .center
        }

        lazy var textContentStackView: UIStackView = .init().then {
            $0.axis = .vertical
            $0.spacing = 4
        }

        lazy var titleAndTimeLabelContainer: UIView = .init()

        lazy var titleLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 15, weight: .medium)
            $0.textColor = R.color.text_000000_90()
        }

        lazy var summaryLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 13, weight: .regular)
            $0.textColor = R.color.text_000000_60()
            $0.numberOfLines = 2
        }

        lazy var timeLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 12)
            $0.textColor = R.color.text_000000_38()
            $0.setContentHuggingPriority(.init(rawValue: 999), for: .horizontal)
        }

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.iconImageView)
            self.iconImageView.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(16)
                make.centerY.equalToSuperview()
                make.height.width.equalTo(46)
            }

            self.contentView.addSubview(self.textContentStackView)
            self.textContentStackView.snp.makeConstraints { make in
                make.leading.equalTo(self.iconImageView.snp.trailing).offset(12)
                make.trailing.equalToSuperview().offset(-16)
                make.centerY.equalToSuperview()
            }

            self.titleAndTimeLabelContainer.addSubview(self.titleLabel)
            self.titleLabel.snp.makeConstraints { make in
                make.top.leading.bottom.equalToSuperview()
            }

            self.titleAndTimeLabelContainer.addSubview(self.timeLabel)
            self.timeLabel.snp.makeConstraints { make in
                make.centerY.trailing.equalToSuperview()
                make.leading.greaterThanOrEqualTo(self.titleLabel.snp.trailing).offset(8)
            }

            self.textContentStackView.addArrangedSubview(self.titleAndTimeLabelContainer)
            self.textContentStackView.addArrangedSubview(self.summaryLabel)
            
            let separator = UIView.init()
            separator.backgroundColor = R.color.lineSeparator()
            self.contentView.addSubview(separator)
            separator.snp.makeConstraints { make in
                make.height.equalTo(0.5)
                make.leading.equalToSuperview().offset(74)
                make.trailing.equalToSuperview().offset(-16)
                make.bottom.equalToSuperview()
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}
