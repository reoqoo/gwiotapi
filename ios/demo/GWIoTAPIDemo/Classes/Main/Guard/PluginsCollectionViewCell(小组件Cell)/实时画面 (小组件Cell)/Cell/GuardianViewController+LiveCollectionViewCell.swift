//
//  GuardianViewController+LiveCollectionCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/8/2023.
//

import Foundation
import RQCore

extension GuardianViewController {

    /// 实时画面组件Cell
    class LiveCollectionCell: PluginCollectionViewCell {

        let vm: ViewModel = .init()

        @DidSetPublished var layoutMode: LiveViewContainer.LayoutMode = .fourGird

        // 选中设备事件
        private(set) var deviceSelectedObservable: Combine.PassthroughSubject<DeviceEntity?, Never> = .init()

        // 点击 more 按钮事件
        private(set) var moreBtnOnClickObservable: Combine.PassthroughSubject<Void, Never> = .init()

        private lazy var titleLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.text = String.localization.localized("AA0195", note: "实时画面")
            $0.textColor = R.color.text_000000_90()!
        }

        private(set) lazy var moreBtn: UIButton = .init(type: .system).then {
            $0.setImage(R.image.guardMore(), for: .normal)
            $0.tintColor = R.color.text_000000_90()!
        }

        private lazy var topContainer: UIView = .init().then {
            $0.backgroundColor = .clear
        }

        private lazy var liveViewContainer: LiveViewContainer = .init(devices: []).then {
            $0.backgroundColor = .clear
            $0.layoutMode = self.layoutMode
            $0.layer.cornerRadius = 14
            $0.layer.cornerCurve = .continuous
            $0.layer.masksToBounds = true
        }

        private lazy var pageControl: UIPageControl = .init().then {
            $0.hidesForSinglePage = true
            $0.pageIndicatorTintColor = R.color.background_000000_5()
            $0.currentPageIndicatorTintColor = R.color.brand()
            $0.isUserInteractionEnabled = false
        }

        // 把 liveViewContainer 的 bottom 约束 mark 起来
        private var liveViewContainerBottomConstraint: Constraint?

        private var anyCancellables: Set<AnyCancellable> = []

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        override init(frame: CGRect) {
            super.init(frame: frame)

            self.contentView.addSubview(self.topContainer)
            self.topContainer.snp.makeConstraints { make in
                make.top.equalToSuperview().offset(6)
                make.leading.trailing.equalToSuperview()
            }

            self.topContainer.addSubview(self.titleLabel)
            self.titleLabel.snp.makeConstraints { make in
                make.top.equalTo(12)
                make.leading.equalTo(12)
                make.bottom.equalTo(-10)
            }

            self.topContainer.addSubview(self.moreBtn)
            self.moreBtn.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.top.equalToSuperview()
                make.bottom.equalToSuperview().offset(-4)
                make.width.equalTo(24)
            }

            // 9 / 16 的宽高比例
            let containerWidth: CGFloat = UIScreen.main.bounds.width - 56
            let containerHeight: CGFloat = containerWidth * 9 / 16
            self.contentView.addSubview(self.liveViewContainer)
            self.liveViewContainer.snp.makeConstraints { make in
                make.top.equalTo(self.topContainer.snp.bottom)
                make.leading.equalToSuperview().offset(12)
                make.trailing.equalToSuperview().offset(-12)
                make.height.equalTo(containerHeight)
                make.width.equalTo(containerWidth)
                self.liveViewContainerBottomConstraint = make.bottom.equalToSuperview().offset(-12).priority(.init(259)).constraint
            }

            self.contentView.addSubview(self.pageControl)
            self.pageControl.snp.makeConstraints { make in
                make.top.equalTo(self.liveViewContainer.snp.bottom).offset(8).priority(.init(749))
                make.leading.equalToSuperview().offset(12)
                make.trailing.equalToSuperview().offset(-12)
                make.bottom.equalToSuperview().offset(-8).priority(.init(749))
            }

            // PageControl 点击
            self.pageControl.currentPagePublisher.sink(receiveValue: { [weak self] i in
                self?.liveViewContainer.updateCurrentPage(i)
            }).store(in: &self.anyCancellables)

            // More btn 点击
            self.moreBtn.tapPublisher.sink(receiveValue: { [weak self] in
                self?.moreBtnOnClickObservable.send(())
            }).store(in: &self.anyCancellables)

            // 页数设置
            // 总
            self.liveViewContainer.$numberOfPages
                .sink(receiveValue: { [weak self] i in
                    self?.pageControl.numberOfPages = i
                    self?.showPageControl(i > 1)
                }).store(in: &self.anyCancellables)

            // 当前页
            self.liveViewContainer.$currentPage.sink { [weak self] i in
                self?.pageControl.currentPage = i
            }.store(in: &self.anyCancellables)

            // 监听 self.liveViewContainer 被点击
            self.liveViewContainer.$selectedDevice.sink(receiveValue: { [weak self] dev in
                self?.deviceSelectedObservable.send(dev)
            }).store(in: &self.anyCancellables)

            // 将 layoutMode 绑定到 liveViewContainer.layoutMode
            self.$layoutMode
                .sink(receiveValue: { [weak self] layoutMode in
                    self?.liveViewContainer.layoutMode = layoutMode
                }).store(in: &self.anyCancellables)
            
            // 监听设备列表发生变化
            DeviceManager.shared.generateDevicesPublisher(keyPaths: [\.liveViewSortID])
                .sink(receiveValue: { [weak self] results in
                    if let devs = results?.sorted(by: \.liveViewSortID, ascending: true) {
                        self?.liveViewContainer.updateDevcies(Array(devs))
                    }else{
                        self?.liveViewContainer.updateDevcies([])
                    }
                }).store(in: &self.anyCancellables)
        }
    }
}

// MARK: Helper
extension GuardianViewController.LiveCollectionCell {
    func showPageControl(_ show: Bool) {
        let priority = show ? ConstraintPriority.init(199) : .init(999)
        self.liveViewContainerBottomConstraint?.update(priority: priority)
        self.layoutIfNeeded()
    }
}
