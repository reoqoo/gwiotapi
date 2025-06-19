package com.gw_reoqoo.cp_account.repository

import androidx.lifecycle.LiveData
import com.gw_reoqoo.cp_account.datasource.LocalUserDataSource
import com.gw_reoqoo.cp_account.datasource.RemoteUserDataSource
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gwell.loglibs.GwellLogUtils
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/7 16:42
 * Description: 用户模块repository
 */
@Singleton
class AccountRepository @Inject constructor(
    private val remoteDataSource: RemoteUserDataSource,
    private val localDataSource: LocalUserDataSource
) {

    companion object {
        private const val TAG = "DeviceRepository"
    }

    /**
     * 本地同步获取用户信息
     *
     * @return UserInfo? 用户信息
     */
    fun getSyncUserInfoFromLocal(): UserInfo? {
        return runBlocking {
            localDataSource.queryAccount()
        }
    }

    suspend fun getLocalUserInfo(): UserInfo? {
        return localDataSource.queryAccount()
    }

    suspend fun updateUserNick(nick: String) {
        localDataSource.queryAccount()?.let {
            it.nickname = nick
            localDataSource.updateAccount(it)
        }
    }

    suspend fun updateLocalUserInfo(userInfo: UserInfo) {
        localDataSource.updateAccount(userInfo)
    }

    suspend fun updateRemoteUserInfo() {
        localDataSource.queryAccount()?.let { userInfo ->
            remoteDataSource.queryUserDetail()
                .onSuccess {
                    this?.let {
                        userInfo.email = it.email
                        userInfo.phone = if (it.mobile.isNullOrEmpty()) "" else it.mobile
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

    /**
     * 用户信息监听
     *
     * @return LiveData<UserInfo?>
     */
    fun watchUserInfo(): LiveData<UserInfo?> {
        return localDataSource.watchUserInfo()
    }

    /**
     * 用户注册地监听
     *
     * @return LiveData<String?>
     */
    fun watchUserArea(): LiveData<String?> {
        return localDataSource.getUserArea()
    }
}

