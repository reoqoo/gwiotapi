package com.gw_reoqoo.cp_account.datasource

import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.component_webview.api.interfaces.IWebViewApi.OnValidationCallback
import com.gw_reoqoo.widget_webview.entity.ValidationResult
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.entity.SendCodeType
import com.gw_reoqoo.cp_account.http.PhoneCodeResponse
import com.gw_reoqoo.cp_account.listener.OnValidationDialogCloseListener
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.entities.CheckAccountExistResponse
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.utils.rxjava.SubscriberListener
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/5 23:13
 * Description: 接口请求数据源
 */
class RemoteInputDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper,
    private val accountMgrImpl: AccountMgrImpl,
    private val iWebViewApi: IWebViewApi
) {

    companion object {
        private const val TAG = "RemoteInputDataSource"
    }

    /**
     * 获取验证码接口回调监听
     */
    private var listener: SubscriberListener? = null

    /**
     * 检查邮箱是否存在
     *
     * @param email String 邮箱地址
     * @return RespResult<CheckAccountExistResponse> 接口回调
     */
    suspend fun checkEmailExist(
        email: String,
    ): RespResult<CheckAccountExistResponse> {
        val result = Channel<RespResult<CheckAccountExistResponse>>(1)
        httpService.checkEmailExist(email, typeSubscriber<CheckAccountExistResponse>(
            onSuccess = { _data ->
                result.trySend(RespResult.Success(_data))
            },
            onFail = { t ->
                if (t is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(t.code, t.message))
                } else {
                    GwellLogUtils.e(TAG, "checkEmailExist onFail: error = ${t.message}")
                    result.trySend(RespResult.LocalError(t))
                }
            }
        ))
        return result.receive()
    }

    /**
     * 检查手机号是否存在
     *
     * @param mobileArea String 手机区号
     * @param mobile String 手机号
     * @return RespResult<CheckAccountExistResponse> 回调
     */
    suspend fun checkMobileExist(
        mobileArea: String,
        mobile: String,
    ): RespResult<CheckAccountExistResponse> {
        val result = Channel<RespResult<CheckAccountExistResponse>>(1)
        httpService.checkMobileExist(mobileArea, mobile, typeSubscriber<CheckAccountExistResponse>(
            onSuccess = { _data ->
                result.trySend(RespResult.Success(_data))
            },
            onFail = { t ->
                if (t is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(t.code, t.message))
                } else {
                    GwellLogUtils.e(TAG, "checkEmailExist onFail: error = ${t.message}")
                    result.trySend(RespResult.LocalError(t))
                }
            }
        ))
        return result.receive()
    }

    /**
     * 验证码校验接口
     *
     * @param countryCode String 国家码
     * @param phoneNumber String 手机号
     * @param type Int 用途（0：注册 1：忘记密码）
     * @return RespResult<PhoneCodeResponse> 回调
     */
    suspend fun registerByPhone(
        countryCode: String,
        phoneNumber: String,
        type: SendCodeType,
        isLogin: Boolean,
        closeListener: OnValidationDialogCloseListener?
    ): RespResult<PhoneCodeResponse> {
        GwellLogUtils.i(TAG, "registerByPhone")
        val result = Channel<RespResult<PhoneCodeResponse>>(1)
        listener = typeSubscriber<PhoneCodeResponse>(
            onSuccess = { _data ->
                GwellLogUtils.i(TAG, "_data: ${_data}")
                result.trySend(RespResult.Success(_data))
            }, onFail = { t ->
                if (t is ResponseNotSuccessException) {
                    GwellLogUtils.e(TAG, "t=${t.code}")
                    if (t.code == ResponseCode.CODE_10902018.code) {
                        // 如果是10902018，表示需要图形校验，拉起图形校验弹窗
                        iWebViewApi.showGraphicCaptchaDialog(object : OnValidationCallback {
                            override fun onValidationFinish(result: ValidationResult?) {
                                if (result?.result == ValidationResult.Result.SUCCESS.result) {
                                    listener?.let {
                                        getPhoneCode(
                                            countryCode,
                                            phoneNumber,
                                            type,
                                            isLogin,
                                            result.ticket,
                                            result.randstr,
                                            it
                                        )
                                    }
                                }
                            }

                            override fun onCloseDialog() {
                                closeListener?.onDialogClose()
                            }

                        })
                    } else {
                        result.trySend(RespResult.ServerError(t.code, t.message))
                    }
                } else {
                    GwellLogUtils.e(TAG, "registerByPhone onFail: error = ${t.message}")
                    result.trySend(RespResult.LocalError(t))
                }
            }
        )
        listener?.let {
            getPhoneCode(countryCode, phoneNumber, type, isLogin, null, null, it)
        }
        return result.receive()
    }

    /**
     * 获取验证码接口
     *
     * @param countryCode String 国家字码
     * @param phoneNumber String 电话号码
     * @param codeType SendCodeType 用途（0：注册 1：忘记密码） [com.gw.cp_account.entity.SendCodeType]
     * @param isLogin Boolean    是否登录
     * @param ticket String?     接口需要的ticket（图形验证码的时候会返回，非图形验证码传空）
     * @param randStr String?    接口需要的randStr（图形验证码的时候会返回，非图形验证码传空）
     * @param listener SubscriberListener  结果回调
     */
    private fun getPhoneCode(
        countryCode: String,
        phoneNumber: String,
        codeType: SendCodeType,
        isLogin: Boolean,
        ticket: String?,
        randStr: String?,
        listener: SubscriberListener
    ) {
        GwellLogUtils.i(TAG, "getPhoneCode")
        if (isLogin) {
            httpService.sendSmsCodeNewWithLogin(
                countryCode,
                phoneNumber,
                codeType.type,
                ticket,
                randStr,
                listener
            )
        } else {
            httpService.sendSmsCodeNew(
                countryCode,
                phoneNumber,
                codeType.type,
                ticket,
                randStr,
                listener
            )
        }
    }

}