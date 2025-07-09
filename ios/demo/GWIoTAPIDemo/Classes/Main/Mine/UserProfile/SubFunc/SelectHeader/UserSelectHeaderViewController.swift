//
//  UserSelectHeaderViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 14/9/2023.
//

import UIKit
import IVAccountMgr
import RQCore
import RQCoreUI

extension UserSelectHeaderViewController {
    class CollectionViewCell: UICollectionViewCell {

        var imageURL: URL? {
            didSet {
                self.imageView.kf.setImage(with: self.imageURL, placeholder: ReoqooImageLoadingPlaceholder(), options: [.processor(ResizingImageProcessor(referenceSize: .init(width: 120, height: 120)))])
            }
        }

        lazy var imageView: UIImageView = .init()

        lazy var sketch: UIImageView = .init(image: R.image.userHeaderSelectedSketch()).then {
            $0.contentMode = .scaleAspectFill
            $0.isHidden = true
        }

        override init(frame: CGRect) {
            super.init(frame: frame)
            self.contentView.addSubview(self.imageView)
            self.imageView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
                make.width.height.equalTo(60)
            }

            self.contentView.addSubview(self.sketch)
            self.sketch.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }
        }

        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
}

class UserSelectHeaderViewController: PageSheetStyleViewController {

    @DidSetPublished var selectedHeaderURL: URL?

    var dataSources: [URL] = []

    var anyCancellables: Set<AnyCancellable> = []

    lazy var cancelBtn: UIButton = .init(type: .system).then {
        $0.setTitleColor(R.color.text_000000_90(), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0059", note: "取消"), for: .normal)
        $0.setBackgroundColor(R.color.background_FFFFFF_white()!, for: .normal)
    }

    lazy var flowLayout: UICollectionViewFlowLayout = .init().then {
        $0.scrollDirection = .vertical
        $0.estimatedItemSize = .init(width: 60, height: 60)
    }

    lazy var collectionView: UICollectionView = .init(frame: .zero, collectionViewLayout: self.flowLayout).then {
        $0.delegate = self
        $0.dataSource = self
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.register(CollectionViewCell.self, forCellWithReuseIdentifier: String.init(describing: CollectionViewCell.self))
    }
    
    lazy var titleLabel: UILabel = .init().then {
        $0.text = String.localization.localized("AA0284", note: "选择头像")
        $0.font = .systemFont(ofSize: 18, weight: .medium)
        $0.textColor = R.color.text_000000_90()
        $0.textAlignment = .center
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.contentView.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
            make.height.equalTo(64)
        }

        self.contentView.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(216)
        }

        let separator = UIView.init()
        separator.backgroundColor = R.color.background_F2F3F6_thinGray()
        self.contentView.addSubview(separator)
        separator.snp.makeConstraints { make in
            make.top.equalTo(self.collectionView.snp.bottom)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(8)
        }

        self.contentView.addSubview(self.cancelBtn)
        self.cancelBtn.snp.makeConstraints { make in
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-12)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(56)
            make.top.equalTo(separator.snp.bottom)
        }

        self.collectionView.publisher(for: \.contentSize).sink(receiveValue: { [weak self] size in
            self?.collectionView.snp.updateConstraints({ make in
                make.height.equalTo(size.height)
            })
        }).store(in: &self.anyCancellables)

        self.requestHeaderListObservable()
            .sink(receiveCompletion: { _ in

            }, receiveValue: { [weak self] headers in
                self?.dataSources = headers
                self?.collectionView.reloadData()
            }).store(in: &self.anyCancellables)

        self.cancelBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.dismiss(animated: true)
        }).store(in: &self.anyCancellables)
    }

    override func layoutContentView() {
        self.contentView.snp.makeConstraints { make in
            make.leading.trailing.equalToSuperview()
            make.bottom.equalToSuperview().offset(16)
        }
    }

}

// MARK: Observable
extension UserSelectHeaderViewController {
    func requestHeaderListObservable() -> AnyPublisher<[URL], Swift.Error> {
        Deferred {
            Future<[URL], Swift.Error> { promise in
                RQCore.Agent.shared.ivAccountMgr.getOptionalHeaders {
                    let result = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                    if case let .failure(failure) = result {
                        promise(.failure(failure))
                    }
                    if case let .success(json) = result {
                        let urls = json["data"]["defaultList"].arrayValue.map { $0["url"].stringValue }.map({ URL.init(string: $0)! })
                        promise(.success(urls))
                    }
                }
            }
        }.eraseToAnyPublisher()
    }
}

extension UserSelectHeaderViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int { self.dataSources.count }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: CollectionViewCell.self), for: indexPath) as! CollectionViewCell
        cell.imageURL = self.dataSources[indexPath.item]
        cell.sketch.isHidden = cell.imageURL != AccountCenter.shared.currentUser?.profileInfo?.headUrl
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let headerURL = self.dataSources[indexPath.item]
        self.modifyHeader(headerURL)
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        .init(top: 0, left: 16, bottom: 16, right: 16)
    }
}

// MARK: Helper
extension UserSelectHeaderViewController {
    func modifyHeader(_ url: URL) {
        let hud = MBProgressHUD.showLoadingHUD_DispatchOnMainThread(isMask: true)
        AccountCenter.shared.currentUser?.modifyUserInfoPublisher(header: url.absoluteString, nick: nil, oldPassword: nil, newPassword: nil)
            .sink(receiveCompletion: {
                hud.hideDispatchOnMainThread()
                guard case let .failure(err) = $0 else { return }
                MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            }, receiveValue: {[weak self] profileInfo in
                self?.selectedHeaderURL = url
                self?.dismiss(animated: true)
            }).store(in: &self.anyCancellables)
    }
}
