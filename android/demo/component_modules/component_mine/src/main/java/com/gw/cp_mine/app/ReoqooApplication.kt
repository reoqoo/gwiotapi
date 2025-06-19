package com.gw.cp_mine.app

import android.util.Log
import com.gw_reoqoo.lib_router.BuildConfig
import com.gw_reoqoo.lib_router.ReoqooRouterInitializer
import com.gw_reoqoo.lib_utils.StorageInitTask
import com.gw_reoqoo.lib_utils.file.StorageUtils
import com.gw_reoqoo.log.Constants
import com.gw_reoqoo.log.ReoqooLogInitTask
import com.gw_reoqoo.module_mount.app.BaseApplication
import com.gw_reoqoo.module_mount.initializetask.InitializeTaskDispatcher
import dagger.hilt.android.HiltAndroidApp
import java.io.File

/**
 *@Description: 壳子Application
 *@Author: ZhangHui
 *@Date: 2023/7/18
 */
//@HiltAndroidApp
class ReoqooApplication : BaseApplication() {

    companion object {
        private const val TAG = "ReoqooApplication"
    }

    override fun onCreate() {
        super.onCreate()
        val logPath = com.gw_reoqoo.lib_utils.file.StorageUtils.getIotLogDir(this)
        val cachePath = this.filesDir.path + File.separator + Constants.APP_LOG_CACHE_PREFIX
        Log.d(TAG, "onCreate")
        InitializeTaskDispatcher.createDispatcher()
            .addInitializeTask(com.gw_reoqoo.lib_utils.StorageInitTask(this))
            .addInitializeTask(ReoqooLogInitTask(logPath, cachePath))
            .addInitializeTask(ReoqooRouterInitializer(BuildConfig.DEBUG))
            .start()
    }

}