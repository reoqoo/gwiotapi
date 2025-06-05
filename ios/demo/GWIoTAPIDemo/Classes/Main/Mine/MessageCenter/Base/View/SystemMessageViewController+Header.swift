//
//  MessageCenterViewController+TableViewHeader.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/9/2023.
//

import Foundation

extension SystemMessageViewController {
    class Header: UIView {

        private(set) lazy var tap: UITapGestureRecognizer = .init()

        private(set) lazy var contentView: UIView = .init().then {
            $0.backgroundColor = R.color.text_link_4280EF()?.withAlphaComponent(0.15)
            $0.layer.masksToBounds = true
            $0.layer.cornerRadius = 12
        }

        private(set) lazy var textLabel: UILabel = .init().then {
            $0.numberOfLines = 0
            $0.font = .systemFont(ofSize: 13)
            $0.textColor = R.color.text_link_4A68A6()
            $0.text = String.localization.localized("AA0227", note: "开启reoqoo APP通知权限，及时获得最新消息")
        }

        private(set) lazy var arrowImgView: UIImageView = .init(image: R.image.commonArrowRightStyle2()).then {
            $0.contentMode = .center
        }

        override init(frame: CGRect) {
            super.init(frame: frame)

            self.addSubview(self.contentView)
            self.contentView.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.trailing.equalToSuperview().offset(-12)
                make.top.equalToSuperview().offset(12)
                make.bottom.equalToSuperview().offset(-12)
                make.height.equalTo(48)
            }

            self.contentView.addSubview(self.textLabel)
            self.textLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.centerY.equalToSuperview()
            }

            self.contentView.addSubview(self.arrowImgView)
            self.arrowImgView.snp.makeConstraints { make in
                make.leading.equalTo(self.textLabel.snp.trailing).offset(8)
                make.trailing.equalToSuperview().offset(-12)
                make.centerY.equalToSuperview()
                make.height.equalToSuperview()
                make.width.equalTo(16)
            }

            self.contentView.addGestureRecognizer(self.tap)
        }

        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
}
