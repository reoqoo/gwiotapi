//
//  LanguageCode+.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/5/2025.
//

import Foundation
import GWIoTApi

extension GWIoTApi.LanguageCode {
    public var translate: String {
        switch self {
        case .zhHans:
            return "简体中文"
        case .en:
            return "English"
        case .vi:
            return "Tiếng Việt"
        case .th:
            return "ไทย"
        case .zhHant:
            return "繁体中文"
        case .id:
            return "Bahasa Indonesia"
        case .ja:
            return "日本語"
        case .ko:
            return "한국어"
        case .ms:
            return "Bahasa Melayu"
        default:
            return self.name
        }
    }

    static func from(nanoCode2 code: String) -> GWIoTApi.LanguageCode? {
        return Self.allCases.first { $0.isoCode == code }
    }

    static var current: LanguageCode {
        // 如果用户指定了当前语言, 就返回当前语言
        if let assignLanguage = Self.assignedLanguageCode, let res = LanguageCode.from(nanoCode2: assignLanguage) {
            return res
        }

        // 否则返回系统当前语言
        guard let languages = UserDefaults.standard.object(forKey: "AppleLanguages") as? [String],
              let currentLang = languages[safe_: 0] else { return .en }
        // ( zh-Hant-TW, yue-Hant-CN, zh-Hans-CN, en-CN, zh-Hant-HK, en-GB)
        let components = (currentLang.lowercased() as NSString).components(separatedBy: "-")
        guard let firstComponent = components.first else { return .en }
        let secondComponent = components[safe_: 1]
        for i in LanguageCase.allCases {
            // 先判断第一个 component, 如果符合, 直接返回
            if i.firstPossibleComponents.contains(firstComponent) && i.secondPossibleComponents.isEmpty {
                return i.code
            }
            // 如果第二个 component 非空, 也就是 "hant" 那一段, 包含则返回
            if let secondComponent = secondComponent, i.firstPossibleComponents.contains(firstComponent), i.secondPossibleComponents.contains(secondComponent) {
                return i.code
            }
        }
        return .en
    }

    static private let Reoqoo_UserDefaultKey_AssignLanguage: String = "Reoqoo_AssignLanguage"
    static public let didChanngeLanguageNotificaitonName: Notification.Name = .init(rawValue: "Reoqoo.didChanngeLanguageNotificaitonName")
    static public let didChanngeLanguageNotificaiton_UserInfoKey_isoCode: String = "Reoqoo.didChanngeLanguageNotificaitonUserInfoKey_isoCode"

    static var assignedLanguageCode: String? {
        get {
            return UserDefaults.standard.string(forKey: Reoqoo_UserDefaultKey_AssignLanguage)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: Reoqoo_UserDefaultKey_AssignLanguage)
            UserDefaults.standard.synchronize()
            NotificationCenter.default.post(name: Self.didChanngeLanguageNotificaitonName, object: nil, userInfo: [Self.didChanngeLanguageNotificaiton_UserInfoKey_isoCode: newValue as Any])
        }
    }
}

extension GWIoTApi.LanguageCode {
    public struct LanguageCase: CaseIterable {
        public let code: LanguageCode
        public let firstPossibleComponents: [String]
        public let secondPossibleComponents: [String]

        public static var allCases: [LanguageCode.LanguageCase] = [
            .init(code: .en, firstPossibleComponents: ["en"], secondPossibleComponents: []),
            .init(code: .th, firstPossibleComponents: ["th"], secondPossibleComponents: []),
            .init(code: .vi, firstPossibleComponents: ["vi"], secondPossibleComponents: []),
            .init(code: .de, firstPossibleComponents: ["de"], secondPossibleComponents: []),
            .init(code: .ko, firstPossibleComponents: ["ko"], secondPossibleComponents: []),
            .init(code: .fr, firstPossibleComponents: ["fr"], secondPossibleComponents: []),
            .init(code: .pl, firstPossibleComponents: ["pl"], secondPossibleComponents: []),
            .init(code: .it, firstPossibleComponents: ["it"], secondPossibleComponents: []),
            .init(code: .ru, firstPossibleComponents: ["ru"], secondPossibleComponents: []),
            .init(code: .ja, firstPossibleComponents: ["ja"], secondPossibleComponents: []),
            .init(code: .es, firstPossibleComponents: ["es"], secondPossibleComponents: []),
            .init(code: .pl, firstPossibleComponents: ["pl"], secondPossibleComponents: []),
            .init(code: .tr, firstPossibleComponents: ["tr"], secondPossibleComponents: []),
            .init(code: .fa, firstPossibleComponents: ["fa"], secondPossibleComponents: []),
            .init(code: .id, firstPossibleComponents: ["id"], secondPossibleComponents: []),
            .init(code: .ms, firstPossibleComponents: ["ms"], secondPossibleComponents: []),
            .init(code: .cs, firstPossibleComponents: ["cs"], secondPossibleComponents: []),
            .init(code: .sk, firstPossibleComponents: ["sk"], secondPossibleComponents: []),
            .init(code: .nl, firstPossibleComponents: ["nl"], secondPossibleComponents: []),
            .init(code: .gr, firstPossibleComponents: ["gr"], secondPossibleComponents: []),
            .init(code: .zhHans, firstPossibleComponents: ["zh", "yue"], secondPossibleComponents: ["hans"]),   // 中文简体, 主要特征是 hans
            .init(code: .zhHant, firstPossibleComponents: ["zh", "yue"], secondPossibleComponents: ["hant"]),   // 中文繁体, 主要特征是 hant
        ]
    }
}
