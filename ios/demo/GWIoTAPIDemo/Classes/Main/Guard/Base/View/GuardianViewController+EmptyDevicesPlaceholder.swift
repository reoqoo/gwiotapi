//
//  GuardianViewController+EmptyDevicesPlaceholder.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import Foundation

extension GuardianViewController {
    class EmptyDevicesPlaceholder: UIView {

        let imageView: UIImageView = .init(image: R.image.guardNoneDevicePlaceholder()).then {
            $0.contentMode = .center
        }

        let label: UILabel = .init().then {
            $0.text = String.localization.localized("AA0193", note: "开启智能看家需要添加一台摄像机")
            $0.numberOfLines = 0
            $0.font = .systemFont(ofSize: 15)
            $0.textColor = R.color.text_000000_90()!
            $0.textAlignment = .center
        }

        let button: UIButton = .init(type: .custom).then {
            $0.setTitle(String.localization.localized("AA0194", note: "去添加"), for: .normal)
            $0.setStyle_0()
            $0.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
            $0.setTitleColor(R.color.text_FFFFFF()!, for: .normal)
            $0.layer.cornerRadius = 23
            $0.layer.masksToBounds = true
        }

        override init(frame: CGRect) {
            super.init(frame: frame)
            self.addSubview(self.imageView)
            self.imageView.snp.makeConstraints { make in
                make.top.equalToSuperview()
                make.centerX.equalToSuperview()
                make.leading.greaterThanOrEqualToSuperview().offset(16)
                make.trailing.lessThanOrEqualToSuperview().offset(-16)
            }

            self.addSubview(self.label)
            self.label.snp.makeConstraints { make in
                make.top.equalTo(self.imageView.snp.bottom).offset(8)
                make.centerX.equalToSuperview()
                make.leading.greaterThanOrEqualToSuperview().offset(16)
                make.trailing.lessThanOrEqualToSuperview().offset(-16)
            }

            self.addSubview(self.button)
            self.button.snp.makeConstraints { make in
                make.top.equalTo(self.label.snp.bottom).offset(40)
                make.centerX.equalToSuperview()
                make.bottom.equalToSuperview()
                make.height.equalTo(46)
                make.width.equalTo(200)
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}
