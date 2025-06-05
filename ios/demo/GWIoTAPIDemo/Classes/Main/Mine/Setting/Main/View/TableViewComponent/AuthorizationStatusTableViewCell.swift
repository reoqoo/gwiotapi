//
//  AuthorizationStatusTableViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 11/6/2024.
//

import UIKit

extension SettingViewController {
    class AuthorizationStatusTableViewCell: UITableViewCell {

        var item: AuthorizationStatusItem? {
            didSet {
                self.anyCancellables = []
                guard let item = item else {
                    self.titleLabel.text = nil
                    self.descriptionLabel.text = nil
                    self.statusLabel.text = nil
                    return
                }
                self.titleLabel.text = item.title
                self.descriptionLabel.text = item.description
                item.$isValid.sink { [weak self] granted in
                    self?.statusLabel.text = granted ? String.localization.localized("AA0618", note: "已开启") : String.localization.localized("AA0617", note: "去授权")
                }.store(in: &self.anyCancellables)
            }
        }

        lazy var titleLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 16)
            $0.textColor = R.color.text_000000_90()
        }

        lazy var descriptionLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 13)
            $0.numberOfLines = 0
            $0.textColor = R.color.text_000000_60()
        }

        lazy var statusLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_60()
        }

        lazy var indicator: UIImageView = .init(image: R.image.commonArrowRightStyle3())

        var anyCancellables: Set<AnyCancellable> = []

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            let container0 = UIView.init()
            self.contentView.addSubview(container0)
            container0.snp.makeConstraints { make in
                make.centerY.equalToSuperview()
                make.top.greaterThanOrEqualToSuperview().offset(12)
                make.bottom.lessThanOrEqualToSuperview().offset(-12)
                make.leading.equalToSuperview().offset(12)
            }

            container0.addSubview(self.titleLabel)
            self.titleLabel.snp.makeConstraints { make in
                make.left.equalToSuperview()
                make.top.equalToSuperview()
                make.right.equalToSuperview()
            }

            container0.addSubview(self.descriptionLabel)
            self.descriptionLabel.snp.makeConstraints { make in
                make.left.equalToSuperview()
                make.right.equalToSuperview()
                make.bottom.equalToSuperview()
                make.top.equalTo(self.titleLabel.snp.bottom).offset(4)
            }

            self.contentView.addSubview(self.statusLabel)
            self.statusLabel.snp.makeConstraints { make in
                make.left.greaterThanOrEqualTo(container0.snp.right).offset(12)
                make.centerY.equalToSuperview()
            }

            self.contentView.addSubview(self.indicator)
            self.indicator.snp.makeConstraints { make in
                make.left.equalTo(self.statusLabel.snp.right).offset(4)
                make.centerY.equalToSuperview()
                make.right.equalToSuperview().offset(-12)
            }

        }
        
    }
}
