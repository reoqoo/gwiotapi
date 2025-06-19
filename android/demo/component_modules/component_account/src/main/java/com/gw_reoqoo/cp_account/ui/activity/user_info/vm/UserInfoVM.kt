package com.gw_reoqoo.cp_account.ui.activity.user_info.vm

import android.app.Application
import android.util.SparseArray
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.cp_account.api.kapi.IAccountMgrApi
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.cp_account.entity.ParamConstants.TYPE_EMAIL
import com.gw_reoqoo.cp_account.entity.ParamConstants.TYPE_MOBILE
import com.gw_reoqoo.cp_account.repository.AccountRepository
import com.gw_reoqoo.cp_account.repository.UserInfoRepository
import com.gw_reoqoo.cp_account.utils.DistrictCodeListManager
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.isEmptyOrZero
import com.gw_reoqoo.lib_utils.text.RegularUtils
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/13 14:52
 * Description: UserInfoVM
 */
@HiltViewModel
class UserInfoVM @Inject constructor() : ABaseVM() {

    companion object {
        private const val TAG = "UserInfoVM"

        const val FINISH_ACTIVITY_CODE = 100
    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var accountRepo: AccountRepository

    @Inject
    lateinit var userInfoRepo: UserInfoRepository

    @Inject
    lateinit var pluginMgr: IPluginManager

    @Inject
    lateinit var accountMgrApi: IAccountMgrApi

    @Inject
    lateinit var manager: DistrictCodeListManager

    val userInfo = MutableLiveData<UserInfo?>()

    val defaultAvatar = MutableLiveData<List<String>>()

    /**
     * 获取地区名称
     */
    private val districtNameLiveData = MutableLiveData<String?>()

    fun getDistrictName(): LiveData<String?> = districtNameLiveData

    fun watchUserInfo(owner: LifecycleOwner) {
        userInfoRepo.watchUserInfo().observe(owner) {
            it?.let {
                userInfo.postValue(it)
            }
        }
    }

    /**
     * 获取用户信息
     */
    fun getUserDetail() {
        viewModelScope.launch(Dispatchers.IO) {
            userInfoRepo.updateUserInfo()
        }
    }

    /**
     * 获取默认头像列表
     */
    fun getDefaultAvatar() {
        viewModelScope.launch(Dispatchers.IO) {
            userInfoRepo.getDefaultAvatars()
                .onSuccess {
                    GwellLogUtils.i(TAG, "getDefaultAvatars $this")
                    this?.defaultList?.map { _head ->
                        _head.url
                    }?.run {
                        GwellLogUtils.i(TAG, "avatars: $this")
                        defaultAvatar.postValue(this)
                    }
                }

                .onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "onServerError, code $code, msg $msg")
                }

                .onLocalError {
                    GwellLogUtils.e(TAG, "onLocalError, ${it.message}")
                }
        }
    }

    /**
     * 修改昵称
     *
     * @param nick String 昵称
     */
    fun changeNickName(nick: String) {
        viewModelScope.launch {
            userInfoRepo.changeNickName(nick)?.let {
                userInfoRepo.updateUserInfo()
            }
        }
    }

    /**
     * 账户绑定手机号
     */
    fun goBindMobile() {
        viewModelScope.launch(Dispatchers.IO) {
            val account = accountRepo.getLocalUserInfo()?.phone
            if (account.isNullOrEmpty()) {
                // 绑定手机
                pageJumpData.postValue(
                    PageJumpData(
                        TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_BIND_ACCOUNT)
                            .withInt(ParamConstants.PARAM_BIND_TYPE, TYPE_MOBILE)
                            .withString(ParamConstants.PARAM_ACCOUNT_INFO, ""),
                        requestCode = FINISH_ACTIVITY_CODE
                    )
                )
            } else {
                // 更换手机
                pageJumpData.postValue(
                    PageJumpData(
                        TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_SHOW_ACCOUNT)
                            .withInt(ParamConstants.PARAM_BIND_TYPE, TYPE_MOBILE)
                            .withString(ParamConstants.PARAM_ACCOUNT_INFO, account),
                        requestCode = FINISH_ACTIVITY_CODE
                    )
                )
            }
        }
    }

    /**
     * 账户绑定邮箱
     */
    fun goBindEmail() {
        viewModelScope.launch(Dispatchers.IO) {
            val account = accountRepo.getLocalUserInfo()?.email
            GwellLogUtils.i(TAG, "email $account")
            if (account.isNullOrEmpty()) {
                // 绑定邮箱
                pageJumpData.postValue(
                    PageJumpData(
                        TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_BIND_ACCOUNT)
                            .withInt(ParamConstants.PARAM_BIND_TYPE, TYPE_EMAIL)
                            .withString(ParamConstants.PARAM_ACCOUNT_INFO, ""),
                        requestCode = FINISH_ACTIVITY_CODE
                    )
                )
            } else {
                // 更换邮箱
                pageJumpData.postValue(
                    PageJumpData(
                        TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_SHOW_ACCOUNT)
                            .withInt(ParamConstants.PARAM_BIND_TYPE, TYPE_EMAIL)
                            .withString(ParamConstants.PARAM_ACCOUNT_INFO, account),
                        requestCode = FINISH_ACTIVITY_CODE
                    )
                )
            }
        }
    }

    /**
     * 用户退出登录
     */
    fun userLogout() {
        accountMgrApi.logout()
    }

    /**
     * 账户注销原因
     *
     * @return SparseArray<String> 数据
     */
    fun getReasonsTypeList(): SparseArray<String> {
        val reasons = SparseArray<String>()
        reasons.put(1, app.getString(RR.string.AA0311))
        reasons.put(2, app.getString(RR.string.AA0312))
        reasons.put(4, app.getString(RR.string.AA0313))
        reasons.put(8, app.getString(RR.string.AA0314))
        reasons.put(0, app.getString(RR.string.AA0315))
        return reasons
    }

    /**
     * 获取展示的注册地
     *
     * @param area String 国家简码
     * @return String 注册地
     */
    fun getDistrictByCode(area: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val district = manager.getDistrictCodeInfo(area)?.districtName
            if (district.isNullOrEmpty()) {
                GwellLogUtils.e(TAG, "getDistrict fail, area is $area")
            } else {
                districtNameLiveData.postValue(district)
            }
        }
    }

    /**
     * 头像更新
     *
     * @param url String 头像地址
     */
    fun updateUserAvatar(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userAvatar = userInfoRepo.changeAvatar(url)
            if (userAvatar.isEmptyOrZero()) {
                toastIntentData.postValue(ToastIntentData(RR.string.AA0494))
            } else {
                userInfo.value?.run {
                    headUrl = url
                    userInfoRepo.updateUserInfo(this)
                }
                userInfo.postValue(userInfoRepo.getLocalUserInfo())
            }
        }
    }

    /**
     * 昵称是否符合规范
     *
     * @param input String 昵称
     * @return Boolean true 符合规范，false 不符合规范
     */
    fun isConformNickName(input: String): Boolean {
        if (RegularUtils.hasChinesePunctuation(input)) {
            return false
        }
        if (RegularUtils.hasEnglishPunctuation(input)) {
            return false
        }
        if (RegularUtils.hasEmojis(input)) {
            return false
        }
        return true
    }

}