package com.gwell.gwiotdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.AppConfig
import com.gw.gwiotapi.entities.BindOptions
import com.gw.gwiotapi.entities.GWResult
import com.gw.gwiotapi.entities.IDevice
import com.gw.gwiotapi.entities.IUserAccessInfo
import com.gw.gwiotapi.entities.InitOptions
import com.gw.gwiotapi.entities.OpenPluginOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    val scope by lazy { MainScope() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val option = InitOptions(
            app = app,
            versionName = versionName,
            versionCode = versionCode,
            appConfig = AppConfig(
                appId = appId,
                appToken = appToken,
                appName = appName,
                cId = cId,
            ),
        )
        option.brandDomain = brandDomain
        option.disableAccountService = true
        GWIoT.initialize(option)
    }

    fun startLogin(view: View) {
        scope.launch {
            val info = object : IUserAccessInfo {
                override var accessId: String = "123"
                override var accessToken: String = ""
                override var area: String = ""
                override var expireTime: String = ""
                override var regRegion: String = ""
                override var terminalId: String = ""
                override var userId: String = ""
            }
            GWIoT.login(info)
        }
    }

    fun startNetConfig(view: View) {
        scope.launch(Dispatchers.Main) {
            try {
                val opt = BindOptions()
                val ret = GWIoT.openBind(opt)
                Log.i(TAG, "ret=$ret")
            } catch (e: Exception) {
                Log.i(TAG, "e=$e")
            }
        }
    }

    fun openHome(view: View) {
        scope.launch {
            val devRet = GWIoT.queryDeviceCacheFirst("devId")
            var device: IDevice? = null
            if (devRet is GWResult.Success) {
                device = devRet.data
            }
            device ?: throw RuntimeException("没有Device")
            GWIoT.openHome(OpenPluginOption(device = device))
        }
    }

    fun getDeviceList(view: View) {
        scope.launch {
            val listRet = GWIoT.queryDeviceList()
            if (listRet !is GWResult.Success) {
                throw RuntimeException("没拿到")
            }
            val deviceList = listRet.data
            Log.i(TAG, "deviceList = $deviceList")
        }
    }
}