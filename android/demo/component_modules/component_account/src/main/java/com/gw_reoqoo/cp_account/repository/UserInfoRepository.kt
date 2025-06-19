package com.gw_reoqoo.cp_account.repository

import androidx.lifecycle.LiveData
import com.gw_reoqoo.cp_account.datasource.LocalUserDataSource
import com.gw_reoqoo.cp_account.datasource.RemoteUserDataSource
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.entities.ListHeadResp
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gw_reoqoo.lib_utils.ktx.isEmptyOrZero
import com.gwell.loglibs.GwellLogUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 2:09
 * Description: UserInfoRepository
 */
@Singleton
class UserInfoRepository @Inject constructor(
    private val localDataSource: LocalUserDataSource,
    private val remoteDataSource: RemoteUserDataSource
) {

    companion object {
        private const val TAG = "UserInfoRepository"
    }


    /**
     * 获取系统默认头像列表数据
     *
     * @return RespResult<ListHeadResp>
     */
    suspend fun getDefaultAvatars(): RespResult<ListHeadResp> {
        return remoteDataSource.getListHead()
    }

    /**
     * 用户信息监听
     *
     * @return LiveData<UserInfo?>
     */
    fun watchUserInfo(): LiveData<UserInfo?> {
        return localDataSource.watchUserInfo()
    }

    suspend fun updateUserInfo() {
        localDataSource.queryAccount()?.let { userInfo ->
            remoteDataSource.queryUserDetail()
                .onSuccess {
                    this?.let {
                        userInfo.email = it.email
                        userInfo.phone = if (it.mobile.isEmptyOrZero()) "" else it.mobile
                        userInfo.nickname = it.nick
                        userInfo.showId = it.showId
                        userInfo.headUrl = it.headUrl
                        userInfo.mobileArea = it.mobileArea
                        localDataSource.updateAccount(userInfo)
                    }
                }
                .onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "UserDetail error: code $code, msg $msg")
                }
                .onLocalError {
                    GwellLogUtils.e(TAG, "UserDetail error: exception ${it.message}")
                }
        }
    }

    suspend fun updateUserInfo(userInfo: UserInfo) {
        localDataSource.updateAccount(userInfo)
    }

    suspend fun getLocalUserInfo(): UserInfo? {
        return localDataSource.queryAccount()
    }

    fun watchUserInfo(userId: String): LiveData<UserInfo?> {
        return localDataSource.watchUserInfo(userId)
    }

    /**
     * 修改用户昵称
     *
     * @param nick String 昵称
     * @return String? 修改后的昵称
     */
    suspend fun changeNickName(nick: String): String? {
        remoteDataSource.changeNickName(nick)
            .onSuccess {
                return nick
            }
            .onServerError { code, msg ->
                GwellLogUtils.e(TAG, "logout fail: code $code, msg $msg")
                return null
            }
            .onLocalError {
                GwellLogUtils.e(TAG, "logout fail: exception ${it.message}")
                return null
            }
        return null
    }

    /**
     * 修改头像
     *
     * @param headUrl String 头像地址
     * @return String? 修改后的头衔地址
     */
    suspend fun changeAvatar(headUrl: String): String? {
        remoteDataSource.changeAvatar(headUrl)
            .onSuccess {
                return headUrl
            }
            .onServerError { code, msg ->
                GwellLogUtils.e(TAG, "logout fail: code $code, msg $msg")
                return null
            }
            .onLocalError {
                GwellLogUtils.e(TAG, "logout fail: exception ${it.message}")
                return null
            }
        return null
    }

    /**
     * 修改密码
     *
     * @param oldPwd String 旧密码
     * @param newPwd String 新密码
     * @return RespResult<Any> 结果回调
     */
    suspend fun modifyPwd(oldPwd: String, newPwd: String): RespResult<Any> {
        return remoteDataSource.modifyPwd(oldPwd, newPwd)
    }

    /**
     * 退出登录
     *
     * @return Boolean 是否成功
     */
    suspend fun userLogout(terminalId: String): Boolean {
        remoteDataSource.logout(terminalId)
        cleanUserInfo()
        return true
    }

    /**
     * 登录失效的情况下，清除用户数据（不复用退出登录，是为了后期业务逻扩展）
     */
    fun loginFailure() {
        cleanUserInfo()
    }

    /**
     * 绑定账号
     *
     * @param type Int            账号类型（1.手机 2.邮箱）
     * @param mobileArea String?  手机地区码
     * @param mobile String?      手机号
     * @param email String?       邮箱
     * @param vcode String        验证码
     * @return RespResult<Any>    回调
     */
    suspend fun accountBind(
        type: Int,
        mobileArea: String?,
        mobile: String?,
        email: String?,
        vcode: String
    ): RespResult<Any> {
        return remoteDataSource.accountBind(type, mobileArea, mobile, email, vcode)
    }

    /**
     * 注销账户
     *
     * @param pwd String       密码
     * @param type Int         账号类型（1.手机 2.邮箱）
     * @param reasonType Int   原因类型
     * @param reason String    原因内容
     * @return RespResult<Any> 回调
     */
    suspend fun closeAccount(
        pwd: String,
        type: Int,
        reasonType: Int,
        reason: String
    ): RespResult<Any> {
        return remoteDataSource.closeAccount(pwd, type, reasonType, reason)
    }

    /**
     * 清除本地账户数据
     */
    fun cleanUserInfo() {
        localDataSource.cleanAccount()
    }

}