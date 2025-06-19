package com.gw.cp_msg.entity.http

import com.google.gson.annotations.SerializedName
import com.jwkj.lib_json_kit.IJsonEntity

/**
 *
 * @property msg 可更新的设备信息
 * @constructor
 */
data class UpdateDeviceEntity(
    /**
     * 设备Id
     */
    @SerializedName("deviceId")
    var deviceId: String,
    /**
     * 需要更新的版本
     */
    @SerializedName("updateVersion")
    var updateVersion: String,
    /**
     * 消息生成时间
     */
    @SerializedName("updateTime")
    var updateTime: Long,
) : IJsonEntity
