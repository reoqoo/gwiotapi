package com.gw_reoqoo.house_watch.receivers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.gw_reoqoo.house_watch.receivers.api.Status
import com.gw_reoqoo.house_watch.receivers.api.impl.NetworkStatus
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * @Description: - 接收网络状态的广播接收器
 * @Author: XIAOLEI
 * @Date: 2023/10/25
 */
@AndroidEntryPoint
class NetworkStatusReceiver : HiltBroadcastReceiver() {

    @Inject
    lateinit var networkStatus: NetworkStatus

    @Inject
    @ApplicationContext
    lateinit var app: Context

    companion object {
        private const val TAG = "NetworkStatusReceiver"

        private val receiverAtom = AtomicReference<NetworkStatusReceiver?>()

        /**
         * 启动广播接收器
         */
        fun startReceiver(context: Context) {
            val app = context.applicationContext
            if (receiverAtom.compareAndSet(null, NetworkStatusReceiver())) {
                val receiver = receiverAtom.get() ?: return
                val filter = IntentFilter()
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                app.registerReceiver(receiver, filter)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent ?: return
        GwellLogUtils.i(TAG, "action:${intent.action}")
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val status = getNetworkStatus()
            GwellLogUtils.i(TAG, "status:${status}")
            networkStatus.changeNetworkStatus(status)
        }
    }

    /**
     * 判断是否正在使用流量
     */
    @SuppressLint("MissingPermission")
    private fun getNetworkStatus(): Status {
        return try {
            val connectivityManager =
                app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            when (val networkInfo = connectivityManager.activeNetworkInfo) {
                null -> Status.UNKNOW
                else -> when (networkInfo.type) {
                    ConnectivityManager.TYPE_MOBILE -> Status.MOBILE
                    else -> Status.WIFI
                }
            }
        } catch (e: Exception) {
            GwellLogUtils.e(TAG, "getNetworkStatus-catch", e)
            Status.UNKNOW
        }
    }
}