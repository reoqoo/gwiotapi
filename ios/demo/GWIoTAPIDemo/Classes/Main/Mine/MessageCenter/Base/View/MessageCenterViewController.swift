//
//  MessageCenterViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 18/9/2023.
//

import UIKit
import RQCore

protocol MessageCenterViewControllerChildren: UIViewController {
    var emptyDataPlaceholder: UIView { get }
}

extension MessageCenterViewControllerChildren {
    var emptyDataPlaceholder: UIView {
        .init().then {
            let imageView = UIImageView.init(image: R.image.messageCenterEmptyData())
            $0.addSubview(imageView)
            let label = UILabel.init()
            label.text = String.localization.localized("AA0228", note: "暂时没有消息哦")
            label.font = .systemFont(ofSize: 14)
            label.textColor = R.color.text_000000_60()
            label.textAlignment = .center
            $0.addSubview(label)
            imageView.snp.makeConstraints { make in
                make.top.leading.trailing.equalToSuperview()
            }
            label.snp.makeConstraints { make in
                make.top.equalTo(imageView.snp.bottom)
                make.leading.trailing.bottom.equalToSuperview()
            }
        }
    }
}

class MessageCenterViewController: BaseViewController {

    lazy var sweepUnreadBarButtonItem: UIBarButtonItem = .init(image: R.image.messageCenterClear()!, style: .done, target: nil, action: nil)

    private var anyCancellables: Set<AnyCancellable> = []

    lazy var scrollableTabBarItems: [ScrollableTabBar.Item] = [
        .init(title: String.localization.localized("AA0604", note: "系统通知"), font: .systemFont(ofSize: 16)),
        .init(title: String.localization.localized("AA0601", note: "活动福利"), font: .systemFont(ofSize: 16))
    ]

    lazy var scrollableTabbar: ScrollableTabBar = .init().then {
        $0.items = self.scrollableTabBarItems
        $0.spacing = 24
        $0.delegate = self
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.contentInset = .init(top: 0, left: 16, bottom: 0, right: 16)
        $0.bottomLineStyle = .show(color: R.color.brand()!, widthDescription: .fixed(24), height: 5, radius: 2.5)
    }

    lazy var collectionViewFlowlayout: UICollectionViewFlowLayout = .init().then {
        $0.sectionInset = .zero
        $0.scrollDirection = .horizontal
        $0.minimumLineSpacing = 0
        $0.minimumInteritemSpacing = 0
    }

    lazy var collectionView: UICollectionView = .init(frame: .zero, collectionViewLayout: self.collectionViewFlowlayout).then {
        $0.delegate = self
        $0.dataSource = self
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.isPagingEnabled = true
        $0.alwaysBounceHorizontal = false
        $0.bounces = false
        $0.register(CollectionViewCell.self, forCellWithReuseIdentifier: String.init(describing: CollectionViewCell.self))
    }

    let systemMessageViewController = SystemMessageViewController.init()
    let welfareActivityViewController = WelfareActivityViewController.init()

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        self.addChild(self.systemMessageViewController)
        self.addChild(self.welfareActivityViewController)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0225", note: "消息中心")

        self.setNavigationBarBackground(R.color.background_FFFFFF_white()!)

        self.navigationItem.rightBarButtonItem = self.sweepUnreadBarButtonItem

        self.view.addSubview(self.scrollableTabbar)
        self.scrollableTabbar.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(48)
        }

        self.view.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.top.equalTo(self.scrollableTabbar.snp.bottom)
            make.leading.trailing.bottom.equalToSuperview()
        }

        self.scrollableTabbar.linkageScrollView = self.collectionView

        // 点击已读
        self.sweepUnreadBarButtonItem.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.systemMessageViewController.vm.sweepAllUnread()
        }).store(in: &self.anyCancellables)
        
        // 监听`系统消息`未读消息数量
        [MessageCenter.shared.$numberOfNewFirmwareMessages,
            MessageCenter.shared.$numberOfUnreadSystemMessages,
            MessageCenter.shared.$numberOfUnreadAppNewVersionMessages]
            .combineLatest()
            .map {
                $0.reduce(0, +)
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] numberOfSysUnreadMsg in
                if numberOfSysUnreadMsg <= 0 {
                    self?.scrollableTabbar.hideBadge(0)
                }else{
                    self?.scrollableTabbar.showBadgeAtIndex(0)
                }
            }.store(in: &self.anyCancellables)

        // 监听`福利活动`未读消息数量
        MessageCenter.shared.$numberOfUnreadWelfareActivityMessages
            .sink { [weak self] num in
                if num <= 0 {
                    self?.scrollableTabbar.hideBadge(1)
                }else{
                    self?.scrollableTabbar.showBadgeAtIndex(1)
                }
            }.store(in: &self.anyCancellables)

        // 手动发起请求
        MessageCenter.shared.manualCheckUnreadMsgCountSwitchSubject.send(true)
    }
    
}

extension MessageCenterViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.children.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: CollectionViewCell.self), for: indexPath) as! CollectionViewCell
        cell.controller = self.children[safe_: indexPath.item] as? MessageCenterViewControllerChildren
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        collectionView.bounds.size
    }
}

extension MessageCenterViewController: TabBarDelegate {
    func reoqooTabBar(_ tabbar: ScrollableTabBar, didSelectItem item: ScrollableTabBar.Item, atIndex: Int) {
        self.collectionView.setContentOffset(.init(x: self.collectionView.bounds.width * Double(atIndex), y: 0), animated: true)
    }
}
