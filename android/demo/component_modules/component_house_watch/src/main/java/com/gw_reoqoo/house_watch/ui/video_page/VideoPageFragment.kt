package com.gw_reoqoo.house_watch.ui.video_page

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.gw.player.constants.PlayerStateEnum
import com.gw.player.constants.VideoViewMode
import com.gw.player.entity.ErrorInfo
import com.gw.player.record.ScreenCaptureConfig
import com.gw.player.render.GwVideoView
import com.gw.player.screenshot.IScreenShotListener
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchLayoutDeviceCamVideoViewBinding
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchLayoutVideoPartBinding
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.component_iotapi_plugin.opt.api.PluginSolution
import com.gw_reoqoo.house_watch.entities.DevicePack
import com.gw_reoqoo.house_watch.entities.MultiCamThumbBean
import com.gw_reoqoo.house_watch.entities.PlayerAndViewMap
import com.gw_reoqoo.house_watch.receivers.api.INetworkStatusApi
import com.gw_reoqoo.house_watch.receivers.api.Status
import com.gw_reoqoo.house_watch.ui.video_card.adapter.VideoPage
import com.gw_reoqoo.house_watch.ui.video_card.vm.VideoCardVM
import com.gw_reoqoo.house_watch.ui.video_page.adapter.SinglePageMultiCamThumbAdapter
import com.gw_reoqoo.house_watch.ui.video_page.vm.VideoPageVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.player.LivePlayer
import com.jwkj.iotvideo.player.api.IoTPlayerListener
import com.jwkj.iotvideo.player.constant.ConnectionIntOption
import com.jwkj.iotvideo.player.constant.VideoDefinition
import com.therouter.router.Autowired
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemVideoListBinding as Binding

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/9/4
 */
@AndroidEntryPoint
class VideoPageFragment : ABaseMVVMDBFragment<Binding, VideoPageVM>() {

    companion object {
        private const val TAG = "VideoPageFragment"
    }

    /**
     * 传过来的设备信息
     */
    @Autowired
    lateinit var videoPage: VideoPage

    /**
     * 当前页面所用到的播放器
     */
    private val playerMap = mutableMapOf<String, PlayerAndViewMap>()

    /**
     * 网络状态API
     */
    @Inject
    lateinit var networkStatusApi: INetworkStatusApi

    @Inject
    lateinit var familyModeApi: FamilyModeApi

    @Inject
    lateinit var igwIotOpt: IGWIotOpt

    /**
     * 缩略图的适配器
     */
    private val multiCamPanelAdapter = SinglePageMultiCamThumbAdapter()

    /**
     * 获取视频卡片的ViewModel
     */
    private val videoCardVM by lazy {
        val parent = parentFragment
        parent?.let { ViewModelProvider(parent)[VideoCardVM::class.java] }
    }

    override fun getLayoutId() = R.layout.house_watch_item_video_list
    override fun <T : ViewModel?> loadViewModel() = VideoPageVM::class.java as Class<T>

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.clMulti.isVisible = videoPage is VideoPage.MultiPage
        mViewBinding.flSingle.root.isVisible = videoPage is VideoPage.SinglePage
        // 宫格屏下，视频格的图标和文字大小不一样，这里作判断调整
        when (videoPage) {
            is VideoPage.SinglePage -> {
                mViewBinding.flSingle.rvMultiCamPanel.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                mViewBinding.flSingle.rvMultiCamPanel.adapter = multiCamPanelAdapter
            }

            is VideoPage.MultiPage -> {
                fun HouseWatchLayoutVideoPartBinding.setMultiPageSize() {
                    this.ivRefresh.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = 23.dp
                        height = 23.dp
                    }
                    this.tvRefresh.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        updateMargins(top = 4.dp)
                    }
                    this.tvRefresh.textSize = 12f
                }
                mViewBinding.flLt.setMultiPageSize()
                mViewBinding.flRt.setMultiPageSize()
                mViewBinding.flLb.setMultiPageSize()
                mViewBinding.flRb.setMultiPageSize()
            }
        }
    }

    override fun initLiveData(viewModel: VideoPageVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 监听网络状态
        viewModel.networkStatus.observe(this) { (oldStatus, newStatus) ->
            val cardVM = videoCardVM ?: return@observe
            if (newStatus != Status.MOBILE) {
                cardVM.hasTipMobileData.set(false)
            }
            // 是切换到流量，并且当前界面有播放器正在播放
            if (
                (oldStatus != newStatus && newStatus == Status.MOBILE) &&
                hasAnyPlaying() &&
                cardVM.hasTipMobileData.compareAndSet(false, true)
            ) {
                toast.show(com.gw_reoqoo.resource.R.string.AA0198)
            }
        }
    }

    override fun initData() {
        super.initData()
        // 单画面的情况显示底部的多摄像头选择面板，否则不显示
        when (videoPage) {
            is VideoPage.SinglePage -> {
                val device = videoPage.devices.first()
                lifecycleScope.launch {
                    mViewBinding.flSingle.bindPlayer(device, false)
                }
            }

            is VideoPage.MultiPage -> {
                lifecycleScope.launch {
                    mViewBinding.flLt.bindPlayer(videoPage.devices.getOrNull(0), true)
                    mViewBinding.flRt.bindPlayer(videoPage.devices.getOrNull(1), true)
                    mViewBinding.flLb.bindPlayer(videoPage.devices.getOrNull(2), true)
                    mViewBinding.flRb.bindPlayer(videoPage.devices.getOrNull(3), true)
                }
            }
        }
    }

    override fun onResume() {
        GwellLogUtils.i(TAG, "onResume-$videoPage")
        super.onResume()
        this.playAll()
    }

    override fun onPause() {
        GwellLogUtils.i(TAG, "onPause-$videoPage")
        super.onPause()
        this.stopAll()
    }

    override fun onDestroy() {
        GwellLogUtils.i(TAG, "onDestroy-$videoPage")
        for ((player, bindings) in playerMap.values) {
            if (player.isConnectingOrConnected()) {
                player.stop()
            }
            player.shutdown()
        }
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        GwellLogUtils.i(TAG, "onHiddenChanged($hidden)-$videoPage")
        if (hidden) {
            this.stopAll()
        } else {
            val filterState = arrayOf(Lifecycle.State.RESUMED)
            if (this.lifecycle.currentState !in filterState) return
            this.playAll()
        }
    }

    /**
     * 【单设备模式下】添加一个摄像头下所有镜头的的截图
     * @param flMultiCamPanel 底部显示缩略图的面板
     * @param flVideoViewContainer videoView的容器
     * @param devId 设备ID
     */
    private fun screenShotAndShow(
        flMultiCamPanel: FrameLayout,
        flVideoViewContainer: FrameLayout,
        devId: String
    ) {
        val (player, videoViewBindings) = getPlayer(devId) ?: return
        flMultiCamPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = 60.dp
        }
        // 截图
        val ctx = this.context ?: return
        val screenCaptureConfig =
            ScreenCaptureConfig("${ctx.cacheDir.absolutePath}${File.separator}${devId}_%2d.jpg").apply {
                this.viewMode = if (videoViewBindings.size > 1) {
                    VideoViewMode.V_E_DIV
                } else {
                    VideoViewMode.ORIGINAL
                }
            }
        player.screenshot(screenCaptureConfig, object : IScreenShotListener {
            override fun onResult(errorInfo: ErrorInfo?, map: HashMap<Int, String>) {
                GwellLogUtils.e(TAG, "errorInfo=${errorInfo},map=${map}")
                val lastChild = flVideoViewContainer.children.lastOrNull()
                val lastVideoView = lastChild?.findViewById<GwVideoView>(R.id.video_view)
                val lastIndex = lastVideoView?.getTag(R.id.house_watch_id_video_view_index) as? Int?
                val newScreenShotData = map.map { (camIndex, imgPath) ->
                    MultiCamThumbBean(imgPath, camIndex, checked = camIndex == (lastIndex ?: 0))
                }
                multiCamPanelAdapter.updateData(newScreenShotData,true)
            }
        })
        // 设置点击切换不同的镜头显示
        multiCamPanelAdapter.setOnItemClickListener { _, data ->
            val camIndex = data.camIndex
            val childBinding = videoViewBindings.firstOrNull { childBinding ->
                val tag = childBinding.videoView.getTag(R.id.house_watch_id_video_view_index)
                (tag as? Int?) == camIndex
            }
            childBinding?.root?.bringToFront()
            // 切换选中状态
            val newData = mutableListOf<MultiCamThumbBean>()
            for (thumbBean in multiCamPanelAdapter.data) {
                newData.add(thumbBean.copy(checked = thumbBean.camIndex == camIndex))
            }
            multiCamPanelAdapter.updateData(newData,true)
        }
    }

    /**
     * 【单设备模式下】移除所有的镜头截图
     */
    private fun clearAllCamScreenShot(flMultiCamPanel: FrameLayout) {
        flMultiCamPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = 0.dp
        }
        multiCamPanelAdapter.updateData(listOf())
    }

    /**
     * 把视图和设备信息，以及播放器绑定在一起
     * @param devicePack 对应的设备
     * @param isMultiPage 是否是多设备在同一个画面的模式
     */
    private suspend fun HouseWatchLayoutVideoPartBinding.bindPlayer(
        devicePack: DevicePack?,
        isMultiPage: Boolean
    ) {
        // 获取设备的镜头数量
        var camCount: Int = 0
        if (devicePack != null) {
            // Reoqoo的设备，镜头数是1
            if (familyModeApi.isReoqooDevice(devicePack.productId ?: "")) {
                camCount = 1
            } else {
                // Yoosee的设备，镜头数要从物模型里拿，没拿到就是 1
                camCount = igwIotOpt.getCamCount(devicePack.deviceId,PluginSolution.YOOSEE)
            }
        }

        // 设置刷新按钮/文字的可见性
        fun HouseWatchLayoutVideoPartBinding.refreshVisible(visible: Boolean) {
            // 刷新按钮
            this.ivRefresh.isVisible = visible
            // 刷新按钮的提示文字
            this.tvRefresh.isVisible = visible
        }

        // 设置刷新按钮/文字的点击事件
        fun HouseWatchLayoutVideoPartBinding.onRefreshClick(block: () -> Unit) {
            this.ivRefresh.setSingleClickListener { block.invoke() }
            this.tvRefresh.setSingleClickListener { block.invoke() }
        }

        // 设置动画可见性
        fun LottieAnimationView.setVisible(visible: Boolean) {
            if (visible) {
                this.cancelAnimation()
                this.progress = 0f
                this.isVisible = true
                this.playAnimation()
            } else {
                this.cancelAnimation()
                this.progress = 0f
                this.isVisible = false
            }
        }

        val device = devicePack?.device
        // 标识加载中
        lavLoading.setVisible(false)
        // 隐藏刷新按钮
        refreshVisible(false)
        GwellLogUtils.i(TAG, "bindPlayer-$device")
        when (device) {
            null -> {
                tvDevName.text = ""
                tvDevStatus.setText(com.gw_reoqoo.resource.R.string.AA0464)
            }

            else -> {
                tvDevName.text = device.remarkName
                when {
                    device.isOnline ->
                        when (device.powerOn) {
                            true -> {
                                // 如果要隐藏画面，则直接跳出并显示画面已关闭
                                if (devicePack.offView) {
                                    tvDevStatus.setText(com.gw_reoqoo.resource.R.string.AA0199)
                                    return
                                }
                                // 设备在线
                                tvDevStatus.text = ""
                                // 获取视频播放器
                                val (player, backVideoViewBinds) = getOrCreatePlayer(device.deviceId)
                                // 设置点击刷新事件
                                onRefreshClick {
                                    player.stop()
                                    player.play()
                                    refreshVisible(false)
                                }
                                // 遮罩层点击事件,进入插件监控页
                                vClickMask.setSingleClickListener {
                                    mFgViewModel.openDeviceMonitor(device)
                                }

                                // 根据镜头数量初始化播放器的ViewMode
                                if (camCount > 1) {
                                    player.setVideoViewMode(VideoViewMode.V_E_DIV)
                                } else {
                                    player.setVideoViewMode(VideoViewMode.ORIGINAL)
                                }

                                // 播放监听
                                player.setIoTListener(object : IoTPlayerListener {
                                    override fun onOpened(
                                        totalDuration: Long,
                                        playableDuration: Long
                                    ) {
                                        GwellLogUtils.i(TAG, "player.setListener onOpened()")
                                    }

                                    override fun onPtsChange(
                                        pts: Long,
                                        position: Long,
                                        utcTime: Long
                                    ) {
                                        // GwellLogUtils.i(TAG, "player.setListener onPtsChange()")
                                    }

                                    // 播放状态控制
                                    override fun onStateChange(state: PlayerStateEnum) {
                                        GwellLogUtils.i(TAG, "player.onStateChange($state)")
                                        super.onStateChange(state)
                                        when (state) {
                                            PlayerStateEnum.PREPARING -> {
                                                if (device.isOnline) {
                                                    lavLoading.setVisible(true)
                                                }
                                                refreshVisible(false)
                                            }

                                            PlayerStateEnum.LOADING -> {
                                                for (videoViewBind in backVideoViewBinds) {
                                                    videoViewBind.videoView.resume()
                                                    videoViewBind.videoView.isInvisible = false
                                                }
                                            }

                                            PlayerStateEnum.PLAYING -> {
                                                refreshVisible(false)
                                                lavLoading.setVisible(false)
                                                tvDevStatus.text = ""
                                                // 当时单Page模式下，并且摄像头的数量大于1 的时候才显示出来
                                                cbMultiCamPanel.isVisible = !isMultiPage && camCount > 1
                                                val cardVM = videoCardVM
                                                if (cardVM != null) {
                                                    if (
                                                        networkStatusApi.isInMobileData &&
                                                        cardVM.hasTipMobileData.compareAndSet(
                                                            false,
                                                            true
                                                        )
                                                    ) {
                                                        toast.show(com.gw_reoqoo.resource.R.string.AA0198)
                                                    }
                                                }
                                            }

                                            PlayerStateEnum.STOPPED -> {
                                                for (videoViewBind in backVideoViewBinds) {
                                                    videoViewBind.videoView.isInvisible = true
                                                    videoViewBind.videoView.pause()
                                                }
                                                lavLoading.setVisible(false)
                                            }

                                            PlayerStateEnum.ERROR -> {
                                                if (player.isPlaying() && device.isOnline && device.powerOn == true) {
                                                    // 显示刷新按钮
                                                    refreshVisible(true)
                                                    // 取消动画并隐藏
                                                    lavLoading.setVisible(false)
                                                }
                                            }

                                            else -> {}
                                        }
                                    }
                                })

                            }

                            false -> {
                                // 视频已关闭
                                tvDevStatus.setText(com.gw_reoqoo.resource.R.string.AA0598)
                            }

                            else -> Unit
                        }

                    device.isOffline -> {
                        // 设备已离线
                        tvDevStatus.setText(com.gw_reoqoo.resource.R.string.AA0200)
                    }
                }
            }
        }

        // 如果是单画面模式，则根据情况进行显示底部的面板（包括操作按钮）
        // 如果是多画面模式，则一律不显示
        // 设置可见性
        when (isMultiPage) {
            // 多画面模式
            true -> when (device) {
                null -> {
                    flMultiCamPanel.isVisible = false
                    cbMultiCamPanel.isVisible = false
                }

                else -> {
                    val (player, backVideoViewBinds, otherVideoViewAdapter) = getOrCreatePlayer(device.deviceId)
                    flMultiCamPanel.isVisible = false
                    cbMultiCamPanel.isVisible = false
                    // 显示第一个镜头的画面
                    val binding = DataBindingUtil.inflate<HouseWatchLayoutDeviceCamVideoViewBinding>(
                            layoutInflater,
                            R.layout.house_watch_layout_device_cam_video_view,
                            flVideoViewContainer,
                            true
                        )
                    backVideoViewBinds.add(binding)
                    player.addVideoView(binding.videoView)
                    // 显示其他镜头的画面
                    rvMultiPageOtherCam.layoutManager = LinearLayoutManager(
                        context, RecyclerView.VERTICAL, false
                    )
                    if (camCount > 1) {
                        rvMultiPageOtherCam.adapter = otherVideoViewAdapter
                        val camCountRange = 1 until camCount
                        otherVideoViewAdapter.updateData(camCountRange.map { it + 1 })
                    }
                }
            }
            // 单画面模式
            false -> when (device) {
                null -> {
                    flMultiCamPanel.isVisible = false
                    cbMultiCamPanel.isVisible = false
                }

                else -> {
                    val (player, backVideoViewBinds) = getOrCreatePlayer(device.deviceId)
                    // 根据镜头数量，初始化对应数量的播放器控件
                    for (i in 0 until camCount) {
                        val binding =
                            DataBindingUtil.inflate<HouseWatchLayoutDeviceCamVideoViewBinding>(
                                layoutInflater,
                                R.layout.house_watch_layout_device_cam_video_view,
                                flVideoViewContainer,
                                true
                            )
                        backVideoViewBinds.add(binding)
                    }

                    // 添加到对应的播放器中
                    backVideoViewBinds.reversed()
                        .forEachIndexed { index, videoViewBind ->
                            val videoView = videoViewBind.videoView
                            videoView.setTag(
                                R.id.house_watch_id_video_view_index,
                                index
                            )
                            player.addVideoView(videoView)
                        }

                    // 设置可见性
                    flMultiCamPanel.isVisible = camCount > 1
                    // 先把可见性设置为false，当playing的时候再显示出来
                    cbMultiCamPanel.isVisible = false
                    // 点击checkbox用来显示底部的多摄像头选择面板
                    cbMultiCamPanel.setOnCheckedChangeListener { _, isChecked ->
                        val playerBindingMap = getPlayer(device.deviceId)
                        if (isChecked && playerBindingMap?.player?.isPlaying() == true) {
                            screenShotAndShow(
                                mViewBinding.flSingle.flMultiCamPanel,
                                mViewBinding.flSingle.flVideoViewContainer,
                                device.deviceId
                            )
                        } else {
                            clearAllCamScreenShot(mViewBinding.flSingle.flMultiCamPanel)
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据设备ID，获取或者创建一个播放器
     */
    private fun getOrCreatePlayer(deviceId: String): PlayerAndViewMap {
        val playerViewMap = playerMap[deviceId] ?: synchronized(playerMap) {
            val player = LivePlayer(deviceId).apply {
                setMute(true)
                setDefinition(VideoDefinition.SD)
                setConnOptInt(ConnectionIntOption.APP_STATE, 0)
            }
            PlayerAndViewMap(player)
        }
        playerMap[deviceId] = playerViewMap
        return playerViewMap
    }

    /**
     * 获取播放器，如果没有就是null
     */
    private fun getPlayer(deviceId: String): PlayerAndViewMap? {
        return playerMap[deviceId]
    }

    /**
     * 播放所有视频
     */
    private fun playAll() {
        GwellLogUtils.i(TAG, "playAll-$videoPage")
        for ((player, bindings, adapter) in playerMap.values) {
            player.play()
        }
    }

    /**
     * 停止播放所有视频
     */
    private fun stopAll() {
        GwellLogUtils.i(TAG, "stopAll-$videoPage")
        for ((player, bindings, adapter) in playerMap.values) {
            player.stop()
        }
    }

    /**
     * 有播放器正在播放
     */
    private fun hasAnyPlaying(): Boolean {
        for ((player, bindings, adapter) in playerMap.values) {
            if (player.isPlaying()) {
                return true
            }
        }
        return false
    }
}