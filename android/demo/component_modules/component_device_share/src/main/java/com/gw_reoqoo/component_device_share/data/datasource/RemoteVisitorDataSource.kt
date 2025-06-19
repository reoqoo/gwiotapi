package com.gw_reoqoo.component_device_share.data.datasource

import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/4 0:06
 * Description: RemoteVisitorDataSource
 */
class RemoteVisitorDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteVisitorDataSource"
    }

    /**
     * 删除分享者
     *
     * @param deviceId 设备Id
     * @param guestId  分享者用户Id
     */
    fun deleteGuest(deviceId: String, guestId: String): Flow<HttpAction<Any>> = callbackFlow {
        send(HttpAction.Loading())
        httpService.cancelShare(
            deviceId,
            guestId,
            typeSubscriber<Any>(
                onSuccess = { data ->
                    GwellLogUtils.i(TAG, "deleteGuest: resp: $data")
                    trySend(HttpAction.Success(data))
                    close()
                },
                onFail = { t ->
                    GwellLogUtils.e(TAG, "shareGuest", t)
                    trySend(HttpAction.Fail(t))
                    close()
                }
            )
        )
        awaitClose { cancel() }
    }

}