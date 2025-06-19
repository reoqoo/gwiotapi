package com.gw_reoqoo.cp_account.ui.fragment.account_input

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountApiImpl
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.entity.SendCodeType
import com.gw_reoqoo.cp_account.http.AccountHttpKit
import com.gw_reoqoo.cp_account.listener.OnValidationDialogCloseListener
import com.gw_reoqoo.cp_account.repository.AccountInputRepository
import com.gw_reoqoo.cp_account.sa.AccountSaEvent
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm.Companion.LOAD_DIALOG_STATE_CLOSE
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm.Companion.LOAD_DIALOG_STATE_OPEN
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.HttpErrUtils
import com.gw_reoqoo.lib_http.HttpErrorCode
import com.gw_reoqoo.lib_http.HttpResponse
import com.gw_reoqoo.lib_http.IBaseResultCallback
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.lib_http.toJson
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import com.jwkj.base_utils.str_utils.GwStringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 17:36
 * Description: AccountInputVM
 */
@HiltViewModel
class AccountInputVM @Inject constructor(
    private val accountApiImpl: AccountApiImpl,
    private val mgrImpl: AccountMgrImpl
) : ABaseVM() {

    companion object {

        private const val TAG = "AccountInputVM"

        /**
         * 邮箱账号类型
         */
        private const val ACCOUNT_TYPE_EMAIL = 1

        /**
         * 手机账号类型
         */
        private const val ACCOUNT_TYPE_MOBILE = 2
    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var repository: AccountInputRepository

    @Inject
    lateinit var configApi: IAppConfigApi

    /**
     * 是否同意隐私协议
     */
    val isAgree: MutableLiveData<Boolean> = MutableLiveData(false)

    /**
     * 是否支持手机号注册
     */
    var isSupport: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 错误提示
     */
    var errorNotice: MutableLiveData<Int> = MutableLiveData()

    /**
     * 地区信息
     */
    var mDistrictBean: DistrictEntity? = null

    /**
     * 注册账号类型
     */
    var registerType: AccountRegisterType? = null

    /**
     * 页面入口来源
     */
    var inputFromPage: AccountInputType? = null

    /**
     * 是否跳转到验证页面
     */
    var jumpVerifyFragment: MutableLiveData<Boolean> = MutableLiveData(false)

    fun agreeTxtClick(state: Boolean) {
        isAgree.postValue(state)
    }

    fun agreeBtnClick(state: Boolean) {
        isAgree.value = state
    }

    fun getVerifyCode(account: String) {
        SA.track(
            AccountSaEvent.SEND_VCODE,
            mapOf(AccountSaEvent.EventAttr.SEND_RESULT to "1")
        )
        inputFromPage?.let {
            when (it) {
                AccountInputType.ACCOUNT_REGISTER -> {
                    registerAccount(account)
                }

                AccountInputType.ACCOUNT_FORGET -> {
                    sendCodeResetPwd(account)
                }

            }
        }
    }

    /**
     * 判断当前地区是否支持手机号注册
     *
     * @param code String 地区码
     * @return Boolean 是否支持手机号
     */
    fun isSupportPhoneRegister(code: String) {
        isSupport.postValue(isSupport(code))
    }

    /**
     * 根据输入的地区码判断是否支持手机注册
     */
    private fun isSupport(districtCode: String): Boolean {
        val districtCodeList = configApi.getCountryCodeList()
        GwellLogUtils.i(
            TAG,
            "districtCode $districtCode, districtCodeList ${districtCodeList.toJson()}"
        )
        for (i in districtCodeList.indices) {
            if (districtCodeList[i] == districtCode) {
                return true
            }
        }
        return false
    }

    /**
     * 注册账号
     *
     * @param account String 账号
     */
    private fun registerAccount(account: String) {
        GwellLogUtils.i(
            TAG,
            "registerAccount :$account, isEmail: ${GwStringUtils.isEmail(account)}"
        )
        // 手机号
        mDistrictBean?.let {
            val support = isSupport(it.districtCode)
            if (support) {
                if (GwStringUtils.isNumeric(account)) {
                    // 纯数字认为是手机号
                    if (it.isZHArea()) {
                        // 如果是中国地区
                        if (GwStringUtils.isZhMobileNO(account)) {
                            registerByPhone(account)
                        } else {
                            toastIntentData.postValue(ToastIntentData(RR.string.AA0012))
                        }
                    } else {
                        if (GwStringUtils.isMobileNO(account)) {
                            registerByPhone(account)
                        } else {
                            toastIntentData.postValue(ToastIntentData(RR.string.AA0012))
                        }
                    }
                } else {
                    // 其他认为是邮箱
                    if (GwStringUtils.isEmail(account)) {
                        registerByEmail(account)
                    } else {
                        toastIntentData.postValue(ToastIntentData(RR.string.AA0012))
                    }
                }
            } else {
                if (GwStringUtils.isEmail(account)) {
                    // 邮箱注册
                    registerByEmail(account)
                } else {
                    toastIntentData.postValue(ToastIntentData(RR.string.AA0577))
                }
            }
        }
    }

    /**
     * 手机号获取验证码
     *
     * @param phone String 手机号
     */
    private fun registerByPhone(phone: String) {
        if ((mDistrictBean?.isZHArea() == true && !GwStringUtils.isZhMobileNO(phone))
            || (mDistrictBean?.isZHArea() == true && !GwStringUtils.isMobileNO(phone))
        ) {
            toastIntentData.postValue(ToastIntentData(RR.string.AA0012))
            return
        }
        SA.track(
            AccountSaEvent.SEND_VCODE,
            mapOf(AccountSaEvent.EventAttr.REGISTER_METHOD to "手机号")
        )
        val type =
            if (inputFromPage == AccountInputType.ACCOUNT_REGISTER) SendCodeType.REGISTER_BIND else SendCodeType.FIND_PWD
        mDistrictBean?.run {
            loadDialogState.postValue(LOAD_DIALOG_STATE_OPEN)
            viewModelScope.launch {
                val result = repository.registerByPhone(
                    districtCode,
                    phone,
                    type,
                    accountApiImpl.isSyncLogin(),
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
                    toastIntentData.postValue(ToastIntentData(strResId = RR.string.AA0040))
                    registerType = AccountRegisterType.TYPE_MOBILE
                    jumpVerifyFragment.postValue(true)
                }
                result.onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
                    loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                    when (val resp = ResponseCode.getRespCode(code)) {
                        null -> Unit

                        ResponseCode.CODE_10902020 -> {
                            errorNotice.postValue(RR.string.AA0524)
                        }

                        ResponseCode.CODE_12013 -> {
                            errorNotice.postValue(ResponseCode.CODE_12013.msgRes)
                        }

                        ResponseCode.CODE_10902004 -> {
                            errorNotice.postValue(ResponseCode.CODE_10902004.msgRes)
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
    private fun registerByEmail(email: String) {
        if (!GwStringUtils.isEmail(email)) {
            toastIntentData.postValue(ToastIntentData(RR.string.AA0577))
            return
        }
        SA.track(AccountSaEvent.SEND_VCODE, mapOf(AccountSaEvent.EventAttr.REGISTER_METHOD to "邮箱"))
        val type = if (inputFromPage == AccountInputType.ACCOUNT_REGISTER) 3 else 4
        AccountHttpKit.emailCheckCode(
            email,
            null,
            type,
            isLogin = accountApiImpl.isSyncLogin(),
            accountMgrImpl = mgrImpl,
            object : IBaseResultCallback<HttpResponse> {

                override fun onStart() {
                    loadDialogState.postValue(LOAD_DIALOG_STATE_OPEN)
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                    errorCode?.let {
                        when (val respCode = ResponseCode.getRespCode(it.toInt())) {
                            null -> Unit

                            ResponseCode.CODE_10902021 -> {
                                errorNotice.postValue(RR.string.AA0525)
                            }

                            ResponseCode.CODE_12013 -> {
                                errorNotice.postValue(ResponseCode.CODE_12013.msgRes)
                            }

                            ResponseCode.CODE_10902004 -> {
                                errorNotice.postValue(ResponseCode.CODE_10902004.msgRes)
                            }

                            else -> {
                                toastIntentData.postValue(ToastIntentData(respCode.msgRes))
                            }
                        }
                    }
                }

                override fun onNext(t: HttpResponse?) {
                    t?.error_code?.let {
                        when (it) {
                            HttpErrorCode.ERROR_0 -> {
                                // 成功
                                loadDialogState.postValue(LOAD_DIALOG_STATE_CLOSE)
                                // 弹出 "验证码已发送"
                                toastIntentData.postValue(ToastIntentData(strResId = RR.string.AA0040))
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

    /**
     * 重置密码时发送验证码
     *
     * @param account String 账号
     */
    private fun sendCodeResetPwd(account: String) {
        // 重置密码：先判断账号是否存在
        checkAccountExit(account, checkAccountType(account))
    }

    /**
     * 判断账号是否存在
     *
     * @param account String 账号信息
     * @param accountType Int 账号类型
     */
    private fun checkAccountExit(account: String, accountType: Int) {
        when (accountType) {
            ACCOUNT_TYPE_EMAIL -> {
                registerByEmail(account)
            }

            ACCOUNT_TYPE_MOBILE -> {
                registerByPhone(account)
            }
        }
    }

    /**
     * 判断当前的账号类型
     *
     * @param account String 账号信息
     * @return Int 1. ACCOUNT_TYPE_EMAIL  2. ACCOUNT_TYPE_MOBILE
     */
    private fun checkAccountType(account: String): Int {
        return if (GwStringUtils.isMobileNO(account)) {
            ACCOUNT_TYPE_MOBILE
        } else {
            ACCOUNT_TYPE_EMAIL
        }
    }

}