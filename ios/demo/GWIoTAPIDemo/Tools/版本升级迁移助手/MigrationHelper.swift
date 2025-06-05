//
//  MigrationHelper.swift
//  Reoqoo
//
//  Created by xiaojuntao on 14/8/2024.
//

import Foundation

/*
 需要考虑的情况
 1. 全新安装:
    将前面所有迁移操作转换为Record记录下来, 算作已执行, 不执行, 退出
    下次再启动, 就不会执行
 2. 正常逐代升级, 跨代升级
    根据保存起来的执行记录, 筛选出需要执行的迁移操作, 对迁移操作排序, 执行迁移, 保存执行记录
 3. 无升级
    在筛选步骤中会因为没有迁移操作而退出
 */
class MigrateHelper {

    /// 迁移操作block
    /// 返回值表示此次操作是否, 该值会被记录下来, 如果返回false, 下次app启动时会再次执行
    typealias MigrationBlock = (_ oldVersion: String) -> Bool
    typealias Migration = (version: String, operation: MigrationBlock)

    /// 迁移操作集合
    static let migrations: [Migration] = [("1.5", migration_15)]

    static func migrate() {

        // 是否新安装
        // Reoqoo_AgreeToUsageAgreementOnAppVersion 这个键从 1.0 版本已经存在, 点击了同意使用后会将版本号记录下来, 所以可以作为全新安装的依据
        let isNewInstall = UserDefaults.standard.string(forKey: UserDefaults.GlobalKey.Reoqoo_AgreeToUsageAgreementOnAppVersion.rawValue) == nil
        if isNewInstall {
            // 将 Self.migrations 中所有迁移操作都记录下来, 以便下次再打开App这些操作都不需要执行了
            let jsonData = try? Self.migrations.map({ Record.init(version: $0.version, didExecutedMigrate: true) }).encoded()
            UserDefaults.standard.set(jsonData, forKey: UserDefaults.GlobalKey.Reoqoo_MigrationRecord.rawValue)
            UserDefaults.standard.synchronize()
            logInfo("[\(MigrateHelper.self)] 全新安装, 不需执行迁移")
            return
        }

        // 取得迁移操作的执行记录
        var records: [MigrateHelper.Record] = (try? UserDefaults.standard.data(forKey: UserDefaults.GlobalKey.Reoqoo_MigrationRecord.rawValue)?.decoded(as: [MigrateHelper.Record].self)) ?? []
        // 已经执行过迁移的版本
        let setOfVersionsThatDidExecuted: Set<String> = records.reduce(into: Set<String>()) { partialResult, record in
            if record.didExecutedMigrate {
                partialResult.insert(record.version)
            }
        }
        // 最近执行迁移的版本
        // 如果没有, 表示该用户第一次升级到1.5版本, 直接返回 1.4
        let latestMigrateVersion = setOfVersionsThatDidExecuted.sorted { $0.compareAsVersionString($1) == .older }.last ?? "1.4"

        // 筛选出需要执行的 migrations
        var migrations = self.migrations.filter { (version: String, operation: MigrationBlock) in
            !setOfVersionsThatDidExecuted.contains(version)
        }

        // 没有需要迁移
        if migrations.isEmpty { 
            logInfo("[\(MigrateHelper.self)] 没有可执行的迁移操作")
            return
        }

        // 按版本号大小排序, 从低到高, 确保 migrate block 按版本号顺序执行
        migrations.sort { $0.version.compareAsVersionString($1.version) == .older }

        // 执行迁移block
        for (version, block) in migrations {
            let flag = block(latestMigrateVersion)
            // 先移除 version 相同的 record
            records.removeAll { $0.version == version }
            // 新增 record
            records.append(.init(version: version, didExecutedMigrate: flag))
        }

        // 执行完, 持久化 records
        let jsonData = try? records.encoded()
        UserDefaults.standard.set(jsonData, forKey: UserDefaults.GlobalKey.Reoqoo_MigrationRecord.rawValue)
        UserDefaults.standard.synchronize()
    }
}

extension MigrateHelper {
    struct Record: Codable {
        let version: String
        var didExecutedMigrate: Bool
    }
}

extension MigrateHelper {
    static var migration_15: MigrationBlock = { oldVersion in

        let theDocumentPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!

        var suc = true

        // 检查 `Documents/ReoqooUsers` 是否存在, 不存在就算了
        let oldUsersDirectoryName = "ReoqooUsers"
        let oldReoqooUsersDirectoryPath = theDocumentPath + "/" + oldUsersDirectoryName

        // 创建 `Library/Application Support/com.reoqoo` 文件夹
        if !FileManager.default.fileExists(atPath: UIApplication.reoqooDirectoryPath) {
            do {
                try FileManager.default.createDirectory(atPath: UIApplication.reoqooDirectoryPath, withIntermediateDirectories: true)
            } catch let err {
                logError("[\(MigrateHelper.self)] 执行1.5迁移时遇到错误, 创建\(UIApplication.reoqooDirectoryPath) 文件夹失败")
                suc = false
            }
        }

        // 将本来在 `Documents/ReoqooUsers` 中的内容迁移到 `Application Support/com.reoqoo/users` 中
        if FileManager.default.fileExists(atPath: oldReoqooUsersDirectoryPath) {
            do {
                try FileManager.default.copyItem(atPath: oldReoqooUsersDirectoryPath, toPath: UIApplication.usersInfoDirectoryPath)
                // 移成了, 干掉原来的
                try FileManager.default.removeItem(atPath: oldReoqooUsersDirectoryPath)
            } catch let err {
                logError("[\(MigrateHelper.self)] 执行1.5迁移时遇到错误, 移动用户信息文件夹失败")
                suc = false
            }
        }

        if suc {
            logInfo("[\(MigrateHelper.self)] 1.5迁移操作执行完成, 文件夹\(oldReoqooUsersDirectoryPath) 迁移至 \(UIApplication.usersInfoDirectoryPath)")
        }

        return suc
    }
}
