package com.gw_reoqoo.house_watch.entities

import android.os.Bundle
import androidx.core.os.bundleOf
import com.gw_reoqoo.component_family.api.interfaces.BundleDevice
import com.gw_reoqoo.component_family.api.interfaces.IDevice

/**
 * @Description: - 对设备类的包装，并且加入是否显示视图的属性
 * @Author: XIAOLEI
 * @Date: 2023/8/23
 * @param device 设备
 * @param offView 是否隐藏视图
 */
class DevicePack(
    /**
     * 设备
     */
    val device: IDevice,
    /**
     * 隐藏视图，为true，意思为隐藏视图
     */
    var offView: Boolean = false,
) : IDevice by device {
    companion object {
        private const val OFFVIEW_KEY = "offView"
        private const val DEVICE_KEY = "device"
    }

    constructor(bundle: Bundle?) : this(
        offView = bundle?.getBoolean(OFFVIEW_KEY) ?: false,
        device = BundleDevice(bundle?.getBundle(DEVICE_KEY))
    )

    /**
     * 克隆函数
     */
    fun clone(): DevicePack {
        return DevicePack(device, offView)
    }

    override fun toBundle(): Bundle {
        return bundleOf(
            OFFVIEW_KEY to offView,
            DEVICE_KEY to device.toBundle(),
        )
    }
}