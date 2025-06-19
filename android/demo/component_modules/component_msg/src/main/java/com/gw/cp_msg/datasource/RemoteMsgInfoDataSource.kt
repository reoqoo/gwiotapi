package com.gw.cp_msg.datasource

import com.gw.cp_msg.entity.http.MsgInfoListEntity
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/7 16:52
 * Description: RemoteMsgDataSource
 */
class RemoteMsgInfoDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteMsgDataSource"
    }

    /**
     * 拉取消息详情
     *
     * @param tag    消息大类
     * @param lastId 最后一条消息Id
     */
    suspend fun loadMsgInfo(
        tag: String,
        lastId: Long,
        pageSize: Int
    ): RespResult<MsgInfoListEntity> {
        val result = Channel<RespResult<MsgInfoListEntity>>(1)
        httpService
            .getMsgInfoList(tag, lastId, pageSize, typeSubscriber<MsgInfoListEntity>(
                onSuccess = {
                    result.trySend(RespResult.Success(it))
                },
                onFail = {
                    if (it is ResponseNotSuccessException) {
                        result.trySend(RespResult.ServerError(it.code, it.msg))
                    } else {
                        result.trySend(RespResult.LocalError(it))
                    }
                }
            ))
        return result.receive()
    }

}