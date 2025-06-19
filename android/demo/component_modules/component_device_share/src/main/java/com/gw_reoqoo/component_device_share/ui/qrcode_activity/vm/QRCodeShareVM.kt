package com.gw_reoqoo.component_device_share.ui.qrcode_activity.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_device_share.data.rspository.DevShareRepository
import com.gw_reoqoo.lib_base_architecture.ToastIntentData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.Guest
import com.gw_reoqoo.lib_http.entities.ShareContent
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.concurrent.timer

/**
 * @Description: - 面对面二维码分享设备界面VM
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 */
@HiltViewModel
class QRCodeShareVM @Inject constructor(
    private val repository: DevShareRepository,
) : ABaseVM() {

    companion object {
        private const val TAG = "QRCodeShareVM"
    }

    /**
     * 指定设备已分享用户
     */
    private val _nearShareUser = MutableLiveData<List<Guest>>()
    val nearShareUser: LiveData<List<Guest>> get() = _nearShareUser

    /**
     * 加载指定设备已分享用户
     */
    fun listGuest(deviceId: String) {
        viewModelScope.launch {
            val flow = repository.listGuest(deviceId)
            flow.collect { action ->
                when (action) {
                    is HttpAction.Loading -> {}
                    is HttpAction.Success -> {
                        _nearShareUser.postValue(action.data?.guestList ?: emptyList())
                    }

                    is HttpAction.Fail -> {
                        _nearShareUser.postValue(emptyList())
                    }
                }
            }

        }
    }

    /**
     * 分享内容
     * @param pair 第一个参数是，表示是否是用户触发，第二个是携带的数据
     */
    private val _shareContent = MutableLiveData<Pair<Boolean, HttpAction<ShareContent>>>()

    /**
     * 分享内容
     * @param pair 第一个参数是，表示是否是用户触发，第二个是携带的数据
     */
    val shareContent: LiveData<Pair<Boolean, HttpAction<ShareContent>>> get() = _shareContent

    private var timeAtJob: Job? = null

    /**
     * 加载分享内容
     * @param deviceId 设备ID
     * @param forced 强制
     * @param fromClick 是否是用户点击触发的
     */
    fun loadShareContent(
        deviceId: String,
        forced: Boolean = false,
        fromClick: Boolean = false
    ) {
        viewModelScope.launch {
            val flow = repository.getShareContent(deviceId, forced)
            flow.collect { action ->
                _shareContent.postValue(fromClick to action)
                if (action is HttpAction.Success) {
                    val content = action.data
                    if (content != null) {
                        val expireTime = content.expireTime * 1000L
                        val now = Date().time
                        val timeDiff = expireTime - now
                        GwellLogUtils.i(
                            TAG,
                            "loadShareContent-expireTime:$expireTime,now:$now,timeDiff:$timeDiff"
                        )
                        if (timeDiff > 0) {
                            timeAtJob?.cancel()
                            timeAtJob = launch {
                                delay(timeDiff)
                                loadShareContent(deviceId, true)
                            }
                        }
                    }
                }
            }
        }
    }
}