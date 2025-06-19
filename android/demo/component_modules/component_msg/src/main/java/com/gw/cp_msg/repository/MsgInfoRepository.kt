package com.gw.cp_msg.repository

import com.gw.cp_msg.datasource.RemoteMsgInfoDataSource
import com.gw.cp_msg.entity.http.MsgInfoListEntity
import com.gw_reoqoo.lib_http.RespResult
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/20 11:52
 * Description: MsgInfoRepository
 */
class MsgInfoRepository @Inject constructor(
    private val remoteMsgInfoDS: RemoteMsgInfoDataSource
) {

    companion object {
        private const val TAG = "MsgInfoRepository"
    }

    suspend fun loadMsgInfo(
        tag: String,
        lastId: Long,
        pageSize: Int
    ): RespResult<MsgInfoListEntity> = remoteMsgInfoDS.loadMsgInfo(tag, lastId, pageSize)

}