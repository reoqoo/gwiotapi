package com.gw_reoqoo.component_device_share.data.rspository

import com.gw_reoqoo.component_device_share.data.datasource.RemoteDevShareDatasource
import com.gw_reoqoo.component_device_share.data.datastore.DeviceShareDatStore
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_http.TAG
import com.gw_reoqoo.lib_http.entities.Guest
import com.gw_reoqoo.lib_http.entities.GuestListContent
import com.gw_reoqoo.lib_http.entities.ListGuestContent
import com.gw_reoqoo.lib_http.entities.ShareContent
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Date
import javax.inject.Inject

/**
 * @Description: - 最近分享用户数据
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 */
class DevShareRepository @Inject constructor(
    private val dataStore: DeviceShareDatStore,
    private val iAccountApi: IAccountApi,
    private val remoteDatasource: RemoteDevShareDatasource,
) {
    companion object {
        private const val TAG = "DevShareRepository"
    }

    /**
     * 获取最近分享用户
     */
    suspend fun getRecentlyShareUser(): List<Guest> {
        return remoteDatasource.getRecentlyShareUser()?.guestList ?: emptyList()
    }

    /**
     * 获取某个设备已分享的用户
     */
    fun listGuest(deviceId: String): Flow<HttpAction<ListGuestContent>> {
        return remoteDatasource.listGuest(deviceId)
    }

    /**
     * 获取分享内容
     * @param deviceId 设备ID
     * @param forced 强制刷新
     */
    fun getShareContent(deviceId: String, forced: Boolean): Flow<HttpAction<ShareContent>> {
        val userId = iAccountApi.getSyncUserId()
        if (userId.isNullOrEmpty()) {
            GwellLogUtils.e(TAG, "getShareContent,userId.isNullOrEmpty() userId:$userId")
            return flow {
                emit(HttpAction.Fail(IllegalStateException("userid == null")))
            }
        }
        if (forced) {
            return getShareContentForced(deviceId, userId)
        }

        // 拿最后一次的数据
        val lastContent = runBlocking { dataStore.getLastShareQRCode(userId) }
        GwellLogUtils.i(TAG, "lastContent:$lastContent")
        // 如果拿到了数据，并且当前时间小于过期时间，则继续使用
        if (lastContent != null && (Date().time / 1000) < lastContent.expireTime) {
            GwellLogUtils.i(
                TAG,
                "lastContent.expireTime > now, now=${Date().time / 1000},expireTime=${lastContent.expireTime}"
            )
            return flow {
                emit(HttpAction.Loading())
                emit(HttpAction.Success(lastContent))
            }
        }
        return getShareContentForced(deviceId, userId)
    }

    /**
     * 强制刷新分享二维码
     */
    private fun getShareContentForced(
        deviceId: String,
        userId: String
    ): Flow<HttpAction<ShareContent>> {
        val flow = remoteDatasource.getShareContent(deviceId)
        return flow.map { action ->
            when (action) {
                is HttpAction.Loading -> Unit
                is HttpAction.Fail -> Unit
                is HttpAction.Success -> {
                    val content = action.data
                    if (content != null) {
                        // 请求数据成功后，保存一份到dataStore里
                        dataStore.saveLastShareQRCode(userId, content)
                    }
                }
            }
            action
        }
    }

    /**
     * 判断账户是否存在/获取账号信息
     * @param guestAccount 用户输入的可能是手机号/邮箱
     */
    fun queryGuestInfo(
        deviceId: String,
        guestAccount: String
    ): Flow<HttpAction<GuestListContent>> {
        return remoteDatasource.queryGuestInfo(deviceId, guestAccount)
    }

    /**
     * 通过账号分享设备
     */
    fun shareGuest(deviceId: String, guestId: String): Flow<HttpAction<Any>> {
        return remoteDatasource.shareGuest(deviceId, guestId)
    }

}