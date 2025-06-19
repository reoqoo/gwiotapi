package com.gw_reoqoo.component_device_share.ui.qrcode_activity

import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.ui.qrcode_activity.adapter.RecentlySharedAdapter
import com.gw_reoqoo.component_device_share.ui.qrcode_activity.vm.QRCodeShareVM
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_qrcode.QRCodeUtil
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.ktx.yyyyMMddHHmmss
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import com.gw_reoqoo.component_device_share.databinding.DevShareActivityQrcodeShareBinding as Binding
import com.gw_reoqoo.lib_widget.R as WR
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 面对面二维码分享设备界面
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 */
@Route(path = ReoqooRouterPath.DevShare.ACTIVITY_QRCODE_SHARE_PATH)
@AndroidEntryPoint
class QRCodeShareActivity : ABaseMVVMDBActivity<Binding, QRCodeShareVM>() {
    override fun getLayoutId() = R.layout.dev_share_activity_qrcode_share
    override fun <T : ViewModel?> loadViewModel() = QRCodeShareVM::class.java as Class<T>
    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

    @Autowired
    lateinit var device: IDevice

    override fun initView() {
        // 关闭
        mViewBinding.appTitle.leftIcon.setOnClickListener {
            finish()
        }
        // 初始化列表
        mViewBinding.recyclerView.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = RecentlySharedAdapter()
        }
        // 刷新按钮
        mViewBinding.ivRefresh.setSingleClickListener {
            val animate = it.animate()
            animate.cancel()
            animate.duration = 800
            animate.interpolator = LinearInterpolator()
            animate.rotationBy(-360f)
            animate.start()
            mViewModel.loadShareContent(device.deviceId, forced = true, fromClick = true)
        }
    }

    override fun initLiveData(viewModel: QRCodeShareVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 已分享账号
        viewModel.nearShareUser.observe(this) { list ->
            mViewBinding.tvTitleGuests.isVisible = list.isNotEmpty()
            val adapter = mViewBinding.recyclerView.adapter as? RecentlySharedAdapter?
            if (list.isEmpty()) {
                mViewBinding.recyclerView.setBackgroundColor(Color.TRANSPARENT)
            } else {
                mViewBinding.recyclerView.setBackgroundResource(WR.drawable.gw_reoqoo_widget_bg_card_12)
            }
            adapter?.updateData(list)
        }
        // 分享内容
        viewModel.shareContent.observe(this) { (fromClick, action) ->
            mViewBinding.lavLoad.isVisible = action is HttpAction.Loading
            mViewBinding.ivRefresh.isInvisible = action is HttpAction.Loading
            mViewBinding.tvExpiration.isInvisible = action is HttpAction.Loading
            when (action) {
                is HttpAction.Loading -> {
                    mViewBinding.ivQrcode.setImageDrawable(null)
                    mViewBinding.lavLoad.setOnClickListener(null)
                    mViewBinding.lavLoad.setAnimation(WR.raw.gw_reoqoo_widget_loading)
                    mViewBinding.lavLoad.playAnimation()
                }

                is HttpAction.Success -> {
                    mViewBinding.lavLoad.cancelAnimation()

                    val content = action.data
                    if (content != null) {
                        val link = content.shareLink
                        val expireTime = content.expireTime * 1000
                        val expireDate = Date(expireTime)

                        mViewBinding.tvExpiration.text = buildString {
                            append(getString(RR.string.AA0157))
                            append(expireDate.yyyyMMddHHmmss())
                        }
                        mViewBinding.ivQrcode.doOnLayout {
                            launch {
                                val width = mViewBinding.ivQrcode.width
                                val height = mViewBinding.ivQrcode.height
                                val qrcodeBitmap =
                                    QRCodeUtil.createQRCode(link, width, height, null)
                                mViewBinding.ivQrcode.setImageBitmap(qrcodeBitmap)
                            }
                        }
                    }
                    if (fromClick) {
                        toast.show(RR.string.AA0560)
                    }
                }

                is HttpAction.Fail -> {
                    mViewBinding.lavLoad.cancelAnimation()
                    mViewBinding.lavLoad.setImageResource(R.drawable.dev_share_icon_middle_refresh)
                    mViewBinding.lavLoad.setSingleClickListener {
                        mViewModel.loadShareContent(device.deviceId)
                    }
                    mViewBinding.tvExpiration.text =
                        getString(RR.string.AA0160)
                }
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
//        mViewModel.listGuest(device.deviceId)
        mViewModel.loadShareContent(device.deviceId)
    }
}