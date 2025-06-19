package com.gw.cp_mine.entity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 14:33
 * Description: SettingsEntity
 */
data class SettingsEntity(
    /**
     * 标题
     */
    val title: Int,

    /**
     * 描述
     */
    val description: Int,

    /**
     * 状态(已授权、未授权)
     */
    val status: Int? = null,

    /**
     * item类型
     */
    val itemType: SettingsItemType? = null
)

enum class SettingsItemType {
    /**
     * 推送
     */
    PUSH,

    /**
     * 定位
     */
    LOCATION,

    /**
     * 蓝牙
     */
    BLUETOOTH,

    /**
     * 存储
     */
    STORAGE,

    /**
     * 后台运行
     */
    BACKGROUND_RUN,

    /**
     * 相机
     */
    CAMERA,

    /**
     * 相册、
     */
    ALBUM,

    /**
     * 麦克风
     */
    MICROPHONE,

    /**
     * 悬浮窗
     */
    FLOATING_WINDOW,

    /**
     * 后台弹窗
     */
    BACKGROUND_POP
}
