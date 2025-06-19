package com.gw.reoqoo.app

import android.app.Application
import android.util.Log
import com.gw.component_debug.api.interfaces.IAppEvnApi
import com.gw_reoqoo.lib_router.ReoqooRouterInitializer
import com.gw_reoqoo.lib_utils.StorageInitTask
import com.gw_reoqoo.lib_utils.file.StorageUtils
import com.gw_reoqoo.log.Constants.APP_LOG_CACHE_PREFIX
import com.gw_reoqoo.log.ReoqooLogInitTask
import com.gw_reoqoo.module_mount.initializetask.InitializeTaskDispatcher
import com.gw.reoqoo.BuildConfig
import com.gwell.loglibs.GwellLogUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 *@Description: 主进程模块初始化
 *@Author: ZhangHui
 *@Date: 2023/7/19
 */
@Singleton
class MainProcessApp @Inject constructor() : IProcessApp {
    companion object {
        private const val TAG = "MainProcessApp"
    }

    @Inject
    lateinit var appCoreInitTask: AppCoreInitTask

    @Inject
    lateinit var autoSizeInitTask: AutoSizeInitTask

    @Inject
    lateinit var addDebugApi: IAppEvnApi

    override lateinit var appContext: Application

    override fun mount(application: Application) {
        this.appContext = application
        Log.i(TAG, "MainProcessApp mount start")
        val cachePath = appContext.filesDir.path + File.separator + APP_LOG_CACHE_PREFIX
        // 执行初始化任务
        InitializeTaskDispatcher.createDispatcher()
            .addInitializeTask(com.gw_reoqoo.lib_utils.StorageInitTask(appContext))
            .addInitializeTask(
                ReoqooLogInitTask(com.gw_reoqoo.lib_utils.file.StorageUtils.getIotLogDir(appContext), cachePath, addDebugApi.getLogsMaxNumber())
            )
            .addInitializeTask(appCoreInitTask)
            .addInitializeTask(ReoqooRouterInitializer(BuildConfig.DEBUG))
            .addInitializeTask(autoSizeInitTask)
            .start()
        GwellLogUtils.i(TAG, "MainProcessApp mount over")
    }

}