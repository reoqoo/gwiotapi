package com.gw.component_push.entity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/5/18 10:56
 * Description: PushMsgEntity
 */

data class PushMsgContentEntity(
    /**
     * uuid : c9000000-0100-0000-580d-1400b96c9eff
     * origin : 031400005f1683acfc3480d768857880
     * topic : ev_alarm_trigger
     * t : 1605003782
     * data : {"deviceId":"031400005f1683acfc3480d768857880","alarmId":"42949674971605003782","alarmType":"1","bucketName":"iotvideo-pictstore-1259781373/2020-11-10/ap-guangzhou","alarmTime":1605003782,"alarmRecordMinTime":8}
     */

    val uuid: String?,
    val origin: String?,
    val topic: String?,
    val t: Long?,
    val data: PushMsgEntity
)

data class PushMsgEntity(
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
    val pushContent: PushContentEntity
)

/**
 * 推送详情
 *
 * @property type String    推送子类型
 * @property value String  推送内容
 * @property flag Int         推送选项，0：全推送，1：只推主人，默认全推送
 * @property alarmType Int    提醒类型
 * @property alarmId String   提醒ID
 * @constructor
 */
data class PushContentEntity(
    // 推送子类型："event", ...
    val type: String,
    // 推送内容，目前手势识别有使用
    val value: String,
    // 推送选项，0：全推送，1：只推主人，默认全推送
    val flag: Int,
    // 可选，当type为event时
    val alarmType: Int,
    // 可选，当type为event时
    val alarmId: String
)


enum class PushMsgType(val type: String) {
    /**
     * 灵敏度调整提醒 (只推送主人)
     */
    SENSITIVITY_NOTIFY("sensitivityNotify"),

    /**
     * 手势识别 - 守护开关
     */
    GUARD_ENABLE("guardEnable"),

    /**
     * 手势识别 - 音频提醒
     */
    AUDIO_REMIND_ENABLE("audioRemindEnable"),

    /**
     * 手势识别 - 人形检测提醒
     */
    HUMAN_TRACK_ENABLE("humanTrackEnable"),

    /**
     * 手势识别 - 移动检测提醒
     */
    MOVE_TRACK_ENABLE("moveTrackEnable"),

    /**
     * 触发事件推送提醒
     */
    EVENT("event"),
}
