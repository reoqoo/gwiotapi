package com.gw_reoqoo.cp_account.ui.fragment.bind_account_input

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentBindInputBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.cp_account.entity.ParamConstants.TYPE_EMAIL
import com.gw_reoqoo.cp_account.entity.ParamConstants.TYPE_MOBILE
import com.gw_reoqoo.cp_account.ui.fragment.ShareVM
import com.gw_reoqoo.cp_account.ui.fragment.bind_account_input.vm.AccountBindInputVM
import com.gw.cp_config.api.AppChannelName
import com.gw.cp_config.api.IAppParamApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_utils.ktx.invisible
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.str_utils.GwStringUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:54
 * Description: 账号输入页
 */
@AndroidEntryPoint
class AccountBindInputFragment :
    ABaseMVVMDBFragment<AccountFragmentBindInputBinding, AccountBindInputVM>() {

    companion object {
        private const val TAG = "AccountBindInputFragment"
    }

    @Inject
    lateinit var appParamApi: IAppParamApi

    /**
     * 共享VM，主要处理数据回传的问题
     */
    private val shareVM: ShareVM by activityViewModels()

    private var accountType = TYPE_MOBILE

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        mViewBinding.layoutTitle.setTitleListener(object :
            WidgetCommonTitleView.OnTitleClickListener {
            override fun onLeftClick() {
                activity?.finish()
            }

            override fun onRightClick() {
            }

        })

//        mViewBinding.tvArea.setOnClickListener {
//            val action =
//                AccountBindInputFragmentDirections.toAccountBindarealistfragment(shareVM.districtLD.value)
//            findNavController().navigate(action)
//        }

        mViewBinding.etAccountNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mViewBinding.btnAccountNext.isEnabled = !s.isNullOrEmpty()
                mViewBinding.tvErrNotice.text = ""
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        mViewBinding.btnAccountNext.setOnClickListener {
            val accountString = mViewBinding.etAccountNumber.text.toString()
            GwellLogUtils.i(TAG, "accountString: $accountString")
            if (accountType == TYPE_MOBILE) {
                if (accountString.isEmpty()) {
                    toast.show(RR.string.AA0490)
                    return@setOnClickListener
                }
                if (!GwStringUtils.isMobileNO(accountString)) {
                    toast.show(RR.string.AA0409)
                    return@setOnClickListener
                }
                mFgViewModel.getCodeByPhone(accountString)
            } else {
                if (accountString.isEmpty()) {
                    toast.show(RR.string.AA0491)
                    return@setOnClickListener
                }
                if (!GwStringUtils.isEmail(accountString)) {
                    toast.show(RR.string.AA0267)
                    return@setOnClickListener
                }
                mFgViewModel.getCodeByEmail(accountString)
            }
        }
    }

    override fun initData() {
        super.initData()
        accountType = arguments?.getInt(ParamConstants.PARAM_BIND_TYPE)
            ?: TYPE_MOBILE
        GwellLogUtils.i(TAG, "accountType: $accountType")

        shareVM.initDistrictInfo(AppChannelName.isXiaotunApp(appParamApi.getAppName()))

        when (accountType) {
            TYPE_MOBILE -> {
                mViewBinding.tvAccountTitle.text = getString(RR.string.AA0299)
                mViewBinding.etAccountNumber.hint = getString(RR.string.AA0299)
                mViewBinding.tvArea.invisible(AppChannelName.isXiaotunApp(appParamApi.getAppName()))
            }

            TYPE_EMAIL -> {
                mViewBinding.tvAccountTitle.text = getString(RR.string.AA0305)
                mViewBinding.etAccountNumber.hint = getString(RR.string.AA0305)
                mViewBinding.tvArea.visibility = View.GONE
            }
        }

    }

    override fun initLiveData(viewModel: AccountBindInputVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        shareVM.districtLD.observe(viewLifecycleOwner) {
            GwellLogUtils.i(TAG, "codeEntity = $it")
            mFgViewModel.mDistrictBean = it
            mViewBinding.tvArea.text = it.districtName
        }

        mFgViewModel.errorNotice.observe(this) {
            GwellLogUtils.i(TAG, "errorNotice: it = $it")
            mViewBinding.tvErrNotice.text = getString(it)
        }

        mFgViewModel.jumpVerifyFragment.observe(this) {
            if (it) {
                val account = mViewBinding.etAccountNumber.text?.toString()
                val district = mFgViewModel.mDistrictBean
                val registerType = mFgViewModel.registerType
                if (account.isNullOrEmpty() || district == null || registerType == null) {
                    GwellLogUtils.e(
                        TAG,
                        "account $account, district $district, registerType $registerType"
                    )
                    return@observe
                }
                val action = AccountBindInputFragmentDirections.toAccountBindverifycodefragment(
                    account,
                    district,
                    registerType
                )
                findNavController().navigate(action)
                mFgViewModel.jumpVerifyFragment.value = false
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.account_fragment_bind_input
    }

    override fun <T : ViewModel?> loadViewModel(): Class<T> {
        return AccountBindInputVM::class.java as Class<T>
    }

}