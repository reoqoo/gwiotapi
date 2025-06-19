package com.gw.reoqoo.ui.logo

import android.app.Application
import android.content.Intent
import com.gw.component_push.api.interfaces.IPushApi
import com.gw_reoqoo.cp_account.api.kapi.IUserInfo
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_iotvideo.IoTSdkInitMgr
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw.reoqoosdk.ReoqooSdkMgr
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/27 13:55
 * Description: LogoVM
 */
@HiltViewModel
class LogoVM @Inject constructor(
    private val app: Application,
    private val pushApi: IPushApi,
    private val ioTSdkInitMgr: IoTSdkInitMgr
) : ABaseVM() {

    companion object {
        private const val TAG = "LogoVM"
    }

    /**
     * IoT SDK注册
     */
    fun iotVideoRegister(iUserInfo: IUserInfo?) {
        // 监听用户信息，更新到iotSDK里注册或则注销
        if (!ioTSdkInitMgr.isSDKInit()) {
            GwellLogUtils.e(TAG, "IotSdk is not init")
            return
        }
        if (iUserInfo != null) {
            val accessId = iUserInfo.accessId
            val accessToken = iUserInfo.accessToken
            ReoqooSdkMgr.instance(app).accountService()?.setUserInfo(accessId, accessToken)
//            ioTSdkInitMgr.registerSdk(accessId, accessToken)
        } else {
            ReoqooSdkMgr.instance(app).accountService()?.logout()
//            ioTSdkInitMgr.unregisterSdk()
        }
    }

    /**
     * 已登录的情况下，跳转到主页
     */
    fun goMainAction() {
        pageJumpData.postValue(
            PageJumpData(
                TheRouter
                    .build(ReoqooRouterPath.AppPath.MAIN_ACTIVITY_PATH)
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        )
    }

    /**
     * 意图信息处理
     *
     * @param intent Intent?
     */
    fun initData(intent: Intent?) {
        parseStartUpIntent(intent)
    }

    /**
     * 解析意图信息
     *
     * @param intent Intent?
     */
    private fun parseStartUpIntent(intent: Intent?) {
        intent?.let {
            val hasSysOfflineMsg = pushApi.parsePushFromIntent(intent)
            GwellLogUtils.i(TAG, "parseStartUpIntent: hasSysOfflineMsg=$hasSysOfflineMsg")
        }
    }

}