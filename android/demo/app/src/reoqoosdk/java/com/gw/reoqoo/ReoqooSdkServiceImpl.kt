package com.gw.reoqoo

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import com.gw.component_plugin_service.api.IPluginManager
import com.gw.cp_config.api.IAppParamApi
import com.gw.cp_mine.api.kapi.ILocaleApi
import com.gw.reoqoo.app.api.IReoqooSdkService
import com.gw.reoqoo.ui.main.MainActivity
import com.gw.reoqoosdk.ReoqooSdkMgr
import com.gw.reoqoosdk.sdk.repository.ConfigEntity
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_lifecycle.LifecycleInitializer
import com.jwkj.base_lifecycle.activity_lifecycle.ActivityLifecycleManager
import com.jwkj.base_lifecycle.activity_lifecycle.listener.ActivityLifecycleListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/8 20:03
 * Description: SdkServiceImpl
 */
@Singleton
class ReoqooSdkServiceImpl @Inject constructor() : IReoqooSdkService {

    companion object {
        private const val TAG = "ReoqooSdkServiceImpl"
    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var configApi: IAppParamApi

    @Inject
    lateinit var localeApi: ILocaleApi

    @Inject
    lateinit var pluginManager: IPluginManager

    override fun initService() {
        GwellLogUtils.i(TAG, "initService")
        val configEntity = ConfigEntity(
            appId = configApi.getAppID(),
            appToken = configApi.getAppToken(),
            appName = configApi.getAppName(),
            appVersion = BuildConfig.VERSION_NAME,
            packageName = BuildConfig.APPLICATION_ID,
            qrCodeDomain = "defender.com"
        )
        ReoqooSdkMgr.instance(app).init(app, configEntity)
        initLifecycle(app)
    }

    /**
     * 生命周期模块的初始化
     */
    private fun initLifecycle(application: Application) {
        LifecycleInitializer.init(application, MainActivity::class.java)
        ActivityLifecycleManager.registerActivityLifecycleListener(object :
            ActivityLifecycleListener {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                // 对Application和Activity更新上下文的语言环境
                localeApi.initAppLanguage(p0)
            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
                if (p0 is MainActivity) {
                    // 退出app
                    pluginManager.closeFloatService()
                }
            }

            override fun onConfigurationChanged(newConfig: Configuration) {
                GwellLogUtils.i(
                    TAG,
                    "initLifecycle : newConfig ${newConfig.locale.country}"
                )
                localeApi.setCurrentCountry(newConfig.locale.country)
                localeApi.initAppLanguage()
            }

            override fun onLowMemory() {
            }

            override fun onTrimMemory(p0: Int) {
            }

        })
    }

}