package com.gw.cp_msg.api.interfaces

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/22 11:00
 * Description: IWebViewApi
 */
interface IBrowserApi {

    /**
     * 打开浏览器页面
     *
     * @param url String        地址
     * @param title String?     标题
     * @param deviceId String?  设备id
     */
    fun openWebView(url: String, title: String? = null, deviceId: String? = null)

}