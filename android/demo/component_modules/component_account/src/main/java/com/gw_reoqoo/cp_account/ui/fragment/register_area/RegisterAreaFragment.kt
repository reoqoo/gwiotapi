package com.gw_reoqoo.cp_account.ui.fragment.register_area

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentRegisterAreaBinding
import com.gw_reoqoo.cp_account.ui.fragment.ShareVM
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import com.gwell.loglibs.GwellLogUtils
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:46
 * Description: 注册地选择页
 */
@AndroidEntryPoint
class RegisterAreaFragment : ABaseMVVMDBFragment<AccountFragmentRegisterAreaBinding, RegisterAreaVM>() {

    companion object {
        private const val TAG = "RegisterAreaFragment"
    }

    private val shareVM: ShareVM by activityViewModels()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.appTitle.run { 
            leftIcon.setOnClickListener { findNavController().navigateUp() }
        }

        mViewBinding.tvRegisterArea.setOnClickListener {
            mFgViewModel.mDistrictBean.value?.let {
                val action = RegisterAreaFragmentDirections.accountActionAccountRegisterareafragmentToAccountArealistfragment(it)
                findNavController().navigate(action)
            }
        }

        mViewBinding.btnSure.setOnClickListener {
            mFgViewModel.mDistrictBean.value?.let {
                val action = RegisterAreaFragmentDirections.accountActionAccountRegisterareafragmentToAccountAccountinputfragment(it, AccountInputType.ACCOUNT_REGISTER)
                findNavController().navigate(action)
            }
        }

    }

    override fun initData() {
        super.initData()

        arguments?.let {
            val entity = RegisterAreaFragmentArgs.fromBundle(it).keyDistrictBean
            mFgViewModel.mDistrictBean.postValue(entity)
        }
    }

    override fun initLiveData(viewModel: RegisterAreaVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        shareVM.districtLD.observe(viewLifecycleOwner) {
            mFgViewModel.mDistrictBean.postValue(it)
        }

        mFgViewModel.mDistrictBean.observe(this) {
            GwellLogUtils.i(TAG, "initLiveData: mDistrictBean = $it")
            it?.let {
                mViewBinding.tvRegisterArea.text = it.districtName
            }
        }
    }

    override fun getLayoutId() = R.layout.account_fragment_register_area

    override fun <T : ViewModel?> loadViewModel() = RegisterAreaVM::class.java as Class<T>

}