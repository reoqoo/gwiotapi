package com.gw.cp_mine.ui.activity.settings.msg_push

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw.cp_mine.entity.SettingsEntity
import com.gw.cp_mine.entity.SettingsItemType
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_utils.permission.PermissionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 11:20
 * Description: PushSettingVM
 */
@HiltViewModel
class MsgPushSettingVM @Inject constructor(
    private val app: Application
) : ABaseVM() {

    companion object {
        private const val TAG = "MsgPushSettingVM"
    }

    private var _settingList = MutableLiveData<List<SettingsEntity>>()

    val settingList: LiveData<List<SettingsEntity>> get() = _settingList

    init {
        settingsStatusCheck()
    }

    fun settingsStatusCheck() {
        val settingList = listOf(
            SettingsEntity(
                title = RR.string.AA0615,
                description = RR.string.AA0616,
                status = if (PermissionUtil.isNotificationsEnabled(app)) {
                    1
                } else {
                    0
                },
                itemType = SettingsItemType.PUSH
            )
        )
        _settingList.postValue(settingList)
    }

}