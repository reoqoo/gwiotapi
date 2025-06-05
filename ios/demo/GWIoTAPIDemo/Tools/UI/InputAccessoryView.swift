//
//  InputAccessoryView.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 18/9/2023.
//

import UIKit

class InputAccessoryView: UIToolbar {

    lazy var doneButtonItem: UIBarButtonItem = .init(title: String.localization.localized("AA0418", note: "完成"), style: .plain, target: nil, action: nil)

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    func setup() {
        self.barStyle = .default
        self.items = [
            UIBarButtonItem.init(barButtonSystemItem: .flexibleSpace, target: nil, action: nil),
            self.doneButtonItem
        ]
        self.snp.makeConstraints { make in
            make.height.equalTo(44)
        }
    }
}
