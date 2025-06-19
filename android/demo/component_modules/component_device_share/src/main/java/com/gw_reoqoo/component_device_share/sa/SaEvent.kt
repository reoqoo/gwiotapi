package com.gw_reoqoo.component_device_share.sa

import com.jwkj.base_statistics.sa.kits.SA

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/3/13 14:57
 * Description: SaEvent
 */
object SaEvent : SA.IEvent {

    /**
     * 分享设备_点击分享
     */
    const val BUTTON_CLICK = "DevShare_ButtonClick"

    /**
     * 分享设备_返回结果
     */
    const val RETURN_RESULT = "DevShare_ReturnResult"

    object Attr {

        const val DEVICE_ID = "deviceid"

        const val PAGE_TITLE = "page_title"

        const val SHARE_MODE = "share_mode"
    }

}