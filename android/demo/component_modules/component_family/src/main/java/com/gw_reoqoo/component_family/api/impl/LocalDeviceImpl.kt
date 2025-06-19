package com.gw_reoqoo.component_family.api.impl

import com.gw_reoqoo.component_family.api.interfaces.ILocalDeviceApi
import com.gw_reoqoo.component_family.datasource.LocalDeviceDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDeviceImpl @Inject constructor(
    val localDeviceDataSource: LocalDeviceDataSource
) : ILocalDeviceApi {
    /**
     * 更新设备的开机状态
     *
     * @param deviceId 设备ID
     * @param powerOn 开机状态
     */
    override fun updateDevicePowerOn(deviceId: String, powerOn: Boolean) {
        localDeviceDataSource.updateDevicePowerOn(deviceId, powerOn)
    }

    /**
     * 更新设备的在线状态
     *
     * @param deviceId 设备ID
     * @param status 在线状态
     */
    override fun updateDeviceOnlineStatus(deviceId: String, status: Int) {
        localDeviceDataSource.updateDeviceOnlineStatus(deviceId, status)
    }
}