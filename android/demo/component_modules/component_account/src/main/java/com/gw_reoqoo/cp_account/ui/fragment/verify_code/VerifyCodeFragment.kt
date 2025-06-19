package com.gw_reoqoo.cp_account.ui.fragment.verify_code

import android.graphics.Rect
import android.os.Bundle
import android.text.TextPaint
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentVerifyCodeBinding
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.entity.AccountRegisterType
import com.gw_reoqoo.cp_account.sa.AccountSaEvent
import com.gw_reoqoo.cp_account.widget.verify_code.InputCompleteListener
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import com.gw_reoqoo.resource.R as RR


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:57
 * Description: 验证码校验页
 */
@AndroidEntryPoint
class VerifyCodeFragment : ABaseMVVMDBFragment<AccountFragmentVerifyCodeBinding, VerifyCodeVM>() {

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
            GwellLogUtils.i(TAG, "accountNum ${mFgViewModel.accountNum}")
            mFgViewModel.accountNum?.let {
                SA.track(
                    AccountSaEvent.SEND_VCODE,
                    mapOf(AccountSaEvent.EventAttr.SEND_RESULT to "2")
                )
                mFgViewModel.getVerifyCode(it)
            } ?: GwellLogUtils.e(TAG, "accountNum is null")
        }
    }

    override fun initData() {
        super.initData()

        arguments?.let {
            mFgViewModel.mDistrictBean = VerifyCodeFragmentArgs.fromBundle(it).keyDistrictBean
            mFgViewModel.accountNum = VerifyCodeFragmentArgs.fromBundle(it).keyAccount
            mFgViewModel.fromPage = VerifyCodeFragmentArgs.fromBundle(it).keyFromPage
            mFgViewModel.registerType = VerifyCodeFragmentArgs.fromBundle(it).keyRegisterType
        }

        startCounting()

        mFgViewModel.accountNum?.let { account ->
            val string = if (mFgViewModel.registerType == AccountRegisterType.TYPE_MOBILE) {
                buildString {
                    append(getString(RR.string.AA0035))
                    append(" +")
                    append(mFgViewModel.mDistrictBean?.districtCode)
                    append(" ")
                    append(account)
                }

            } else {
                buildString {
                    append(getString(RR.string.AA0035))
                    append(" ")
                    append(account)
                }
            }
            string.let {
                val bounds = Rect()
                val paint: TextPaint = mViewBinding.tvVerifyNotice.paint
                paint.getTextBounds(it, 0, it.length, bounds)
                val width = bounds.width()
                GwellLogUtils.i(TAG, "width $width")
                mViewBinding.tvVerifyNotice.text = if (width > 306.dp) {
                    if (mFgViewModel.registerType == AccountRegisterType.TYPE_MOBILE) {
                        buildString {
                            append(getString(RR.string.AA0035))
                            append("\n+")
                            append(mFgViewModel.mDistrictBean?.districtCode)
                            append(" ")
                            append(account)
                        }
                    }
                    buildString {
                        append(getString(RR.string.AA0035))
                        append("\n")
                        append(account)
                    }
                } else {
                    it
                }
            }
        }
    }

    override fun initLiveData(viewModel: VerifyCodeVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        mFgViewModel.repeatSend.observe(this) {
            if (it) {
                startCounting()
            }
        }

        mFgViewModel.jumpData.observe(this) {
            val accountNum = mFgViewModel.accountNum!!
            val vCode = mFgViewModel.verifyCode!!
            val districtEntity = mFgViewModel.mDistrictBean!!
            val registerType = mFgViewModel.registerType!!
            val inputType = mFgViewModel.fromPage!!
            if (it) {
                when (inputType) {
                    AccountInputType.ACCOUNT_REGISTER -> {
                        val action =
                            VerifyCodeFragmentDirections.accountActionAccountVerifycodefragmentToAccountPwdinputfragment(
                                keyAccount = accountNum,
                                keyVerifyCode = vCode,
                                districtEntity,
                                registerType,
                                inputType
                            )
                        findNavController().navigate(action)
                    }

                    AccountInputType.ACCOUNT_FORGET -> {
                        val action =
                            VerifyCodeFragmentDirections.accountActionAccountVerifycodefragmentToAccountPwdinputfragment(
                                keyAccount = accountNum,
                                keyVerifyCode = vCode,
                                districtEntity,
                                registerType,
                                inputType
                            )
                        findNavController().navigate(action)
                    }
                }
                mFgViewModel.jumpData.value = false
            }
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

    override fun <T : ViewModel?> loadViewModel() = VerifyCodeVM::class.java as Class<T>

}