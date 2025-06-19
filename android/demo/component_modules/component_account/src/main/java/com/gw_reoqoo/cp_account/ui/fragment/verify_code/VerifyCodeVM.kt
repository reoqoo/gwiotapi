package com.gw_reoqoo.cp_account.ui.fragment.verify_code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountApiImpl
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.entity.AccountInputType.ACCOUNT_FORGET
import com.gw_reoqoo.cp_account.entity.AccountInputType.ACCOUNT_REGISTER
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.entity.SendCodeType
import com.gw_reoqoo.cp_account.http.*
import com.gw_reoqoo.cp_account.listener.OnValidationDialogCloseListener
import com.gw_reoqoo.cp_account.repository.AccountInputRepository
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.HttpErrUtils
import com.gw_reoqoo.lib_http.HttpErrorCode
import com.gw_reoqoo.lib_http.HttpErrorCode.ERROR_0
import com.gw_reoqoo.lib_http.HttpErrorCode.ERROR_10902009
import com.gw_reoqoo.lib_http.HttpErrorCode.ERROR_10902029
import com.gw_reoqoo.lib_http.HttpResponse
import com.gw_reoqoo.lib_http.IBaseResultCallback
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.resource.R
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.str_utils.GwStringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 18:21
 * Description: VerifyCodeVM
 */
@HiltViewModel
class VerifyCodeVM @Inject constructor(
    private val accountApiImpl: AccountApiImpl,
    private val mgrImpl: AccountMgrImpl
) : ABaseVM() {

    companion object {
        private const val TAG = "VerifyCodeVM"

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
    lateinit var repository: AccountInputRepository

    /**
     * 地区码对象
     */
    var mDistrictBean: DistrictEntity? = null

    /**
     * 账号
     */
    var accountNum: String? = null

    /**
     * 注册的方式（邮箱或者手机号）
     */
    var registerType: AccountRegisterType? = null

    /**
     * 验证码
     */
    var verifyCode: String? = null


    /**
     * 是从哪个页面跳转过来（注册流程，找回密码流程）
     */
    var fromPage: AccountInputType? = null

    /**
     * 重新发送成功
     */
    var repeatSend: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 成功之后的跳转
     */
    var jumpData: MutableLiveData<Boolean> = MutableLiveData()

    private var timer: Job? = null

    fun getVerifyCode(account: String) {
        GwellLogUtils.i(TAG, "getVerifyCode :$account")
        fromPage?.let {
            when (it) {
                ACCOUNT_REGISTER -> {
                    registerAccount(account)
                }

                ACCOUNT_FORGET -> {
                    sendCodeResetPwd(account)
                }

                else -> {
                    registerAccount(account)
                }
            }
        }
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
        if (GwStringUtils.isEmail(account)) {
            // 邮箱注册
            registerByEmail(account)
        } else {
            registerByPhone(account)
        }
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
     * 手机号获取验证码
     *
     * @param phone String 手机号
     */
    private fun registerByPhone(phone: String) {
        val type =
            if (fromPage == ACCOUNT_REGISTER) SendCodeType.REGISTER_BIND else SendCodeType.FIND_PWD
        mDistrictBean?.run {
            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
            viewModelScope.launch {
                val result = repository.registerByPhone(
                    districtCode,
                    phone,
                    type,
                    isLogin = accountApiImpl.isSyncLogin(),
                    object : OnValidationDialogCloseListener {
                        override fun onDialogClose() {
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                        }
                    }
                )
                result.onSuccess {
                    GwellLogUtils.i(TAG, "resp $this")
                    // 成功
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                    // 弹出 "验证码已发送"
                    toastIntentData.postValue(ToastIntentData(strResId = R.string.AA0040))
                    registerType = AccountRegisterType.TYPE_MOBILE
                    repeatSend.postValue(true)
                }
                result.onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                    when (val resp = ResponseCode.getRespCode(code)) {
                        null -> Unit
                        else -> {
                            toastIntentData.postValue(ToastIntentData(resp.msgRes))
                        }
                    }
                }
                result.onLocalError {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
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
        val type = if (fromPage == ACCOUNT_REGISTER) 3 else 4
        AccountHttpKit.emailCheckCode(
            email,
            null,
            type,
            accountApiImpl.isSyncLogin(),
            accountMgrImpl = mgrImpl,
            object : IBaseResultCallback<HttpResponse> {

                override fun onStart() {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                    errorCode?.let {
                        val errRes = HttpErrUtils.showErrorToast(it)
                        if (errRes == -1) {
                            // SESSION过期或者错误，需要做退出登录操作
                        }
                        toastIntentData.postValue(ToastIntentData(errRes))
                    }
                }

                override fun onNext(t: HttpResponse?) {
                    t?.error_code?.let {
                        when (it) {
                            HttpErrorCode.ERROR_0 -> {
                                // 成功
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                // 弹出 "验证码已发送"
                                toastIntentData.postValue(ToastIntentData(strResId = R.string.AA0040))
                                registerType = AccountRegisterType.TYPE_EMAIL
                                repeatSend.postValue(true)
                            }

                            else -> {
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
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
     * 判断当前的账号类型
     *
     * @param account String 账号信息
     * @return Int 1. ACCOUNT_TYPE_EMAIL  2. ACCOUNT_TYPE_MOBILE
     */
    private fun checkAccountType(account: String): Int {
        return if (GwStringUtils.isEmail(account)) {
            ACCOUNT_TYPE_EMAIL
        } else {
            ACCOUNT_TYPE_MOBILE
        }
    }

    /**
     * 验证码校验
     */
    fun checkVerifyCode(vCode: String) {
        val callback = object : IBaseResultCallback<VerifyCodeResponse> {
            override fun onStart() {
                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
            }

            override fun onNext(t: VerifyCodeResponse?) {
                GwellLogUtils.i(TAG, "onNext： result = $t")
                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                when (t?.code.toString()) {
                    ERROR_0 -> {
                        verifyCode = vCode
                        // 校验成功，跳转到设置密码页面
                        when (fromPage) {
                            ACCOUNT_REGISTER -> {
                                jumpData.postValue(true)
                            }

                            ACCOUNT_FORGET -> {
                                jumpData.postValue(true)
                            }

                            else -> {
                                GwellLogUtils.e(TAG, "fromPage is unknown，please check it")
                            }
                        }
                    }

                    ERROR_10902009, ERROR_10902029 -> {
                        // 手机 邮件验证码不正确
                        toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0390))
                    }
                }
            }

            override fun onError(errorCode: String?, throwable: Throwable?) {
                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                errorCode?.let {
                    when (val resp = ResponseCode.getRespCode(errorCode.toInt())) {
                        null -> Unit

                        else -> {
                            toastIntentData.postValue(ToastIntentData(resp.msgRes))
                        }
                    }
                } ?: run {
                    toastIntentData.postValue(ToastIntentData(strResId = R.string.AA0573))
                }
            }

        }

        mDistrictBean?.let {
            GwellLogUtils.i(TAG, "accountNum: $accountNum, vCode: $verifyCode")
            val type = if (registerType == AccountRegisterType.TYPE_MOBILE) 1 else 2
            AccountHttpKit.checkVerifyCode(
                accountNum,
                accountNum,
                vCode,
                type,
                it.districtCode,
                accountApiImpl.isSyncLogin(),
                accountMgrImpl = mgrImpl,
                callback
            )
        } ?: let {
            GwellLogUtils.i(TAG, "checkVerifyCode failed, districtBean is null")
        }

    }

    /**
     * 于协程中实现倒计时
     * @param duration 倒计时时长
     * @param interval 倒计时步长
     * @param scope 协程作用域
     * @param onTick 倒计时变更，回调
     * @param onStart 倒计时开始，回调
     * @param onEnd 倒计时结束，回调
     *
     * @return [Job] 可用于在需要时，取消倒计时
     */
    fun countDownCoroutine(
        duration: Int,
        interval: Int = 1,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onStart: ((Int) -> Unit)? = null,
        onEnd: (() -> Unit)? = null,
    ) {
        if (duration <= 0 || interval <= 0) {
            throw IllegalArgumentException("duration or interval can not less than zero")
        }
        timer = flow {
            for (i in duration downTo 0 step interval) {
                delay((interval * 1000).toLong())
                emit(i)
            }
        }
            .onEach { onTick.invoke(it) }
            .onStart { onStart?.invoke(duration) }
            .onCompletion {
                // 正常结束，才能回调。
                if (it == null) {
                    onEnd?.invoke()
                }
            }
            // 确保上游回调，是在主线程回调
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel("Fragment is finished")
    }

}