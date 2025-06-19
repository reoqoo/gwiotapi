package com.gw.cp_mine.ui.activity.settings.sys_permission

import android.app.Application
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
class SysPermissionSettingVM @Inject constructor(
    private val app: Application
) : ABaseVM() {

    companion object {
        private const val TAG = "SysPermissionSettingVM"
    }

    /**
     * 列表数据
     */
    val list: MutableLiveData<List<SettingsEntity>>
        get() = _list

    /**
     * 列表数据
     */
    private val _list = MutableLiveData<List<SettingsEntity>>()

    init {
        initSettingsList()
    }

    fun initSettingsList() {
        val settingsList = mutableListOf<SettingsEntity>()
        val settingEntity = SettingsEntity(
            title = RR.string.AA0619,
            description = RR.string.AA0620,
            itemType = SettingsItemType.LOCATION,
            status = if (PermissionUtil.hasLocationPermissions(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(settingEntity)
        val bluetooth = SettingsEntity(
            title = RR.string.AA0621,
            description = RR.string.AA0620,
            itemType = SettingsItemType.BLUETOOTH,
            status = if (PermissionUtil.hasBluetoothPermissions(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(bluetooth)
        val storage = SettingsEntity(
            title = RR.string.AA0622,
            description = RR.string.AA0623,
            itemType = SettingsItemType.STORAGE,
            status = if (PermissionUtil.hasStoragePermissions(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(storage)
        val camera = SettingsEntity(
            title = RR.string.AA0546,
            description = RR.string.AA0626,
            itemType = SettingsItemType.CAMERA,
            status = if (PermissionUtil.hasCameraPermission(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(camera)
        val album = SettingsEntity(
            title = RR.string.AA0064,
            description = RR.string.AA0623,
            itemType = SettingsItemType.ALBUM,
            status = if (PermissionUtil.hasStoragePermissions(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(album)
        val microphone = SettingsEntity(
            title = RR.string.AA0627,
            description = RR.string.AA0628,
            itemType = SettingsItemType.MICROPHONE,
            status = if (PermissionUtil.hasMicrophonePermission(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(microphone)
        val floatingWindow = SettingsEntity(
            title = RR.string.AA0629,
            description = RR.string.AA0630,
            itemType = SettingsItemType.FLOATING_WINDOW,
            status = if (PermissionUtil.hasOverlayPermission(app)) {
                1
            } else {
                0
            }
        )
        settingsList.add(floatingWindow)
        val backgroundPop = SettingsEntity(
            title = RR.string.AA0631,
            description = RR.string.AA0632,
            itemType = SettingsItemType.BACKGROUND_POP,
            status = -1
        )
        settingsList.add(backgroundPop)
        _list.postValue(settingsList)
    }

}