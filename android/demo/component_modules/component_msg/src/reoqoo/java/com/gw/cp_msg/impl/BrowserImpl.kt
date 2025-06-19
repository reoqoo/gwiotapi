package com.gw.cp_msg.impl

import android.app.Activity
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_msg.api.interfaces.IBrowserApi
import com.gw_reoqoo.widget_webview.jsinterface.WebViewJsCallback
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/22 11:01
 * Description: WebViewImpl
 */
@Singleton
class BrowserImpl @Inject constructor(
    private val webViewApi: IWebViewApi
) : IBrowserApi {

    override fun openWebView(url: String, title: String?, deviceId: String?) {
        webViewApi.openWebView(url, title ?: "", deviceId)
    }

    fun showWebViewDialog(
        activity: Activity,
        width: Int,
        height: Int,
        url: String?,
        deviceId: String?,
        callBack: WebViewJsCallback?
    ): Boolean {
        return webViewApi.showWebViewDialog(activity, width, height, url, deviceId, callBack)
    }

}