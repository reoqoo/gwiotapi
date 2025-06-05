//
//  BeginnerGuidanceView+BubbleView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 25/9/2023.
//

import Foundation

extension BeginnerGuidance {

    class BubbleView: UIView {

        let item: Item
        init(item: Item) {
            self.item = item
            super.init(frame: .zero)

            self.backgroundColor = .clear

            self.addSubview(self.label)
            self.label.snp.makeConstraints { make in
                make.top.equalToSuperview().offset(20)
                make.leading.equalToSuperview().offset(12)
                make.trailing.equalToSuperview().offset(-12)
            }

            self.addSubview(self.actionBtn)
            self.actionBtn.snp.makeConstraints { make in
                make.top.equalTo(self.label.snp.bottom)
                make.leading.trailing.bottom.equalToSuperview()
            }
        }

        private override init(frame: CGRect) { fatalError("init(frame: CGRect) has not been implemented") }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        lazy var label: UILabel = .init().then {
            $0.backgroundColor = .clear
            $0.text = self.item.content
            $0.textColor = self.item.contentColor
            $0.font = self.item.contentFont
            $0.numberOfLines = 0
        }

        lazy var actionBtn: UIButton = .init(type: .system).then {
            $0.setTitle(self.item.actionTitle, for: .normal)
            $0.tintColor = self.item.actionColor
            $0.titleLabel?.font = self.item.actionFont
            $0.contentEdgeInsets = .init(top: 9, left: 0, bottom: 9, right: 0)
        }
        
    }

}
