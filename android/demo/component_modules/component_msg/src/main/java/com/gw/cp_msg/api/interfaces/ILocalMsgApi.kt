package com.gw.cp_msg.api.interfaces

import com.gw.cp_msg.entity.http.MsgDetailEntity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/11/2 14:27
 * Description: IMsgApi
 */
interface ILocalMsgApi {

    /**
     * 获取消息中的数据
     *
     * @param onResult Function1<List<MsgDetailEntity>?, Unit> 回调
     */
    fun initMsgList(onResult: (List<MsgDetailEntity>?) -> Unit)

    /**
     * 获取设备升级的列表
     *
     * @return List<MsgDetailEntity>
     */
    fun getDevUpgradeList(): List<MsgDetailEntity>

    /**
     * 获取未读消息的数量
     *
     * @return Int 未读消息的数量
     */
    fun getUnreadMsgCount(unReadMsgCount: ((Int) -> Unit))

}