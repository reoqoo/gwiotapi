package com.gw_reoqoo.component_device_share.ui.permission_setting.adapter

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/7/13 15:34
 * Description: PermissionEntity
 */
data class PermissionEntity(
    /**
     * 权限组名称
     */
    val title: String,

    /**
     * 权限组实体功能
     */
    val function: List<FunctionEntity>,
)

data class FunctionEntity(

    val functionKey: FunctionKey? = null,

    /**
     * 功能名称
     */
    val functionName: String,

    /**
     * 功能是否可用
     */
    var functionEnable: Boolean,

    /**
     * 功能描述
     */
    val functionDesc: String? = null,

    /**
     * 功能状态
     */
    var functionStatus: Boolean,
)

enum class FunctionKey(val key: Int) {

    TALK(1),

    PTZ(2),

    PLAYBACK(3),

    WATCH(4),

    CONFIG(5);

}