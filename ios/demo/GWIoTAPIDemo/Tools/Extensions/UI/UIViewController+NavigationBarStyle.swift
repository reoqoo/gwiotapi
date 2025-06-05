//
//  UIViewController+NavigationBarStyle.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 7/8/2023.
//

import Foundation

extension UIViewController {

    /// 修改导航栏颜色
    /// - Parameters:
    ///   - backgroundColor: 背景色
    ///   - tintColor: title字体颜色, 返回按钮颜色
    func setNavigationBarBackground(_ backgroundColor: UIColor, tintColor: UIColor = R.color.text_000000_90()!) {
        self.navigationController?.navigationBar.tintColor = tintColor
        self.navigationController?.navigationBar.barTintColor = backgroundColor
        // 背景颜色和细节
        let appearance = UINavigationBarAppearance()
        // 去掉navigationBar底部黑色线
        appearance.shadowImage = UIImage()
        appearance.shadowColor = .clear
        appearance.backgroundColor = backgroundColor
        appearance.backgroundEffect = nil
        appearance.titleTextAttributes = [.foregroundColor: tintColor]
        self.navigationController?.navigationBar.scrollEdgeAppearance = appearance
        self.navigationController?.navigationBar.standardAppearance = appearance
    }
    
    /// 设置导航栏渐变颜色
    func setNavigationBarGradientBackgrounds(startColor: UIColor, endColor: UIColor, startPoint: CGPoint = .init(x: 0, y: 0.5), endPoint: CGPoint = .init(x: 1, y: 0.5), tintColor: UIColor = R.color.text_000000_90()!) {
        let appearance = UINavigationBarAppearance()

        let navigationBarSize = self.navigationController?.navigationBar.frame.size ?? .init(width: UIScreen.main.bounds.width * 3, height: 64)

        let gradientLayer = CAGradientLayer.init()
        gradientLayer.frame = .init(origin: .zero, size: navigationBarSize)
        gradientLayer.colors = [startColor.cgColor, endColor.cgColor]
        gradientLayer.startPoint = startPoint
        gradientLayer.endPoint = endPoint
        let gradientImage = UIGraphicsImageRenderer.init(size: navigationBarSize).image { context in
            gradientLayer.render(in: context.cgContext)
        }

        appearance.backgroundImage = gradientImage
        // 去掉navigationBar底部黑色线
        appearance.shadowImage = UIImage()
        appearance.shadowColor = .clear
        appearance.backgroundEffect = nil
        appearance.titleTextAttributes = [.foregroundColor: tintColor]

        self.navigationController?.navigationBar.scrollEdgeAppearance = appearance
        self.navigationController?.navigationBar.standardAppearance = appearance
    }

}
