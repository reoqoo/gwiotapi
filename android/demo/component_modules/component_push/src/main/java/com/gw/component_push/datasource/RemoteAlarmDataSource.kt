package com.gw.component_push.datasource

import com.gw.component_plugin_service.service.IPluginVasService
import com.gw_reoqoo.lib_http.IotHttpCallback
import com.gw_reoqoo.lib_http.entities.AlarmInfoEntity
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.httpviap2p.HttpViaP2PProxy
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/7 16:52
 * Description: RemoteMsgDataSource
 */
class RemoteAlarmDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteAlarmDataSource"
    }

    private val vasService by lazy { HttpViaP2PProxy().create(IPluginVasService::class.java) }

    /**
     * 获取消息列表数据
     *
     * @return RespResult<MsgListEntity> 回调
     */
    suspend fun getAlarmEventInfo(tid: String, alarmId: String): RespResult<AlarmInfoEntity> {
        val result = Channel<RespResult<AlarmInfoEntity>>(1)
        vasService.queryEventInfo(tid,
            alarmId,
            IotHttpCallback.create(
                onSuccess = {
                    GwellLogUtils.i(TAG, "getAlarmEventInfo conSuccess alarmInfo:$it")
                    result.trySend(RespResult.Success(it))
                },
                onFail = {code, msg ->
                    result.trySend(RespResult.ServerError(code, msg))
                    GwellLogUtils.e(TAG, "getAlarmEventInfo code:$code msg:$msg")
                }
            )
        )
        return result.receive()
    }

}