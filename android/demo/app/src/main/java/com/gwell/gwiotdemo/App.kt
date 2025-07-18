package com.gwell.gwiotdemo

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.AlbumConfig
import com.gw.gwiotapi.entities.AppConfig
import com.gw.gwiotapi.entities.InitOptions
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val option = InitOptions(
            app = this,
            versionName = BuildConfig.GWIOT_VERSION_NAME,
            versionCode = BuildConfig.GWIOT_VERSION_CODE,
            appConfig = AppConfig(
                appId = BuildConfig.GWIOT_APP_ID,
                appToken = BuildConfig.GWIOT_APP_TOKEN,
                appName = BuildConfig.GWIOT_APP_NAME,
                cId = BuildConfig.GWIOT_CID,
            ),
        )
        option.brandDomain = BuildConfig.GWIOT_BRAND_DOMAIN
        option.disableAccountService = true
        val snapshotDir = "${this.getExternalFilesDir(null)}${File.separator}iotplugin${File.separator}ScreenShots"
        val recordDir = "${this.getExternalFilesDir(null)}${File.separator}iotplugin${File.separator}RecordVideo"
        option.albumConfig = AlbumConfig(
            snapshotDir = snapshotDir,
            recordDir = recordDir,
            watermarkConfig = null
        )
        GWIoT.initialize(option)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        FirebaseApp.initializeApp(this)
    }
}