//
//  PromotionalFloatingView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 7/12/2023.
//

import Foundation

/// 运营推广浮窗
class PromotionalFloatingView: UIView {

    public var banner: IVBBSMgr.Banner? {
        didSet {
            guard let banner = banner, let url_str = banner.picUrl, let url = URL.init(string: url_str) else { return }
            self.imageView.kf.setImage(with: url, placeholder: ReoqooImageLoadingPlaceholder())
        }
    }

    var anyCancellables: Set<AnyCancellable> = []

    let tapOnImageViewGestureBehaviorObservable: Combine.PassthroughSubject<URL, Never> = .init()

    private lazy var tapOnImageViewGesture: UITapGestureRecognizer = .init()
    
    private(set) lazy var imageView: UIImageView = .init().then {
        $0.contentMode = .scaleAspectFit
        $0.isUserInteractionEnabled = true
    }

    private(set) lazy var closeButton: UIButton = .init(type: .custom).then {
        $0.setImage(R.image.commonCross2(), for: .normal)
    }

    override init(frame: CGRect) {
        super.init(frame: frame)

        self.addSubview(self.imageView)
        self.imageView.snp.makeConstraints { make in
            make.leading.trailing.bottom.equalToSuperview()
            make.width.height.equalTo(80)
        }

        self.imageView.addGestureRecognizer(self.tapOnImageViewGesture)

        self.addSubview(self.closeButton)
        self.closeButton.snp.makeConstraints { make in
            make.top.trailing.equalToSuperview()
            make.bottom.equalTo(self.imageView.snp.top)
            make.width.height.equalTo(26)
        }

        self.closeButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                self?.removeFromSuperview()
            }).store(in: &self.anyCancellables)

        self.tapOnImageViewGesture
            .tapPublisher.sink(receiveValue: { [weak self] gesture in
                if gesture.state != .ended { return }
                guard let url_str = self?.banner?.url, let url = URL.init(string: url_str) else { return }
                self?.tapOnImageViewGestureBehaviorObservable.send(url)
            }).store(in: &self.anyCancellables)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
}
