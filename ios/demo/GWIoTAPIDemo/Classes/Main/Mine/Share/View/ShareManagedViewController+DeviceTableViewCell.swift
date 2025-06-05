//
//  ShareManagedViewController+DeviceTableViewCell.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 13/6/2024.
//

import Foundation
import RQCore

extension ShareManagedViewController {
    class DeviceTableViewCell: UITableViewCell {

        var device: DeviceEntity? {
            didSet {
                self.anyCancellables = []

                guard let dev = self.device else {
                    self.deviceImageView.image = nil
                    self.deviceNameLabel.text = nil
                    return
                }
                
                dev.getImageURLPublisher(type: .A).sink(receiveValue: { [weak self] url in
                    self?.deviceImageView.kf.setImage(with: url, placeholder: ReoqooImageLoadingPlaceholder(), options: [
                        .processor(Kingfisher.ResizingImageProcessor(referenceSize: CGSize(width: 200, height: 200)))
                    ])
                }).store(in: &self.anyCancellables)

                self.deviceNameLabel.text = dev.remarkName
            }
        }

        lazy var deviceImageView: UIImageView = .init()
        lazy var deviceNameLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 16)
            $0.textColor = R.color.text_000000_90()
        }

        lazy var indicator: UIImageView = .init(image: R.image.commonArrowRightStyle3()!)

        var anyCancellables: Set<AnyCancellable> = []

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.deviceImageView)
            self.deviceImageView.snp.makeConstraints { make in
                make.top.leading.equalToSuperview().offset(12)
                make.bottom.equalToSuperview().offset(-12)
                make.width.height.equalTo(56)
            }

            self.contentView.addSubview(self.deviceNameLabel)
            self.deviceNameLabel.snp.makeConstraints { make in
                make.leading.equalTo(self.deviceImageView.snp.trailing).offset(4)
                make.centerY.equalToSuperview()
            }

            self.contentView.addSubview(self.indicator)
            self.indicator.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.centerY.equalToSuperview()
                make.leading.greaterThanOrEqualTo(self.deviceNameLabel.snp.trailing).offset(4)
            }
        }
        
    }
}
