package com.gw_reoqoo.house_watch.ui.video_card

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gw_reoqoo.component_house_watch.R
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.house_watch.entities.ViewTypeModel
import com.gw_reoqoo.house_watch.ui.video_card.adapter.PageInstructAdapter
import com.gw_reoqoo.house_watch.ui.video_card.adapter.VideoFragmentAdapter
import com.gw_reoqoo.house_watch.ui.video_card.dialog.VideoConfigDialog
import com.gw_reoqoo.house_watch.ui.video_card.vm.VideoCardVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.adapter.ChangeDataObserver
import com.gw_reoqoo.lib_utils.ktx.getDrawableAndBounds
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.popups.GuidePopup
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchFragmentVideoCardBinding as Binding
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 视频卡片
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@Route(path = ReoqooRouterPath.HouseWatch.FRAGMENT_VIDEO_CARD)
@AndroidEntryPoint
class VideoCardFragment : ABaseMVVMDBFragment<Binding, VideoCardVM>() {
    companion object {
        private const val TAG = "VideoCardFragment"
    }

    @Inject
    lateinit var pluginManager: IPluginManager

    private var guidePopup: GuidePopup? = null

    /**
     * 页面数据改变回调
     */
    private val observer = ChangeDataObserver {
        val adapter = mViewBinding.vpVideo.adapter
        if (adapter is VideoFragmentAdapter) {
            val pageData = adapter.data
            mViewBinding.rvInstruct.isVisible = pageData.size >= 2
            val instructAdapter = mViewBinding.rvInstruct.adapter
            if (instructAdapter is PageInstructAdapter) {
                instructAdapter.updateData(pageData)
            }
        }
    }

    /**
     * 页面切换回调
     */
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            GwellLogUtils.i(TAG, "onPageSelected:$position")
            val instructAdapter = mViewBinding.rvInstruct.adapter
            if (instructAdapter is PageInstructAdapter) {
                instructAdapter.setCurrent(position)
            }
        }
    }

    override fun getLayoutId() = R.layout.house_watch_fragment_video_card
    override fun <T : ViewModel?> loadViewModel() = VideoCardVM::class.java as Class<T>

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        // 初始化ViewPager2
        mViewBinding.vpVideo.run {
            val adapter = VideoFragmentAdapter(this@VideoCardFragment)
            adapter.registerAdapterDataObserver(observer)
            this.adapter = adapter
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }
        // 点击菜单
        mViewBinding.ivMenu.setSingleClickListener {
            val devList = mFgViewModel.deviceList.value
            val context = context
            if (context != null && devList != null) {
                VideoConfigDialog(
                    context,
                    toast = toast,
                    viewType = mFgViewModel.viewType.value,
                    packList = mFgViewModel.deviceList.value,
                    onConfirm = { viewType, packList ->
                        viewType?.let(mFgViewModel::setViewType)
                        packList?.let(mFgViewModel::setSortDeviceList)
                    }
                ).show()
            }
        }
        // 底部指示器
        mViewBinding.rvInstruct.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            val drawable = context.getDrawableAndBounds(
                R.drawable.house_watch_shape_divider_trans_8
            )
            if (drawable != null) {
                val divider = DividerItemDecoration(context, RecyclerView.HORIZONTAL)
                divider.setDrawable(drawable)
                addItemDecoration(divider)
            }
            adapter = PageInstructAdapter()
        }
    }

    override fun initLiveData(viewModel: VideoCardVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 监听设备列表
        viewModel.deviceList.observe(this) { devices ->
            val adapter = mViewBinding.vpVideo.adapter
            if (adapter is VideoFragmentAdapter) {
                val viewType = viewModel.viewType.value ?: ViewTypeModel.SINGLE
                adapter.updateData(devices, viewType)
            }
        }
        // 视图模式切换
        viewModel.viewType.observe(this) { viewType ->
            val adapter = mViewBinding.vpVideo.adapter
            if (adapter is VideoFragmentAdapter) {
                adapter.setViewType(viewType)
            }
        }
        // 新手引导
        viewModel.videoCardGuide.observe(this) { shown ->
            if (shown == false) {
                context?.let {
                    guidePopup =
                        GuidePopup(it, this, RR.string.AA0339).apply {
                            setOnIKnowClick {
                                viewModel.iKnowVideoCardGuide()
                            }
                        }
                    guidePopup?.show(mViewBinding.ivMenu)
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mFgViewModel.loadDeviceList()

        mFgViewModel.setVideoSettings()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        GwellLogUtils.i(TAG, "onHiddenChanged:$hidden")
        if (hidden) {
            guidePopup?.dismiss()
        }
    }

}