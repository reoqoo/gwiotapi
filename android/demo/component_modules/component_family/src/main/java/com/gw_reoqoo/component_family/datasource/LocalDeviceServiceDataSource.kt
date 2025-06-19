package com.gw_reoqoo.component_family.datasource

import com.gw_reoqoo.lib_room.device.DeviceService
import com.gw_reoqoo.lib_room.device.DeviceServiceDao
import javax.inject.Inject

/**
 * @Description: - 本地的DeviceServiceDataSource
 * @Author: XIAOLEI
 * @Date: 2023/8/17
 */
class LocalDeviceServiceDataSource @Inject constructor(
    private val deviceServiceDao: DeviceServiceDao
) {
    /**
     * 删除指定设备ID的信息
     */
    fun deleteBy(deviceIds: List<String>) {
        deviceServiceDao.deleteBy(deviceIds)
    }

    /**
     * 判断设备指定ID的设备信息是否存在
     */
    fun deviceServiceExist(deviceId: String): Boolean {
        return deviceServiceDao.deviceServiceExist(deviceId)
    }

    /**
     * 修改DeviceService信息
     */
    fun update(service: DeviceService) {
        deviceServiceDao.update(service)
    }

    /**
     * 新增DeviceService
     */
    fun insert(service: DeviceService) {
        deviceServiceDao.insert(service)
    }

    /**
     * 通过设备ID，获取设备服务的数据Bean
     */
    fun getDeviceServiceBy(deviceId: String): DeviceService? {
        return deviceServiceDao.getDeviceServiceByDevId(deviceId)
    }
}