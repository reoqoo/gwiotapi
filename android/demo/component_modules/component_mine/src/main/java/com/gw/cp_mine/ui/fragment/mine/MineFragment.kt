package com.gw.cp_mine.ui.fragment.mine

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw.component_website.api.interfaces.IWebsiteApi
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineFragmentMineBinding
import com.gw.cp_mine.entity.MenuListEntity
import com.gw.cp_mine.ui.fragment.mine.adapter.MenuListAdapter
import com.gw.cp_mine.ui.fragment.mine.vm.MineFgVM
import com.gw.cp_mine.ui.fragment.mine.vm.MineFgVM.Companion.MY_COUPONS
import com.gw.cp_mine.ui.fragment.mine.vm.MineFgVM.Companion.MY_ORDERS
import com.gw.cp_upgrade.api.interfaces.IUpgradeMgrApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Route
import com.zackratos.ultimatebarx.ultimatebarx.addStatusBarTopPadding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
@author: xuhaoyuan
@date: 2023/8/18
description:
1.我的页面
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MinePath.FRAGMENT_MAIN)
class MineFragment : ABaseMVVMDBFragment<MineFragmentMineBinding, MineFgVM>() {

    companion object {
        private const val TAG = "MineFragment"
    }

    private var mAdapter: MenuListAdapter? = null

    @Inject
    lateinit var iWebViewApi: IWebViewApi

    @Inject
    lateinit var upgradeApi: IUpgradeMgrApi

    @Inject
    lateinit var websiteApi: IWebsiteApi

    @Inject
    lateinit var familyModeApi: FamilyModeApi

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        mViewBinding.ivMessage.addStatusBarTopPadding()

        mViewBinding.ivMessage.setSingleClickListener {
            mFgViewModel.jumpToNext(ReoqooRouterPath.MsgCenterPath.ACTIVITY_MSG_CENTER)
        }
        mViewBinding.clUserMsg.setSingleClickListener {
            mFgViewModel.jumpToNext(ReoqooRouterPath.AccountPath.ACTIVITY_USER_INFO)
        }
        mViewBinding.ivSetting.setSingleClickListener {
            mFgViewModel.jumpToNext(ReoqooRouterPath.MinePath.ACTIVITY_SETTINGS)
        }
        mViewBinding.btnDevUpgrade.setSingleClickListener {
            mFgViewModel.jumpToNext(ReoqooRouterPath.Family.FAMILY_ACTIVITY_DEVICE_UPDATE)
        }
        mViewBinding.btnAlbum.setSingleClickListener {
            mFgViewModel.startAlbumPage()
        }
        mViewBinding.btnShare.setSingleClickListener {
            mFgViewModel.jumpToNext(ReoqooRouterPath.DevShare.ACTIVITY_SHARE_MANAGER_PATH)
        }
        mViewBinding.btnCloud.setSingleClickListener {
            iWebViewApi.openWebView(websiteApi.getCloudPrivilegeUrl(), getString(RR.string.AA0247))
        }
        mViewBinding.btn4g.setSingleClickListener {
            iWebViewApi.openWebView(websiteApi.get4GPrivilegeUrl(), getString(RR.string.AA0652))
        }
        mViewBinding.rvMenuList.layoutManager = LinearLayoutManager(context)
        mAdapter = MenuListAdapter().also {
            it.setOnItemClickListener(object : MenuListAdapter.ItemClickListener {
                override fun onItemClick(item: MenuListEntity) {
                    item.routerPath.let { _path ->
                        if (_path.isNotEmpty()) {
                            when (_path) {
                                ReoqooRouterPath.MinePath.ACTIVITY_FEEDBACK -> {
                                    iWebViewApi.openWebView(websiteApi.getHelpAndFeedbackUrl(), "")
                                }

                                MY_ORDERS -> {
                                    iWebViewApi.openWebView(
                                        websiteApi.getMyOrderUrl(),
                                        getString(RR.string.AA0653)
                                    )
                                }

                                MY_COUPONS -> {
                                    iWebViewApi.openWebView(
                                        websiteApi.getMyCouponUrl(),
                                        getString(RR.string.AA0654)
                                    )
                                }

                                else -> {
                                    mFgViewModel.jumpToNext(_path)
                                }
                            }
                            GwellLogUtils.i(TAG, "MenuListAdapter: start jump")
                        }
                    }
                }
            })
        }
        mViewBinding.rvMenuList.adapter = mAdapter
    }

    override fun initData() {
        super.initData()
        mAdapter?.updateData(mFgViewModel.initLMenuList())

        mFgViewModel.updateUserInfo()
        mFgViewModel.initUpgradeInfo()
        mFgViewModel.loadBenefits()
    }

    override fun onResume() {
        super.onResume()
        mFgViewModel.updateMsgRedPoint()
        mFgViewModel.checkAreaCloudServer()
        mFgViewModel.checkHas4GDevice()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        GwellLogUtils.i(TAG, "onHiddenChanged $hidden")
        if (!hidden) {
            mFgViewModel.initUpgradeInfo()
            mFgViewModel.loadBenefits()
        }
    }

    override fun initLiveData(viewModel: MineFgVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        viewModel.watchUserInfo(this)

        viewModel.userInfo.observe(this) {
            it?.run {
                GwellLogUtils.i(TAG, "userInfo: ${this.userId}, ${this.nickName}, ${this.headUrl}")
                mViewBinding.tvUserName.text = this.getInsensitiveName()
                mViewBinding.ivHead.loadUrl(
                    imageUrl = headUrl,
                    placeHolder = RR.drawable.gw_reoqoo_icon_default_avatar,
                    error = RR.drawable.gw_reoqoo_icon_default_avatar,
                )
            }
        }

        upgradeApi.hasNewVersionDevice().observe(this) {
            mViewBinding.btnDevUpgrade.setPointVisible(it)
        }

        mFgViewModel.redPointState.observe(this) {
            mViewBinding.viewRedPoint.visible(it)
        }

        mFgViewModel.isSupportCloud.observe(this) {
            mViewBinding.btnCloud.visible(it)
            updateEquityServicesStatus()
        }
        mFgViewModel.isSupport4G.observe(this) {
            mViewBinding.btn4g.visible(it)
            updateEquityServicesStatus()
        }

        mFgViewModel.getUnReadBenefitsCount()?.observe(this) {
            if (it != null) {
                mFgViewModel.redPointState.postValue(it > 0)
            }
        }

        familyModeApi.watchDeviceList(mFgViewModel.getUserId()).observe(this) {
            mFgViewModel.checkAreaCloudServer()
            mFgViewModel.checkHas4GDevice()
            mAdapter?.updateData(mFgViewModel.initLMenuList(it.isNullOrEmpty()))
        }
    }

    override fun getLayoutId(): Int = R.layout.mine_fragment_mine

    override fun <T : ViewModel?> loadViewModel(): Class<T> = MineFgVM::class.java as Class<T>

    /**
     * 更新权益服务状态
     */
    private fun updateEquityServicesStatus() {
        GwellLogUtils.i(TAG, "Cloud: ${mViewBinding.btnCloud.isVisible}, 4g: ${mViewBinding.btn4g.isVisible}")
        if (mViewBinding.btnCloud.isVisible || mViewBinding.btn4g.isVisible) {
            mViewBinding.clEquityServices.visible(true)
        } else {
            mViewBinding.clEquityServices.visible(false)
        }
    }

}