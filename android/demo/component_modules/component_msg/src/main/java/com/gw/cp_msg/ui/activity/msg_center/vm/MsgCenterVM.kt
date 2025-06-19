package com.gw.cp_msg.ui.activity.msg_center.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw.cp_msg.entity.http.MainNoticeEntity
import com.gw.cp_msg.manger.NoticeMgrImpl
import com.gw.cp_msg.repository.MsgCenterRepository
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/18 10:12
 * Description: MsgCenterVM
 */
@HiltViewModel
class MsgCenterVM @Inject constructor() : ABaseVM() {

    companion object {
        private const val TAG = "MsgCenterVM"
    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var repository: MsgCenterRepository

    @Inject
    lateinit var noticeMgrImpl: NoticeMgrImpl

    val mainNoticeEntityList = MutableLiveData<List<MainNoticeEntity>>()

    fun getNoticeInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            noticeMgrImpl.requestNoticeMsg(false)
            GwellLogUtils.i(TAG, "getNoticeInfo: ${noticeMgrImpl.getNoticeMainNoticeList()}")
            val list = noticeMgrImpl.getNoticeMainNoticeList()
            if (list.isNotEmpty()) {
                mainNoticeEntityList.postValue(list)
            }
        }
    }

}