//
//  ShareManagedViewController+DeviceTableViewCell.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 13/6/2024.
//

import Foundation

extension ShareManagedViewController {
    class DeviceTableViewHeader: UITableViewHeaderFooterView {

        var text: String? {
            didSet {
                self.titleLabel.text = text
            }
        }

        lazy var titleLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_90()
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        override init(reuseIdentifier: String?) {
            super.init(reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.titleLabel)
            self.titleLabel.snp.makeConstraints { make in
                make.left.equalToSuperview()
                make.top.equalToSuperview().offset(12)
                make.bottom.equalToSuperview().offset(-12)
            }
        }
        
    }
}
