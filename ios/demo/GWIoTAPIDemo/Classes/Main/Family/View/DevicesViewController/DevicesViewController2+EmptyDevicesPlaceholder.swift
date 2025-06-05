//
//  DevicesViewController2+EmptyView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/2/2024.
//

import Foundation

extension DevicesViewController2 {
    /// 设备列表空页面
    class EmptyDevicesPlaceholder: UIView {

        lazy var addDeviceBtnOnClickObservable = self.button.tapPublisher

        //MARK: private
        private var anyCancellables: Set<AnyCancellable> = []

        private lazy var imageView = UIImageView(image: R.image.family_device_empty()).then {
            $0.contentMode = .center
        }

        private lazy var label = UILabel().then {
            $0.backgroundColor = .clear
            $0.textAlignment = .center
            $0.font = .systemFont(ofSize: 15, weight: .regular)
            $0.textColor = R.color.text_000000_90()
            $0.text = String.localization.localized("AA0571", note: "Enjoy your real cool life")
            $0.numberOfLines = 0
        }

        private(set) lazy var button = UIButton(type: .custom).then {
            $0.layer.cornerRadius = 23
            $0.titleLabel!.font = .systemFont(ofSize: 16, weight: .medium)
            $0.setTitleColor(R.color.text_FFFFFF(), for: .normal)
            $0.setTitle(String.localization.localized("AA0049", note: "添加设备"), for: .normal)
            $0.backgroundColor = R.color.brand()
            $0.contentEdgeInsets = .init(top: 0, left: 16, bottom: 0, right: 16)
        }

        override init(frame: CGRect) {
            super.init(frame: .zero)
            setupUI()
        }

        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

        private func setupUI() {
            self.layer.cornerRadius = 12
            self.backgroundColor = R.color.background_FFFFFF_white()

            self.addSubview(self.imageView)
            self.imageView.snp.makeConstraints { make in
                make.top.equalToSuperview().offset(50)
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
                make.bottom.equalToSuperview().offset(-50)
                make.height.equalTo(46)
                make.width.equalTo(200)
                make.leading.greaterThanOrEqualToSuperview().offset(16)
                make.trailing.lessThanOrEqualToSuperview().offset(-16)
            }
        }
    }

}
