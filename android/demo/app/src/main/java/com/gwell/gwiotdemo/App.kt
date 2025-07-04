package com.gwell.gwiotdemo

import android.app.Application
import android.content.Context
import com.gwell.gwiotdemo.BuildConfig
import com.google.firebase.FirebaseApp
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.AppConfig
import com.gw.gwiotapi.entities.InitOptions
import dagger.hilt.android.HiltAndroidApp

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
        GWIoT.initialize(option)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        FirebaseApp.initializeApp(this)
    }
}