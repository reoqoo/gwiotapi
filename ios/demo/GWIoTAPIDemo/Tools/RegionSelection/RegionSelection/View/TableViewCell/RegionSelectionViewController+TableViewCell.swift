//
//  RegionSelectionViewController+TableViewCell.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 24/7/2023.
//

import Foundation
import RQCore

extension RegionSelectionViewController {
    class TableViewCell: UITableViewCell {

        var countryInfo: RegionInfo? {
            didSet {
                guard let countryInfo = countryInfo else {
                    self.countryNameLabel.text = nil
                    self.countryCodeLabel.text = nil
                    return
                }
                self.countryNameLabel.text = countryInfo.countryName
                self.countryCodeLabel.text = "+" + countryInfo.countryCode
            }
        }

        lazy var countryNameLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 16, weight: .regular)
            $0.textColor = R.color.text_000000_90()
        }

        lazy var countryCodeLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14, weight: .regular)
            $0.textColor = R.color.text_000000_60()
        }

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.countryNameLabel)
            self.countryNameLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(28)
                make.top.bottom.equalToSuperview()
            }

            self.contentView.addSubview(self.countryCodeLabel)
            self.countryCodeLabel.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-28)
                make.top.bottom.equalToSuperview()
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}
