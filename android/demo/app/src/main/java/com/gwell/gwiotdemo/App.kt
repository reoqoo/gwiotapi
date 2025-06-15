package com.gwell.gwiotdemo

import android.app.Application
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.AlbumConfig
import com.gw.gwiotapi.entities.InitOptions
import com.gw.gwiotapi.entities.WaterMarkConfig
import com.gw.gwiotapi.entities.WaterMarkPosition
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val app = this

        // initSdk
        GWIoT.initialize(InitOptions().apply {
            from = "REOQOO"
            albumConfig = AlbumConfig(
                snapshotDir = "${app.getExternalFilesDir(null)}${File.separator}iotplugin${File.separator}ScreenShots",
                recordDir = "${app.getExternalFilesDir(null)}${File.separator}iotplugin${File.separator}RecordVideo",
                watermarkConfig = WaterMarkConfig(
                    filePath = File(app.filesDir, "watermask_.png").path,
                    position = WaterMarkPosition.RIGHT_TOP
                )
            )
            // iotAssessId和iotAccessToken决定了监控鉴权是否发起成功
            this["iotAssessId"] = "-"
            this["iotAccessToken"] = ""
            // region 和 regRegion 用来与后台通信时认证使用
            this["region"] = "CN"
            this["regRegion"] = "CN"
            // 这个KEY决定了监控内部某些包含APP名称的字符串
            this["KEY_HOST_APPNAME_RES"] = R.string.app_name
            // 这个很重要，跟后台通信会携带过去
            this["KEY_APPLICATION_ID"] = ""
            // 这个是决定了后台服务接口的地址
            this["KEY_BASE_URL"] = ""
            // 向平台注册的APPID
            this["APP_ID"] = ""
            // 向平台注册的APP_TOKEN
            this["APP_TOKEN"] = ""
            // 向平台注册的APP_NAME
            this["KEY_APP_NAME"] = ""
            // 当前国家/地区的缩写，比如中国(cn)
            this["KEY_COUNTRY_SHORT_NAME"] = "cn"
        })

        // just for test watermask
        val fileDir = app.filesDir
        val waterFile = File(fileDir, "watermask_.png")
        if (!waterFile.exists()) {
            app.resources.openRawResource(R.raw.watermask_).use { inStream ->
                waterFile.outputStream().use { outStream ->
                    inStream.copyTo(outStream)
                }
            }
        }
    }
}