package com.gw_reoqoo.component_family.datasource

import com.gw.cp_mine.api.kapi.ILocaleApi
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.gw_reoqoo.lib_http.datasource.HttpDataSource
import com.gw_reoqoo.lib_http.entities.DeviceList
import com.gw_reoqoo.lib_http.entities.ScanShareQRCodeResult
import kotlinx.coroutines.flow.Flow
import java.util.LinkedList
import java.util.Locale
import javax.inject.Inject

/**
 * @Description: - 远程服务器设备数据源
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 */
class RemoteDeviceDataSource @Inject constructor(
    private val httpDataSource: HttpDataSource,
    private val localeApi: ILocaleApi
) {

    companion object {
        private const val TAG = "RemoteDeviceDataSource"
    }

    /**
     * 加载远程设备列表
     */
    suspend fun loadRemoteDeviceList(): DeviceList? {
        var lastDeviceId = 0L
        val resultList = LinkedList<DeviceList>()
        val region = localeApi.getCurrentCountry()
        do {
            val deviceList = httpDataSource.listDeviceV2Async(region, lastDeviceId) ?: return null
            val newLastDeviceId = (deviceList.deviceList.lastOrNull()?.devId) ?: lastDeviceId
            if (newLastDeviceId == lastDeviceId) break
            lastDeviceId = newLastDeviceId
            resultList.add(deviceList)
        } while (deviceList.deviceList.size >= deviceList.size)
        return DeviceList(resultList.map { it.deviceList }.flatten())
    }

    /**
     * 解绑设备
     */
    fun unBindDevice(deviceId: String): Flow<HttpAction<Any>> {
        return httpDataSource.deviceUnbind(deviceId)
    }

    /**
     * 访客-删除被分享的设备
     */
    fun cancelShare(deviceId: String): Flow<HttpAction<Any>> {
        return httpDataSource.cancelShare(deviceId, "")
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
        return httpDataSource.scanShareQrcode(qrcodeToken, remarkName)
    }

    /**
     * 设备重命名
     */
    suspend fun reName(deviceId: String, devName: String): Boolean {
        return httpDataSource.modifyDeviceNameAsync(deviceId, devName)
    }
}