//
//  Codable+.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 25/7/2023.
//

import Foundation
import SwiftyJSON
import Codextended

/*
 为 SwiftyJSON.JSON 扩展, 将基础数据类型转换为 模型
 因为 SwiftyJSON 可作为任意基础数据类型的载体: jsonData, jsonString, dictionary, array. 所以以 SwiftyJSON 作为中间媒介, 为其扩展反序列化功能

 例如:
 jsonData:
 let j = JSON.init(data: jsonData)
 j.decode(as: Person.self)

 jsonString:
 let j = JSON.init(parseJSON: jsonString)
 j.decode(as: Person.self)
 */
extension SwiftyJSON.JSON {
    func decoded<T: Decodable>(as type: T.Type = T.self, using decoder: AnyDecoder = JSONDecoder()) throws -> T {
        let data = try self.rawData()
        return try decoder.decode(T.self, from: data)
    }
}
