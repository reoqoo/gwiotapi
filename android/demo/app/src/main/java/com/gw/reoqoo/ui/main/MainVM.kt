package com.gw.reoqoo.ui.main

import androidx.lifecycle.viewModelScope
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_iotvideo.IoTSdkInitMgr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor() : ABaseVM() {

    @Inject
    lateinit var accountApi: IAccountApi

    @Inject
    lateinit var globalApi: IAppConfigApi

    @Inject
    lateinit var iotSdkInitMgr: IoTSdkInitMgr

    private companion object {
        private const val TAG = "MainVM"
    }

    /**
     * 用户信息更新
     * tips：由于用户的头像，昵称等信息不会在登录时返回，为了不影响登录过程，便把用户信息更新放在这里来调用
     */
    fun refreshUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            accountApi.getRemoteUserInfo()
        }
    }

}