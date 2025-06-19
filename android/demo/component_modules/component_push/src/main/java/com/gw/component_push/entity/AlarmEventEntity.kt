package com.gw.component_push.entity

import com.squareup.moshi.Json

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/18 15:38
 * Description: 告警消息实体类
 */
data class AlarmEventEntity(

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
    val data: AlarmEventData

)

data class AlarmEventData(

    /**
     *  {
     *         "deviceId": "12885285114",
     *         "alarmId": "128852851141702885173",
     *         "alarmType": "2",
     *         "picPath": "2023-12-18",
     *         "bucketVal": "329759",
     *         "pkgType": "3",
     *         "triTime": 1702885173585,
     *         "endTime": 1702885182000
     *     }
     */

    @Json(name = "deviceId")
    val deviceId: String,

    @Json(name = "alarmId")
    val alarmId: String,

    @Json(name = "alarmType")
    val alarmType: String,

    @Json(name = "picPath")
    val picPath: String?,

    @Json(name = "bucketVal")
    val bucketName: String?,

    @Json(name = "pkgType")
    val pkgType: Int?,

    /**
     * 事件触发的时间戳
     */
    @Json(name = "triTime")
    val triTime: Long,

    @Json(name = "endTime")
    val endTime: Long?,
)