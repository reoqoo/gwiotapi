package com.gw.component_push.entity

import com.squareup.moshi.Json

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/20 16:36
 * Description: AlarmPushEntity
 */
data class AlarmPushEntity(
    /**
     * DevId : 4294967644
     * EvtId : 42949676441617105279
     * TrgTime : 1617105279
     * TrgType : 1
     */
    @Json(name = "DevId")
    val deviceId: Long,

    @Json(name = "EvtId")
    val evtId: String,

    @Json(name = "TrgTime")
    val trgTime: Long,

    @Json(name = "TrgType")
    val trgType: Int,
)
