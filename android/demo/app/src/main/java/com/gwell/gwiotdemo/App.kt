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
            this["iotAssessId"] = ""
            this["iotAccessToken"] = ""
            this["region"] = ""
            this["regRegion"] = ""
            this["KEY_HOST_APPNAME_RES"] = R.string.app_name
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