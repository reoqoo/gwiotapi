//
//  LiveView+AssistantPlayerViewsContainer.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/3/2025.
//

import Foundation

extension LiveView {
    class SubPlayerViewsContainer: UIView {

        var vm: LiveView.ViewModel
        var anyCancellables: Set<AnyCancellable> = []

        private override init(frame: CGRect) { fatalError("init(frame:) has not been implemented") }
        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        fileprivate lazy var stackView: LiveView.SubPlayerViewsContainer.SubPlayerStackView = .init().then {
            $0.backgroundColor = .clear
        }

        init(vm: LiveView.ViewModel) {
            self.vm = vm
            super.init(frame: .zero)

            self.addSubview(self.stackView)
            self.stackView.snp.makeConstraints { make in
                make.bottom.equalToSuperview().offset(-4)
                make.top.equalToSuperview().offset(4)
                make.trailing.equalToSuperview().offset(-5)
                make.width.equalTo(54)
            }

            self.vm.$status.sink { [weak self] in
                guard case let .numberOfPlayerViewsDidChanged(views) = $0 else { return }
                self?.handlePlayerViewsChanged(views)
            }.store(in: &self.anyCancellables)
        }

        func handlePlayerViewsChanged(_ views: [GWPlayerView]) {
            // 移除所有已有的副画面
            self.stackView.removeAllArrangedSubview()

            let afterDropFirst = views.dropFirst()
            if afterDropFirst.isEmpty { return }

            afterDropFirst.reversed().forEach {
                let wrapper = self.wrapPlayerView($0)
                self.stackView.addArrangedSubview(wrapper)
                wrapper.snp.makeConstraints { make in
                    make.height.equalTo(29)
                    make.width.equalTo(54)
                }
            }
        }

        func wrapPlayerView(_ playerView: GWPlayerView) -> UIView {
            let wrapper = UIView.init()
            wrapper.layer.cornerRadius = 8
            wrapper.layer.masksToBounds = true
            wrapper.layer.borderWidth = 1
            wrapper.layer.borderColor = R.color.border_38E667()?.cgColor
            wrapper.addSubview(playerView)
            playerView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }
            return wrapper
        }
    }
}

extension LiveView.SubPlayerViewsContainer {
    fileprivate class SubPlayerStackView: UIView {

        var arrangedSubviews: [UIView] = []

        func addArrangedSubview(_ view: UIView) {
            // 将 view 加入到 arrangedSubviews 中
            if self.arrangedSubviews.contains(where: { $0 == view }) {
                self.arrangedSubviews.removeAll(where: { $0 === view })
            }
            self.arrangedSubviews.append(view)

            self.layout()
        }

        func removeArrangedSubview(_ view: UIView) {
            if self.arrangedSubviews.contains(where: { $0 == view }) {
                self.arrangedSubviews.removeAll(where: { $0 === view })
                self.layout()
            }
        }

        func removeAllArrangedSubview() {
            self.arrangedSubviews.forEach({ $0.removeFromSuperview() })
            self.arrangedSubviews = []
        }

        func layout() {
            // 移除所有子view, 重新布局
            self.subviews.forEach({ $0.removeFromSuperview() })

            var pervious: UIView?
            self.arrangedSubviews.forEach {

                self.addSubview($0)

                if let pervious = pervious {
                    $0.snp.makeConstraints { make in
                        make.centerX.equalToSuperview()
                        make.bottom.equalTo(pervious.snp.top).offset(-4)
                    }
                }else{
                    $0.snp.makeConstraints { make in
                        make.centerX.equalToSuperview()
                        make.bottom.equalToSuperview()
                    }
                }

                pervious = $0
            }
        }
    }
}
