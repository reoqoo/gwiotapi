//
//  MineCellModel.swift
//  Reoqoo
//
//  Created by chenchangxin on 2023/8/19.
//

import Foundation

extension MineViewController {
    class CellItem {
        var image: UIImage
        var title: String
        var action: () -> Void

        init(image: UIImage, title: String, action: @escaping ()->Void) {
            self.image = image
            self.title = title
            self.action = action
        }
    }
}
