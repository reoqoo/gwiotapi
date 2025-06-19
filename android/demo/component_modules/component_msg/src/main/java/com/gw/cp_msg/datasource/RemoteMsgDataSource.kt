package com.gw.cp_msg.datasource

import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_msg.entity.http.MsgListEntity
import com.gw.cp_msg.entity.http.MsgReadEntity
import com.gw_reoqoo.lib_http.entities.VersionInfoEntity
import com.gw_reoqoo.lib_http.DeviceInfoService
import com.gw_reoqoo.lib_http.IotHttpCallback
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.entities.AlarmEvent
import com.gw_reoqoo.lib_http.entities.AlarmEventData
import com.gw_reoqoo.lib_http.entities.AppUpgradeEntity
import com.gw_reoqoo.lib_http.mapActionFlow
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.httpviap2p.HttpViaP2PProxy
import kotlinx.coroutines.channels.Channel
import java.util.Locale
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/7 16:52
 * Description: RemoteMsgDataSource
 */
class RemoteMsgDataSource @Inject constructor(
    private val accountApi: IAccountApi,
    private val familyApi: FamilyModeApi,
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteMsgDataSource"
    }

    private val devService by lazy {
        HttpViaP2PProxy().create(DeviceInfoService::class.java)
    }

    /**
     * 获取消息列表数据
     *
     * @return RespResult<MsgListEntity> 回调
     */
    suspend fun getMsgList(): RespResult<MsgListEntity> {
        val result = Channel<RespResult<MsgListEntity>>(1)
        httpService.getMsgList("",
            typeSubscriber<MsgListEntity>(
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
            )
        )
        return result.receive()
    }

    /**
     * 获取最新的报警事件
     *
     * @return 最新的报警事件
     */
    suspend fun loadLastAlarmEvent(): RespResult<List<AlarmEvent>> {
        val result = Channel<RespResult<List<AlarmEvent>>>()
        accountApi.getAsyncUserId()?.run {
            familyApi.getDeviceList(userId = this).map {
                it.deviceId.toLong()
            }.toLongArray().run {
                httpService.getNewestEvent(
                    true,
                    this,
                    typeSubscriber<AlarmEventData>(
                        onSuccess = { entity ->
                            GwellLogUtils.i(TAG, "AlarmEventData: $entity")
                            result.trySend(RespResult.Success(entity?.alarmList))
                        },
                        onFail = {
                            if (it is ResponseNotSuccessException) {
                                result.trySend(RespResult.ServerError(it.code, it.msg))
                            } else {
                                result.trySend(RespResult.LocalError(it))
                            }
                        }
                    )
                )
                httpService.getNewestEvent(true, this).mapActionFlow<AlarmEventData>()
            }
        } ?: kotlin.run {
            GwellLogUtils.e(TAG, "loadLastAlarmEvent fail, userId is null")
            result.trySend(RespResult.LocalError(Throwable("loadLastAlarmEvent fail, userId is null")))
        }
        return result.receive()
    }

    suspend fun getAppUpdate(): RespResult<AppUpgradeEntity> {
        val result = Channel<RespResult<AppUpgradeEntity>>(1)
        httpService.appUpgrade(typeSubscriber<AppUpgradeEntity>(
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

    /**
     * 获取设备的新版本
     *
     * @param deviceId String 设备ID
     * @param curVersion String 设备当前版本
     * @return RespResult<Any>
     */
    suspend fun getDevUpdateMsg(
        deviceId: String,
        curVersion: String
    ): RespResult<VersionInfoEntity?> {
        val result = Channel<RespResult<VersionInfoEntity?>>(1)
        devService.queryDeviceNewVersionInfoLocale(
            deviceId,
            "",
            Locale.getDefault(),
            curVersion,
            IotHttpCallback.create(
                onSuccess = {
                    result.trySend(RespResult.Success(it))
                },
                onFail = { code, msg ->
                    result.trySend(RespResult.ServerError(code, msg))
                }
            )
        )
        return result.receive()
    }

    /**
     * 将某类消息置为已读
     * @param tag   消息tag
     */
    suspend fun readSystemMsg(tag: String?, deviceId: Long?): RespResult<MsgReadEntity> {
        val result = Channel<RespResult<MsgReadEntity>>(1)
        httpService.readMsg(
            tag, deviceId ?: 0, typeSubscriber<MsgReadEntity>(
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
            )
        )
        return result.receive()
    }

}