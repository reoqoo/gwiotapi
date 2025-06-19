package com.gw_reoqoo.house_watch.receivers.api.impl

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.house_watch.receivers.NetworkStatusReceiver
import com.gw_reoqoo.house_watch.receivers.api.INetworkStatusApi
import com.gw_reoqoo.house_watch.receivers.api.Status
import com.gw_reoqoo.house_watch.receivers.api.StatusDiff
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/25
 */
@Singleton
class NetworkStatus @Inject constructor(
    @ApplicationContext private val context: Context
) : INetworkStatusApi {
    companion object {
        private const val TAG = "NetworkStatus"
    }

    private val _networkStatus = MutableLiveData<StatusDiff>()

    /**
     * 开启广播接收器
     */
    override fun startReceiver() {
        GwellLogUtils.i(TAG, "startReceiver")
        NetworkStatusReceiver.startReceiver(context)
    }

    /**
     * 改变网络状态
     */
    fun changeNetworkStatus(status: Status) {
        val oldStatus = _networkStatus.value?.newStatus
        _networkStatus.postValue(StatusDiff(oldStatus = oldStatus, newStatus = status))
    }

    /**
     * 对网络状态的监听回调
     */
    override val networkStatus: LiveData<StatusDiff> get() = _networkStatus

    /**
     * 是否正在使用流量
     */
    override val isInMobileData: Boolean get() = networkStatus.value?.newStatus == Status.MOBILE

}