package com.gw_reoqoo.component_device_share.ui.share_owner.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.component_device_share.data.rspository.DevVisitorRepository
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.Guest
import com.gw_reoqoo.lib_http.entities.ListGuestContent
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
@author: xuhaoyuan
@date: 2023/8/28
description:
1.
 */
@HiltViewModel
class ShareOwnerVM @Inject constructor(
    private val repository: DevVisitorRepository
) : ABaseVM() {

    companion object {
        private const val TAG = "ShareOwnerVM"
    }

    /**
     * 设备分享的信息
     */
    private var device: IDevice? = null

    /**
     * 设备访客列表
     */
    private val _guestList = MutableLiveData<List<Guest>>()

    /**
     * 设备访客列表
     */
    val guestList: LiveData<List<Guest>> get() = _guestList

    /**
     * 设备访客最大数量
     */
    private var maxGuestCount: Int? = null

    /**
     * 设置设备分享的信息
     *
     * @param device 设备信息
     */
    fun setDevice(device: IDevice) {
        this.device = device
    }

    /**
     * 获取设备访客最大数量
     */
    fun getMaxGuestCount(): Int {
        return maxGuestCount ?: 0
    }

    /**
     * 获取设备访客信息
     */
    fun getGuestList(): Flow<HttpAction<ListGuestContent>> {
        val deviceId = device?.deviceId ?: return flowOf()
        val flow = repository.getDevVisitors(deviceId)
        return flow.map { action ->
            when (action) {
                is HttpAction.Loading -> {}
                is HttpAction.Success -> {
                    val guestList = action.data?.guestList
                    this.maxGuestCount = action.data?.guestCount
                    guestList?.let(_guestList::postValue)
                }

                is HttpAction.Fail -> {}
            }
            action
        }
    }

    /**
     * 设备删除单一访客
     *
     * @param visitorId String 访客ID
     */
    fun delGuest(visitorId: String): Flow<HttpAction<Any>> {
        val devId = device?.deviceId ?: return flowOf()
        return repository.delDevVisitor(devId, visitorId)
    }

    /**
     * 设备停止共享
     */
    fun delAllGuest(): Flow<HttpAction<Any>> {
        val devId = device?.deviceId ?: return flowOf()
        return repository.delAllGuest(devId)
    }

}