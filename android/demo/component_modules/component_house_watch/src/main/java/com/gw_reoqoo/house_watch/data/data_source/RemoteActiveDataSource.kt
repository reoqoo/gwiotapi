package com.gw_reoqoo.house_watch.data.data_source

import com.gw.component_plugin_service.service.IPluginVasService
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_http.IotHttpCallback
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.entities.ActiveList
import com.gw_reoqoo.lib_http.entities.VersionInfoEntity
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.httpviap2p.HttpViaP2PProxy
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * @Description: - 远程设备活动数据源
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 */
class RemoteActiveDataSource @Inject constructor(
    val httpService: HttpServiceWrapper,
    val accountApi: IAccountApi
) {
    companion object {
        private const val TAG = "RemoteActiveDataSource"
    }

    private val vasService by lazy { HttpViaP2PProxy().create(IPluginVasService::class.java) }

    /**
     * 获取用户全设备事件列表
     * @param devIds 设备ID列表
     * @param startTime 事件开始时间
     * @param endTime 事件结束时间
     * @param almTypeMasks 时间类型过滤列表
     *
     * 参数名                参数类型      是否必填   参数描述
     * <p>
     * devIds               array<int64>    是   设备id列表
     * startTime            int64           是   查询的起始时间
     * endTime              int64           是   查询的结束时间
     * almTypeMasks         array<int64>    否   告警类型掩码过滤列表
     * validCloudStorage    bool            否   是否只返回有效云存期内的事件，不填默认为否
     * detail               int             否   是否返回事件的详细列表
     * ------------------------------------------> 0或不填    不返回详细列表
     * ------------------------------------------> 1           返回完整的详细事件列表
     * ------------------------------------------> 2           返回经过过滤的详细列表，过滤规则:每种类型的事件只返回第一次触发的时间
     * faceOpt              int             否   人脸识别查询选项
     * ------------------------------------------> 0或不填      不包含人脸过滤条件，不带上人脸识别信息
     * ------------------------------------------> 1           返回仅包含人脸识别的事件的，带上对应的人脸识别信息
     * ------------------------------------------> 2           返回全部事件，如果事件中带有人脸识别，带上对应的人脸识别信息
     */
    suspend fun getActiveList(
        devIds: List<String>,
        startTime: Long,
        endTime: Long,
        almTypeMasks: List<Long>
    ): RespResult<ActiveList> {
        val longDevIds = devIds.mapNotNull { it.toLongOrNull() }
        val validCloudStorage = false
        val detail = 0
        val faceOpt = 0
        val result = Channel<RespResult<ActiveList>>(1)
        val accessId = accountApi.getSyncUserInfo()?.accessId ?: ""

        vasService.listAllDevEvent(
            longDevIds,
            startTime,
            endTime,
            almTypeMasks,
            validCloudStorage,
            accessId,
            detail,
            faceOpt,
            IotHttpCallback.create(
                onSuccess = {
                    GwellLogUtils.i(TAG, "getActiveList conSuccess activeList:$it")
                    result.trySend(RespResult.Success(it))
                },
                onFail = { code, msg ->
                    GwellLogUtils.e(TAG, "getActiveList code:$code msg:$msg")
                    result.trySend(RespResult.ServerError(code, msg))
                }
            )
        )
        return result.receive()
    }
}