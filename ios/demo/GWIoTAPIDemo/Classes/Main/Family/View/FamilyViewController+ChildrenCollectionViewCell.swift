//
//  FamilyViewController+ChildrenCollectionViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/2/2024.
//

import Foundation

extension FamilyViewController2 {
    class ChildrenCollectionViewCell: UICollectionViewCell {
        weak var controller: FamilyViewControllerChildren? {
            didSet {
                self.contentView.removeAllSubviews()
                guard let controller = self.controller else { return }
                self.contentView.addSubview(controller.view)
                controller.view.backgroundColor = .clear
                controller.view.snp.makeConstraints { make in
                    make.edges.equalToSuperview()
                }
            }
        }
    }
}
