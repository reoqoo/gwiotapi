package com.gw.cp_msg.impl

import com.gw.cp_msg.api.interfaces.IBrowserApi
import com.gw.reoqoosdk.paid_service.IPaidService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/22 11:01
 * Description: WebviewImpl
 */
@Singleton
class BrowserImpl @Inject constructor(
    private val webViewApi: IPaidService
) : IBrowserApi {

    override fun openWebView(url: String, title: String?, deviceId: String?) {
        webViewApi.openWebView(url, title, deviceId)
    }

}