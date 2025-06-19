package com.gw_reoqoo.component_device_share.ui.share_to_user.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_device_share.data.rspository.DevShareRepository
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.Guest
import com.gw_reoqoo.lib_http.entities.GuestListContent
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Description: - 输入用户账号分享设备VM
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 */
@HiltViewModel
class Share2UserVM @Inject constructor(
    private val repository: DevShareRepository,
) : ABaseVM() {
    companion object {
        private const val TAG = "Share2UserVM"
    }
    
    /**
     * 最近分享的用户数据
     */
    private val _recentlyShareUser = MutableLiveData<List<Guest>>()
    
    /**
     * 访客的账号
     */
    private val _guestAccount = MutableLiveData<Pair<String, String>?>()
    
    /**
     * 分享前检查账号列表
     */
    private val _guestListInfo = MutableLiveData<HttpAction<GuestListContent>>()
    
    /**
     * 分享前检查账号列表
     */
    val guestListInfo: LiveData<HttpAction<GuestListContent>> get() = _guestListInfo
    
    /**
     * 最近分享的用户数据
     */
    val recentlyShareUser: LiveData<List<Guest>> get() = _recentlyShareUser
    
    /**
     * 访客的账号
     */
    val guestAccount: LiveData<Pair<String, String>?> get() = _guestAccount
    
    /**
     * 加载最近分享用户
     */
    fun loadRecentlyShareUser() {
        viewModelScope.launch {
            val nearUsers = repository.getRecentlyShareUser()
            GwellLogUtils.i(TAG, "loadRecentlyShareUser：$nearUsers")
            _recentlyShareUser.postValue(nearUsers)
        }
    }
    
    /**
     * 校验账户是否存在
     * @param guestAccount 用户输入的可能是手机号/邮箱
     */
    fun queryGuestListInfoExits(
        deviceId: String,
        guestAccount: String
    ) {
        val flow = repository.queryGuestInfo(deviceId, guestAccount)
        viewModelScope.launch {
            flow.collect { action ->
                _guestListInfo.postValue(action)
            }
        }
    }
    
    /**
     * 通过账号分享设备
     */
    fun shareGuest(deviceId: String, guestId: String): Flow<HttpAction<Any>> {
        return repository.shareGuest(deviceId, guestId)
    }
    
    /**
     * 根据访客ID，获取访客的账号
     * @param guestId 访客ID
     * @param deviceId 设备ID
     */
    fun loadGuestAccount(deviceId: String, guestId: String) {
        viewModelScope.launch {
            val flow = repository.queryGuestInfo(deviceId, guestId)
            flow.collect { action ->
                when (action) {
                    is HttpAction.Loading -> Unit
                    is HttpAction.Success -> {
                        val account = action.data?.userList?.firstOrNull()?.account
                        if (account != null) {
                            _guestAccount.postValue(guestId to account)
                        } else {
                            _guestAccount.postValue(null)
                        }
                    }
                    
                    is HttpAction.Fail -> {
                        _guestAccount.postValue(null)
                    }
                }
            }
        }
    }
}