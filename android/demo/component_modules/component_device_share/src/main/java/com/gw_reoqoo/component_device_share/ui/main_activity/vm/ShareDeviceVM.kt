package com.gw_reoqoo.component_device_share.ui.main_activity.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_INIT_DEVICE
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_PAGE_FROM
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_USER_ID
import com.gw_reoqoo.component_device_share.entities.FragmentRouteWrapper
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Description: - 选择设备列表分享界面VM
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 */
@HiltViewModel
class ShareDeviceVM @Inject constructor(
    private val accountApi: IAccountApi,
) : ABaseVM() {
    /**
     * 当前界面显示哪个fragment的包装
     */
    private val _fragmentData = MutableLiveData<FragmentRouteWrapper>()
    val fragmentData: LiveData<FragmentRouteWrapper> get() = _fragmentData

    /**
     * 加载设备列表fragment
     */
    fun loadDeviceListFragment(device: IDevice?, pageFrom: String) {
        viewModelScope.launch {
            val url = ReoqooRouterPath.DevShare.FRAGMENT_DEVICE_LIST
            val userId = accountApi.getAsyncUserId()
            if (userId != null) {
                val with = if (device == null) {
                    mapOf(KEY_PARAM_USER_ID to userId, KEY_PARAM_PAGE_FROM to pageFrom)
                } else {
                    mapOf(
                        KEY_PARAM_USER_ID to userId,
                        KEY_PARAM_INIT_DEVICE to device,
                        KEY_PARAM_PAGE_FROM to pageFrom
                    )
                }
                _fragmentData.postValue(FragmentRouteWrapper(url, with))
            }
        }
    }

}