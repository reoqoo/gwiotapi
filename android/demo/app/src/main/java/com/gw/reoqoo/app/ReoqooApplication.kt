package com.gw.reoqoo.app

import android.content.res.Configuration
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.gw.cp_mine.api.kapi.ILocaleApi
import com.gw_reoqoo.module_mount.app.BaseApplication
import com.gw.reoqoo.app.api.IReoqooSdkService
import com.gwell.loglibs.GwellLogUtils
import com.jakewharton.processphoenix.ProcessPhoenix
import com.jwkj.base_lifecycle.process_lifecycle.ProcessLifecycleManager
import com.jwkj.base_lifecycle.process_lifecycle.listener.ProcessLifecycleListener
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

/**
 *@Description: 壳子Application
 *@Author: ZhangHui
 *@Date: 2023/7/18
 */
@HiltAndroidApp
class ReoqooApplication : BaseApplication() {

    companion object {
        private const val TAG = "ReoqooApplication"
        private const val CHILD_THREAD_NAME = "LogSyncThread"
    }

    @Inject
    lateinit var localeApi: ILocaleApi

    @Inject
    lateinit var sdkService: IReoqooSdkService

    override fun onCreate() {
        super.onCreate()
        // 如果是重启进程，则不进行初始化操作
        if (ProcessPhoenix.isPhoenixProcess(this)) return
        ProcessLifecycleManager.init(this)
        setLocale()
        sdkService.initService()
        WebView(this).destroy()
        // 获取当前系统语言
        GwellLogUtils.i(
            TAG,
            "local ${Locale.getDefault().country}, language ${Locale.getDefault().language}"
        )
        localeApi.setCurrentCountry(Locale.getDefault().country)
        // 设置主题
        setColorTheme()
        // 监听APP整体生命周期
        ProcessLifecycleManager.registerProcessLifecycleListener(object : ProcessLifecycleListener {
            private val handler: Handler

            init {
                val thread = HandlerThread(CHILD_THREAD_NAME)
                thread.start()
                handler = Handler(thread.looper)
            }

            override fun onForeground() = Unit
            override fun onBackground() {
                handler.post {
                    GwellLogUtils.flushLogFile(true)
                }
            }

            override fun onTrimMemory(p0: Int) = Unit
            override fun onConfigurationChanged(p0: Configuration) = Unit
            override fun onLowMemory() = Unit
        })
    }

    /**
     * 设置颜色主题
     */
    private fun setColorTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * 设置系统语言
     */
    private fun setLocale() {
        val locale = localeApi.getCurrentLanguageLocale()
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

}
