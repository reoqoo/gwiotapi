package com.gw.cp_mine.ui.activity.settings

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineActivitySettingsBinding
import com.gw.cp_mine.entity.MenuListEntity
import com.gw.cp_mine.ui.fragment.mine.adapter.MenuListAdapter
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 10:00
 * Description: SettingsActivity
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MinePath.ACTIVITY_SETTINGS)
class SettingsActivity : ABaseMVVMDBActivity<MineActivitySettingsBinding, SettingsVM>() {

    companion object {
        private const val TAG = "SettingsActivity"
    }

    private var adapter = MenuListAdapter()

    override fun getLayoutId() = R.layout.mine_activity_settings

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener { finish() }
        mViewBinding.rvSettingList.layoutManager = LinearLayoutManager(this)
        adapter = MenuListAdapter().also {
            it.setOnItemClickListener(object : MenuListAdapter.ItemClickListener {
                override fun onItemClick(item: MenuListEntity) {
                    item.routerPath.let { _path ->
                        if (_path.isNotEmpty()) {
                            mViewModel.jumpToNext(_path)
                            GwellLogUtils.i(TAG, "MenuListAdapter: start jump")
                        }
                    }
                }
            })
        }
        mViewBinding.rvSettingList.adapter = adapter
        adapter.updateData(mViewModel.getItems())
    }

    override fun <T : ViewModel?> loadViewModel() = SettingsVM::class.java as Class<T>

    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

}