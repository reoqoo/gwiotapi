package com.gw_reoqoo.cp_account.ui.fragment.retrieve_pwd

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentRetrievePwdResultBinding
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 17:01
 * Description: 找回密码结果页
 */
@AndroidEntryPoint
class RetrievePwdResultFragment : ABaseMVVMDBFragment<AccountFragmentRetrievePwdResultBinding, RetrievePwdResultVM>() {

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.btnFindSuccess.setOnClickListener {
            findNavController().popBackStack(R.id.account_loginfragment, false)
        }
    }

    override fun initData() {
        super.initData()
    }

    override fun initLiveData(viewModel: RetrievePwdResultVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
    }

    override fun getLayoutId() = R.layout.account_fragment_retrieve_pwd_result

    override fun <T : ViewModel?> loadViewModel() = RetrievePwdResultVM::class.java as Class<T>

}