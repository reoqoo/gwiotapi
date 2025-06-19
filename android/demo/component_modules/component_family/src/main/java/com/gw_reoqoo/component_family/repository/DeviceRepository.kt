package com.gw_reoqoo.component_family.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.gw_reoqoo.component_family.api.impl.DeviceImpl
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.component_family.datasource.LocalDeviceDataSource
import com.gw_reoqoo.component_family.datasource.LocalDeviceServiceDataSource
import com.gw_reoqoo.component_family.datasource.RemoteDeviceDataSource
import com.gw_reoqoo.component_family.datasource.RemoteSceneDataSource
import com.gw_reoqoo.component_family.services.DeviceStatusServices
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_http.entities.ScanShareQRCodeResult
import com.gw_reoqoo.lib_http.toJson
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.device.DeviceService
import com.gw_reoqoo.lib_utils.ktx.bitAt
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * @Description: - 设备管理的数据IO中心
 * @Author: XIAOLEI
 * @Date: 2023/7/31
 */
class DeviceRepository @Inject constructor(
    private val localDeviceDataSource: LocalDeviceDataSource,
    private val remoteDeviceDataSource: RemoteDeviceDataSource,
    private val remoteSceneDataSource: RemoteSceneDataSource,
    private val localDeviceServiceDataSource: LocalDeviceServiceDataSource,
    private val accountApi: IAccountApi,
    @ApplicationContext private val context: Context
) {

    companion object {
        private val mutex = Mutex()
        private const val TAG = "DeviceRepository"

        private const val TIME_ONE_MONTH = 1000 * 60 * 60 * 24 * 30L
    }

    /**
     * 根据用户ID，获取所有设备
     */
    fun getAllDeviceSyncBy(userId: String): List<DeviceInfo> {
        return localDeviceDataSource.getAllDeviceSyncBy(userId)
    }

    /**
     * 根据用户ID获取设备列表
     */
    fun getAllDeviceBy(userId: String): LiveData<List<DeviceInfo>> {
        return localDeviceDataSource.getAllDeviceBy(userId)
    }

    /**
     * 添加设备
     */
    fun addDevice(device: DeviceInfo): Boolean {
        return localDeviceDataSource.addDevice(device)
    }

    /**
     * 批量添加设备
     */
    fun addDevices(devices: List<DeviceInfo>): Boolean {
        return localDeviceDataSource.addDevices(devices)
    }

    /**
     * 删除设备
     */
    fun deleteDevice(device: DeviceInfo): Boolean {
        return localDeviceDataSource.deleteDevice(device)
    }

    /**
     * 根据设备ID查找设备信息
     */
    fun deviceInfo(deviceId: String): DeviceInfo? {
        return localDeviceDataSource.deviceInfo(deviceId)
    }

    /**
     * 更新设备信息
     */
    fun updateDevice(device: DeviceInfo) {
        return localDeviceDataSource.updateDevice(device)
    }

    /**
     * 更新设备的开机状态
     *
     * @param deviceId 设备ID
     * @param powerOn 开机状态
     */
    fun updateDevicePowerOn(deviceId: String, powerOn: Boolean) {
        localDeviceDataSource.updateDevicePowerOn(deviceId, powerOn)
    }

    /**
     * 更新设备的在线状态
     *
     * @param deviceId 设备ID
     * @param status 在线状态
     */
    fun updateDeviceOnlineStatus(deviceId: String, status: Int) {
        localDeviceDataSource.updateDeviceOnlineStatus(deviceId, status)
    }

    /**
     * 更新设备信息
     */
    fun updateDevice(devices: List<DeviceInfo>) {
        return localDeviceDataSource.updateDevice(devices)
    }

    /**
     * 根据设备ID观察最新设备信息
     * @param deviceId 设备ID
     */
    fun watchDevice(deviceId: String): LiveData<DeviceInfo?> {
        return localDeviceDataSource.watchDevice(deviceId)
    }

    /**
     * 根据设备ID观察最新设备信息
     * @param deviceId 设备ID
     */
    fun watchIDevice(deviceId: String): LiveData<IDevice?> {
        val liveData = watchDevice(deviceId)
        return liveData.map { it?.let { DeviceImpl(it) } }
    }

    /**
     * 从远程加载设备列表
     */
    suspend fun loadDeviceFromRemote() {
        mutex.withLock {
            val calendar = Calendar.getInstance()
            val userId = accountApi.getAsyncUserId() ?: return
            val devListBean = remoteDeviceDataSource.loadRemoteDeviceList() ?: return
            // 把网络数据转换成本地数据库里的DeviceInfo和ServiceInfo
            val networkDevicePairs = devListBean.deviceList.map { deviceModel ->
                val info = DeviceInfo(
                    deviceId = deviceModel.devId.toString(),
                    userId = userId,
                    remarkName = deviceModel.remarkName,
                    relation = deviceModel.relation,
                    permission = deviceModel.saas?.permission?.toIntOrNull() ?: 0,
                    modifyTime = calendar.time.time.toString(),
                    productId = deviceModel.saas?.productId,
                    sn = deviceModel.saas?.sn,
                    status = deviceModel.status,
                    originJson = deviceModel.toJson()
                )
                val service = DeviceService(
                    deviceId = deviceModel.devId.toString(),
                    isAreaSupportVas = deviceModel.vss?.support == 1,
                    vasType = deviceModel.vss?.type ?: 0,
                    vasExpireTime = deviceModel.vss?.vssExpireTime?.let { Date(it * 1000) },
                    isVasReNew = deviceModel.vss?.vssRenew == 1,
                    vasCornerUrl = deviceModel.vss?.cornerUrl,
                    isGwell4g = (deviceModel.properties ?: 0L).bitAt(4) == 1L,
                    isSupport4g = deviceModel.fourCard?.support == 1,
                    fourGCornerUrl = deviceModel.fourCard?.cornerUrl,
                    is4gReNew = deviceModel.fourCard?.fgRenew == 1,
                    fourGExpireTime = deviceModel.fourCard?.fgExpireTime?.let { Date(it * 1000) },
                    fourGWebUrl = deviceModel.fourCard?.purchaseUrl,
                    surplusFlow = deviceModel.fourCard?.surplusFlow
                )
                info to service
            }

            // 根据用户名，查询所有设备
            val localIds = localDeviceDataSource.getAllDeviceIdSyncBy(userId)
            // 找出在远程数据中没有的设备列表，意思是远程服务器里，已经被删除的设备列表
            val needDeleteDeviceIds = localIds.filter { localId ->
                networkDevicePairs.none { (_, service) ->
                    service.deviceId == localId
                }
            }
            // 如果需要删除的设备列表不是空，则进行数据库删除
            if (needDeleteDeviceIds.isNotEmpty()) {
                localDeviceDataSource.deleteDeviceById(needDeleteDeviceIds)
                localDeviceServiceDataSource.deleteBy(needDeleteDeviceIds)
            }
            // 循环添加或者更新数据
            for ((info, service) in networkDevicePairs) {
                if (localDeviceServiceDataSource.deviceServiceExist(service.deviceId)) {
                    localDeviceServiceDataSource.update(service)
                } else {
                    localDeviceServiceDataSource.insert(service)
                }
                val localDeviceId = localIds.firstOrNull { localId -> localId == info.deviceId }
                if (localDeviceId != null) {
                    val localDevice = localDeviceDataSource.deviceInfo(localDeviceId)
                    if (localDevice != null) {
                        val newDevice = info.copy(
                            online = localDevice.online,
                            powerOn = localDevice.powerOn,
                        )
                        localDeviceDataSource.updateDevice(newDevice)
                    } else {
                        localDeviceDataSource.addDevice(info)
                    }
                } else {
                    localDeviceDataSource.addDevice(info)
                }
            }
            // 转到主线程，启动服务同步设备在线状态
            withContext(Dispatchers.Main) {
                startSyncDeviceStatusService()
            }
        }
    }

    /**
     * 启动同步设备状态信息服务
     */
    private fun startSyncDeviceStatusService() {
        val intent = Intent(context, DeviceStatusServices::class.java)
        context.startService(intent)
    }

    /**
     * 解绑设备
     */
    fun unbindDevice(deviceId: String): Flow<HttpAction<Any>> {
        return remoteDeviceDataSource.unBindDevice(deviceId)
    }

    /**
     * 访客-删除被分享的设备
     */
    fun cancelShare(deviceId: String): Flow<HttpAction<Any>> {
        return remoteDeviceDataSource.cancelShare(deviceId)
    }

    /**
     * 根据设备ID，查询设备是否开启云服务
     * @param deviceId 设备ID
     */
    fun checkDeviceCloudOn(deviceId: String): Boolean? {
        val deviceServiceBean = localDeviceDataSource.getDeviceServiceByDevId(deviceId)
        val vasExpireTime = deviceServiceBean?.vasExpireTime
        return if (deviceServiceBean?.isAreaSupportVas == true) {
            ((vasExpireTime != null && vasExpireTime > Calendar.getInstance().time))
        } else {
            null
        }
    }

    /**
     * 根据设备ID，查询设备是否开启4g服务
     * @param deviceId 设备ID
     */
    fun checkDevice4gOn(deviceId: String): Boolean? {
        val deviceServiceBean = localDeviceDataSource.getDeviceServiceByDevId(deviceId)
        val fourGExpireTime = deviceServiceBean?.fourGExpireTime
        return if (deviceServiceBean?.isSupport4g == true) {
            ((fourGExpireTime != null && fourGExpireTime > Calendar.getInstance().time))
        } else {
            null
        }
    }


    /**
     * 通过扫描别人分享的设备二维码添加设备
     *
     * @param qrcodeToken 二维码token
     * @param remarkName 设备名称
     */
    fun addDeviceByScanShareCode(
        qrcodeToken: String,
        remarkName: String
    ): Flow<HttpAction<ScanShareQRCodeResult>> {
        return remoteDeviceDataSource.addDeviceByScanShareCode(qrcodeToken, remarkName)
    }

    /**
     * 设备重命名
     */
    suspend fun reName(deviceId: String, devName: String): Boolean {
        return remoteDeviceDataSource.reName(deviceId, devName)
    }

    /**
     * 根据设备ID，获取设备服务器的信息
     */
    fun getDeviceServiceByDevId(deviceId: String): DeviceService? {
        return localDeviceDataSource.getDeviceServiceByDevId(deviceId)
    }

    /**
     * 设备是否支持云存
     */
    fun deviceSupportCloud(deviceId: String): Boolean? {
        val service = localDeviceServiceDataSource.getDeviceServiceBy(deviceId)
        if (service == null) {
            GwellLogUtils.e(TAG, "deviceSupportCloud($deviceId)==null")
            return null
        }
        return service.isAreaSupportVas
    }

    /**
     * 设备云服务过期时间
     *
     * @param deviceId String 设备ID
     * @return Long? 过期时间（单位ms）
     */
    fun deviceCloudExpireTime(deviceId: String): Long? {
        val service = localDeviceServiceDataSource.getDeviceServiceBy(deviceId)
        if (service == null) {
            GwellLogUtils.e(TAG, "deviceCloudExpireTime($deviceId)==null")
            return null
        }
        if (service.isVasReNew == true) {
            return System.currentTimeMillis() + TIME_ONE_MONTH
        }
        return service.vasExpireTime?.time ?: 0L
    }

    /**
     * 设备是否支持4G
     *
     * @param deviceId String 设备id
     * @return Boolean?
     */
    fun deviceSupport4G(deviceId: String): Boolean? {
        val service = localDeviceServiceDataSource.getDeviceServiceBy(deviceId)
        if (service == null) {
            GwellLogUtils.e(TAG, "deviceSupportFourG($deviceId)==null")
            return null
        }
        return service.isSupport4g
    }

}