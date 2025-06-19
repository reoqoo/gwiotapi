package com.gw_reoqoo.cp_account.datasource

import com.google.gson.Gson
import com.gw_reoqoo.cp_account.http.RefreshTokenResponse
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.entities.ListHeadResp
import com.gw_reoqoo.lib_http.entities.UserDetailResp
import com.gw_reoqoo.lib_http.mapActionFlow
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gw_reoqoo.lib_utils.device_utils.PhoneIDUtils
import com.jwkj.iotvideo.netconfig.data.NetMatchTokenResult
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.tencentcs.iotvideo.http.utils.HttpUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/7 16:52
 * Description: IAccountDataSource
 */
class RemoteUserDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper
) {

    companion object {
        private const val TAG = "RemoteUserDataSource"
    }

    /**
     * 获取当前用户的信息
     *
     * @return UserInfo 用户信息
     */
    suspend fun queryUserDetail(): RespResult<UserDetailResp> {
        val result = Channel<RespResult<UserDetailResp>>(1)
        httpService.userInfoQuery(typeSubscriber<UserDetailResp>(
            onSuccess = {
                result.trySend(RespResult.Success(it))
            },
            onFail = {
                if (it is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(it.code, it.msg))
                } else {
                    result.trySend(RespResult.LocalError(it))
                }
            }
        ))
        return result.receive()
    }

    /**
     * 刷新token
     *
     * @return Flow<HttpAction<Any>>
     */
    fun refreshToken(): Flow<HttpAction<RefreshTokenResponse>> =
        httpService.replaceTokenWithPhoneID(PhoneIDUtils.phoneUniqueId).mapActionFlow()

    /**
     * 获取默认头像列表
     *
     * @return UserInfo 用户信息
     */
    suspend fun getListHead(): RespResult<ListHeadResp> {
        val result = Channel<RespResult<ListHeadResp>>(1)
        httpService.listHeadUrl(typeSubscriber<ListHeadResp>(
            onSuccess = {
                result.trySend(RespResult.Success(it))
            },
            onFail = {
                if (it is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(it.code, it.msg))
                } else {
                    result.trySend(RespResult.LocalError(it))
                }
            }
        ))
        return result.receive()
    }

    suspend fun changeNickName(nick: String): RespResult<Any> {
        val result = Channel<RespResult<Any>>(1)
        httpService.changeNickName(nick, PhoneIDUtils.phoneUniqueId, typeSubscriber<Any>(
            onSuccess = {
                result.trySend(RespResult.Success(it))
            },
            onFail = {
                if (it is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(it.code, it.msg))
                } else {
                    result.trySend(RespResult.LocalError(it))
                }
            }
        ))
        return result.receive()
    }

    suspend fun changeAvatar(headUrl: String): RespResult<Any> {
        val result = Channel<RespResult<Any>>(1)
        httpService.changeAvatar(headUrl, PhoneIDUtils.phoneUniqueId, typeSubscriber<Any>(
            onSuccess = {
                result.trySend(RespResult.Success(it))
            },
            onFail = {
                if (it is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(it.code, it.msg))
                } else {
                    result.trySend(RespResult.LocalError(it))
                }
            }
        ))
        return result.receive()
    }

    suspend fun modifyPwd(oldPwd: String, newPwd: String): RespResult<Any> {
        val result = Channel<RespResult<Any>>(1)
        val oldPwdMD5 = HttpUtils.md5(oldPwd)
        val newPwdMD5 = HttpUtils.md5(newPwd)
        httpService.userModifyPwd(
            oldPwdMD5,
            newPwdMD5,
            PhoneIDUtils.phoneUniqueId,
            typeSubscriber<Any>(
                onSuccess = {
                    result.trySend(RespResult.Success(it))
                },
                onFail = {
                    if (it is ResponseNotSuccessException) {
                        result.trySend(RespResult.ServerError(it.code, it.msg))
                    } else {
                        result.trySend(RespResult.LocalError(it))
                    }
                }
            ))
        return result.receive()
    }

    suspend fun accountBind(
        type: Int,
        mobileArea: String?,
        mobile: String?,
        email: String?,
        vcode: String
    ): RespResult<Any> {
        val result = Channel<RespResult<Any>>(1)
        httpService.accountBind(
            type.toString(),
            mobileArea,
            mobile,
            email,
            "",
            vcode,
            typeSubscriber<Any?>(
                onSuccess = {
                    result.trySend(RespResult.Success(it))
                },
                onFail = {
                    if (it is ResponseNotSuccessException) {
                        result.trySend(RespResult.ServerError(it.code, it.msg))
                    } else {
                        result.trySend(RespResult.LocalError(it))
                    }
                }
            )
        )
        return result.receive()
    }

    suspend fun logout(terminalId: String): RespResult<Any> {
        val result = Channel<RespResult<Any>>(1)
        httpService.userLogout(terminalId, typeSubscriber<Any>(
            onSuccess = {
                result.trySend(RespResult.Success(it))
            },
            onFail = {
                if (it is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(it.code, it.msg))
                } else {
                    result.trySend(RespResult.LocalError(it))
                }
            }
        ))
        return result.receive()
    }

    suspend fun closeAccount(
        pwd: String,
        type: Int,
        reasonType: Int,
        reason: String
    ): RespResult<Any> {
        val result = Channel<RespResult<Any>>(1)
        val pwdMD5 = HttpUtils.md5(pwd)
        httpService.unregist(
            pwdMD5,
            type,
            reasonType,
            reason,
            typeSubscriber<Any>(
                onSuccess = {
                    result.trySend(RespResult.Success(it))
                },
                onFail = {
                    if (it is ResponseNotSuccessException) {
                        result.trySend(RespResult.ServerError(it.code, it.msg))
                    } else {
                        result.trySend(RespResult.LocalError(it))
                    }
                }
            )
        )
        return result.receive()
    }
}