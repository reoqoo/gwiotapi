package com.gw_reoqoo.cp_account.ui.fragment.bind_verify_code

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentVerifyCodeBinding
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.ui.fragment.bind_verify_code.bind_verify.BindVerifyCodeVM
import com.gw_reoqoo.cp_account.widget.verify_code.InputCompleteListener
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.activity_utils.ActivityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:57
 * Description: 验证码校验页
 */
@AndroidEntryPoint
class BindVerifyCodeFragment :
    ABaseMVVMDBFragment<AccountFragmentVerifyCodeBinding, BindVerifyCodeVM>() {

    companion object {
        private const val TAG = "VerifyCodeFragment"
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        // title初始化
        mViewBinding.layoutTitle.setTitleListener(object :
            WidgetCommonTitleView.OnTitleClickListener {
            override fun onLeftClick() {
                findNavController().navigateUp()
            }

            override fun onRightClick() {
            }
        })

        mViewBinding.etCheckCode.inputCompleteListener = object : InputCompleteListener {
            override fun inputComplete() {
                val verifyCode = mViewBinding.etCheckCode.getEditContent()
                verifyCode?.let {
                    mFgViewModel.checkVerifyCode(it)
                }
            }

            override fun invalidContent() {
                GwellLogUtils.i(TAG, "inputCompleteListener: invalidContent")
            }

        }

        mViewBinding.btnRepeatSend.setOnClickListener {
            mFgViewModel.accountNum?.let {
                if (mFgViewModel.registerType == AccountRegisterType.TYPE_MOBILE) {
                    mFgViewModel.getCodeByPhone(it)
                } else {
                    mFgViewModel.getCodeByEmail(it)
                }
            } ?: GwellLogUtils.e(TAG, "accountNum is null")
        }
    }

    override fun initData() {
        super.initData()

        arguments?.let {
            mFgViewModel.mDistrictBean = BindVerifyCodeFragmentArgs.fromBundle(it).keyDistrictBean
            mFgViewModel.accountNum = BindVerifyCodeFragmentArgs.fromBundle(it).keyAccount
            mFgViewModel.registerType = BindVerifyCodeFragmentArgs.fromBundle(it).keyRegisterType
        }

        startCounting()

        mFgViewModel.accountNum?.let {
            if (mFgViewModel.registerType == AccountRegisterType.TYPE_MOBILE) {
                mViewBinding.tvVerifyNotice.text = buildString {
                    append(getString(RR.string.AA0035))
                    append(" +")
                    append(mFgViewModel.mDistrictBean?.districtCode)
                    append(" ")
                    append(it)
                }
            } else {
                mViewBinding.tvVerifyNotice.text = buildString {
                    append(getString(RR.string.AA0035))
                    append(" ")
                    append(it)
                }
            }
        } ?: GwellLogUtils.e(TAG, "accountNum is null")
    }

    override fun initLiveData(viewModel: BindVerifyCodeVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        mFgViewModel.repeatSend.observe(this) {
            if (it) {
                startCounting()
            }
        }

        mFgViewModel.jumpData.observe(this) {
            if (mFgViewModel.registerType == AccountRegisterType.TYPE_MOBILE) {
                if (mFgViewModel.hasBindMobile()) {
                    toast.show(RR.string.AA0303)
                } else {
                    toast.show(RR.string.AA0302)
                }
            } else {
                if (mFgViewModel.hasBindEmail()) {
                    toast.show(RR.string.AA0308)
                } else {
                    toast.show(RR.string.AA0307)
                }
            }
            if (ActivityUtils.isActivityUsable(activity)) {
                activity?.run {
                    setResult(RESULT_OK)
                    finish()
                }
            }
            mFgViewModel.finishActivityLD.postValue(true)
        }
    }

    private fun startCounting() {
        mFgViewModel.countDownCoroutine(
            duration = 60,
            interval = 1,
            scope = GlobalScope,
            onTick = {
                mViewBinding.btnRepeatSend.text = buildString {
                    append(getString(com.gw_reoqoo.resource.R.string.AA0036))
                    append("(")
                    append(it)
                    append("s)")
                }
            },
            onStart = {
                mViewBinding.btnRepeatSend.isEnabled = false
                mViewBinding.btnRepeatSend.text = buildString {
                    append(getString(com.gw_reoqoo.resource.R.string.AA0036))
                    append("（60s）")
                }
            },
            onEnd = {
                mViewBinding.btnRepeatSend.isEnabled = true
                mViewBinding.btnRepeatSend.text = getString(com.gw_reoqoo.resource.R.string.AA0036)
            })
    }

    override fun getLayoutId() = R.layout.account_fragment_verify_code

    override fun <T : ViewModel?> loadViewModel() = BindVerifyCodeVM::class.java as Class<T>

}