//
//  CommonEmptyView.swift
//  Reoqoo
//
//  Created by chenchangxin on 2023/8/22.
//

import Foundation

/// 通用列表空页面
class CommonEmptyView: UIView {
    
    /// 字体颜色，默认black + alpha 0.6
    var textColor: UIColor? = R.color.text_000000_60()
    
    /// 字体大小，默认16 + regular
    var font: UIFont = .systemFont(ofSize: 16, weight: .regular)
    
    // MARK: private
    private lazy var imageView = UIImageView().then {
        $0.backgroundColor = .clear
    }
    
    private lazy var textLabel = UILabel().then {
        $0.backgroundColor = .clear
        $0.textAlignment = .center
        $0.textColor = self.textColor
        $0.font = self.font
    }
    
    init(image: UIImage, text: String) {
        super.init(frame: .zero)
        setupUI(image: image, text: text)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI(image: UIImage, text: String) {
        imageView.image = image
        textLabel.text = text
        
        addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview().offset((text.isEmpty ? 0 : -50))
            make.width.height.equalTo(0)
        }
        
        addSubview(textLabel)
        textLabel.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom)
            make.left.equalTo(16)
            make.right.equalTo(-16)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let imageW = self.width*200/375
        imageView.snp.updateConstraints { make in
            make.width.height.equalTo(imageW)
        }
    }
}
