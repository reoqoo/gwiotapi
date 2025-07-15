package com.gwell.gwiotdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.BindOptions
import com.gw.gwiotapi.entities.GWResult
import com.gw.gwiotapi.entities.IDevice
import com.gw.gwiotapi.entities.OpenPluginOption
import com.gw.gwiotapi.entities.UserC2CInfo
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
        GWIoT.deviceList.observe(this) { devList ->
            Log.i(TAG, "deviceList = $devList")
        }
        GWIoT.user.observe(this) { user ->
            Log.i(TAG, "user = $user")
        }
        GWIoT.propsChanged.observe(this) { props ->
            Log.i(TAG, "propsChanged = $props")
        }
    }

    fun startLogin(view: View) {
        scope.launch {
//            val info = object : IUserAccessInfo {
//                override var accessId: String = BuildConfig.USER_ACCESS_ID
//                override var accessToken: String = BuildConfig.USER_ACCESS_TOKEN
//                override var area: String = BuildConfig.USER_AREA
//                override var expireTime: String = BuildConfig.USER_EXPIRE_TIME
//                override var regRegion: String = BuildConfig.USER_REG_REGION
//                override var terminalId: String = BuildConfig.USER_TERMINAL_ID
//                override var userId: String = BuildConfig.USER_USER_ID
//            }
//            GWIoT.login(info)
            
            val info = UserC2CInfo(
                accessId = BuildConfig.USER_ACCESS_ID,
                accessToken = BuildConfig.USER_ACCESS_TOKEN,
                expireTime = BuildConfig.USER_EXPIRE_TIME,
                terminalId = BuildConfig.USER_TERMINAL_ID,
                expend = """{"area":"sg","regRegion":"US"}"""
            )
            
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

    fun recognizeQRCode(view: View) {
        scope.launch(Dispatchers.Main) {
            val qrcodeValue = BuildConfig.TEST_QRCODE_VALUE
            GWIoT.recognizeQRCode(qrcodeValue, true)
        }
    }

    fun openHome(view: View) {
        scope.launch {
            val devRet = GWIoT.queryDeviceCacheFirst(BuildConfig.DEV_DEV_ID)
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