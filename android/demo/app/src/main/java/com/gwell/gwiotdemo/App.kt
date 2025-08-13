package com.gwell.gwiotdemo

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.AlbumConfig
import com.gw.gwiotapi.entities.AppConfig
import com.gw.gwiotapi.entities.AppTexts
import com.gw.gwiotapi.entities.DeviceShareOption
import com.gw.gwiotapi.entities.InitOptions
import com.gw.gwiotapi.entities.Theme
import com.gw.gwiotapi.entities.UIConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import java.io.File

@HiltAndroidApp
class App : Application() {
    private val scope by lazy { MainScope() }
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
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
            mainActvityKlass = MainActivity::class.java as Class<Activity>,
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
        // 设备分享的功能
        option.deviceShareOptions = listOf(
            DeviceShareOption.QRCode
        )
        GWIoT.initialize(option)
        GWIoT.setUIConfiguration(
            UIConfiguration(
                theme = Theme(),
                texts = AppTexts(
                    appNamePlaceHolder = this.getString(R.string.demo_app_name)
                )
            )
        )
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        FirebaseApp.initializeApp(this)
    }
}