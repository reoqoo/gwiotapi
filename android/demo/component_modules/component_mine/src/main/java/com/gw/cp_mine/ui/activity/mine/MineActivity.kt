package com.gw.cp_mine.ui.activity.mine

import androidx.lifecycle.ViewModel
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineActivityHomeBinding
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/9 15:46
 * Description: MineActivity
 */
class MineActivity : ABaseMVVMDBActivity<MineActivityHomeBinding, MineVM>() {
    override fun getLayoutId(): Int = R.layout.mine_activity_home

    override fun initView() {
    }

    override fun <T : ViewModel?> loadViewModel() = MineVM::class.java as Class<T>
}