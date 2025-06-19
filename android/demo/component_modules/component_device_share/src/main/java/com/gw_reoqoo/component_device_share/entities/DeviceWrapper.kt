package com.gw_reoqoo.component_device_share.entities

import com.gw_reoqoo.component_family.api.interfaces.IDevice

/**
 * @Description: - 对设备的包装，在设备列表中使用
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 * @param device 设备信息
 * @param checked 是否选中
 */
data class DeviceWrapper(
    val device: IDevice,
    var checked: Boolean = false
)