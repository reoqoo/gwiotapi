//
//  Kingfish+Placeholder.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 17/10/2023.
//

import Foundation

class ReoqooImageLoadingPlaceholder: UIView, Kingfisher.Placeholder {

    lazy var animationView: UIActivityIndicatorView = .init(style: .large)

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    func setup() {
        self.addSubview(self.animationView)
        self.animationView.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
    }

    func add(to imageView: KFCrossPlatformImageView) {
        self.animationView.startAnimating()
        imageView.addSubview(self)
        self.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }

    func remove(from imageView: KFCrossPlatformImageView) {
        self.animationView.stopAnimating()
        self.removeFromSuperview()
    }
}
