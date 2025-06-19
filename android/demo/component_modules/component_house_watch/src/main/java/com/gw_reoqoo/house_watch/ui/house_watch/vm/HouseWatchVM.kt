package com.gw_reoqoo.house_watch.ui.house_watch.vm

import androidx.lifecycle.LiveData
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @Description: - 看家主界面VM
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@HiltViewModel
class HouseWatchVM @Inject constructor(
    private val familyModeApi: FamilyModeApi,
    private val iAccountApi: IAccountApi,
) : ABaseVM() {
    /**
     * 设备列表
     */
    private var devList: LiveData<List<IDevice>>? = null

    /**
     * 设备列表
     */
    fun watchDeviceList(): LiveData<List<IDevice>>? {
        val userId = iAccountApi.getSyncUserId() ?: return null
        val livedata = devList ?: familyModeApi.watchDeviceList(userId)
        devList = livedata
        return livedata
    }
}