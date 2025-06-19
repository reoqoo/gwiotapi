package com.gw_reoqoo.cp_account.ui.fragment.bind_verify_code.bind_verify

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountApiImpl
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.entity.SendCodeType
import com.gw_reoqoo.cp_account.http.AccountHttpKit
import com.gw_reoqoo.cp_account.http.VerifyCodeResponse
import com.gw_reoqoo.cp_account.listener.OnValidationDialogCloseListener
import com.gw_reoqoo.cp_account.repository.AccountInputRepository
import com.gw_reoqoo.cp_account.repository.AccountRepository
import com.gw_reoqoo.cp_account.repository.UserInfoRepository
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
import com.gw_reoqoo.resource.R
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 18:21
 * Description: BindVerifyCodeVM
 */
@HiltViewModel
class BindVerifyCodeVM @Inject constructor(
    private val accountApiImpl: AccountApiImpl,
    private val mgrImpl: AccountMgrImpl
) : ABaseVM() {

    companion object {
        private const val TAG = "BindVerifyCodeVM"
    }

    @Inject
    lateinit var accountResp: AccountRepository

    @Inject
    lateinit var repository: AccountInputRepository

    @Inject
    lateinit var userInfoResp: UserInfoRepository

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
     * 成功之后的跳转
     */
    var repeatSend: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 成功之后的跳转
     */
    var jumpData: MutableLiveData<Boolean> = MutableLiveData()

    private var timer: Job? = null

    /**
     * 验证码校验
     */
    fun checkVerifyCode(vCode: String) {
        mDistrictBean?.let {
            GwellLogUtils.i(TAG, "accountNum: $accountNum, vCode: $verifyCode")
            viewModelScope.launch(Dispatchers.IO) {
                val result = if (registerType == AccountRegisterType.TYPE_MOBILE) {
                    userInfoResp.accountBind(1, it.districtCode, accountNum, null, vCode)
                } else {
                    userInfoResp.accountBind(2, null, null, accountNum, vCode)
                }
                result
                    .onSuccess {
                        jumpData.postValue(true)
                    }
                    .onServerError { code, msg ->
                        GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
                        when (code.toString()) {
                            ERROR_10902009, ERROR_10902029 -> {
                                // 手机 邮件验证码不正确
                                toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0390))
                            }

                            else -> {
                                code.let {
                                    toastIntentData.postValue(
                                        ToastIntentData(
                                            HttpErrUtils.showErrorToast(
                                                it.toString()
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                    .onLocalError {
                        HttpErrUtils.throwableToErrCode(it)
                    }

            }
        } ?: let {
            GwellLogUtils.i(TAG, "checkVerifyCode failed, districtBean is null")
        }

    }

    /**
     * 于协程中实现倒计时
     *
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

    /**
     * 手机号获取验证码
     *
     * @param phone String 手机号
     */
    fun getCodeByPhone(phone: String) {
        GwellLogUtils.i(TAG, "getCodeByPhone $phone")
        mDistrictBean?.run {
            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
            viewModelScope.launch {
                val result = repository.registerByPhone(
                    districtCode,
                    phone,
                    SendCodeType.REGISTER_BIND,
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
                    toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(code.toString())))
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
    fun getCodeByEmail(email: String) {
        GwellLogUtils.i(TAG, "getCodeByEmail $email")
        AccountHttpKit.emailCheckCode(email, null, 3,
            isLogin = accountApiImpl.isSyncLogin(),
            accountMgrImpl = mgrImpl,
            object : IBaseResultCallback<HttpResponse> {
                override fun onStart() {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    GwellLogUtils.e(
                        TAG,
                        "onError: errorCode $errorCode, throwable ${throwable?.message}"
                    )
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
                    GwellLogUtils.i(TAG, "onNext: ${t.toString()}")
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

    fun hasBindMobile(): Boolean {
        return runBlocking {
            accountResp.getLocalUserInfo()?.phone?.isNotEmpty() ?: false
        }
    }

    fun hasBindEmail(): Boolean {
        return runBlocking {
            accountResp.getLocalUserInfo()?.email?.isNotEmpty() ?: false
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel("Fragment is finished")
    }

}