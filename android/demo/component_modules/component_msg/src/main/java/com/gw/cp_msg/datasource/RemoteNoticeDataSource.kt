package com.gw.cp_msg.datasource

import com.gw.cp_msg.entity.http.EventBenefitsEntity
import com.gw.cp_msg.entity.http.NoticeDataEntity
import com.gw.cp_msg.entity.http.NoticeList
import com.gw.cp_msg.entity.http.UserMessageData
import com.gw_reoqoo.lib_http.entities.MsgStatusChangeList
import com.gw_reoqoo.lib_http.mapActionFlow
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/12 10:28
 * Description: RemoteNoticeDataSource
 */
class RemoteNoticeDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteNoticeDataSource"
    }

    /**
     * 获取公告数据
     *
     * @param notAutoRead false则自动设置成已读，true则需要app手动设置成已读
     * @return Flow<HttpAction<Any>>
     */
    fun getNoticeData(notAutoRead: Boolean): Flow<HttpAction<NoticeDataEntity>> {
        return httpService.getNotice(notAutoRead).mapActionFlow()
    }

    /**
     * 获取活动福利列表
     *
     * @return Flow<HttpAction<MsgStatusChangeList>>
     */
    fun getEventBenefits(): Flow<HttpAction<NoticeList>> {
        return httpService.noticeList.mapActionFlow()
    }

    /**
     * 获取用户消息
     *
     * @param pageSize Int   请求最大值200
     * @param pageIndex Int  请求的页码
     * @param isUnRead Boolean  是否只拉取未读消息
     * @param notAutoRead Boolean 是否自动设置成已读
     * @return Flow<HttpAction<UserMessageEntity>> 用户消息
     */
    fun getUserMessage(
        pageSize: Int, pageIndex: Int, isUnRead: Boolean, notAutoRead: Boolean,
    ): Flow<HttpAction<UserMessageData>> {
        return httpService.getUserMsg(
            pageSize,
            pageIndex,
            isUnRead,
            notAutoRead,
            0
        ).mapActionFlow()
    }

    /**
     * 确认免费服务
     *
     * @param deviceId String 设备id
     * @param msgId String 免费服务消息id
     * @return Flow<HttpAction<Any>>
     */
    fun confirmShowFreeService(
        deviceId: String?,
        msgId: String
    ): Flow<HttpAction<Any>> {
        return httpService.confirmShowFreeService(deviceId, msgId).mapActionFlow()
    }

    /**
     * 设置消息状态
     *
     * @param msgId 消息ID
     * @param status 状态 0:未读，1：已读，2：删除
     */
    suspend fun setMessageStatus(msgId: Long, status: Int): Boolean? {
        val result = Channel<Boolean?>(1)
        httpService.setUserMsgStatus(
            msgId,
            status,
            typeSubscriber<MsgStatusChangeList>(
                onSuccess = { data ->
                    GwellLogUtils.i(TAG, "setMessageStatus: $data")
                    val list = data?.list
                    result.trySend(list == null)
                }, onFail = { t ->
                    result.trySend(false)
                    GwellLogUtils.e(TAG, "setMessageStatus", t)
                }
            )
        )
        return result.receive()
    }

    /**
     * 设置活动福利状态为已读
     *
     * @param activeIds LongArray 活动福利的ids
     * @return Boolean? 是否成功 true -- 成功， false -- 失败
     */
    suspend fun setBenefitsStatusRead(activeIds: LongArray): Boolean? {
        val result = Channel<Boolean?>(1)
        httpService.readActiveMsg(
            activeIds,
            typeSubscriber<EventBenefitsEntity>(
                onSuccess = { data ->
                    GwellLogUtils.i(TAG, "setBenefitsStatusRead: $data")
                    val list = data?.dataEntity
                    result.trySend(list == null)
                }, onFail = { t ->
                    result.trySend(null)
                    GwellLogUtils.e(TAG, "setBenefitsStatusRead", t)
                })
        )
        return result.receive()
    }

    /**
     * 设置公告状态
     *
     * @param tag 公告tag
     * @param status 状态 0:未读，1：已读，2：删除
     */
    suspend fun setNoticeStatus(tag: String, status: Int): Boolean? {
        val result = Channel<Boolean?>(1)
        httpService.setNoticeStatus(
            tag,
            status,
            typeSubscriber<MsgStatusChangeList>(
                onSuccess = { data ->
                    GwellLogUtils.i(TAG, "setMessageStatus: $data")
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

}
