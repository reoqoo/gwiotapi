package com.gw_reoqoo.component_family.datasource

import androidx.lifecycle.LiveData
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.device.DeviceInfoDao
import com.gw_reoqoo.lib_room.device.DeviceService
import com.gw_reoqoo.lib_room.device.DeviceServiceDao
import com.gwell.loglibs.GwellLogUtils
import java.lang.Exception
import java.util.Date
import javax.inject.Inject


/**
 * @Description: - 设备本地数据库数据操作
 * @Author: XIAOLEI
 * @Date: 2023/7/31
 */
class LocalDeviceDataSource @Inject constructor(
    private val deviceInfoDao: DeviceInfoDao,
    private val deviceServiceDao: DeviceServiceDao
) {
    companion object {
        private const val TAG = "LocalDeviceDataSource"
    }

    /**
     * 根据用户ID获取所有的设备
     */
    fun getAllDeviceSyncBy(userId: String): List<DeviceInfo> {
        return deviceInfoDao.getAllDeviceSyncBy(userId)
    }

    /**
     * 根据用户ID获取所有的设备ID
     */
    fun getAllDeviceIdSyncBy(userId: String): List<String> {
        return deviceInfoDao.getAllDeviceIdSyncBy(userId)
    }

    /**
     * 获取用户名下所有的设备
     */
    fun getAllDeviceBy(userId: String) = deviceInfoDao.getAllDeviceBy(userId)

    /**
     * 添加设备
     */
    fun addDevice(device: DeviceInfo): Boolean {
        return try {
            val nextIndex = deviceInfoDao.getNextIndex(device.userId)
            device.index = nextIndex
            deviceInfoDao.insert(device)
            true
        } catch (e: Exception) {
            GwellLogUtils.e(TAG, "addDevice-exception", e)
            false
        }
    }

    /**
     * 批量添加设备
     */
    fun addDevices(devices: List<DeviceInfo>): Boolean {
        return try {
            for (device in devices) {
                addDevice(device)
            }
            true
        } catch (e: Exception) {
            GwellLogUtils.e(TAG, "addDevices-exception", e)
            false
        }
    }

    /**
     * 删除设备
     */
    fun deleteDevice(devices: List<DeviceInfo>): Boolean {
        return try {
            deviceInfoDao.delete(devices)
            true
        } catch (e: Exception) {
            GwellLogUtils.e(TAG, "deleteDevice-s-exception", e)
            false
        }
    }

    /**
     * 根据设备ID删除设备
     */
    fun deleteDeviceById(deviceIds: List<String>) {
        deviceInfoDao.deleteDeviceById(deviceIds)
    }

    /**
     * 根据设备ID获取设备原始信息
     * @param deviceId String 设备ID
     * @return String?
     */
    fun getOriginJsonById(deviceId: String) = deviceInfoDao.getOriginJsonById(deviceId)

    /**
     * 删除设备
     */
    fun deleteDevice(device: DeviceInfo): Boolean {
        return try {
            deviceInfoDao.delete(device)
            true
        } catch (e: Exception) {
            GwellLogUtils.e(TAG, "deleteDevice-exception", e)
            false
        }
    }

    /**
     * 根据设备ID查找设备信息
     */
    fun deviceInfo(deviceId: String): DeviceInfo? {
        return deviceInfoDao.getDeviceInfoByIdSync(deviceId)
    }

    /**
     * 更新设备信息
     */
    fun updateDevice(device: DeviceInfo) {
        if (device.index == null) {
            var index = deviceInfoDao.getIndexByDeviceId(device.deviceId)
            if (index == null) {
                index = deviceInfoDao.getNextIndex(device.userId)
            }
            device.index = index
        }
        deviceInfoDao.update(device)
    }

    /**
     * 更新设备的开机状态
     *
     * @param deviceId 设备ID
     * @param powerOn 开机状态
     */
    fun updateDevicePowerOn(deviceId: String, powerOn: Boolean) {
        deviceInfoDao.updateDevicePowerOn(deviceId, powerOn)
    }

    /**
     * 更新设备的在线状态
     *
     * @param deviceId 设备ID
     * @param status 在线状态
     */
    fun updateDeviceOnlineStatus(deviceId: String, status: Int) {
        deviceInfoDao.updateDeviceOnlineStatus(deviceId, status)
    }

    /**
     * 更新设备信息
     */
    fun updateDevice(devices: List<DeviceInfo>) {
        for (device in devices) {
            updateDevice(device)
        }
    }

    /**
     * 根据设备ID观察最新设备信息
     * @param deviceId 设备ID
     */
    fun watchDevice(deviceId: String): LiveData<DeviceInfo?> {
        return deviceInfoDao.getDeviceInfoByIdAsync(deviceId)
    }

    /**
     * 根据设备ID获取设备服务信息
     * @param deviceId 设备ID
     */
    fun getDeviceServiceByDevId(deviceId: String): DeviceService? {
        return deviceServiceDao.getDeviceServiceByDevId(deviceId)
    }
}