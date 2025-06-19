package com.gw_reoqoo.cp_account.ui.activity.close_success.vm

import androidx.lifecycle.viewModelScope
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.cp_account.repository.UserInfoRepository
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.jwkj.base_lifecycle.activity_lifecycle.ActivityLifecycleManager
import com.jwkj.iotvideo.init.IoTVideoInitializer
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/15 0:19
 * Description: CloseSuccessVM
 */
@HiltViewModel
class CloseSuccessVM @Inject constructor() : ABaseVM() {

    @Inject
    lateinit var userResp: UserInfoRepository

    @Inject
    lateinit var pluginMgr: IPluginManager

    /**
     * 注销成功，退出登录状态
     */
    fun userLogout() {
        viewModelScope.launch(Dispatchers.IO) {
            userResp.cleanUserInfo()
            pluginMgr.onAccountExit()
            IoTVideoInitializer.unregister()
            ActivityLifecycleManager.finishAllActivity()
            pageJumpData.postValue(PageJumpData(TheRouter.build(ReoqooRouterPath.AccountPath.LOGIN_ACTIVITY_PATH)))
        }
    }

}