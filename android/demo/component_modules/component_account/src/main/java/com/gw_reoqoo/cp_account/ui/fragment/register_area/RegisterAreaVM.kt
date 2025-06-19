package com.gw_reoqoo.cp_account.ui.fragment.register_area

import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:52
 * Description: RegisterAreaVM
 */
@HiltViewModel
class RegisterAreaVM @Inject constructor() : ABaseVM() {

    /**
     * 当前的地区码
     */
    var mDistrictBean: MutableLiveData<DistrictEntity> = MutableLiveData()

}