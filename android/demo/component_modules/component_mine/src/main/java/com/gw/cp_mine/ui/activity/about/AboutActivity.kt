package com.gw.cp_mine.ui.activity.about

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.component_website.api.interfaces.IWebsiteApi
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_config.api.AppChannelName
import com.gw.cp_config.api.IAppParamApi
import com.gw.cp_mine.BuildConfig
import com.gw.cp_mine.R
import com.gw.cp_mine.api.kapi.IMineModuleApi
import com.gw.cp_mine.databinding.MineActivityAboutBinding
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.invisible
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.CommDialog
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.InputContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.therouter.TheRouter
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MinePath.ACTIVITY_ABOUT)
class AboutActivity : ABaseMVVMDBActivity<MineActivityAboutBinding, AboutVM>() {

    companion object {
        private const val TAG = "AboutActivity"

        private const val DEBUG_MODE_PWD = "gwell123"

    }

    @Inject
    lateinit var iWebViewApi: IWebViewApi

    @Inject
    lateinit var websiteAPi: IWebsiteApi

    @Inject
    lateinit var mineApi: IMineModuleApi

    @Inject
    lateinit var appParamApi: IAppParamApi

    private var mAdapter: AboutListAdapter? = null

    private var debugDialog: CommDialog? = null

    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

    override fun getLayoutId(): Int = R.layout.mine_activity_about

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener { finish() }
        mViewBinding.tvAppName.text = getString(com.gw_reoqoo.resource.R.string.AA0447)
        mViewBinding.tvAppVersion.text = buildString {
            append(getString(RR.string.AA0537))
            append(" ")
            append(packageManager.getPackageInfo(packageName, 0).versionName)
        }
        mViewBinding.rvAbout.layoutManager = LinearLayoutManager(applicationContext)
        mAdapter = AboutListAdapter(mViewModel.aboutMenuList).also {
            it.setOnItemClickListener { item ->
                when (item) {
                    AboutEnum.VERSION_UPDATE -> {
                        mineApi.appVersionUpgrade(this)
                    }

                    AboutEnum.USER_PROTOCOL -> {
                        iWebViewApi.openWebView(
                            websiteAPi.getUserProtocolUrl(),
                            getString(item.strRes)
                        )
                    }

                    AboutEnum.PRIVACY_POLICY -> {
                        iWebViewApi.openWebView(
                            websiteAPi.getUserPrivacyUrl(),
                            getString(item.strRes)
                        )
                    }
                }
            }
        }
        mViewBinding.rvAbout.adapter = mAdapter

        mViewBinding.imageLogo.setOnClickListener {
            mViewModel.jump2Debug()
        }

        mViewBinding.ivXiaotunLogo.setOnClickListener {
            mViewModel.jump2Debug()
        }

        mViewBinding.tvAppName.invisible(AppChannelName.isXiaotunApp(appParamApi.getAppName()))
        mViewBinding.imageLogo.invisible(AppChannelName.isXiaotunApp(appParamApi.getAppName()))
        mViewBinding.ivXiaotunLogo.visible(AppChannelName.isXiaotunApp(appParamApi.getAppName()))
    }

    override fun <T : ViewModel?> loadViewModel(): Class<T> {
        return AboutVM::class.java as Class<T>
    }

    override fun initLiveData(viewModel: AboutVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.showDebugDialog.observe(this) {
            if (it) {
                if (BuildConfig.DEBUG) {
                    TheRouter.build(ReoqooRouterPath.DebugModePath.ACTIVITY_DEBUG_MODE)
                        .navigation()
                    return@observe
                }
                debugDialog = showCommDialog {
                    var inputText: String? = null
                    content = InputContent(
                        hint = getString(RR.string.enter_debug_pwd),
                        isShowContent = false,
                        onTextChange = { text ->
                            inputText = text
                        })
                    actions = listOf(
                        CommDialogAction(
                            text = getString(RR.string.AA0059),
                        ),
                        CommDialogAction(
                            text = getString(RR.string.AA0018),
                            conformAutoHide = false,
                            onClick = {
                                if (DEBUG_MODE_PWD.equals(inputText, true)) {
                                    debugDialog?.dismiss()
                                    TheRouter.build(ReoqooRouterPath.DebugModePath.ACTIVITY_DEBUG_MODE)
                                        .navigation()
                                } else {
                                    toast.show(getString(RR.string.debug_pwd_error))
                                }
                            },
                        )
                    )
                }
            }
        }
    }

}