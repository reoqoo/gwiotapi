//
//  AuthorizationStatusTableViewHeader.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/6/2024.
//

import Foundation

extension SettingViewController {
    class AuthorizationStatusTableViewHeader: UITableViewHeaderFooterView {
        lazy var label: UILabel = .init().then {
            $0.numberOfLines = 0
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_60()
        }

        override init(reuseIdentifier: String?) {
            super.init(reuseIdentifier: reuseIdentifier)
            self.contentView.addSubview(self.label)
            self.label.snp.makeConstraints { make in
                make.leading.equalToSuperview()
                make.trailing.equalToSuperview()
                make.top.equalToSuperview().offset(24)
                make.bottom.equalToSuperview().offset(-10)
            }
        }
        
        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}
