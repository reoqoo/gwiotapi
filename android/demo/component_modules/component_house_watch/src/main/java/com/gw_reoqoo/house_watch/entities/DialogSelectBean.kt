package com.gw_reoqoo.house_watch.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.resource.R as RR


/**
 * 事件类的装饰类
 */
data class ActiveTypeWrapper(
    val type: com.gw_reoqoo.house_watch.entities.ActiveType?,
    var checked: Boolean,
)

/**
 * 设备类的装饰类
 */
data class DeviceWrapper(
    val device: IDevice?,
    var checked: Boolean,
)

/**
 * 活动类型
 * @param descRes 显示名称
 * @param bitOfIndex bit位的位置
 * @param iconRes 图标资源
 */
enum class ActiveType(
    @StringRes val descRes: Int,
    val bitOfIndex: Int,
    @DrawableRes val iconRes: Int
) {
    /**
     * 设备呼叫
     */
    CALL_ACTIVE(
        RR.string.AA0363,
        6,
        R.drawable.house_watch_icon_call_active
    ),

    /**
     * 人脸识别
     */
    FACE_ACTIVE(
        RR.string.AA0364,
        4,
        R.drawable.house_watch_icon_face_active
    ),

    /**
     * 有人活动
     */
    HUMAN_ACTIVE(
        RR.string.AA0209,
        1,
        R.drawable.house_watch_icon_human_active
    ),

    /**
     * 宝宝哭声
     */
    BABY_CRY_ACTIVE(
        RR.string.AA0365,
        23,
        R.drawable.house_watch_icon_baby_cry_active
    ),

    /**
     * 宠物活动
     */
    PET_ACTIVE(
        RR.string.AA0366,
        20,
        R.drawable.house_watch_icon_pet_active
    ),

    /**
     * 车辆移动
     */
    CAR_ACTIVE(
        RR.string.AA0212,
        21,
        R.drawable.house_watch_icon_car_active
    ),

    /**
     * 疑似火焰
     */
    FIRE_ACTIVE(
        RR.string.AA0512,
        24,
        R.drawable.house_watch_icon_fire_active
    ),

    /**
     * 场景联动
     */
    SCENE_ACTIVE(
        RR.string.AA0211,
        3,
        R.drawable.house_watch_icon_scene_active
    ),

    /**
     * 画面变化
     */
    VIEW_ACTIVE(
        RR.string.AA0210,
        0,
        R.drawable.house_watch_icon_view_active
    ),

}
