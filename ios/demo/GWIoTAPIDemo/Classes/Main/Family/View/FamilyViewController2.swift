//
//  FamilyViewController2.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/8/2023.
//

import Foundation
import RQCore
import RQDeviceShared
import RQWebServices

import GWIoTApi

class FamilyViewController2: BaseViewController {

    let vm: ViewModel = .init()

    lazy var deviceSharePopupHelper: DeviceSharePopupHelper = .init()

    lazy var backgroundImageView = UIImageView().then {
        $0.image = R.image.family_bg()
    }
    
    weak var activeChild: FamilyViewControllerChildren?

    lazy var mj_header: MJCommonHeader = .init().then {
        $0.setRefreshingTarget(self, refreshingAction: #selector(self.mj_headerRefreshing(_:)))
    }

    private var canParentViewScroll: Bool = true
    private var canChildViewScroll: Bool = false

    lazy var scrollView = UIScrollView.init().then {
        $0.backgroundColor = .clear
        $0.contentInsetAdjustmentBehavior = .always
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.scrollsToTop = false
        $0.delegate = self
        $0.mj_header = self.mj_header
    }

    /// xxx的家
    lazy var headerView = FamilyHeaderView(frame: .zero)

    /// 公告栏位置
    lazy var bannerContainer: BannerContainer = .init()

    /// 新增设备引导Banner, 在没有设备时显示
    lazy var addDeviceGuideBanner: AddDeviceGuideBanner = .init()

    lazy var collectionViewFlowLayout: UICollectionViewFlowLayout = .init().then {
        $0.scrollDirection = .horizontal
        $0.sectionInset = .zero
        $0.minimumLineSpacing = 0
        $0.minimumInteritemSpacing = 0
    }
    /// 装 devicesViewController 的 Collectionview, 提供左右滑切换功能
    lazy var collectionView: UICollectionView = .init(frame: .zero, collectionViewLayout: self.collectionViewFlowLayout).then {
        $0.delegate = self
        $0.dataSource = self
        $0.backgroundColor = .clear
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.isPagingEnabled = true
        $0.register(ChildrenCollectionViewCell.self, forCellWithReuseIdentifier: String.init(describing: ChildrenCollectionViewCell.self))
    }

    lazy var tabView: ScrollableTabBar = .init().then {
        $0.snp.makeConstraints { make in
            make.height.equalTo(56)
        }
        $0.items = [.init(title: String.localization.localized("AA0048", note: "设备"))]
    }

    lazy var devicesViewController: DevicesViewController2 = .init()
    
    /// 用户数据加载中, 于用户名称处展示的菊花
    lazy var userInfoLoadingPlaceholder: UIActivityIndicatorView = .init().then {
        $0.hidesWhenStopped = true
    }

    private var anyCancellables: Set<AnyCancellable> = []

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        self.tabBarItem = .init(title: String.localization.localized("AA0041", note: "家庭"), image: R.image.tab_family_unselected()?.withRenderingMode(.alwaysOriginal), selectedImage: R.image.tab_family_selected()?.withRenderingMode(.alwaysOriginal))
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController?.setNavigationBarHidden(true, animated: false)
        
        self.view.backgroundColor = UIColor(white: 0, alpha: 0.05)

        self.view.addSubview(self.backgroundImageView)
        self.backgroundImageView.snp.makeConstraints { make in
            make.top.left.right.equalToSuperview()
        }

        self.view.addSubview(self.scrollView)
        self.scrollView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        let headerViewHeight = 44.0
        self.scrollView.addSubview(self.headerView)
        self.headerView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalToSuperview()
            make.height.equalTo(headerViewHeight)
            make.width.equalTo(self.view.snp.width)
        }

        self.scrollView.addSubview(self.userInfoLoadingPlaceholder)
        self.userInfoLoadingPlaceholder.snp.makeConstraints { make in
            make.centerY.equalTo(self.headerView)
            make.leading.equalToSuperview().offset(16)
        }
        
        // 公告栏
        self.scrollView.addSubview(self.bannerContainer)
        self.bannerContainer.snp.makeConstraints { make in
            make.top.equalTo(self.headerView.snp.bottom).offset(12)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.height.equalTo(40).priority(.init(249))
        }

        let tabViewHeight = 56.0
        self.scrollView.addSubview(self.tabView)
        self.tabView.snp.makeConstraints { make in
            make.top.equalTo(self.bannerContainer.snp.bottom).offset(12)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.height.equalTo(tabViewHeight)
        }

        self.scrollView.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.top.equalTo(self.tabView.snp.bottom)
            make.leading.trailing.bottom.equalToSuperview()
            let top = (AppEntranceManager.shared.keyWindow?.safeAreaInsets.top ?? 0) + tabViewHeight
            let bottom = self.view.safeAreaInsets.bottom + (self.tabBarController?.tabBar.frame.height ?? 0)
            make.height.equalTo(self.view.snp.height).offset(-(top + bottom))
        }

        self.tabView.linkageScrollView = self.collectionView

        self.addChildren([self.devicesViewController])

        self.eventSetup()
    }

    @objc func mj_headerRefreshing(_ header: MJCommonHeader) {
        self.vm.checkOutInviteMessage()
        self.activeChild?.pullToRefresh(completion: { [weak self] in
            self?.mj_header.endRefreshing()
        })
    }

    private func eventSetup() {
        // 添加按钮点击
        self.headerView.addButton.tapPublisher.sink { [weak self] in
            let popoverOptions: [PopoverOption] = [.arrowSize(.zero), .animationIn(0.2), .animationOut(0), .cornerRadius(16), .springDamping(1), .initialSpringVelocity(1), .showBlackOverlay(false), .shadowOffset(.init(width: 4, height: 4)), .shadowRadius(8)]
            let listView = PopoverListView.init(options: popoverOptions, items: [
                .init(image: nil, title: String.localization.localized("AA0049", note: "添加设备"), handler: {
                    Task {
                        try await GWIoT.shared.openBind(opts: .init(qrCodeValue: nil))
                    }
                }),
                .init(image: nil, title: String.localization.localized("AA0050", note: "分享设备"), handler: {
                    let vc = ShareDevicesListViewController.init()
                    self?.navigationController?.pushViewController(vc, animated: true)
                }),
                .init(image: nil, title: String.localization.localized("AA0051", note: "扫一扫"), handler: {
                    Task {
                        try await GWIoT.shared.openScanQRCodePage()
                    }
                })
            ], frame: .init(x: 0, y: 0, width: 128, height: 150), rowHeight: 50)

            guard let targetView = self?.headerView.addButton else { return }
            listView.show(fromView: targetView, inView: AppEntranceManager.shared.keyWindow!)
        }.store(in: &self.anyCancellables)

        // 新增设备引导banner被点击事件
        self.addDeviceGuideBanner.didTapObservable
            .sink(receiveValue: { [weak self] in
                let vc = WebViewController(url: URL.init(string: StandardConfiguration.shared.h5Host + "/h5/views/guide/index"))
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)

        // 监听昵称更新
        AccountCenter.shared.currentUser?.$profileInfo.sink(receiveValue: { [weak self] info in
            if let info = info {
                self?.headerView.title = String.localization.localized("AA0047", note: "%@的家", args: info.nickNameMask)
                self?.userInfoLoadingPlaceholder.stopAnimating()
            }else{
                self?.headerView.title = ""
                self?.userInfoLoadingPlaceholder.startAnimating()
            }
        }).store(in: &self.anyCancellables)

        // 查询到分享邀请新消息, 会触发此发布者
        self.vm.deviceShareInviteObservable.sink(receiveValue: { [weak self] inviteModel in
            // 显示邀请
            self?.showBannerIfNeed(inviteModel)
        }).store(in: &self.anyCancellables)

        // 收到分享邀请处理结果后移除banner
        self.vm.shareInviteHandlingResultObservable.sink(receiveValue: { [weak self] device_id, didBind in
            self?.removeInviteBanner(withDeviceId: device_id)
        }).store(in: &self.anyCancellables)

        // 查询到新的公告消息, 显示于首页顶部banner区域
        self.vm.headerBannerSubject.sink(receiveValue: { [weak self] bannerItem in
            self?.showBannerIfNeed(bannerItem)
        }).store(in: &self.anyCancellables)

        // 监听设备数量, 当设备列表为空时显示 `添加设备` banner
        DeviceManager.shared.$devices
            .map({ $0.count })
            .debounce(for: 1, scheduler: DispatchQueue.main)
            .removeDuplicates()
            .sink(receiveValue: { [weak self] devCount in
                guard let self else { return }
                if devCount == 0 {
                    self.insertBanner(self.addDeviceGuideBanner, animate: false)
                }else{
                    self.removeBanner(self.addDeviceGuideBanner)
                }
            }).store(in: &self.anyCancellables)
    }
    
    // 给 Children ViewController 调用, 以告知 self child ScrollView 被滑动了
    public func childViewControllerScrollViewDidScroll(_ scrollView: UIScrollView) {
        self.linkageScroll(parentScrollView: nil, childScrollView: scrollView)
    }
}

extension FamilyViewController2: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int { self.children.count }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: ChildrenCollectionViewCell.self), for: indexPath) as! ChildrenCollectionViewCell
        cell.controller = self.children[indexPath.item] as? FamilyViewControllerChildren
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize { collectionView.bounds.size }
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        guard self.scrollView == scrollView else { return }
        self.linkageScroll(parentScrollView: scrollView, childScrollView: nil)
        if self.tabView.frame.minY == 0 { return }
        // 当滑动到指定位置, 隐藏 bannerContainer 以及 headerView
        UIView.animate(withDuration: 0.2) {
            self.bannerContainer.alpha = scrollView.contentOffset.y >= self.tabView.frame.minY - scrollView.adjustedContentInset.top ? 0 : 1
            self.headerView.alpha = scrollView.contentOffset.y >= self.tabView.frame.minY - scrollView.adjustedContentInset.top ? 0 : 1
        }
    }
}

// MARK: Helper
extension FamilyViewController2 {

    // 将当前 active child 的 ScrollView 的滑动 self.scrollView 的滑动的联动起来
    func linkageScroll(parentScrollView: UIScrollView?, childScrollView: UIScrollView?) {
        if self.scrollView.contentSize.height == 0 || self.scrollView.size.height == 0 { return }
        // child scroll 是否有滑动空间
        let childCanNotScrollable = (self.activeChild?.mainScrollView?.contentSize.height ?? 0) <= (self.activeChild?.mainScrollView?.size.height ?? 0)
        let tabViewMinY = self.tabView.frame.minY
        if let parentScrollView = parentScrollView {
            if !self.canParentViewScroll {
                parentScrollView.contentOffset.y = tabViewMinY - parentScrollView.adjustedContentInset.top
                self.canChildViewScroll = true
            } else if parentScrollView.contentOffset.y >= tabViewMinY - parentScrollView.adjustedContentInset.top {
                parentScrollView.contentOffset.y = tabViewMinY - parentScrollView.adjustedContentInset.top
                // 如果 child scroll 没有可滑动空间, 就不强锁定 parentScrollView 不能滑动了
                if childCanNotScrollable { return }
                self.canParentViewScroll = false
                self.canChildViewScroll = true
            }
        }
        if let childScrollView = childScrollView {
            if !self.canChildViewScroll {
                childScrollView.contentOffset.y = -childScrollView.adjustedContentInset.top
            } else if childScrollView.contentOffset.y <= -childScrollView.adjustedContentInset.top {
                self.canChildViewScroll = false
                self.canParentViewScroll = true
            }
        }
    }

    func addChildren(_ children: [FamilyViewControllerChildren]) {
        for child in children {
            self.addChild(child)
        }
        self.activeChild = children.first
        self.collectionView.reloadData()
    }
}

// Banner显示相关方法
// Banner 显示规则:
// Banner视图要求遵循 FamilyViewControllerBanner 协议, 该协议定义了 Banner 的优先级, 类型等属性
// 当收到需要显示 Banner 的消息时, 执行 showBannerIfNeed(...)
// 在 showBannerIfNeed(...) 中, 会执行不同类型的 Banner 创建
// 在显示 Banner 的过程中, 只管关注 Banner 实例都是 FamilyViewControllerBanner 类型, 不会关注其本身的实际类型
// 取得 FamilyViewControllerBanner 实例后, 根据视图容器中当前显示的 banner, 优先级, 将 banner 插入到显示容器
extension FamilyViewController2 {
    
    // 显示公告Banner
    func showBannerIfNeed(_ bannerItem: IVBBSMgr.Banner) {
        let banner = self.createBanner(bannerItem)
        self.insertBanner(banner)
    }

    // 显示设备分享邀请Banner
    func showBannerIfNeed(_ inviteModel: MessageCenter.DeviceShareInviteModel) {
        let banner = self.createBanner(inviteModel)
        self.insertBanner(banner)
    }

    /// 创建公告banner
    func createBanner(_ bannerItem: IVBBSMgr.Banner) -> FamilyViewControllerBanner {
        let banner = NoticeBanner.init(bannerItem: bannerItem)
        banner.closeBtn
            .tapPublisher.sink(receiveValue: { [weak self, weak banner] _ in
                self?.removeBanner(banner)
            }).store(in: &self.anyCancellables)

        banner.didTapObservable
            .sink(receiveValue: { [weak self] url in
                let vc = WebViewController(url: url)
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)

        return banner
    }

    // 创建分享邀请banner
    func createBanner(_ inviteModel: MessageCenter.DeviceShareInviteModel) -> FamilyViewControllerBanner {
        let banner = ShareInviteBanner.init(inviteModel: inviteModel)
        banner.checkBtn.tapPublisher
            .sink(receiveValue: { [weak self, weak banner] in
                // 点击查看, 移除 banner
                self?.removeBanner(banner)
                // 执行检查方法, 检查分享码是否还有效, 通过则自动弹框
                self?.deviceSharePopupHelper.checkShare(inviteModel: inviteModel)
            }).store(in: &self.anyCancellables)

        banner.ignoreBtn.tapPublisher
            .sink(receiveValue: { [weak self] in
                // 点击忽略, 移除 banner
                self?.removeBanner(banner)
            }).store(in: &self.anyCancellables)
        
        return banner
    }

    func insertBanner(_ banner: FamilyViewControllerBanner, animate: Bool = true) {
        // 如果需要显示的banner 和 当前显示的 banner uniqueTag 相同, 不做进一步处理
        if let currentBanner = self.bannerContainer.banners.last, currentBanner.uniqueTag == banner.uniqueTag { return }
        self.bannerContainer.insertBanner(banner)
        
        // 这一步会将 _banners 中的元素以显示优先级排序, 返回显示优先级的一个
        let targetBanner = self.bannerContainer.updateDisplayIfNeed()
        // 如果需要 显示的banner 和 insertBanner 不是同一个, 就不执行显示动画了
        if targetBanner?.uniqueTag != banner.uniqueTag { return }
        
        if animate {
            targetBanner?.alpha = 0
            targetBanner?.transform = .init(translationX: 0, y: -128)
            UIView.animate(withDuration: 0.3) {
                targetBanner?.alpha = 1
                targetBanner?.transform = .identity
            }
        }else{
            targetBanner?.alpha = 1
            targetBanner?.transform = .identity
        }
    }

    func removeBanner(_ banner: FamilyViewControllerBanner?) {
        guard let banner = banner else { return }
        // 如果是邀请banner, 将消息置为已读
        if let banner = banner as? ShareInviteBanner {
            RQCore.Agent.shared.ivUserMsgMgr.updateUserMessageStatus(1, msgId: banner.inviteModel.msgId, responseHandler: nil)
        }
        self.bannerContainer.removeBanner(banner)
        UIView.animate(withDuration: 0.3) {
            banner.alpha = 0
            banner.transform = .init(translationX: 0, y: -128)
        } completion: { fin in
            self.bannerContainer.updateDisplayIfNeed()
        }
    }

    func removeInviteBanner(withDeviceId deviceId: String) {
        // 找
        let targetBanner = self.bannerContainer.banners.filter {
            guard let bannerView = $0 as? ShareInviteBanner else { return false }
            return bannerView.inviteModel.deviceId == deviceId
        }.first
        // 移除
        self.removeBanner(targetBanner)
    }
}
