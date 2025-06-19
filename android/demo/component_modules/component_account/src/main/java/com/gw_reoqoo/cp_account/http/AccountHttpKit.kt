package com.gw_reoqoo.cp_account.http

import com.google.gson.JsonObject
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.entity.*
import com.gw_reoqoo.lib_http.*
import com.gw_reoqoo.lib_utils.device_utils.PhoneIDUtils
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.str_utils.GwStringUtils
import com.tencentcs.iotvideo.http.utils.HttpUtils
import com.tencentcs.iotvideo.utils.JSONUtils.JsonToEntity
import com.tencentcs.iotvideo.utils.rxjava.SubscriberListener

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/4 9:45
 * Description: IAccountHttpApi
 */
object AccountHttpKit {

    private const val TAG = "AccountHttpKit"

    /**
     * 登录(支持邮箱,手机号码(必须带地区码 eg:86-18922222222),用户ID)
     *
     * @param userName           登陆名,支持邮箱,手机号码(必须带地区码 eg:86-18922222222),用户ID
     * @param password           登陆密码
     * @param uuid               设备唯一码
     * @param callback 登陆结果回调
     */
    fun login(
        userName: String,
        password: String,
        uuid: String,
        accountMgrImpl: AccountMgrImpl,
        callback: IBaseResultCallback<LoginResponse>
    ) {
        val listener: SubscriberListener = object : SubscriberListener {
            override fun onStart() {
                callback.onStart()
            }

            override fun onSuccess(jsonObject: JsonObject) {
                val loginResult = JsonToEntity(jsonObject.toString(), LoginResponse::class.java)
                callback.onNext(loginResult)
            }

            override fun onFail(throwable: Throwable) {
                callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
            }
        }
        val httpService = HiltApi.httpServiceWrapper
        val pwdMD5 = HttpUtils.md5(password)
        if (GwStringUtils.isEmailValid(userName)) {
            httpService.emailLogin(userName, pwdMD5, uuid, listener)
        } else if (userName.startsWith("-")) {
            httpService.userIdLogin(userName, pwdMD5, uuid, listener)
        } else {
            if (userName.contains("-")) {
                val phone =
                    userName.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                httpService.mobileLogin(phone[1], phone[0], pwdMD5, uuid, listener)
            } else {
                httpService.mobileLogin(userName, "0", pwdMD5, uuid, listener)
            }
        }
    }

    /**
     * 手机用户注册
     *
     * @param mobileArea         国家码
     * @param mobile             手机号码
     * @param pwd                密码
     * @param vcode              验证码
     * @param uniqueId           终端唯一id(即登录时传的UUID)
     * @param regRegion          用户所选注册区域，用于账号隔离，国家二字码，大写
     * @param callback Http回调结果的观察者
     */
    fun mobileRegister(
        mobileArea: String?,
        mobile: String?,
        pwd: String?,
        vcode: String?,
        uniqueId: String?,
        regRegion: String?,
        accountMgrImpl: AccountMgrImpl,
        callback: IBaseResultCallback<LoginResponse>
    ) {
        val httpService = HiltApi.httpServiceWrapper
        val pwdMd5 = if (pwd == null) {
            null
        } else {
            HttpUtils.md5(pwd)
        }
        httpService.mobileRegister(
            mobileArea,
            mobile,
            pwdMd5,
            vcode,
            uniqueId,
            regRegion,
            object : SubscriberListener {
                override fun onStart() {
                    callback.onStart()
                }

                override fun onSuccess(jsonObject: JsonObject) {
                    val loginResult: LoginResponse? =
                        JsonToEntity(jsonObject.toString(), LoginResponse::class.java)
                    if (loginResult == null) {
                        callback.onNext(null)
                        return
                    }
                    loginResult.error_code = java.lang.String.valueOf(loginResult.code)
                    callback.onNext(loginResult)

                }

                override fun onFail(throwable: Throwable) {
                    callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
                }
            })
    }

    /**
     * 邮箱注册
     *
     * @param email              邮箱
     * @param pwd                密码
     * @param vcode              验证码
     * @param uniqueId           终端唯一id(即登录时传的UUID)
     * @param regRegion          用户所选注册区域，用于账号隔离，国家二字码，大写
     * @param callback Http回调结果的观察者
     */
    fun emailRegister(
        email: String?,
        pwd: String?,
        vcode: String?,
        uniqueId: String?,
        regRegion: String?,
        accountMgrImpl: AccountMgrImpl,
        callback: IBaseResultCallback<LoginResponse>
    ) {
        val pwdMd5 = if (pwd == null) {
            null
        } else {
            HttpUtils.md5(pwd)
        }
        val httpService = HiltApi.httpServiceWrapper
        httpService.emailRegister(
            email,
            pwdMd5,
            vcode,
            uniqueId,
            regRegion,
            object : SubscriberListener {
                override fun onStart() {
                    callback.onStart()
                }

                override fun onSuccess(jsonObject: JsonObject) {
                    val loginResult: LoginResponse? =
                        JsonToEntity(jsonObject.toString(), LoginResponse::class.java)
                    loginResult?.error_code = java.lang.String.valueOf(loginResult?.code)
                    callback.onNext(loginResult)
                }

                override fun onFail(throwable: Throwable) {
                    callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
                }
            })
    }

    /**
     * 重置密码
     *
     * @param email              邮箱
     * @param mobile             手机号
     * @param mobileArea         手机区号
     * @param newPwd             新密码
     * @param type               找回密码途径类型 1：手机号找回密码，2：邮箱找回密码
     * @param vCode              验证码
     * @param callback Http回调结果的观察者
     */
    fun resetPwd(
        email: String?,
        mobile: String?,
        mobileArea: String?,
        newPwd: String?,
        type: Int,
        vCode: String?,
        accountMgrImpl: AccountMgrImpl,
        callback: IBaseResultCallback<HttpResponse>
    ) {
        val httpService = HiltApi.httpServiceWrapper
        val newPwdMD5 = if (newPwd == null) {
            null
        } else {
            HttpUtils.md5(newPwd)
        }
        httpService.forgotPwd(
            email,
            mobile,
            mobileArea,
            newPwdMD5,
            PhoneIDUtils.phoneUniqueId,
            type,
            vCode,
            object : SubscriberListener {
                override fun onStart() {
                    callback.onStart()
                }

                override fun onSuccess(jsonObject: JsonObject) {
                    val httpResult: HttpResponse? =
                        JsonToEntity(jsonObject.toString(), HttpResponse::class.java)
                    if (httpResult == null) {
                        callback.onNext(null)
                        return
                    }
                    httpResult.error_code = java.lang.String.valueOf(httpResult.code)
                    callback.onNext(httpResult)
                }

                override fun onFail(throwable: Throwable) {
                    callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
                }
            })
    }

    /**
     * 注销账户
     *
     * @param
     * @param pwd        密码
     * @param type       账户类型
     * @param reasonType 注销原因类型
     * @param reason     原因描述
     */
    fun unRegister(
        account: String,
        pwd: String?,
        sessionId: Int,
        type: Int,
        reasonType: Int,
        reason: String?,
        callback: IBaseResultCallback<HttpResponse>
    ) {
        val userId: String = (account.toInt() or -0x80000000).toString() + ""
        GwellLogUtils.d(TAG, "unRegister: sessionId $sessionId")
        val pwdMD5 = if (pwd == null) {
            null
        } else {
            HttpUtils.md5(pwd)
        }
        val httpService = HiltApi.httpServiceWrapper
        httpService.unRegister(
            userId,
            pwdMD5,
            sessionId,
            type,
            reasonType,
            reason,
            object : SubscriberListener {
                override fun onStart() {
                    callback.onStart()
                }

                override fun onSuccess(jsonObject: JsonObject) {
                    // TODO 推送注销
//                IoTAlarmPushManager.unRegisterPush(true)
                    val httpResult: HttpResponse? =
                        JsonToEntity(jsonObject.toString(), HttpResponse::class.java)
                    callback.onNext(httpResult)
                }

                override fun onFail(throwable: Throwable) {
                    callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
                }
            })
    }

    /**
     * 重置密码
     *
     * @param oldPwd   旧密码
     * @param pwd      新密码
     * @param callback 回调
     */
    fun modifyPwd(
        oldPwd: String?,
        pwd: String?,
        callback: IBaseResultCallback<HttpResponse>
    ) {
        val httpService = HiltApi.httpServiceWrapper
        httpService.modifyPwd(oldPwd, pwd, object : SubscriberListener {
            override fun onStart() {
                callback.onStart()
            }

            override fun onSuccess(jsonObject: JsonObject) {
                val httpResult: HttpResponse? =
                    JsonToEntity(jsonObject.toString(), HttpResponse::class.java)
                callback.onNext(httpResult)
            }

            override fun onFail(throwable: Throwable) {
                callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
            }
        })
    }

    /**
     * 邮箱获取验证码
     *
     * @param email              邮箱
     * @param pwd                密码
     * @param flag               参数类型：0：通过激活链接注册和绑定
     * 1：找回密码
     * 2：只发送一条短信
     * 3：通过验证码注册和绑定
     * 4：新版本找回密码
     * @param ticket             app进行图形验证结果的票据
     * @param randstr            app进行图形验证结果的随机字符串
     * @param callback Http回调结果的观察者
     */
    fun emailCheckCode(
        email: String,
        pwd: String?,
        flag: Int,
        isLogin: Boolean = false,
        accountMgrImpl: AccountMgrImpl,
        callback: IBaseResultCallback<HttpResponse>
    ) {
        val httpService = HiltApi.httpServiceWrapper
        val listener = object : SubscriberListener {
            override fun onStart() {
                callback.onStart()
            }

            override fun onSuccess(jsonObject: JsonObject) {
                val httpResult: HttpResponse? =
                    JsonToEntity(jsonObject.toString(), HttpResponse::class.java)
                if (httpResult == null) {
                    callback.onNext(null)
                    return
                }
                httpResult.error_code = java.lang.String.valueOf(httpResult.code)
                callback.onNext(httpResult)
            }

            override fun onFail(throwable: Throwable) {
                callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
            }
        }
        if (isLogin) {
            httpService.emailCheckCodeWithLogin(email, pwd, flag, null, null, listener)
        } else {
            httpService.emailCheckCode(email, pwd, flag, null, null, listener)
        }
    }

    /**
     * 手机或邮箱验证码校验
     *
     * @param email              邮箱地址（非必填）
     * @param mobile             手机号（非必填）
     * @param vcode              验证码（必填）
     * @param type               验证码类型（必填：1、手机号 2、邮箱）
     * @param mobileArea         手机区号（非必填）
     * @param callback Http回调结果的观察者
     * @notice 备注：当type=1时，mobile和mobileArea为必填；当type=2时，email为必填
     */
    fun checkVerifyCode(
        email: String?,
        mobile: String?,
        vcode: String,
        type: Int,
        mobileArea: String,
        isLogin: Boolean = false,
        accountMgrImpl: AccountMgrImpl,
        callback: IBaseResultCallback<VerifyCodeResponse>
    ) {
        val httpService = HiltApi.httpServiceWrapper
        val listener = object : SubscriberListener {
            override fun onStart() {
                callback.onStart()
            }

            override fun onSuccess(jsonObject: JsonObject) {
                val result: VerifyCodeResponse? =
                    JsonToEntity(jsonObject.toString(), VerifyCodeResponse::class.java)
                callback.onNext(result)
            }

            override fun onFail(throwable: Throwable) {
                callback.onError(HttpErrUtils.throwableToErrCode(throwable), throwable)
            }
        }
        if (isLogin) {
            httpService.verifyCodeWithLogin(email, mobile, vcode, type, mobileArea, listener)
        } else {
            httpService.verifyCode(email, mobile, vcode, type, mobileArea, listener)
        }
    }

}