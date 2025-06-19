package com.gw_reoqoo.cp_account.ui.activity.close_verify.vm

import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.repository.AccountRepository
import com.gw_reoqoo.cp_account.repository.UserInfoRepository
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.HttpErrUtils
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 21:14
 * Description: CloseVerifyVM
 */
/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 21:14
 * Description: CloseVerifyVM
 */
@HiltViewModel
class CloseVerifyVM @Inject constructor() : ABaseVM() {

    companion object {
        private const val TAG = "CloseVerifyVM"
    }

    @Inject
    lateinit var accountResp: AccountRepository

    @Inject
    lateinit var userInfoResp: UserInfoRepository

    @Inject
    lateinit var accountMgrImpl: AccountMgrImpl

    fun closeAccount(
        pwd: String,
        type: Int,
        reasonType: Int,
        reason: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            accountResp.getLocalUserInfo()?.run {
                userInfoResp.closeAccount(pwd, type, reasonType, reason)
                    .onSuccess {
                        GwellLogUtils.i(TAG, "closeAccount success")
                        accountMgrImpl.loginFailure()
                        pageJumpData.postValue(PageJumpData(TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_CLOSE_SUCCESS)))
                    }
                    .onServerError { code, msg ->
                        GwellLogUtils.e(TAG, "code: $code, msg: $msg")
                        toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(code.toString())))
                    }
                    .onLocalError {
                        GwellLogUtils.e(TAG, "onLocalError: ${it.message}")
                        it.message?.let {
                            toastIntentData.postValue(ToastIntentData(str = it))
                        } ?: toastIntentData.postValue(ToastIntentData(RR.string.AA0475))
                    }
            } ?: GwellLogUtils.e(TAG, "closeAccount error, userInfo is null")
        }
    }

}