package com.gwell.gwiotdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.GWResult
import com.gw.gwiotapi.entities.IDevice
import com.gw.gwiotapi.entities.OpenPluginOption
import com.gw.gwiotapi.entities.PushNotification
import com.gw.gwiotapi.entities.ScanQRCodeOptions
import com.gw.gwiotapi.entities.UserC2CInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    val scope by lazy { MainScope() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 设备列表改动
        GWIoT.deviceList.observe(this) { devList ->
            Log.i(TAG, "deviceList = $devList")
        }
        // 用户信息改动（登录/信息改动）
        GWIoT.user.observe(this) { user ->
            Log.i(TAG, "user = $user")
        }
        // 设备状态改变
        GWIoT.propsChanged.observe(this) { props ->
            Log.i(TAG, "propsChanged = $props")
        }
        Log.i(TAG, "intent = $intent")
        this.initLogger()
        // 离线推送处理
        runOnUiThread {
            GWIoT.receivePushNotification(PushNotification(intent = intent))
        }
    }

    fun startLogin(view: View) {
        scope.launch {

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

    fun startLogin2(view: View) {
        scope.launch {
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

    fun loginOut(view: View) {
        scope.launch {
            GWIoT.logout()
        }
    }

    fun startNetConfig(view: View) {
        scope.launch(Dispatchers.Main) {
            val opt = ScanQRCodeOptions()
            opt.enableBuiltInHandling = true
            val ret = GWIoT.openScanQRCodePage(opt) { type, close ->
                Log.i(TAG, "startNetConfig.type=$type")
            }
            Log.i(TAG, "ret=$ret")
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

    fun getDeviceLastFramePath(view: View) {
        scope.launch {
            val devResult = GWIoT.queryDeviceCacheFirst("devid")
            if (devResult is GWResult.Success) {
                val device = devResult.data
                if (device != null) {
                    val lastFramePath = GWIoT.getLastSnapshotPath(device)
                    Log.i(TAG, "lastFramePath = $lastFramePath")
                }
            }
        }
    }
}