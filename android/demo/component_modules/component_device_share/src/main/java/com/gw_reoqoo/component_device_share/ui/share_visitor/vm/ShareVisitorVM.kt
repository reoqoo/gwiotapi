package com.gw_reoqoo.component_device_share.ui.share_visitor.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_device_share.data.rspository.DevVisitorRepository
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_config.entity.DevConfigEntity
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.OwnerInfo
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
@author: xuhaoyuan
@date: 2023/8/28
description:
1.
 */
@HiltViewModel
class ShareVisitorVM @Inject constructor(
    private val repository: DevVisitorRepository,
    private val accountId: IAccountApi,
    private val familyModeApi: FamilyModeApi,
    private val igwIotOpt: IGWIotOpt,
) : ABaseVM() {

    companion object {
        private const val TAG = "ShareVisitorVM"
    }

    /**
     * 分享设备实体数据
     */
    private var device: IDevice? = null

    /**
     * 访客删除主人分享过来的设备
     */
    private val _cancelShareResult = MutableLiveData<Boolean?>()

    /**
     * 主人信息
     */
    private val _ownerInfo = MutableLiveData<HttpAction<OwnerInfo>>()

    /**
     * 设备配置信息
     */
    private val _devConfigEntity = MutableLiveData<DevConfigEntity?>()

    /**
     * 设备配置信息
     */
    val devConfigEntity: LiveData<DevConfigEntity?> get() = _devConfigEntity

    /**
     * 访客删除主人分享过来的设备
     */
    val cancelShareResult: LiveData<Boolean?> get() = _cancelShareResult

    /**
     * 主人信息
     */
    val ownerInfo: LiveData<HttpAction<OwnerInfo>> get() = _ownerInfo

    /**
     * 设备信息实体数据
     *
     * @param device DevShareInfoEntity 设备信息
     */
    fun setDevEntity(device: IDevice) {
        this.device = device
    }

    /**
     * 访客移除设备
     */
    fun cancelDevice() {
        val visitorId = accountId.getSyncUserId()
        val devId = device?.deviceId
        if (visitorId.isNullOrEmpty() ||
            devId.isNullOrEmpty()
        ) {
            GwellLogUtils.e(TAG, "delVisitor error: visitorId $visitorId, devId: $devId")
            return
        }
        viewModelScope.launch {
            val flow = repository.delDevVisitor(devId, visitorId)
            flow.collect { action ->
            GwellLogUtils.i(TAG, "visitor-delDevVisitor $action")
                when (action) {
                    is HttpAction.Loading -> {}
                    is HttpAction.Success -> {
                        familyModeApi.refreshDevice()
                        igwIotOpt.refreshDevices()
                        toastIntentData.postValue(ToastIntentData(RR.string.AA0186))
                        _cancelShareResult.postValue(true)
                    }

                    is HttpAction.Fail -> {
                        toastIntentData.postValue(ToastIntentData(RR.string.AA0187))
                        _cancelShareResult.postValue(false)
                    }
                }
            }
        }
    }


    /**
     * 查询分享者的信息
     */
    fun queryOwnerInfo() {
        val visitorId = accountId.getSyncUserId()
        val devId = device?.deviceId
        if (visitorId.isNullOrEmpty() ||
            devId.isNullOrEmpty()
        ) {
            GwellLogUtils.e(TAG, "queryGuestInfo error: visitorId $visitorId, devId: $devId")
            return
        }
        viewModelScope.launch {
            repository.loadOwnerInfo(devId).collect { action ->
                _ownerInfo.postValue(action)
            }
        }
    }
}