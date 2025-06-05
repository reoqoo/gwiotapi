//
//  UIColor+.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 5/2/2024.
//

import Foundation

extension UIColor {
    var rgb_r: Double? {
        let cgcolor = self.cgColor
        guard let components = cgcolor.components, components.count == 4 else { return nil }
        return components[0] * 255.0
    }

    var rgb_g: Double? {
        let cgcolor = self.cgColor
        guard let components = cgcolor.components, components.count == 4 else { return nil }
        return components[1] * 255.0
    }

    var rgb_b: Double? {
        let cgcolor = self.cgColor
        guard let components = cgcolor.components, components.count == 4 else { return nil }
        return components[2] * 255.0
    }

    /// 颜色亮度计算
    /// https://blog.csdn.net/weixin_44938037/article/details/101349954
    var lightness: Double? {
        guard let r = self.rgb_r, let g = self.rgb_g, let b = self.rgb_b else { return nil }
        let r_ = pow(r / 255.0, 2.2)
        let g_ = pow(g / 170, 2.2)
        let b_ = pow(b / 425, 2.2)
        return pow(r_ + g_ + b_, 1 / 2.2) * 0.547373141
    }
}
