package com.gw.cp_mine.ui.activity.settings.msg_push

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineActivitySettingMsgPushBinding
import com.gw.cp_mine.ui.activity.settings.SettingsAdapter
import com.gw.cp_mine.entity.SettingsEntity
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.permission.PermissionUtil
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 11:20
 * Description: PushSettingActivity
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MinePath.ACTIVITY_SETTING_MSG_PUSH)
class MsgPushSettingActivity :
    ABaseMVVMDBActivity<MineActivitySettingMsgPushBinding, MsgPushSettingVM>() {

    private var mAdapter: SettingsAdapter? = null

    override fun getLayoutId() = R.layout.mine_activity_setting_msg_push

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener { finish() }
        mViewBinding.rvSettingList.layoutManager = LinearLayoutManager(this)
        mAdapter = SettingsAdapter(
            listOf(),
            object : SettingsAdapter.ItemClickListener {
                override fun onItemClick(item: SettingsEntity) {
                    PermissionUtil.openNotificationSettings(this@MsgPushSettingActivity)
                }
            })
        mViewBinding.rvSettingList.adapter = mAdapter
    }

    override fun initLiveData(viewModel: MsgPushSettingVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.settingList.observe(this) {
            mAdapter?.updateData(it)
        }
    }

    override fun onRestart() {
        super.onRestart()
        mViewModel.settingsStatusCheck()
    }

    override fun <T : ViewModel?> loadViewModel() = MsgPushSettingVM::class.java as Class<T>

    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

}