package com.gw.cp_msg.entity

import com.jwkj.lib_json_kit.IJsonEntity

/**
 * @message   SimplePushDataBean
 * @user      caizhiyong
 * @date      8/18/2022
 */
data class SimplePushDataBean(
    val deviceId: String?,
    val pushTime: Long = 0,
    val pushType: Long = 0
) : IJsonEntity {
    object PushType {
        /**
         * 低电量报警
         */
        const val LOW_POWER = 1L shl 32

        /**
         * 设备离线
         */
        const val DEVICE_OFF_LINE = 1L shl 33

        /**
         * 切换至省电模式
         */
        const val CHANGE_POWER_SAVING = 1L shl 34

        /**
         * 已插入电源，可切换至不休眠模式
         */
        const val CHANGE_NOT_SLEEP = 1L shl 35
    }
}