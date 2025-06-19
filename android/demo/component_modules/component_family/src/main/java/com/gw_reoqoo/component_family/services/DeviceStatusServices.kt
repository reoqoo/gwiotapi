package com.gw_reoqoo.component_family.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.gw_reoqoo.component_family.repository.DeviceRepository
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_upgrade.api.interfaces.IUpgradeMgrApi
import com.gw.lib_plugin_service.IPluginDeviceStatusListener
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.init.IoTVideoInitializerState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @Description: - 用作设备的状态信息的同步服务
 * @Author: XIAOLEI
 * @Date: 2023/8/4
 */
@AndroidEntryPoint
class DeviceStatusServices : LifecycleService(), IPluginDeviceStatusListener {

    companion object {
        private const val TAG = "DeviceStatusServices"
    }

    /**
     * 设备管理
     */
    @Inject
    lateinit var deviceRepository: DeviceRepository

    /**
     * 插件管理器
     */
    @Inject
    lateinit var pluginManager: IPluginManager

    @Inject
    lateinit var iAccountApi: IAccountApi

    @Inject
    lateinit var upgradeMgr: IUpgradeMgrApi

    @Inject
    lateinit var familyModeApi: FamilyModeApi

    override fun onCreate() {
        super.onCreate()
        GwellLogUtils.i(TAG, "onCreate")
        val stateLiveData = iAccountApi.getIotSdkState()
        GwellLogUtils.i(TAG, "onCreate-stateLiveData：$stateLiveData")
        stateLiveData?.observe(this) { state ->
            GwellLogUtils.i(TAG, "stateLiveData:$state")
            when (state) {
                IoTVideoInitializerState.ONLINE -> this.loadDeviceStatus()
                IoTVideoInitializerState.OFFLINE -> Unit
                else -> {}
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GwellLogUtils.i(TAG, "onStartCommand")
        // 注册
        pluginManager.registerDeviceStatusListener(this)
        this.loadDeviceStatus()
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 请求设备状态
     */
    private fun loadDeviceStatus() {
        GwellLogUtils.i(TAG, "loadDeviceStatus")
        val userId = iAccountApi.getSyncUserId()
        if (userId != null) {
            val device = deviceRepository.getAllDeviceSyncBy(userId)
            val devIds = device.filter {
                val pid = it.productId
                familyModeApi.isReoqooDevice(pid ?: "")
            }.map { it.deviceId }
            pluginManager.getDevicePowerStatus(devIds)
            pluginManager.getDeviceOnlineStatus(devIds)
        }
    }

    override fun onDeviceOnlineStatusChange(deviceId: String, status: Int) {
        GwellLogUtils.i(TAG, "onDeviceOnlineStatusChange deviceId:$deviceId status:$status")
        deviceRepository.updateDeviceOnlineStatus(deviceId, status)
    }

    override fun onDeviceShutdown(deviceId: String, status: Int) {
        GwellLogUtils.i(TAG, "onDeviceShutdown deviceId:$deviceId status:$status")
        deviceRepository.updateDevicePowerOn(deviceId, status == 1)
    }

    /**
     * 设备升级进度更新
     *
     * @param deviceId String 设备ID
     * @param progress Int  进度值
     */
    override fun onDevUpgradeProgressChange(deviceId: String, progress: Int) {
        GwellLogUtils.i(
            TAG,
            "onDevUpgradeProgressChange: upgradeMgr $upgradeMgr, deviceId $deviceId, progress $progress"
        )
        upgradeMgr.refreshProgress(deviceId, progress)
    }

}