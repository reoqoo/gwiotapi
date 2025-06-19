package com.gw_reoqoo.component_device_share.data.datasource

import com.gw_reoqoo.lib_http.entities.GuestListContent
import com.gw_reoqoo.lib_http.entities.ListGuestContent
import com.gw_reoqoo.lib_http.entities.ListRecentlyGuest
import com.gw_reoqoo.lib_http.entities.OwnerInfo
import com.gw_reoqoo.lib_http.entities.ShareContent
import com.gw_reoqoo.lib_http.mapActionFlow
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * @Description: - 网络数据最近分享用户数据源
 * @Author: XIAOLEI
 * @Date: 2023/8/23
 */
class RemoteDevShareDatasource @Inject constructor(
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteDevShareDatasourc"
        private const val FULL_PERMISSION = 117L
    }

    /**
     * 获取最近分享用户
     */
    suspend fun getRecentlyShareUser(): ListRecentlyGuest? {
        val channel = Channel<ListRecentlyGuest?>(1)
        httpService.listRecentlyGuest(typeSubscriber<ListRecentlyGuest>(
            onSuccess = { data ->
                GwellLogUtils.i(TAG, "getRecentlyShareUser.onSuccess()->$data")
                channel.trySend(data)
            },
            onFail = { t ->
                GwellLogUtils.e(TAG, "getRecentlyShareUser.onFail", t)
                channel.trySend(null)
            }
        ))
        return channel.receive()
    }

    /**
     * 获取某个设备已分享的用户
     */
    fun listGuest(deviceId: String): Flow<HttpAction<ListGuestContent>> {
        return httpService.listGuest(deviceId).mapActionFlow()
    }

    /**
     * 获取分享内容
     */
    fun getShareContent(deviceId: String): Flow<HttpAction<ShareContent>>  {
        return httpService.genShareQrcode(deviceId, FULL_PERMISSION).mapActionFlow()
    }

    /**
     * 根据电话号码、邮箱、或用户id（设备主人通过账号分享前调用）查询受邀访客信息
     */
    fun queryGuestInfo(
        deviceId: String,
        guestAccount: String
    ): Flow<HttpAction<GuestListContent>> = callbackFlow {
        send(HttpAction.Loading())
        httpService.findUser(
            deviceId,
            guestAccount,
            typeSubscriber<GuestListContent>(
                onSuccess = { data ->
                    trySend(HttpAction.Success(data))
                    close()
                },
                onFail = { t ->
                    GwellLogUtils.e(TAG, "queryGuestInfo", t)
                    trySend(HttpAction.Fail(t))
                    close()
                }
            )
        )
        awaitClose { cancel() }
    }

    /**
     * 通过账号分享设备
     */
    fun shareGuest(deviceId: String, guestId: String): Flow<HttpAction<Any>> {
        return httpService.shareGuest(deviceId, guestId, FULL_PERMISSION, true).mapActionFlow()
    }

    /**
     * 访客查询设备主人信息
     * @param deviceId 设备ID
     */
    fun loadOwnerInfo(deviceId: String): Flow<HttpAction<OwnerInfo>> {
        return httpService.ownerInfo(deviceId).mapActionFlow()
    }

    /**
     * 访客-删除被分享的设备
     */
    fun cancelShare(deviceId: String): Flow<HttpAction<Any>> = callbackFlow {
        send(HttpAction.Loading())
        httpService.cancelShare(deviceId, "", typeSubscriber<Any>(
            onSuccess = { data ->
                trySend(HttpAction.Success(data))
                close()
            },
            onFail = { t ->
                GwellLogUtils.e(TAG, "cancelShare", t)
                trySend(HttpAction.Fail(t))
                close()
            }
        ))
        awaitClose { cancel() }
    }
}