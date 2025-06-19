package com.gw.cp_msg.ui.fragment.event_benefits.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw.cp_msg.entity.http.Notice
import com.gw.cp_msg.manger.BenefitsMgrImpl
import com.gw.cp_msg.repository.NoticeRepository
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *@message   ActiveMsgVM
 *@user      zouhuihai
 *@date      2022/8/4
 */
@HiltViewModel
class EventBenefitsVM @Inject constructor(
    private val benefitsMgrImpl: BenefitsMgrImpl
) : ABaseVM() {

    companion object {
        private const val TAG = "ActiveMsgVM"
    }

    /**
     * 活动福利列表数据
     */
    val noticeEvent: MutableLiveData<MutableList<Notice>?> = MutableLiveData()

    /**
     * 获取活动福利列表
     */
    fun loadActiveMsg() {
        benefitsMgrImpl.loadBenefits(onStart = {
            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
        }, onSuccess = {
            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
            it?.let { list ->
                if (list.isEmpty()) {
                    noticeEvent.postValue(null)
                } else {
                    noticeEvent.postValue(list.toMutableList())
                }
            }
        }, onFail = {
            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
            GwellLogUtils.e(TAG, "loadActiveMsg fail: error = ${it.message}")
        })
    }
}