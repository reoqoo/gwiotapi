//
//  MineViewController+ServiceButton.swift
//  Reoqoo
//
//  Created by xiaojuntao on 20/11/2023.
//

import Foundation

extension MineViewController {
    class ServiceButton: UIView {

        var showBadge: Bool = false {
            didSet {
                self.badge.isHidden = !self.showBadge
            }
        }

        private lazy var badge: UIView = .init().then {
            $0.isHidden = true
            $0.layer.cornerRadius = 2.5
            $0.backgroundColor = UIColor.red
        }

        private lazy var button: UIButton = .init(type: .custom)
        private(set) lazy var imageView: UIImageView = .init().then {
            $0.contentMode = .center
        }
        private(set) lazy var label: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 12)
            $0.numberOfLines = 0
            $0.textAlignment = .center
            $0.textColor = R.color.text_000000_90()
        }

        convenience init(title: String, image: UIImage) {
            self.init(frame: .zero)
            self.imageView.image = image
            self.label.text = title
        }

        override init(frame: CGRect) {
            super.init(frame: frame)
            self.setup()
        }
        
        required init?(coder: NSCoder) {
            super.init(coder: coder)
            self.setup()
        }

        func setup() {
            self.addSubview(self.button)
            self.button.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }

            self.addSubview(self.imageView)
            self.imageView.snp.makeConstraints { make in
                make.top.equalToSuperview()
                make.centerX.equalToSuperview()
            }

            self.addSubview(self.label)
            self.label.snp.makeConstraints { make in
                make.leading.trailing.bottom.equalToSuperview()
                make.top.equalTo(self.imageView.snp.bottom).offset(8)
            }

            self.addSubview(self.badge)
            self.badge.snp.makeConstraints { make in
                make.top.equalTo(self.imageView.snp.top).offset(8)
                make.trailing.equalTo(self.imageView.snp.trailing).offset(-8)
                make.height.width.equalTo(5)
            }
        }

        lazy var tapPublisher: AnyPublisher<Void, Never> = self.button.tapPublisher
    }
}

extension MineViewController.ServiceButton {
    static func placeholderButton(_ size: CGSize = .init(width: 1, height: 1)) -> MineViewController.ServiceButton {
        let img = UIColor.clear.pureImage(size: size)!
        let res = MineViewController.ServiceButton.init(title: " ", image: img)
        return res
    }
}
