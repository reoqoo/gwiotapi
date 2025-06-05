//
//  MessageCenterViewController+CollectionViewCewll.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/2/2024.
//

import Foundation

extension MessageCenterViewController {
    class CollectionViewCell: UICollectionViewCell {

        weak var controller: MessageCenterViewControllerChildren? {
            didSet {
                oldValue?.view.removeFromSuperview()
                guard let controller = self.controller else { return }
                self.contentView.addSubview(controller.view)
                controller.view.snp.makeConstraints { make in
                    make.edges.equalToSuperview()
                }
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        override init(frame: CGRect) {
            super.init(frame: frame)
        }

    }

}
