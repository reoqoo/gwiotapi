package com.gw.cp_msg.entity.http

import com.google.gson.annotations.SerializedName
import com.jwkj.lib_json_kit.IJsonEntity

/**
 * 消息中心数据对象
 *
 * @property msg 一级界面消息列表
 * @constructor
 */

data class MsgListEntity(

    @SerializedName("list")
    val list: List<MsgDetailEntity>?

) : IJsonEntity
