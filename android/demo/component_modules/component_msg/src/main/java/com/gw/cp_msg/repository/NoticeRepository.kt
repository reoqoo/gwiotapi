package com.gw.cp_msg.repository

import com.gw.cp_msg.datasource.RemoteNoticeDataSource
import com.gw.cp_msg.entity.http.NoticeDataEntity
import com.gw.cp_msg.entity.http.NoticeList
import com.gw.cp_msg.entity.http.UserMessageData
import com.gw.cp_msg.entity.http.UserMessageListBean
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/12 10:24
 * Description: 公告数据Repository
 */
class NoticeRepository @Inject constructor(
    private val noticeDataSource: RemoteNoticeDataSource,
) {

    companion object {
        private const val TAG = "NoticeRepository"

        /**
         * 获取用户消息每页长度，服务器无限制，App每次拉取200
         */
        private const val USER_MSG_MAX_SIZE = 200


        /**
         * 用户消息是否只拉取未读消息
         */
        private const val USER_MSG_UNREAD = true

    }

    /**
     * 用户消息页码
     */
    private var pageSizeIndex = 0

    private var isLoadEnd = false

    /**
     * 用户消息列表
     */
    private val userMsgList = mutableListOf<UserMessageListBean>()

    /**
     * 获取公告数据
     *
     * @return Flow<HttpAction<NoticeDataEntity>>
     */
    fun getNoticeList(notAutoRead: Boolean): Flow<HttpAction<NoticeDataEntity>> {
        return noticeDataSource.getNoticeData(notAutoRead)
    }

    /**
     * 获取活动福利列表
     *
     * @return Flow<HttpAction<MsgStatusChangeList>>
     */
    fun getEventBenefits(): Flow<HttpAction<NoticeList>> {
        return noticeDataSource.getEventBenefits()
    }

    /**
     * 获取用户消息
     *
     * @return Flow<HttpAction<UserMessageEntity>>
     */
    fun getUserMessageByPageFlow(notAutoRead: Boolean): Flow<HttpAction<UserMessageData>> {
        return noticeDataSource.getUserMessage(
            USER_MSG_MAX_SIZE,
            pageSizeIndex,
            USER_MSG_UNREAD,
            notAutoRead
        )
    }

    /**
     * 确认免费服务
     *
     * @param deviceId String
     * @param msgId String
     * @return Flow<HttpAction<Any>>
     */
    fun confirmShowFreeService(deviceId: String, msgId: String): Flow<HttpAction<Any>> {
        return noticeDataSource.confirmShowFreeService(deviceId, msgId)
    }

    /**
     * 设置消息状态
     *
     * @param msgId 消息ID
     * @param status 状态 0:未读，1：已读，2：删除
     */
    suspend fun setMessageStatus(msgId: Long, status: Int): Boolean? {
        return noticeDataSource.setMessageStatus(msgId, status)
    }

    /**
     * 设置活动福利状态为已读
     *
     * @param activeIds LongArray 活动福利的ids
     * @return Boolean? 是否成功 true -- 成功， false -- 失败
     */
    suspend fun setBenefitsStatusRead(activeIds: LongArray): Boolean? {
        return noticeDataSource.setBenefitsStatusRead(activeIds)
    }

    /**
     * 设置公告状态
     *
     * @param tag 标签
     * @param status 状态 0:未读，1：已读，2：删除
     */
    suspend fun setNoticeStatus(tag: String, status: Int): Boolean? {
        return noticeDataSource.setNoticeStatus(tag, status)
    }

}
