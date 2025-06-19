package com.gw_reoqoo.cp_account.ui.fragment.bind_account_input.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountApiImpl
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.datastore.AccountDataStoreApi
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.entity.SendCodeType
import com.gw_reoqoo.cp_account.http.AccountHttpKit
import com.gw_reoqoo.cp_account.listener.OnValidationDialogCloseListener
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.cp_account.repository.AccountInputRepository
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm.Companion.LOAD_DIALOG_STATE_CLOSE
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm.Companion.LOAD_DIALOG_STATE_OPEN
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.*
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.resource.R
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 17:36
 * Description: AccountInputVM
 */
@HiltViewModel
class AccountBindInputVM @Inject constructor(
    private val accountApiImpl: AccountApiImpl,
    private val mgrImpl: AccountMgrImpl
) : ABaseVM() {

    companion object {

        private const val TAG = "AccountBindInputVM"

    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var repository: AccountInputRepository

    @Inject
    lateinit var api: AccountDataStoreApi

    /**
     * 错误提示
     */
    var errorNotice: MutableLiveData<Int> = MutableLiveData()

    var mDistrictBean: DistrictEntity? = null

    var registerType: AccountRegisterType? = null

    var jumpVerifyFragment: MutableLiveData<Boolean> = MutableLiveData(false)

    /**
     * 手机号获取验证码
     *
     * @param phone String 手机号
     */
    fun getCodeByPhone(phone: String) {
        GwellLogUtils.i(TAG, "getCodeByPhone $phone")
        mDistrictBean?.run {
            loadDialogState.postValue(LOAD_DIALOG_STATE_OPEN)
            viewModelScope.launch {
                val result = repository.registerByPhone(
                    districtCode,
                    phone,
                    SendCodeType.REGISTER_BIND,
                    isLogin = accountApiImpl.isSyncLogin(),
                    object : OnValidationDialogCloseListener {
                        override fun onDialogClose() {
                            loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                        }

                    }
                )
                result.onSuccess {
                    GwellLogUtils.i(TAG, "resp $this")
                    // 成功
                    loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                    // 弹出 "验证码已发送"
                    toastIntentData.postValue(ToastIntentData(strResId = R.string.AA0040))
                    registerType = AccountRegisterType.TYPE_MOBILE
                    jumpVerifyFragment.postValue(true)
                }
                result.onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
                    loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                    when (val resp = ResponseCode.getRespCode(code)) {
                        null -> Unit

                        ResponseCode.CODE_10902020 -> {
                            errorNotice.postValue(R.string.AA0524)
                        }

                        else -> {
                            toastIntentData.postValue(ToastIntentData(resp.msgRes))
                        }
                    }
                }
                result.onLocalError {
                    loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                    GwellLogUtils.e(TAG, "onLocalError: ${it.message}")
                    it.message?.run {
                        toastIntentData.postValue(ToastIntentData(str = this))
                    }
                }
            }
        }
    }

    /**
     * 邮箱获取验证码
     *
     * @param email String 邮箱
     */
    fun getCodeByEmail(email: String) {
        GwellLogUtils.i(TAG, "getCodeByEmail $email")
        AccountHttpKit.emailCheckCode(
            email = email,
            pwd = null,
            flag = 3,
            isLogin = accountApiImpl.isSyncLogin(),
            accountMgrImpl = mgrImpl,
            object : IBaseResultCallback<HttpResponse> {
                override fun onStart() {
                    loadDialogState.postValue(LOAD_DIALOG_STATE_OPEN)
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    GwellLogUtils.e(
                        TAG,
                        "onError: errorCode $errorCode, throwable ${throwable?.message}"
                    )
                    loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                    errorCode?.let {
                        when (val respCode = ResponseCode.getRespCode(it.toInt())) {
                            null -> Unit

                            ResponseCode.CODE_10902021 -> {
                                errorNotice.postValue(R.string.AA0525)
                            }

                            else -> {
                                toastIntentData.postValue(ToastIntentData(respCode.msgRes))
                            }
                        }
                    }
                }

                override fun onNext(t: HttpResponse?) {
                    GwellLogUtils.i(TAG, "onNext: ${t.toString()}")
                    t?.error_code?.let {
                        when (it) {
                            HttpErrorCode.ERROR_0 -> {
                                // 成功
                                loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                                // 弹出 "验证码已发送"
                                toastIntentData.postValue(ToastIntentData(strResId = R.string.AA0040))
                                registerType = AccountRegisterType.TYPE_EMAIL
                                jumpVerifyFragment.postValue(true)
                            }

                            else -> {
                                loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                                toastIntentData.postValue(
                                    ToastIntentData(
                                        HttpErrUtils.showErrorToast(
                                            it
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

            })
    }

}