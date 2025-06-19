package com.gw_reoqoo.component_family.repository

import com.gw_reoqoo.component_family.datasource.RemoteUserMsgDataSource
import com.gw.lib_http.entities.DeviceHistoryBean
import com.gw_reoqoo.lib_http.entities.DeviceShareDetail
import com.gw_reoqoo.lib_http.entities.MessageBean
import com.gw_reoqoo.lib_http.entities.MessageStatus
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @Description: - 用户消息的Repository
 * @Author: XIAOLEI
 * @Date: 2023/8/24
 */
class UserMsgRepository @Inject constructor(
    private val dataSource: RemoteUserMsgDataSource
) {
    /**
     * 获取未读的设备分享消息列表
     */
    suspend fun loadUnReadDeviceShareMsgList(): List<MessageBean> {
        return dataSource.loadUnReadDeviceShareMsgList() ?: emptyList()
    }

    /**
     * 加载设备分享详情
     */
    fun loadDeviceShareDetail(
        inviteToken: String,
        deviceId: String
    ): Flow<HttpAction<DeviceShareDetail>> {
        return dataSource.loadDeviceShareDetail(inviteToken, deviceId)
    }

    /**
     * 设置消息状态
     * @param msgId 消息ID
     * @param status 状态 0:未读，1：已读，2：删除
     */
    suspend fun setMessageStatus(msgId: Long, status: MessageStatus): Boolean? {
        return dataSource.setMessageStatus(msgId, status)
    }

    /**
     * 访客确认接受主人对设备的分享
     */
    suspend fun acceptDeviceShare(inviteToken: String, remarkName: String): Boolean? {
        return dataSource.acceptDeviceShare(inviteToken, remarkName)
    }

    /**
     * 历史用户首次登录新app获取相关设备信息
     * @return List<DeviceHistoryBean> 历史设备信息列表
     */
    suspend fun getDeviceHistoryList(): List<DeviceHistoryBean> {
        return dataSource.getDeviceHistoryList()?: emptyList()
    }
}