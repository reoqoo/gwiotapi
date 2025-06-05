//
//  UIScrollView+.swift
//  RQSDKDemo
//
//  Created by chenchangxin on 2023/9/17.
//

import Foundation
import MJRefresh
import Lottie

class MJCommonHeader: MJRefreshGifHeader {

    lazy var animationView = LottieAnimationView.init(name: R.file.family_updateJson.name).then {
        $0.loopMode = .loop // 无限动画
        $0.backgroundBehavior = .pauseAndRestore // 后台模式
    }

    override func prepare() {
        super.prepare()
        self.stateLabel?.isHidden = true
        self.lastUpdatedTimeLabel?.isHidden = true
        self.gifView?.addSubview(self.animationView)
        self.gifView?.snp.makeConstraints { make in
            make.top.left.right.equalToSuperview()
            make.height.equalTo(MJRefreshHeaderHeight)
        }
        self.animationView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.height.equalTo(30)
        }
    }

    override var pullingPercent: CGFloat {
        didSet {
            self.animationView.alpha = pullingPercent
        }
    }

    override func beginRefreshing() {
        super.beginRefreshing()
        self.animationView.play()
    }

    override func endRefreshing() {
        super.endRefreshing()
        self.animationView.pause()
    }
}

class MJCommonFooter: MJRefreshAutoFooter {

    let animationView = LottieAnimationView(name: "family_update").then {
        $0.loopMode = .loop // 无限动画
        $0.backgroundBehavior = .pauseAndRestore // 后台模式
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func prepare() {
        super.prepare()
        self.addSubview(self.animationView)
        self.animationView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.height.equalTo(30)
        }

        self.publisher(for: \.state).sink { [weak self] state in
            switch state {
            case .idle, .noMoreData:
                self?.animationView.isHidden = true
                self?.animationView.pause()
            case .pulling, .refreshing, .willRefresh:
                self?.animationView.isHidden = false
                self?.animationView.play()
            @unknown default:
                break
            }
        }.store(in: &self.anyCancellables)
    }
}
