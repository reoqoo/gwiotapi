package com.gw_reoqoo.component_family.api.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.component_family.repository.DeviceRepository
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @Description: - 对外提供API实现类
 * @Author: XIAOLEI
 * @Date: 2023/7/31
 */
@Singleton
class FamilyModelImpl @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val appConfigApi: IAppConfigApi,
) : FamilyModeApi {

    companion object {
        private const val TAG = "FamilyModelImpl"
    }

    /**
     * 根据用户ID，获取设备列表
     */
    override fun getDeviceList(userId: String): List<IDevice> {
        return deviceRepository.getAllDeviceSyncBy(userId).map {
            DeviceImpl(it)
        }
    }

    /**
     * 观察某个用户设备列表
     */
    override fun watchDeviceList(userId: String): LiveData<List<IDevice>> {
        return deviceRepository.getAllDeviceBy(userId).map { list ->
            list.map { deviceInfo ->
                DeviceImpl(deviceInfo)
            }
        }
    }

    /**
     * 添加设备
     */
    override suspend fun addDevice(device: IDevice): Boolean {
        return deviceRepository.addDevice(
            DeviceInfo(
                deviceId = device.deviceId,
                userId = device.userId,
                remarkName = device.remarkName,
                relation = device.relation,
                permission = device.permission,
                modifyTime = device.modifyTime
            )
        )
    }

    /**
     * 删除设备
     */
    override fun deleteDevice(device: IDevice): Flow<HttpAction<Any>> {
        return if (device.isMaster) {
            deviceRepository.unbindDevice(device.deviceId)
        } else {
            deviceRepository.cancelShare(device.deviceId)
        }.map { action ->
            if (action is HttpAction.Success) {
                this.refreshDevice()
            }
            action
        }
    }

    /**
     * 获取设备信息
     */
    override fun deviceInfo(id: String): IDevice? {
        val deviceInfo = deviceRepository.deviceInfo(id)
        return if (deviceInfo == null) {
            null
        } else {
            DeviceImpl(deviceInfo)
        }
    }

    /**
     * 根据ID观察设备的最新信息
     * @param deviceId 设备ID
     */
    override fun watchDevice(deviceId: String): LiveData<IDevice?> {
        return deviceRepository.watchIDevice(deviceId)
    }

    /**
     * 通知设备模块刷新最新设备数据
     */
    override suspend fun refreshDevice() {
        deviceRepository.loadDeviceFromRemote()
    }

    /**
     * 设备重命名
     */
    override suspend fun reName(deviceId: String, devName: String): Boolean {
        val success = deviceRepository.reName(deviceId, devName)
        this.refreshDevice()
        return success
    }

    /**
     * 判断是不是4G设备
     */
    override fun isFourGDevice(deviceId: String): Boolean? {
        val bean = deviceRepository.getDeviceServiceByDevId(deviceId) ?: return null
        return bean.isGwell4g
    }

    /**
     * 监听在线设备
     *
     * @param userId String 用户ID
     * @return LiveData<List<IDevice>> 设备列表
     */
    override fun watchOnlineDevice(userId: String): LiveData<List<IDevice>> {
        return deviceRepository.getAllDeviceBy(userId).map { list ->
            GwellLogUtils.i(TAG, "deviceInfo: list $list")
            list.mapNotNull { deviceInfo ->
                if (deviceInfo.online == 1) {
                    DeviceImpl(deviceInfo)
                } else {
                    null
                }
            }
        }
    }

    /**
     * 设备是否支持云存
     */
    override fun deviceSupportCloud(deviceId: String): Boolean? {
        return deviceRepository.deviceSupportCloud(deviceId)
    }

    /**
     * 设备云服务的过期时间（0表示未开通云服务，自动续费默认在当前时间+30天）
     *
     * @param deviceId String 设备ID
     * @return Long? 过期时间（单位ms）
     */
    override fun deviceVasExpireTime(deviceId: String): Long? {
        return deviceRepository.deviceCloudExpireTime(deviceId)
    }

    /**
     * 判断是否是4G设备
     *
     * @param deviceId String 设备id
     * @return Boolean?
     */
    override fun is4GDevice(deviceId: String): Boolean? {
        return deviceRepository.deviceSupport4G(deviceId)
    }

    /**
     * 判断是不是Reoqoo设备
     */
    override fun isReoqooDevice(pid: String): Boolean {
        val bean = appConfigApi.getDevConfigByPid(pid)
        val solution = bean?.solution
        return solution.isNullOrEmpty() || solution.equals("reoqoo", true)
    }
}