package com.gw_reoqoo.component_family.entrties

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/5/23 14:16
 * Description: EventSysEntity
 */
data class EventSysEntity(
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
    val data: EventSysData
)

data class EventSysData(

    /**
     * {
     *   "did": "1288514xxx",
     *   "uid": "-922337199175xxxx"
     * }
     */
    val did: String,

    val uid: Long?,
)
