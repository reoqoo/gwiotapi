//
//  ReoqooAlertViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 27/7/2023.
//

import Foundation
import RQCore

// 一些通用的常见弹框展示写在这里
extension ReoqooAlertViewController {

    // 这个 attributedText 文案在很多地方有用到, 所以设计为类计算属性, 供多个地方使用
    static var agreementAttributedContent: NSMutableAttributedString {
        let content = String.localization.localized("AA0545", note: "我已阅读并同意《XXXXXXXX用户协议》和《隐私政策》")
        let rangeOfAggrementPart = (content as NSString).range(of: String.localization.localized("AA0570", note: "《XXXXXXXX用户协议》"))
        let rangeOfPrivacyPart = (content as NSString).range(of: String.localization.localized("AA0408", note: "《隐私政策》"))
        let res = NSMutableAttributedString.init(string: content)
        
        /// 统一添加样式
        res.addAttributes([.foregroundColor: R.color.text_000000_90()!, .font: UIFont.systemFont(ofSize: 12)], range: .init(location: 0, length: content.count))
        
        // 链接部分
        // 用户协议
        res.addAttributes([.foregroundColor: R.color.text_link_4A68A6()!, .link: StandardConfiguration.shared.usageAgreementURL as NSURL], range: rangeOfAggrementPart)
        // 隐私协议
        res.addAttributes([.foregroundColor: R.color.text_link_4A68A6()!, .link: StandardConfiguration.shared.privacyPolicyURL as NSURL], range: rangeOfPrivacyPart)

        return res
    }

    /// 显示同意协议弹框
    /// - Parameters:
    ///   - controller: 在哪个控制器模态弹出
    ///   - agreeClickHandler: 同意按钮点击
    ///   - urlClickHandler: url 点击
    static func showUsageAgreementAlert(withPresentedViewController controller: UIViewController, agreeClickHandler: ReoqooAlertViewController.ActionHandler?, urlClickHandler: ((URL)->())?) {

        let content = ReoqooAlertViewController.agreementAttributedContent
        content.addAttributes([.foregroundColor: R.color.text_000000_90()!, .font: UIFont.systemFont(ofSize: 16)], range: .init(location: 0, length: content.length))
        let attributedContent: ReoqooAlertViewController.Content = .attributedString(content)
        let alertViewController = ReoqooAlertViewController.init(alertTitle: .none, alertContent: attributedContent)
        alertViewController.attributeTextLinkOnClickHandler = urlClickHandler

        alertViewController.addAction(.init(title: String.localization.localized("AA0011", note: "不同意"), style: .custom, color: R.color.text_link_4A68A6(), font: .systemFont(ofSize: 16, weight: .medium)))
        alertViewController.addAction(.init(title: String.localization.localized("AA0010", note: "同意"), style: .custom, color: R.color.text_link_4A68A6(), font: .systemFont(ofSize: 16, weight: .medium), handler: agreeClickHandler))

        controller.present(alertViewController, animated: true)
    }

    static var attributedTitleOfUsageAgreement: NSMutableAttributedString {
        let content = String.localization.localized("AA0045", note: "用户服务协议和隐私政策概要")
        let attributedString = NSMutableAttributedString.init(string: content)
        attributedString.addAttributes([.font: UIFont.systemFont(ofSize: 18, weight: .medium), .foregroundColor: R.color.text_000000_90()!], range: NSRange.init(location: 0, length: content.count))
        return attributedString
    }

    static var attributedContentOfUsageAgreement: NSMutableAttributedString {

        let content_userAgreement = String.localization.localized("AA0570", note: "《XXXXXXXX用户协议》")
        let content_privacyAgreement = String.localization.localized("AA0569", note: "《隐私协议》")
        let content = String.localization.localized("AA0568", note: "欢迎您使用XXXXXXXX！XXXXXXXX是由深圳智多豚物联技术有限公司（以下简称“我们”）研发和运营的在线音视频平台，我们将通过《隐私政策》和《用户协议》帮助您了解我们收集、使用、存储和共享个人信息的情况，以及您所享有的相关权利。\n为了向您提供视频直播、视频回放、收藏订阅、社区互动等服务，我们需要收集您的视频录像、视频图片、设备信息、操作日志等个人信息；\n您可以在个人中心访问、更正、删除您的个人信息并管理您的授权；\n我们会采用业界领先的安全技术保护好您的个人信息。\n您可以通过阅读完整版%@和%@了解详细信息。\n如您同意，请点击“同意”接受我们的服务。", args: content_userAgreement, content_privacyAgreement)
        let res = NSMutableAttributedString.init(string: content)
        
        let rangeOfAggrementPart = (content as NSString).range(of: content_userAgreement, options: .backwards)
        let rangeOfPrivacyPart = (content as NSString).range(of: content_privacyAgreement, options: .backwards)
        
        /// 统一添加样式
        res.addAttributes([.foregroundColor: R.color.text_000000_90()!, .font: UIFont.systemFont(ofSize: 16)], range: .init(location: 0, length: content.count))
        
        // 链接部分
        // 用户协议
        res.addAttributes([.foregroundColor: R.color.text_link_4A68A6()!, .link: StandardConfiguration.shared.usageAgreementURL as NSURL], range: rangeOfAggrementPart)
        // 隐私协议
        res.addAttributes([.foregroundColor: R.color.text_link_4A68A6()!, .link: StandardConfiguration.shared.privacyPolicyURL as NSURL], range: rangeOfPrivacyPart)
        return res
    }

    /// 展示完整版的用户协议
    static func presentFullEditionUsageAgreement(withPresentedViewController controller: UIViewController, attributeTextLinkOnClickHandler: @escaping (URL)->()) {
        let attributedTitle: ReoqooAlertViewController.Content = .attributedString(ReoqooAlertViewController.attributedTitleOfUsageAgreement)
        let attributedContent: ReoqooAlertViewController.Content = .attributedString(ReoqooAlertViewController.attributedContentOfUsageAgreement)
        let alertViewController = ReoqooAlertViewController.init(alertTitle: attributedTitle, alertContent: attributedContent)
        alertViewController.attributeTextLinkOnClickHandler = attributeTextLinkOnClickHandler
        alertViewController.property.position = .bottom
        alertViewController.addAction(.init(title: String.localization.localized("AA0010", note: "同意"), style: .custom, color: R.color.text_link_4A68A6(), handler: {
            UserDefaults.standard.set(Bundle.appVersion, forKey: UserDefaults.GlobalKey.Reoqoo_AgreeToUsageAgreementOnAppVersion.rawValue)
            UserDefaults.standard.synchronize()
        }))
        
        controller.present(alertViewController, animated: true)
    }

}
