//
//  FamilyViewController2+InviteBanner.swift
//  Reoqoo
//
//  Created by xiaojuntao on 17/2/2024.
//

import Foundation
import RQCore

protocol FamilyViewControllerBanner: UIView {
    /// 类型
    var type: FamilyViewController2.BannerType { get }

    /// 唯一标识符
    /// 当 唯一标识符 非空, 插入banner时, 会先判断 BannerContainer 内是否已有同样标识符的 banner, 如有, 先移除已存在的banner, 再显示新的banner
    var uniqueTag: String? { get }
}

extension FamilyViewController2 {

    enum BannerType: Int {
        // 公告
        case Notice
        // 添加设备引导Banner
        case AddDeviceGuide
        // 分享邀请
        case ShareInvite

        /// 展示权限, 当展示时, 权限越高则优先展示
        var priority: Int {
            return self.rawValue
        }
    }

    /// Banner 容器
    class BannerContainer: UIView {

        private var _banners: [FamilyViewControllerBanner] = []

        var banners: [FamilyViewControllerBanner] {
            // 根据 prioriry 排序 banners, priority 越大越靠后
            self._banners.sorted { $0.type.priority < $1.type.priority }
        }

        /// 插入 banner, 当此方法被调用后, 并不会马上执行 addSubview, 而是移除 _banners 中的元素
        /// 在任意时刻内, self.subViews 仅有一个 banner 作为子视图, 这是为了移除 / 添加动画更方便而设计
        public func insertBanner(_ banner: FamilyViewControllerBanner) {
            // 先检查 _banners 中是否有 uniqueTag 相同的 banner, 如有, 先移除
            if let duplicateBanner = self._banners.filter({ $0.uniqueTag == banner.uniqueTag }).first {
                self.removeBanner(duplicateBanner)
            }
            self._banners.append(banner)
        }
        
        /// 移除banner, 同 inserBanner() 方法, 并不会马上执行 removeFromSuperView, 而是移除 _banners 中的元素
        public func removeBanner(_ banner: FamilyViewControllerBanner) {
            let bs = self._banners.filter({ $0 == banner })
            self._banners.removeAll { b in
                bs.contains { $0 == b }
            }
        }
        

        /// addBanner() 以及 removeBanner() 都不会马上影响到视图显示情况, 需执行此方法才会马上将做出影响视图显示的操作
        /// - Returns: 当前正在显示的Banner, 以便外部控制显示动画
        @discardableResult public func updateDisplayIfNeed() -> FamilyViewControllerBanner? {
            // 移除当前正在显示的 banner
            self.subviews.last?.removeFromSuperview()
            // 将 banner 添加到视图
            guard let targetBanner = self.banners.last else { return nil }
            self.addSubview(targetBanner)
            targetBanner.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }
            return targetBanner
        }
    }
    
    // MARK: Banner 类型
    /// 设备分享邀请banner
    class ShareInviteBanner: UIView, FamilyViewControllerBanner {
        
        // MARK: FamilyViewControllerBanner
        /// 类型
        var type: FamilyViewController2.BannerType = .ShareInvite

        var uniqueTag: String? { self.inviteModel.shareToken }

        let inviteModel: MessageCenter.DeviceShareInviteModel

        let externalCancellables: Set<AnyCancellable> = []

        init(inviteModel: MessageCenter.DeviceShareInviteModel) {
            self.inviteModel = inviteModel
            super.init(frame: .zero)
            self.setupUI()

            self.descriptionLabel.text = String.localization.localized("AA0161", note: "收到%@分享的设备", args: inviteModel.inviteAccount)
            self.ignoreBtn.setTitle(String.localization.localized("AA0162", note: "忽略"), for: .normal)
            self.checkBtn.setTitle(String.localization.localized("AA0163", note: "查看"), for: .normal)
        }
        
        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        lazy var descriptionLabel: UILabel = .init().then {
            $0.backgroundColor = .clear
            $0.numberOfLines = 0
            $0.font = .systemFont(ofSize: 14, weight: .regular)
            $0.textColor = R.color.text_000000_90()
            $0.textAlignment = .left
        }

        private(set) lazy var ignoreBtn: UIButton = .init().then {
            $0.backgroundColor = .clear
            $0.contentEdgeInsets = UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16)
            $0.titleLabel!.font = .systemFont(ofSize: 14, weight: .medium)
            $0.setTitleColor(R.color.text_link_4A68A6(), for: .normal)
        }

        private(set) lazy var checkBtn: UIButton = .init().then {
            $0.backgroundColor = .clear
            $0.contentEdgeInsets = UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16)
            $0.titleLabel!.font = .systemFont(ofSize: 14, weight: .medium)
            $0.setTitleColor(R.color.text_link_4A68A6(), for: .normal)
        }

        func setupUI() {
            self.layer.cornerRadius = 12
            self.backgroundColor = R.color.text_FFFFFF()

            self.addSubview(self.descriptionLabel)
            self.descriptionLabel.snp.makeConstraints { make in
                make.top.left.equalTo(16)
                make.right.equalTo(-16)
            }

            self.addSubview(self.checkBtn)
            self.checkBtn.snp.makeConstraints { make in
                make.top.equalTo(self.descriptionLabel.snp.bottom).offset(8)
                make.trailing.equalToSuperview().offset(-16)
                make.bottom.equalToSuperview().offset(-8)
            }

            self.addSubview(self.ignoreBtn)
            self.ignoreBtn.snp.makeConstraints { make in
                make.top.equalTo(self.checkBtn.snp.top)
                make.trailing.equalTo(self.checkBtn.snp.leading).offset(-8)
            }
        }
    }
    
    /// 公告banner
    class NoticeBanner: UIView, FamilyViewControllerBanner {
        
        // MARK: FamilyViewControllerBanner
        /// 类型
        var type: FamilyViewController2.BannerType = .Notice

        var uniqueTag: String? { self.bannerItem.url }
        
        // MARK:
        let bannerItem: IVBBSMgr.Banner

        var externalCancellables: Set<AnyCancellable> = []
        var anyCancellables: Set<AnyCancellable> = []

        lazy var closeBtn: UIButton = .init().then {
            $0.setImage(R.image.commonCross2()!, for: .normal)
            $0.contentEdgeInsets = .init(top: 8, left: 8, bottom: 8, right: 8)
        }
        
        // banner 点击手势
        private lazy var tapGesture: UITapGestureRecognizer = .init()
        
        // banner 被点击发布者
        lazy var didTapObservable: Combine.PassthroughSubject<URL, Never> = .init()

        lazy var imageView: UIImageView = .init().then {
            $0.contentMode = .scaleToFill
            $0.clipsToBounds = true
            $0.isUserInteractionEnabled = true
        }

        override init(frame: CGRect) { fatalError("init(frame:) has not been implemented") }

        init(bannerItem: IVBBSMgr.Banner) {

            self.bannerItem = bannerItem
            super.init(frame: .zero)

            self.addSubview(self.imageView)
            self.imageView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
                make.height.equalTo(80)
            }

            self.addSubview(self.closeBtn)
            self.closeBtn.snp.makeConstraints { make in
                make.top.trailing.equalToSuperview()
            }

            if let urlPath = self.bannerItem.picUrl {
                self.imageView.kf.setImage(with: URL.init(string: urlPath), placeholder: ReoqooImageLoadingPlaceholder(), completionHandler:  { [weak self] in
                    guard case let .success(result) = $0 else { return }
                    let size = result.image.size
                    // 计算 imageView 的高度并更新约束
                    let height = (UIScreen.main.bounds.width - 32) * size.height / size.width
                    self?.imageView.snp.updateConstraints({ make in
                        make.height.equalTo(height)
                    })
                })
            }

            self.addGestureRecognizer(self.tapGesture)
            self.tapGesture.tapPublisher.map({ $0.state == .ended })
                .sink { [weak self] flag in
                    if !flag { return }
                    guard let urlPath = self?.bannerItem.url, let url = URL.init(string: urlPath) else { return }
                    self?.didTapObservable.send(url)
                }.store(in: &self.anyCancellables)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }

    /// 新增设备引导Banner
    class AddDeviceGuideBanner: UIView, FamilyViewControllerBanner {
        var type: FamilyViewController2.BannerType = .AddDeviceGuide

        // 写死唯一标识
        var uniqueTag: String? = "AddDeviceGuideBanner"

        // banner 点击手势
        private(set) lazy var tapGesture: UITapGestureRecognizer = .init()

        // banner 被点击发布者
        lazy var didTapObservable: Combine.PassthroughSubject<Void, Never> = .init()

        var anyCancellables: Set<AnyCancellable> = []

        /// 毛玻璃底
        lazy var visualEffectView: UIVisualEffectView = .init(effect: UIBlurEffect(style: .light)).then {
            $0.layer.masksToBounds = true
            $0.layer.cornerRadius = 12
        }

        lazy var btn: UIButton = .init(type: .system).then {
            $0.tintColor = R.color.text_000000()
            $0.titleLabel?.font = .systemFont(ofSize: 14, weight: .semibold)
            $0.setTitle(String.localization.localized("AA0470", note: "操作指引视频"), for: .normal)
            $0.setImage(R.image.family_addDeviceGuideBanner_arrow(), for: .normal)
            $0.isUserInteractionEnabled = false
        }

        lazy var bgImageView: UIImageView = .init(image: R.image.family_addDeviceGuideBanner_bg()).then {
            $0.contentMode = .scaleToFill
        }

        lazy var deviceImageView: UIImageView = .init(image: R.image.family_addDeviceGuideBanner_camera()).then {
            $0.contentMode = .scaleAspectFill
        }

        init() {
            super.init(frame: .zero)
            self.addSubview(self.visualEffectView)
            self.visualEffectView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
                make.height.equalTo(80)
            }

            self.addSubview(self.btn)
            self.btn.snp.makeConstraints { make in
                make.centerY.equalToSuperview()
                make.trailing.equalToSuperview().offset(-12)
            }

            self.addSubview(self.deviceImageView)
            self.deviceImageView.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(24)
                make.top.bottom.equalToSuperview()
            }

            self.addSubview(self.bgImageView)
            self.bgImageView.snp.makeConstraints {
                $0.top.bottom.trailing.equalToSuperview()
            }

            self.addGestureRecognizer(self.tapGesture)
            self.tapGesture.tapPublisher.map({ $0.state == .ended }).sink { [weak self] flag in
                if !flag { return }
                self?.didTapObservable.send(())
            }.store(in: &self.anyCancellables)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        override func layoutSubviews() {
            super.layoutSubviews()
            self.btn.exchangedPoistionWithTitleLabelAndImageView(margin: 6)
        }
    }
}
