package com.gw.cp_mine.ui.activity.settings.sys_permission

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineActivitySettingSysPermissionBinding
import com.gw.cp_mine.entity.SettingsEntity
import com.gw.cp_mine.entity.SettingsItemType
import com.gw.cp_mine.ui.activity.settings.SettingsAdapter
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.permission.PermissionPageManagement
import com.gw_reoqoo.lib_utils.permission.PermissionUtil
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 11:20
 * Description: PushSettingActivity
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MinePath.ACTIVITY_SETTING_SYSTEM_PERMISSION)
class SysPermissionSettingActivity :
    ABaseMVVMDBActivity<MineActivitySettingSysPermissionBinding, SysPermissionSettingVM>() {

    private var mAdapter: SettingsAdapter? = null

    override fun getLayoutId() = R.layout.mine_activity_setting_sys_permission

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener { finish() }
        mViewBinding.rvSettingList.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingsAdapter(
            listOf(),
            object : SettingsAdapter.ItemClickListener {
                override fun onItemClick(item: SettingsEntity) {
                    when (item.itemType) {
                        SettingsItemType.LOCATION,
                        SettingsItemType.BLUETOOTH,
                        SettingsItemType.STORAGE,
                        SettingsItemType.BACKGROUND_RUN,
                        SettingsItemType.CAMERA,
                        SettingsItemType.ALBUM,
                        SettingsItemType.MICROPHONE -> {
                            PermissionUtil.goToAppSetting(this@SysPermissionSettingActivity)
                        }

                        SettingsItemType.FLOATING_WINDOW -> {
                            PermissionUtil.goFloatingWindowPermissionSetting(this@SysPermissionSettingActivity)
                        }

                        SettingsItemType.BACKGROUND_POP -> {
                            PermissionPageManagement.goToSetting(this@SysPermissionSettingActivity)
                        }

                        else -> {
                            PermissionUtil.goToAppSetting(this@SysPermissionSettingActivity)
                        }
                    }
                }
            })
        mViewBinding.rvSettingList.adapter = mAdapter
    }

    override fun initLiveData(viewModel: SysPermissionSettingVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.list.observe(this) {
            mAdapter?.updateData(it)
        }
    }

    override fun onRestart() {
        super.onRestart()
        mViewModel.initSettingsList()
    }

    override fun <T : ViewModel?> loadViewModel() = SysPermissionSettingVM::class.java as Class<T>

    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

}