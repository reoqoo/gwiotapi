package com.gw.component_push.api.interfaces

import com.gw.component_push.entity.AlarmEventEntity
import com.gw.component_push.entity.PushMsgContentEntity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/19 10:52
 * Description: IDevCallApi
 */
interface IDevCallApi {

    /**
     * 接收呼叫事件
     *
     * @param entity EventMessage 事件消息
     */
    fun receiveCallEvent(entity: AlarmEventEntity)

    /**
     * 接收呼叫事件
     *
     * @param entity PushMsgContentEntity 事件消息
     */
    fun receiveCallEvent(entity: PushMsgContentEntity)

}