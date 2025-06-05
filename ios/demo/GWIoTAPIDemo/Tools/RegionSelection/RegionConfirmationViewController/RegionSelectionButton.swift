//
//  RegionSelectionButton.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit

class RegionSelectionButton: UIButton {

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    lazy var currentRegionLabel: UILabel = {
        let res = UILabel.init()
        res.textColor = R.color.text_000000_90()!
        res.font = .systemFont(ofSize: 16)
        return res
    }()

    lazy var arrowImageView: UIImageView = .init(image: R.image.commonArrowRightStyle1())

    func setup() {
        self.addSubview(self.currentRegionLabel)
        self.currentRegionLabel.snp.makeConstraints { make in
            make.leading.equalToSuperview()
            make.bottom.equalToSuperview().offset(-8)
        }

        self.addSubview(self.arrowImageView)
        self.arrowImageView.snp.makeConstraints { make in
            make.trailing.equalToSuperview()
            make.bottom.equalToSuperview().offset(-8)
        }

        let line: UIView = .init()
        line.backgroundColor = R.color.lineInputDisable()
        self.addSubview(line)
        line.snp.makeConstraints { make in
            make.bottom.equalToSuperview()
            make.height.equalTo(0.5)
            make.leading.trailing.equalToSuperview()
        }
    }

    override func setTitle(_ title: String?, for state: UIControl.State) {
        self.currentRegionLabel.text = title
    }

    override var isEnabled: Bool {
        didSet {
            self.currentRegionLabel.textColor = self.isEnabled ? R.color.text_000000_90() : R.color.text_000000_60()
            self.arrowImageView.isHidden = !self.isEnabled
        }
    }

}
