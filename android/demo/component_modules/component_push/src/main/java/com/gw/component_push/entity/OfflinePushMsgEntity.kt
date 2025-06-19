package com.gw.component_push.entity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/5/31 20:48
 * Description: OfflinePushMsgEntity
 */
data class OfflinePushMsgEntity(
    /**
     * {
     *   "deviceId":"设备ID",       // 设备ID
     *   "pushType":274877906944,   // 推送类型, bit38
     *   "pushTime":1706789498,     // 推送时间
     *   "pushContent":{            // 推送内容
     *     "type":"推送子类型",      // 推送子类型："event", ...
     *     "value":"推送内容",       // 推送内容，目前手势识别有使用
     *     "flag":0,                // 推送选项，0：全推送，1：只推主人，默认全推送
     *     "alarmType":1,     // 可选，当type为event时
     *     "alarmId":"aaaaa", // 可选，当type为event时
     *   }
     * }
     */
    // 设备ID
    val deviceId: String,
    // 推送类型, bit38
    val pushType: Long,
    // 推送时间
    val pushTime: Long,
    // 推送内容
    val pushContent: OfflinePushContentEntity
)

data class OfflinePushContentEntity(
    // 推送子类型："event", ...
    val Type: String,
    // 推送内容，目前手势识别有使用
    val value: String,
    // 推送选项，0：全推送，1：只推主人，默认全推送
    val flag: Int,
    // 可选，当type为event时
    val alarmType: Int,
    // 可选，当type为event时
    val alarmId: String
)