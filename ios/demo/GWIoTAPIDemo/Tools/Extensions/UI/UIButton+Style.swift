//
//  UIButton+Style.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 27/7/2023.
//

import Foundation

extension UIButton {

    /// 主题色背景按钮
    func setStyle_0 () {
        self.setBackgroundColor(R.color.brand()!, for: .normal)
        self.setTitleColor(R.color.text_FFFFFF()!, for: .disabled)

        self.setBackgroundColor(R.color.brandHighlighted()!, for: .highlighted)
        self.setTitleColor(R.color.text_FFFFFF()!, for: .disabled)
        
        self.setBackgroundColor(R.color.brandDisable()!, for: .disabled)
        self.setTitleColor(R.color.text_000000_38()!, for: .disabled)
    }
    
    /// 主题色字体按钮
    func setStyle_1() {
        self.setTitleColor(R.color.brand()!, for: .normal)
        self.setBackgroundColor(R.color.background_000000_5()!, for: .normal)

        self.setTitleColor(R.color.brandHighlighted()!, for: .highlighted)
        self.setBackgroundColor(R.color.background_DADADA()!, for: .highlighted)
        
        self.setTitleColor(R.color.brand()!.withAlphaComponent(0.2), for: .disabled)
        self.setBackgroundColor(R.color.background_000000_5()!, for: .disabled)
    }

    // 链接按钮风格
    func setLinkTextStyle() {
        self.setTitleColor(R.color.text_link_4A68A6()!, for: .normal)
        self.setTitleColor(R.color.text_000000_38()!, for: .disabled)
    }
}
