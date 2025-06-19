package com.gw_reoqoo.cp_account.ui.fragment.login

import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.datastore.AccountDataStore
import com.gw_reoqoo.cp_account.datastore.AccountDataStoreApi
import com.gw_reoqoo.cp_account.http.AccountHttpKit
import com.gw_reoqoo.cp_account.http.LoginResponse
import com.gw_reoqoo.cp_account.http.toUserInfo
import com.gw_reoqoo.cp_account.repository.AccountRepository
import com.gw_reoqoo.cp_account.sa.AccountSaEvent
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.HttpErrUtils
import com.gw_reoqoo.lib_http.HttpErrorCode
import com.gw_reoqoo.lib_http.IBaseResultCallback
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.device_utils.PhoneIDUtils
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import com.jwkj.base_utils.str_utils.GwStringUtils
import com.tencentcs.iotvideo.accountmgr.AccountMgr
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:23
 * Description: LoginFrgVM
 */
@HiltViewModel
class LoginFrgVM @Inject constructor(
    private val dataStore: AccountDataStore,
    private val repository: AccountRepository,
    private val mgrImpl: AccountMgrImpl,
    private val accountMgr: AccountMgr,
    private val dataStoreApi: AccountDataStoreApi,
) : ABaseVM() {

    companion object {
        private const val TAG = "LoginFrgVM"
    }

    /**
     * 是否同意隐私协议
     */
    val isAgree: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _startFeedbackLD = MutableLiveData<Boolean>()

    /**
     * 是否跳转到反馈页面
     */
    val startFeedbackLD: LiveData<Boolean> = _startFeedbackLD

    /**
     * 是否需要展示隐私协议
     *
     * @return Boolean true：展示  false：不显示
     */
    fun needShowProtocol(): Boolean {
        return dataStore.getNeedShowProtocol()
    }

    /**
     * 同意隐私协议
     *
     * @param isAgree Boolean 是否同意（true：已同意，false：未同意）
     */
    fun isAgreeProtocol(isAgree: Boolean) {
        // 如果是已经同意，则不需要展示
        dataStore.setNeedShowProtocol(!isAgree)
    }

    fun agreeTxtClick(state: Boolean) {
        isAgree.postValue(state)
    }

    fun agreeBtnClick(state: Boolean) {
        isAgree.value = state
    }

    fun loginByAccount(districtEntity: DistrictEntity, account: String, pwd: String) {
        if (GwStringUtils.isEmailValid(account)) {
            // 优先判断是否邮箱
            login(account, pwd, false)
            SA.track(
                AccountSaEvent.LOGIN_ACCOUNTBUTTONCLICK,
                mapOf(AccountSaEvent.EventAttr.LOGIN_METHOD to "邮箱")
            )
        } else if (account[0] == '0') {
            // 是用户ID登录
            login(account, pwd, true)
        } else {
            // 手机号登录
            val phoneNum = buildString {
                append("+")
                append(districtEntity.districtCode)
                append("-")
                append(account)
            }
            SA.track(
                AccountSaEvent.LOGIN_ACCOUNTBUTTONCLICK,
                mapOf(AccountSaEvent.EventAttr.LOGIN_METHOD to "手机号")
            )
            GwellLogUtils.i(TAG, "loginByAccount: $phoneNum")
            login(phoneNum, pwd, false)
        }
    }

    private fun login(username: String, password: String, isLoginByUID: Boolean) {
        GwellLogUtils.i(
            TAG,
            "login: username $username, password $password, isLoginByUID $isLoginByUID"
        )
        val uuid: String = PhoneIDUtils.phoneUniqueId
        val accountName = if (isLoginByUID) {
            (username.toInt() or -0x80000000).toString()
        } else {
            username
        }
        val callback = object : IBaseResultCallback<LoginResponse> {
            override fun onStart() {
                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
            }

            override fun onError(error_code: String?, throwable: Throwable?) {
                error_code?.let {
                    when (it) {
                        HttpErrorCode.NET_ERROR, HttpErrorCode.NO_SERVICE -> {
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0376))
                        }

                        HttpErrorCode.ERROR_10902012 -> {
//                            val i = Intent()
//                            i.action = ReceiverConstants.Action.SESSION_ID_ERROR
//                            AppEnv.APP.sendBroadcast(i)
                        }

                        HttpErrorCode.ERROR_998 -> {
                            login(username, password, isLoginByUID)
                        }

                        else -> {
                            toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(it)))
                        }
                    }
                }
                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
            }

            override fun onNext(t: LoginResponse?) {
                GwellLogUtils.i(TAG, "Login Success, loginResponse = ${t?.data}")
                t?.data?.run {
                    if (t.code == 0) {
                        val userInfo = if (username.contains("-")) {
                            this.toUserInfo(phone = username)
                        } else {
                            this.toUserInfo(email = username)
                        }
                        userInfo?.let {
                            viewModelScope.launch(Dispatchers.IO) {
                                GwellLogUtils.i(TAG, "login: userInfo = $userInfo")
                                repository.updateLocalUserInfo(userInfo)
                                dataStoreApi.setTokenRefreshTime(System.currentTimeMillis())
                                accountMgr.setRegRegion(it.regRegion)
                                finishActivityLD.postValue(true)
                                pageJumpData.postValue(
                                    PageJumpData(
                                        TheRouter.build(ReoqooRouterPath.AppPath.MAIN_ACTIVITY_PATH)
                                            .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                )
                            }
                        } ?: GwellLogUtils.e(TAG, "Convert failed: userInfo is null")
                    }
                }

            }

        }
        AccountHttpKit.login(accountName, password, uuid, accountMgrImpl = mgrImpl, callback)
    }

    private var mHits = LongArray(6)

    /**
     * 记录次数，2.5s内连续点击5次则跳转到反馈页面
     *
     */
    fun jumpToFeedback() {
        // 每点击一次 实现左移一格数据
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        // 给数组的最后赋当前时钟值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] > SystemClock.uptimeMillis() - 2500) {
            mHits = LongArray(5)
            _startFeedbackLD.postValue(true)
        }
    }
}