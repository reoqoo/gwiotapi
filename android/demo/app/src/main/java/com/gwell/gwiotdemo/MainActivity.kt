package com.gwell.gwiotdemo

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gw.gwiotapi.GWIoT
import com.gw.gwiotapi.entities.BindOptions
import com.gw.gwiotapi.entities.IDevice
import com.gw.gwiotapi.entities.IUserInfo
import com.gw.gwiotapi.entities.OpenPluginOption
import com.gw.gwiotapi.entities.Solution
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * 登录
     */
    fun startLogin(v: View) {
        lifecycleScope.launch {
            val info = object : IUserInfo {
                override var accessId: String = ""
                override var accessToken: String = ""
                override var area: String = ""
                override var expireTime: String = ""
                override var regRegion: String = ""
                override var terminalId: String = ""
                override var userId: String = ""
            }
            GWIoT.refreshUserInfo(info)
        }
    }

    /**
     * 配网
     */
    fun startNetConfig(v: View) {
        lifecycleScope.launch {
            val options = BindOptions(Solution.YOOSEE, qrCodeValue = "", listener = null)
            GWIoT.openBind(options)
        }
    }

    /**
     * 监控
     */
    fun startMonitor(v: View) {
        lifecycleScope.launch {
            val option = OpenPluginOption(
                device = object : IDevice {
                    override var deviceId: String = "devId"
                    override val solution: Solution = Solution.YOOSEE
                }
            )
            GWIoT.openHome(option)
        }
    }
}