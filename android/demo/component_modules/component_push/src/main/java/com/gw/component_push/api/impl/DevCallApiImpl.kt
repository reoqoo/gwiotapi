package com.gw.component_push.api.impl

import com.gw.component_plugin_service.api.IPluginManager
import com.gw.component_push.api.interfaces.IDevCallApi
import com.gw.component_push.entity.AlarmEventEntity
import com.gw.component_push.entity.PushMsgContentEntity
import com.gwell.loglibs.GwellLogUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/19 10:58
 * Description: DevCallApiImpl
 */
@Singleton
class DevCallApiImpl @Inject constructor(
    private val iPluginManager: IPluginManager,
) : IDevCallApi {
    companion object {
        private const val TAG = "DevCallApiImpl"
    }

    /**
     * 接收呼叫事件
     *
     * @param entity EventMessage 事件消息
     */
    override fun receiveCallEvent(entity: AlarmEventEntity) {
        GwellLogUtils.i(TAG, "receiveCallEvent: entity= $entity")
        iPluginManager.startCallActivity(entity.data.deviceId, entity.data.triTime)
    }

    override fun receiveCallEvent(entity: PushMsgContentEntity) {
        GwellLogUtils.i(TAG, "receiveCallEvent: entity= $entity")
        iPluginManager.startCallActivity(entity.data.deviceId, entity.data.pushTime)
    }

}