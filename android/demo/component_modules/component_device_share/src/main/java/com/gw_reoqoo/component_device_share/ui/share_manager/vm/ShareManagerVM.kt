package com.gw_reoqoo.component_device_share.ui.share_manager.vm

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_device_share.data.rspository.ShareMgrRepository
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
@author: xuhaoyuan
@date: 2023/8/28
description:
1.
 */
@HiltViewModel
class ShareManagerVM @Inject constructor(
    private val repository: ShareMgrRepository,
    private val accountApi: IAccountApi,
    private val familyModeApi: FamilyModeApi,
) : ABaseVM() {
    companion object {
        private const val TAG = "ShareManagerVM"
    }

    /**
     * 全部设备
     */
    private val _allDevice = MutableLiveData<List<IDevice>>()

    /**
     * 我分享的设备
     */
    private val _sharedDeviceList = MutableLiveData<List<IDevice>>()

    /**
     * 来自分享的设备
     */
    private val _fromSharedDeviceList = MutableLiveData<List<IDevice>>()

    /**
     * 我分享的设备
     */
    val sharedDeviceList: LiveData<List<IDevice>> get() = _sharedDeviceList


    /**
     * 来自分享的设备
     */
    val fromSharedDeviceList: LiveData<List<IDevice>> get() = _fromSharedDeviceList

    /**
     * 全部设备
     */
    val allDevice: LiveData<List<IDevice>> get() = _allDevice

    /**
     * 加载设备列表
     */
    fun watchDeviceList(owner: LifecycleOwner) {
        val userId = accountApi.getSyncUserId() ?: return
        val devices = repository.watchDevices(userId)
        devices.observe(owner) { list ->
            // 我分享的设备
            val sharedList = list.filter { it.hasShared == true && it.isMaster }
            _sharedDeviceList.postValue(sharedList)
            // 来自分享的设备
            val fromShareList = list.filter { !it.isMaster }
            _fromSharedDeviceList.postValue(fromShareList)
            // 全部的设备
            _allDevice.postValue(sharedList + fromShareList)
        }
    }
    
    /**
     * 从服务器刷新最新设备的数据
     */
    fun refreshDeviceFromRemote() {
        viewModelScope.launch {
            familyModeApi.refreshDevice()
        }
    }
}