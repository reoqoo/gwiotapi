//
//  LiveView+SubViews.swift
//  Reoqoo
//
//  Created by xiaojuntao on 18/3/2025.
//

import Foundation

extension LiveView.CamSwitchView {
    class CollectionViewCell: UICollectionViewCell {

        lazy var imageView: UIImageView = .init().then {
            $0.backgroundColor = R.color.background_placeholder0_4D4D4D()
        }

        override init(frame: CGRect) {
            super.init(frame: frame)

            self.contentView.addSubview(self.imageView)
            self.imageView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }

            self.contentView.layer.borderColor = R.color.border_38E667()?.cgColor
            self.contentView.layer.borderWidth = 1
            self.contentView.layer.cornerRadius = 8
            self.contentView.layer.masksToBounds = true
        }
        
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

        override var isSelected: Bool {
            didSet {
                super.isSelected = isSelected
                if self.isSelected {
                    self.contentView.layer.borderColor = R.color.border_38E667()?.cgColor
                }else{
                    self.contentView.layer.borderColor = R.color.background_FFFFFF_white()?.cgColor
                }
            }
        }
    }
}

extension LiveView {
    class CamSwitchView: UIView {

        var vm: LiveView.ViewModel

        var isCollapse: Bool = true {
            didSet {
                let offset = self.isCollapse ? 60 : 0
                self.collectionViewContainer.snp.updateConstraints { make in
                    make.bottom.equalTo(self.snp.bottom).offset(offset)
                }
                let image = self.isCollapse ? R.image.guardCollapseUp() : R.image.guardCollapseDown()
                self.displayButton.setImage(image, for: .normal)
                UIView.animate(withDuration: 0.3) {
                    self.layoutIfNeeded()
                    self.collectionViewContainer.alpha = self.isCollapse ? 0 : 1
                }
            }
        }

        lazy var displayButton: UIButton = .init().then {
            $0.setImage(R.image.guardCollapseUp(), for: .normal)
            $0.setBackgroundColor(R.color.background_000000_40()!, for: .normal)
            $0.layer.cornerRadius = 18
            $0.layer.masksToBounds = true
        }

        lazy var collectionViewContainer: UIView = .init().then {
            $0.backgroundColor = R.color.background_000000_40()
            $0.alpha = 0
        }

        lazy var flowLayout: AlignedCollectionViewFlowLayout = .init(horizontalAlignment: .justified, verticalAlignment: .center).then {
            $0.scrollDirection = .horizontal
        }

        lazy var collectionView: UICollectionView = .init(frame: .zero, collectionViewLayout: self.flowLayout).then {
            $0.register(CollectionViewCell.self, forCellWithReuseIdentifier: String.init(describing: CollectionViewCell.self))
            $0.delegate = self
            $0.dataSource = self
            $0.backgroundColor = .clear
            $0.allowsMultipleSelection = false
        }

        private var anyCancellables: Set<AnyCancellable> = []

        init(vm: LiveView.ViewModel) {
            self.vm = vm
            super.init(frame: .zero)

            self.addSubview(self.collectionViewContainer)
            self.collectionViewContainer.snp.makeConstraints { make in
                make.bottom.equalTo(self.snp.bottom).offset(60)
                make.leading.trailing.equalToSuperview()
                make.height.equalTo(60)
            }

            self.collectionViewContainer.addSubview(self.collectionView)
            self.collectionView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }

            self.addSubview(self.displayButton)
            self.displayButton.snp.makeConstraints { make in
                make.bottom.equalTo(self.collectionViewContainer.snp.top).offset(-8)
                make.trailing.equalToSuperview().offset(-16)
                make.width.height.equalTo(36)
            }

            self.displayButton.tapPublisher.sink { [weak self] in
                self?.isCollapse.toggle()
            }.store(in: &self.anyCancellables)

            // 监听画面发生变化
            Publishers.CombineLatest3(self.vm.$indexOfSelectedCam, self.vm.$numOfViews, self.vm.$snapshotImages.prepend([]))
                .debounce(for: 0.3, scheduler: DispatchQueue.main)
                .receive(on: DispatchQueue.main)
                .sink { [weak self] i, numOfViews, snapShotImages in
                    self?.collectionView.reloadData()
                }.store(in: &self.anyCancellables)
        }

        @MainActor required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

        private override init(frame: CGRect) {
            fatalError("init(frame:) has not been implemented")
        }
    }
}

extension LiveView.CamSwitchView: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.vm.numOfViews
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: CollectionViewCell.self), for: indexPath) as! CollectionViewCell
        cell.imageView.image = self.vm.snapshotImages[safe_: indexPath.item] ?? R.image.commonImageLoadingPlaceholder()
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.vm.processEvent(.updateSelectedCameraIndex(indexPath.item))
    }

    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        if indexPath.item == self.vm.indexOfSelectedCam {
            collectionView.selectItem(at: indexPath, animated: false, scrollPosition: .centeredVertically)
            // 调 collectionView 的 selectItem(at: IndexPath...) 方法不会触发 cell.isSelected, 因此需要手动调用一下
            cell.isSelected = true
        }else{
            collectionView.deselectItem(at: indexPath, animated: false)
            cell.isSelected = false
        }
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        .init(width: 77, height: 42)
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        16
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        .init(top: 0, left: 16, bottom: 0, right: 16)
    }
}

