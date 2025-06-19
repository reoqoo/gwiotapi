package com.gw_reoqoo.cp_account.datasource

import android.content.Context
import androidx.lifecycle.LiveData
import com.gw_reoqoo.cp_account.kits.AccountMgrKit
import com.gw_reoqoo.lib_room.user.UserDao
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/7 16:57
 * Description: IAccountLocalData
 */
class LocalUserDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao
) {

    companion object {
        private const val TAG = "LocalUserDataSource"
    }

    /**
     * 主动获取当前用户的信息
     *
     * @return UserInfo 用户信息
     */
    suspend fun queryAccount(): UserInfo? {
        val userList = userDao.getAllUser().first()
        return if (userList.isEmpty()) {
            null
        } else {
            userList[0]
        }
    }

    /**
     * 监听当前用户的信息
     *
     * @return LiveData<UserInfo?> 用户信息
     */
    fun watchUserInfo(): LiveData<UserInfo?> {
        return userDao.getUserLiveDataForFirst()
    }

    /**
     * userinfo的监听
     *
     * @param userId String 用户Id
     * @return LiveData<UserInfo?> 用户信息
     */
    fun watchUserInfo(userId: String): LiveData<UserInfo?> {
        return userDao.getUserLiveDataById(userId)
    }

    /**
     * 更新用户信息
     *
     * @param userInfo UserInfo 用户信息
     */
    suspend fun updateAccount(userInfo: UserInfo) {
        AccountMgrKit.setAccessInfo(userInfo.accessId, userInfo.accessToken)
        userDao.insertUser(userInfo)
    }

    /**
     * 用户注册地的监听
     *
     * @return LiveData<String?> 用户注册地
     */
    fun getUserArea(): LiveData<String?> {
        return userDao.getUserArea()
    }

    fun cleanAccount() {
        GwellLogUtils.i(TAG, "cleanAccount")
        userDao.deleteAllUsers()
    }

}