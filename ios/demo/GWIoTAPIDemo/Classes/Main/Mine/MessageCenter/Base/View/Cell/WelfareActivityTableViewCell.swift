//
//  WelfareActivityTableViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 20/2/2024.
//

import Foundation
import RQCore

extension WelfareActivityViewController {
    class WelfareActivityTableViewCell: UITableViewCell {

        var item: MessageCenter.WelfareActivityItem? {
            didSet {
                guard let item = self.item else { return }
                self.bannerImageView.kf.setImage(with: URL.init(string: item.picUrl), placeholder: ReoqooImageLoadingPlaceholder())
                self.descriptionLable.text = String.localization.localized("AA0157", note: "有效期至") + ": " + Date.init(timeIntervalSince1970: item.expireTime).string(with: "yyyy/MM/dd HH:mm")
                self.expiredMask.isHidden = item.expireTime > Date().timeIntervalSince1970
            }
        }

        lazy var mainContent: UIView = .init().then {
            $0.layer.cornerRadius = 16
            $0.layer.masksToBounds = true
            $0.backgroundColor = R.color.background_FFFFFF_white()
        }

        lazy var bannerImageView: UIImageView = .init().then {
            $0.contentMode = .scaleToFill
            $0.clipsToBounds = true
        }

        lazy var descriptionLable: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
        }

        lazy var expiredMask: UIView = .init().then {
            $0.backgroundColor = .black.withAlphaComponent(0.75)
            let label = UILabel.init()
            label.textColor = R.color.text_FFFFFF()!
            label.font = .systemFont(ofSize: 16)
            label.text = String.localization.localized("AA0603", note: "活动已结束~")
            $0.addSubview(label)
            label.snp.makeConstraints { make in
                make.center.equalToSuperview()
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)
            
            self.selectionStyle = .none
            self.backgroundColor = .clear
            self.contentView.backgroundColor = .clear

            self.contentView.addSubview(self.mainContent)
            self.mainContent.snp.makeConstraints { make in
                make.edges.equalTo(UIEdgeInsets.init(top: 16, left: 16, bottom: 16, right: 16))
            }

            self.mainContent.addSubview(self.bannerImageView)
            self.bannerImageView.snp.makeConstraints { make in
                make.top.leading.trailing.equalToSuperview()
                make.height.equalTo(174)
            }

            self.mainContent.addSubview(self.expiredMask)
            self.expiredMask.snp.makeConstraints { make in
                make.top.leading.trailing.bottom.equalTo(self.bannerImageView)
            }

            self.mainContent.addSubview(self.descriptionLable)
            self.descriptionLable.snp.makeConstraints { make in
                make.top.equalTo(self.bannerImageView.snp.bottom).offset(24)
                make.leading.equalToSuperview().offset(16)
                make.trailing.equalToSuperview().offset(-16)
                make.bottom.equalToSuperview().offset(-24)
            }
        }

    }
}
