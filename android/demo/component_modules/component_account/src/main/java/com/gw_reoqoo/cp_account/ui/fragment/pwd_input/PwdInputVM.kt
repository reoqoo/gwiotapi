package com.gw_reoqoo.cp_account.ui.fragment.pwd_input

import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.datastore.AccountDataStoreApi
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.http.AccountHttpKit
import com.gw_reoqoo.cp_account.http.LoginResponse
import com.gw_reoqoo.cp_account.http.toUserInfo
import com.gw_reoqoo.cp_account.repository.AccountRepository
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.SingleLiveEvent
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.HttpErrUtils
import com.gw_reoqoo.lib_http.HttpErrorCode
import com.gw_reoqoo.lib_http.HttpResponse
import com.gw_reoqoo.lib_http.IBaseResultCallback
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.device_utils.PhoneIDUtils
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 18:16
 * Description: PwdInputVM
 */
@HiltViewModel
class PwdInputVM @Inject constructor(
    private val repository: AccountRepository,
    private val mgrImpl: AccountMgrImpl,
    private val dataStoreApi: AccountDataStoreApi
) : ABaseVM() {

    companion object {
        private const val TAG = "PwdInputVM"
    }

    var account: String? = null

    var vCode: String? = null

    var districtEntity: DistrictEntity? = null

    var registerType: AccountRegisterType? = null

    var inputType: AccountInputType? = null

    val jumpLogin: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val jumpFindSuccess: SingleLiveEvent<Boolean> = SingleLiveEvent()

    /**
     * 账号注册设置密码
     *
     * @param pwd String  密码
     */
    fun setPwdFromAccount(pwd: String) {
        val districtEntity = this.districtEntity
        GwellLogUtils.i(TAG, "setPwdFromAccount: districtEntity $districtEntity")
        val account = this.account
        val vCode = this.vCode
        if (null == districtEntity
            || account.isNullOrEmpty()
            || vCode.isNullOrEmpty()
        ) {
            return
        }
        when (registerType) {
            AccountRegisterType.TYPE_MOBILE -> {
                setPwdFromMobile(
                    districtEntity.districtCode,
                    account,
                    pwd,
                    vCode,
                    districtEntity.district
                )
            }

            AccountRegisterType.TYPE_EMAIL -> {
                setPwdFromEmail(account, pwd, vCode, districtEntity.district)
            }

            else -> {}
        }

    }

    /**
     * 忘记密码流程设置密码
     */
    fun setPwdFromForgetPwd(pwd: String) {
        when (registerType) {
            AccountRegisterType.TYPE_MOBILE -> {
                resetPwdFromMobile(account!!, districtEntity!!.districtCode, pwd, vCode!!)
            }

            AccountRegisterType.TYPE_EMAIL -> {
                resetPwdFromEmail(account!!, pwd, vCode!!)
            }

            else -> {}
        }
    }

    /**
     * 手机号注册
     *
     * @param mobileArea String 国家区号
     * @param mobile String     手机号码
     * @param pwd String        密码
     * @param vCode String      手机验证码
     * @param regRegion String  区域码
     */
    private fun setPwdFromMobile(
        mobileArea: String, mobile: String, pwd: String, vCode: String, regRegion: String
    ) {
        val uniqueId = PhoneIDUtils.phoneUniqueId

        val subscriberListener: IBaseResultCallback<LoginResponse> =
            object : IBaseResultCallback<LoginResponse> {
                override fun onStart() {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
                }

                override fun onNext(t: LoginResponse?) {
                    when (t?.error_code) {
                        HttpErrorCode.ERROR_10902012 -> {
                            // session过期
//                        val reLoginIntent = Intent()
//                        reLoginIntent.action = ReceiverConstants.Action.SESSION_ID_ERROR
//                        AppEnv.APP.sendBroadcast(reLoginIntent)
                        }

                        HttpErrorCode.ERROR_998 -> {
                            // 重试
                            setPwdFromMobile(mobileArea, mobile, pwd, vCode, regRegion)
                            return
                        }

                        HttpErrorCode.ERROR_0 -> {
                            // 成功
                            t.data?.let {
                                registerSuccess(isMobile = true, mobile, it)
                            } ?: let {
                                GwellLogUtils.e(TAG, "userResponse.data is null")
                                toastIntentData.postValue(
                                    ToastIntentData(com.gw_reoqoo.resource.R.string.AA0381)
                                )
                            }
                        }

                        HttpErrorCode.ERROR_10902020 -> {
                            // "手机号已被注册"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0382))
                        }

                        HttpErrorCode.ERROR_10901022 -> {
                            // "手机号格式错误"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0267))
                        }

                        HttpErrorCode.ERROR_999 -> {
                            // "网络繁忙,请稍后再试"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0381))
                        }

                        else -> {
                            // "操作失败"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(
                                ToastIntentData(
                                    HttpErrUtils.showErrorToast(
                                        t?.error_code
                                    )
                                )
                            )
                        }
                    }
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                    if (HttpErrorCode.ERROR_10035 == errorCode) {
                        return
                    }
                    toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(errorCode)))
                }
            }

        AccountHttpKit.mobileRegister(
            mobileArea,
            mobile,
            pwd,
            vCode,
            uniqueId,
            regRegion,
            accountMgrImpl = mgrImpl,
            subscriberListener
        )
    }

    /**
     * 邮箱注册
     *
     * @param email String      邮箱地址
     * @param pwd String        密码
     * @param vCode String      邮箱验证码
     * @param regRegion String  区域码
     */
    private fun setPwdFromEmail(
        email: String, pwd: String, vCode: String, regRegion: String
    ) {
        val uniqueId = PhoneIDUtils.phoneUniqueId

        val subscriberListener: IBaseResultCallback<LoginResponse> =
            object : IBaseResultCallback<LoginResponse> {
                override fun onStart() {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
                }

                override fun onNext(t: LoginResponse?) {
                    when (t?.error_code) {
                        HttpErrorCode.ERROR_998 -> {
                            // 重试
                            setPwdFromEmail(email, pwd, vCode, regRegion)
                            return
                        }

                        HttpErrorCode.ERROR_0 -> {
                            // 成功
                            t.data?.let {
                                registerSuccess(isMobile = false, email, it)
                            } ?: run {
                                GwellLogUtils.e(TAG, "LoginResponse.data is null")
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0381))
                            }
                        }

                        HttpErrorCode.ERROR_10902021 -> {
                            // "邮箱已被注册"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0306))
                        }

                        HttpErrorCode.ERROR_10901023 -> {
                            // "邮箱格式错误"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0267))
                        }

                        HttpErrorCode.ERROR_999 -> {
                            // "网络繁忙,请稍后再试"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0381))
                        }

                        else -> {
                            // "操作失败"
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(
                                ToastIntentData(HttpErrUtils.showErrorToast(t?.error_code))
                            )
                        }
                    }
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                    if (HttpErrorCode.ERROR_10035 == errorCode) {
                        return
                    }
                    toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(errorCode)))
                }
            }
        AccountHttpKit.emailRegister(
            email,
            pwd,
            vCode,
            uniqueId,
            regRegion,
            accountMgrImpl = mgrImpl,
            subscriberListener
        )
    }

    /**
     * 手机重置密码
     *
     * @param account      String 手机号
     * @param districtCode String 手机区码
     * @param pwd          String 新密码
     * @param vCode        String 验证码
     */
    private fun resetPwdFromMobile(
        account: String,
        districtCode: String,
        pwd: String,
        vCode: String
    ) {
        val subscriberListener: IBaseResultCallback<HttpResponse> =
            object : IBaseResultCallback<HttpResponse> {
                override fun onStart() {
                    loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
                }

                override fun onNext(t: HttpResponse?) {
                    t?.let {
                        when (t.error_code) {
                            HttpErrorCode.ERROR_10902012, HttpErrorCode.ERROR_10901020 -> {
                                // sessionId过期 或 缺少参数，不知道为啥处理是一样的
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                // 发送 SESSION_ID_ERROR 广播.
                                //          MainActivity 中的 广播接收器 会 处理 这个 SESSION_ID_ERROR 广播, 关闭掉所有的页面(包括关闭掉MainActivity), 然后 跳到 登陆页
//                            val reLogin = Intent()
//                            reLogin.action = ReceiverConstants.Action.SESSION_ID_ERROR
//                            AppEnv.APP.sendBroadcast(reLogin)
                            }

                            HttpErrorCode.ERROR_998 -> {
                                // 重试
                                resetPwdFromEmail(account, pwd, vCode)
                            }

                            HttpErrorCode.ERROR_0 -> {
                                // 重置密码成功
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                // 跳转到登录页
                                jumpFindSuccess.postValue(true)
                            }

                            HttpErrorCode.ERROR_10902011 -> {
                                // 用户不存在，理论上不会出现，前面已经拦截了
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0372))
                            }

                            HttpErrorCode.ERROR_999 -> {
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0381))
                            }

                            else -> {
                                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                                toastIntentData.postValue(
                                    ToastIntentData(
                                        HttpErrUtils.showErrorToast(
                                            t.error_code!!
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onError(errorCode: String?, throwable: Throwable?) {
                    toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(errorCode!!)))
                }

            }
        AccountHttpKit.resetPwd(
            null,
            account,
            districtCode,
            pwd,
            1,
            vCode,
            accountMgrImpl = mgrImpl,
            subscriberListener
        )
    }

    /**
     * 邮箱重置密码
     *
     * @param account String 邮箱
     * @param pwd     String 新密码
     * @param vCode   String 验证码
     */
    private fun resetPwdFromEmail(account: String, pwd: String, vCode: String) {
        val subscriberListener = object : IBaseResultCallback<HttpResponse> {
            override fun onStart() {
                loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
            }

            override fun onNext(t: HttpResponse?) {
                t?.error_code?.let {
                    when (it) {
                        HttpErrorCode.ERROR_10902012, HttpErrorCode.ERROR_10901020 -> {
                            // sessionId过期 或 缺少参数，不知道为啥处理是一样的
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            // 发送 SESSION_ID_ERROR 广播.
                            //          MainActivity 中的 广播接收器 会 处理 这个 SESSION_ID_ERROR 广播, 关闭掉所有的页面(包括关闭掉MainActivity), 然后 跳到 登陆页
//                            val reLogin = Intent()
//                            reLogin.action = ReceiverConstants.Action.SESSION_ID_ERROR
//                            AppEnv.APP.sendBroadcast(reLogin)
                        }

                        HttpErrorCode.ERROR_998 -> {
                            // 重试
                            resetPwdFromEmail(account, pwd, vCode)
                        }

                        HttpErrorCode.ERROR_0 -> {
                            // 重置密码成功
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            // 跳转到登录页
                            jumpFindSuccess.postValue(true)
                        }

                        HttpErrorCode.ERROR_10902011 -> {
                            // 用户不存在，理论上不会出现，前面已经拦截了
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0372))
                        }

                        HttpErrorCode.ERROR_999 -> {
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(com.gw_reoqoo.resource.R.string.AA0381))
                        }

                        else -> {
                            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
                            toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(it)))
                        }
                    }
                }
            }

            override fun onError(errorCode: String?, throwable: Throwable?) {
                toastIntentData.postValue(ToastIntentData(HttpErrUtils.showErrorToast(errorCode!!)))
            }

        }
        AccountHttpKit.resetPwd(
            account,
            null,
            null,
            pwd,
            2,
            vCode,
            accountMgrImpl = mgrImpl,
            subscriberListener
        )
    }

    private fun registerSuccess(
        isMobile: Boolean,
        account: String,
        userResponse: LoginResponse.DataBean
    ) {
        // 登录成功
        viewModelScope.launch(Dispatchers.IO) {
            val userInfo = if (isMobile) {
                userResponse.toUserInfo(phone = account)
            } else {
                userResponse.toUserInfo(email = account)
            }
            userInfo?.let {
                repository.updateLocalUserInfo(userInfo)
                dataStoreApi.setTokenRefreshTime(System.currentTimeMillis())
                finishActivityLD.postValue(true)
                pageJumpData.postValue(
                    PageJumpData(
                        TheRouter.build(ReoqooRouterPath.AppPath.MAIN_ACTIVITY_PATH)
                            .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                )
            } ?: GwellLogUtils.e(TAG, "Convert failed: userInfo is null")
        }
    }
}