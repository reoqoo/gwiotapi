package com.gw.cp_msg.ui.activity.msg_center

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_msg.R
import com.gw.cp_msg.databinding.MsgActivityMsgCenterBinding
import com.gw.cp_msg.manger.BenefitsMgrImpl
import com.gw.cp_msg.ui.activity.msg_center.vm.MsgCenterVM
import com.gw.cp_msg.ui.fragment.event_benefits.EventBenefitsFragment
import com.gw.cp_msg.ui.fragment.system_msg.SystemMsgFragment
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.activity_utils.ActivityUtils
import com.jwkj.base_utils.ui.DensityUtil
import com.gw_reoqoo.widget_webview.jsinterface.WebViewJSCallbackImpl
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/18 10:11
 * Description: 消息中心 - 设备升级的列表
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MsgCenterPath.ACTIVITY_MSG_CENTER)
class MsgCenterActivity : ABaseMVVMDBActivity<MsgActivityMsgCenterBinding, MsgCenterVM>() {

    companion object {
        private const val TAG = "MsgCenterActivity"

        /**
         * 展示系统消息
         */
        const val SHOW_SYSTEM_MSG_TYPE = 1

        /**
         * 展示活动福利
         */
        const val SHOW_ACTIVE_MSG_TYPE = 0
    }

    private val childFragments = arrayListOf<Fragment>()

    /**
     * 系统通知
     */
    private var systemMsgFragment: SystemMsgFragment? = null

    /**
     * 活动福利
     */
    private var eventBenefitsFragment: EventBenefitsFragment? = null

    /**
     * 展示哪种消息
     */
    private var showMsgType = SHOW_SYSTEM_MSG_TYPE

    @Inject
    lateinit var webViewApi: IWebViewApi

    @Inject
    lateinit var benefitsMgrImpl: BenefitsMgrImpl

    override fun initView() {
        mViewBinding.layoutTitle.run {
            leftIcon.setOnClickListener { finish() }
            rightIcon.setOnClickListener {
                systemMsgFragment?.cleanUnreadMsg()
            }
        }

        if (systemMsgFragment == null) {
            systemMsgFragment = SystemMsgFragment.newInstance()
            systemMsgFragment?.let { childFragments.add(it) }
        }
        if (eventBenefitsFragment == null) {
            eventBenefitsFragment = EventBenefitsFragment.newInstance()
            eventBenefitsFragment?.let { childFragments.add(it) }
        }

        // 设置adapter
        mViewBinding.viewpager.adapter = fragmentAdapter
        // 设置viewpage的滑动方向
        mViewBinding.viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        // 设置缓存页
        mViewBinding.viewpager.offscreenPageLimit = childFragments.size

        // 设置选中事件
        mViewBinding.viewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCheckUI(position == 0)
            }
        })

        setCheckUI(SHOW_SYSTEM_MSG_TYPE == showMsgType)
        mViewBinding.clSystemMsg.setOnClickListener {
            mViewBinding.viewpager.currentItem = 0
            setCheckUI(true)
        }
        mViewBinding.clActiveMsg.setOnClickListener {
            mViewBinding.viewpager.currentItem = 1
            setCheckUI(false)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.getNoticeInfo()
    }

    override fun initLiveData(viewModel: MsgCenterVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        viewModel.mainNoticeEntityList.observe(this) {
            val notice = it[0]
            if (ActivityUtils.isActivityUsable(this@MsgCenterActivity)) {
                webViewApi.showWebViewDialog(
                    activity = this@MsgCenterActivity,
                    width = DensityUtil.getScreenWidth(this),
                    height = DensityUtil.getScreenHeight(this),
                    url = notice.url,
                    deviceId = notice.deviceId,
                    callBack = object : WebViewJSCallbackImpl() {
                        /**
                         * 打开其他webview
                         *
                         * @param url webview地址
                         */
                        override fun openWebView(url: String?) {
                            super.openWebView(url)
                            if (url.isNullOrEmpty()) {
                                GwellLogUtils.e(TAG, "openWebView: url is null or empty")
                                return
                            }
                            webViewApi.openWebView(
                                url = url,
                                title = "",
                                deviceId = notice.deviceId
                            )
                        }
                    }
                )
            }
        }

        benefitsMgrImpl.getUnReadBenefitsCount().observe(this) {
            mViewBinding.tvActiveMsgCount.visible(it > 0)
        }
    }

    /**
     * 设置tab上的UI
     * @param showSystemMsg 是否展示系统通知
     */
    private fun setCheckUI(showSystemMsg: Boolean) {
        if (showSystemMsg) {
            mViewBinding.tvSysMsg.setTextColor(resources.getColor(RR.color.black_90))
            mViewBinding.tvSysMsg.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(RR.dimen.sp_18)
            )
            mViewBinding.viewSystemLine.visibility = View.VISIBLE
            mViewBinding.tvActiveMsg.setTextColor(resources.getColor(RR.color.black_60))
            mViewBinding.tvActiveMsg.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(RR.dimen.sp_14)
            )
            mViewBinding.tvActiveMsg.typeface = Typeface.DEFAULT
            mViewBinding.viewActiveLine.visibility = View.GONE
        } else {
            mViewBinding.tvActiveMsg.setTextColor(resources.getColor(RR.color.black_90))
            mViewBinding.tvActiveMsg.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(RR.dimen.sp_18)
            )
            mViewBinding.viewActiveLine.visibility = View.VISIBLE
            mViewBinding.tvSysMsg.setTextColor(resources.getColor(RR.color.black_60))
            mViewBinding.tvSysMsg.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, resources.getDimension(RR.dimen.sp_14)
            )
            mViewBinding.tvSysMsg.typeface = Typeface.DEFAULT
            mViewBinding.viewSystemLine.visibility = View.GONE
            benefitsMgrImpl.setBenefitsStatusRead()
        }
    }

    /**
     * viewPager adapter
     */
    private val fragmentAdapter: FragmentStateAdapter by lazy {
        object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return childFragments.size
            }

            override fun createFragment(position: Int): Fragment {
                return childFragments[position]
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.msg_activity_msg_center

    override fun <T : ViewModel?> loadViewModel(): Class<T> = MsgCenterVM::class.java as Class<T>

    override fun getTitleView(): View = mViewBinding.layoutTitle

}