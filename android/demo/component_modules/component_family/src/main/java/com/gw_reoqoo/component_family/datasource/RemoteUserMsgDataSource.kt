package com.gw_reoqoo.component_family.datasource

import com.gw_reoqoo.lib_http.datasource.HttpDataSource
import com.gw.lib_http.entities.DeviceHistoryBean
import com.gw.lib_http.entities.DeviceHistoryResp
import com.gw_reoqoo.lib_http.entities.DeviceShareDetail
import com.gw_reoqoo.lib_http.entities.MessageBean
import com.gw_reoqoo.lib_http.entities.MessageList
import com.gw_reoqoo.lib_http.entities.MessageStatus
import com.gw_reoqoo.lib_http.entities.MsgStatusChangeList
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @Description: - 远程用户消息数据源
 * @Author: XIAOLEI
 * @Date: 2023/8/24
 */
class RemoteUserMsgDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper,
    private val httpDataSource: HttpDataSource,
) {
    companion object {
        private const val TAG = "RemoteUserMsgDataSource"
    }

    /**
     * 获取未读的设备分享消息列表
     */
    suspend fun loadUnReadDeviceShareMsgList(): List<MessageBean>? {
        val channel = Channel<List<MessageBean>?>(1)
        httpService.getUserMsg(
            20,
            0,
            true,
            true,
            0,
            typeSubscriber<MessageList>(
                onSuccess = { msgList ->
                    if (msgList == null) {
                        channel.trySend(null)
                    } else {
                        val contents = msgList.list.filter { messageBean: MessageBean ->
                            messageBean.tag == MessageBean.TYPE_DEVICE_SHARE_INVITE
                        }
                        channel.trySend(contents)
                    }
                }, onFail = { t ->
                    GwellLogUtils.e(TAG, "loadDeviceShareMsgList", t)
                    channel.trySend(null)
                }
            )
        )
        return channel.receive()
    }

    /**
     * 历史用户首次登录新app获取相关设备信息
     * @return List<DeviceHistoryBean>? 历史设备信息列表
     */
    suspend fun getDeviceHistoryList(): List<DeviceHistoryBean>? {
        val channel = Channel<List<DeviceHistoryBean>?>(1)
        httpService.getDeviceHistoryList(typeSubscriber<DeviceHistoryResp>(
            onSuccess = { data ->
                if (data?.isShowed == 1 && data.list?.isNotEmpty() == true) {
                    channel.trySend(data.list)
                } else {
                    channel.trySend(null)
                }
            }, onFail = { t ->
                channel.trySend(null)
                GwellLogUtils.e(TAG, "getDeviceHistoryList", t)
            }
        ))
        return channel.receive()
    }

    /**
     * 加载设备分享详情
     */
    fun loadDeviceShareDetail(
        inviteToken: String,
        deviceId: String
    ): Flow<HttpAction<DeviceShareDetail>> {
        val longDevId = deviceId.toLongOrNull() ?: 0
        return httpDataSource.getInviteInfo(inviteToken, longDevId)
    }

    /**
     * 设置消息状态
     * @param msgId 消息ID
     * @param status 状态 0:未读，1：已读，2：删除
     */
    suspend fun setMessageStatus(msgId: Long, status: MessageStatus): Boolean? {
        val result = Channel<Boolean?>(1)
        httpService.setUserMsgStatus(
            msgId,
            status.code,
            typeSubscriber<MsgStatusChangeList>(
                onSuccess = { data ->
                    val list = data?.list
                    result.trySend(list == null)
                }, onFail = { t ->
                    result.trySend(null)
                    GwellLogUtils.e(TAG, "setMessageStatus", t)
                }
            )
        )
        return result.receive()
    }

    /**
     * 访客确认接受主人对设备的分享
     */
    suspend fun acceptDeviceShare(inviteToken: String, remarkName: String): Boolean? {
        val result = Channel<Boolean?>(1)
        httpDataSource.confirmShare(inviteToken, remarkName)
            .collect { action ->
                when (action) {
                    is HttpAction.Loading -> Unit
                    is HttpAction.Success -> result.trySend(action.data != null)
                    is HttpAction.Fail -> result.trySend(false)
                }
            }
        return result.receive()
    }

    /**
     * 访客拒绝设备分享
     */
    fun rejectDeviceShare(devId: String, inviteToken: String): Flow<HttpAction<Any>> {
        return httpDataSource.cancelShare(devId, inviteToken)
    }
}