package com.gw_reoqoo.cp_account.api.impl

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.cp_account.api.kapi.IUserInfo
import com.gw_reoqoo.cp_account.datasource.RemoteUserDataSource
import com.gw_reoqoo.cp_account.datastore.AccountDataStoreApi
import com.gw_reoqoo.cp_account.kits.AccountMgrKit
import com.gw_reoqoo.cp_account.repository.AccountRepository
import com.gw_reoqoo.lib_iotvideo.IoTVideoMgr
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.RouterParam
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import com.jwkj.base_utils.byte_utils.ByteUtils
import com.jwkj.base_utils.local.LanguageUtils
import com.jwkj.iotvideo.init.IoTVideoInitializerState
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.RouteItem
import com.therouter.router.addRouterReplaceInterceptor
import com.therouter.router.interceptor.RouterReplaceInterceptor
import com.therouter.router.matchRouteMap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/8 8:28
 * Description: AccountApiImpl
 */
@Singleton
class AccountApiImpl @Inject constructor(
    private val app: Application,
    private val repository: AccountRepository,
    private val dataStoreApi: AccountDataStoreApi,
    private val dataSource: RemoteUserDataSource
) : IAccountApi {

    companion object {
        private const val TAG = "AccountApiImpl"
    }

    private val scope by lazy {
        MainScope()
    }

    /**
     * 初始化账户模块（主要是设置登录拦截器）
     */
    override fun initAccount() {
        // 替换拦截，例如未登录跳转到登录界面
        addRouterReplaceInterceptor(object : RouterReplaceInterceptor() {
            override fun replace(routeItem: RouteItem?): RouteItem? {
                // IoTVideo SDK 的注册
                val userInfo = repository.getSyncUserInfoFromLocal()
                // IotVideo注册拦截
                if (ReoqooRouterPath.AppPath.MAIN_ACTIVITY_PATH == routeItem?.path
                    && null != userInfo
                ) {
                    GwellLogUtils.i(TAG, "iotVideo register")
                    SA.init(
                        app,
                        false,
                        LanguageUtils.getLanguage2(app),
                        app.packageManager.getPackageInfo(app.packageName, 0).versionName,
                        SA.AppType.REOQOO
                    )
                    SA.login(userInfo.showId)
                }

                val isNeedLogin = routeItem?.params?.get(RouterParam.PARAM_NEED_LOGIN)?.toBoolean() ?: true
                GwellLogUtils.i(
                    TAG,
                    "initAccount: isNeedLogin $isNeedLogin, loginstate ${isSyncLogin()}"
                )
                return if (isNeedLogin && !isSyncLogin()) {
                    // 拦截跳转到登录界面
                    matchRouteMap(ReoqooRouterPath.AccountPath.LOGIN_ACTIVITY_PATH)
                } else {
                    routeItem
                }
            }
        })

        // 判断token是否需要更新，理论上是24小时更新一次
        scope.launch {
            repository.getSyncUserInfoFromLocal()?.let { userInfo ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - dataStoreApi.getTokenRefreshTime() > DateUtils.DAY_IN_MILLIS) {
                    // 需要更新token
                    dataSource.refreshToken().collect { response ->
                        when (response) {
                            is HttpAction.Success -> {
                                GwellLogUtils.i(TAG, "refreshToken success: response = $response")
                                response.data?.let {
                                    userInfo.accessToken = it.accessToken
                                    userInfo.expireTime = it.expireTime.toString()
                                    repository.updateLocalUserInfo(userInfo)
                                    AccountMgrKit.setAccessInfo(
                                        userInfo.accessId,
                                        it.accessToken
                                    )
                                    dataStoreApi.setTokenRefreshTime(currentTime)
                                } ?: GwellLogUtils.e(TAG, "refreshToken fail: data is null")

                            }

                            is HttpAction.Fail -> {
                                GwellLogUtils.e(TAG, "refreshToken fail: $response")
                            }
                        }
                    }
                } else {
                    GwellLogUtils.i(TAG, "token refresh is less then 24 hours")
                }
            }
        }
    }

    override fun isSyncLogin(): Boolean {
        var isLogin = false
        runBlocking {
            val userInfo = repository.getLocalUserInfo()
            userInfo?.let {
                isLogin = true
                AccountMgrKit.setAccessInfo(it.accessId, it.accessToken)
            } ?: let {
                isLogin = false
            }
        }
        return isLogin
    }

    override suspend fun isAsyncLogin(): Boolean {
        GwellLogUtils.i(TAG, "repository $repository")
        val userInfo = repository.getLocalUserInfo()
        return userInfo?.let {
            AccountMgrKit.setAccessInfo(it.accessId, it.accessToken)
            true
        } ?: false
    }

    override fun getSyncUserInfo(): IUserInfo? {
        val userInfo: UserInfo?
        runBlocking {
            userInfo = repository.getLocalUserInfo()
        }
        return userInfo?.let {
            UserInfoApiImpl(userInfo)
        }
    }

    override suspend fun getAsyncUserInfo(): IUserInfo? {
        return repository.getLocalUserInfo()?.let {
            UserInfoApiImpl(it)
        }
    }

    override suspend fun getRemoteUserInfo() {
        return repository.updateRemoteUserInfo()
    }

    /**
     * 监听用户信息
     *
     * @return LiveData<IUserInfo?> 用户信息
     */
    override fun watchUserInfo(): LiveData<IUserInfo?> {
        return repository.watchUserInfo().map { userInfo ->
            userInfo?.let(::UserInfoApiImpl)
        }
    }

    override fun getSyncUserId(): String? {
        var userId: String?
        runBlocking {
            userId = repository.getLocalUserInfo()?.id
        }
        return userId
    }

    override suspend fun getAsyncUserId(): String? {
        return repository.getLocalUserInfo()?.id
    }

    override fun getIotSdkState(): LiveData<IoTVideoInitializerState>? {
        return IoTVideoMgr.getAppStateLiveData()
    }

    /**
     * 获取用户token
     *
     * @return String? token
     */
    override fun getUserToken(): String? {
        var token: String? = null
        runBlocking {
            repository.getLocalUserInfo()?.run {
                if (sessionId.isNotEmpty()
                    && id.isNotEmpty()
                ) {
                    token =
                        ByteUtils.numToHex32(id.toInt()) + ByteUtils.numToHex32(sessionId.toInt())
                }
            }
        }
        return token
    }

    override fun startUserInfoPage() {
    }

    /**
     * 用户注册地的监听
     *
     * @return LiveData<String?>
     */
    override fun watchUserArea(): LiveData<String?> {
        return repository.watchUserArea()
    }

    /**
     * 获取当前用户的 注册地
     *
     * @return String? 注册地
     */
    override fun getUserArea(): String? {
        return repository.getSyncUserInfoFromLocal()?.area
    }

    override fun setUserInfo(userInfo: UserInfo) {
        scope.launch {
            repository.updateLocalUserInfo(userInfo)
        }
    }

}