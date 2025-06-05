//
//  Localization.swift
//  RQCore
//
//  Created by xiaojuntao on 25/9/2024.
//

import Foundation
import Combine
import GWIoTApi

extension String {

    public class Localization {

        let bundle: Bundle
        /// which bundle's localization files want load
        init(bundle: Bundle, languageIsoCode: String) {
            self.bundle = bundle
            self.languageCode = languageIsoCode
            self.update(languageCode: self.languageCode)

            NotificationCenter.default.publisher(for: LanguageCode.didChanngeLanguageNotificaitonName).sink { [weak self] _ in
                self?.languageCode = LanguageCode.current.isoCode
            }.store(in: &self.anyCancellables)
        }

        var languageCode: String {
            didSet {
                self.update(languageCode: self.languageCode)
            }
        }

        /// 指向app包中的 .lproj 文件
        var localizationFile: Bundle?

        var anyCancellables: Set<AnyCancellable> = []

        private func update(languageCode: String) {
            if let path = self.bundle.path(forResource: languageCode, ofType: "lproj") {
                self.localizationFile = Bundle.init(path: path)
                return
            }
            self.localizationFile = nil
            logError("无法从bundle\(self.bundle)中加载 \(languageCode).lproj 文件")
        }

        /// 语言国际化
        ///
        ///     Localized("AA0111", note: "密码错误")
        ///
        /// - Parameters:
        ///   - key: 键值
        ///   - note: 注释信息，为便于阅读代码请传入中文注释
        ///   - args: 字符串中要替换的参数值, 例如: "My name is %@, i'm %ld years old"
        /// - Returns: 翻译文案
        public func localized(_ key: String, note: String, args: String...) -> String {
            return self.localized(key, note: note, args: args.map({ $0 }))
        }

        public func localized(_ key: String, note: String, args: [String]) -> String {
            var translation = self.localizationFile?.localizedString(forKey: key, value: nil, table: "Localizable") ?? note
            if translation.isEmpty || translation == key || translation == "null" {
                translation = note
            }
            translation = String(format: translation, arguments: args)
            return translation
        }

    }

    static var localization: Localization = {
        return .init(bundle: Bundle.main, languageIsoCode: LanguageCode.current.isoCode)
    }()
}
