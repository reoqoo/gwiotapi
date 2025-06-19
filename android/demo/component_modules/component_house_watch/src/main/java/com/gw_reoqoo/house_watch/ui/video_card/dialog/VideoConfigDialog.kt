package com.gw_reoqoo.house_watch.ui.video_card.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.entities.DevicePack
import com.gw_reoqoo.house_watch.entities.ViewTypeModel
import com.gw_reoqoo.house_watch.ui.video_card.adapter.VideoConfigAdapter
import com.gw_reoqoo.house_watch.ui.video_card.item_callback.ItemTouchCallback
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.toast.IToast
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchDialogVideoConfigBinding as Binding
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 视频配置弹窗
 * @Author: XIAOLEI
 * @Date: 2023/8/23
 *
 * @param viewType 视图类型
 * @param packList 设备列表
 * @param onConfirm 确认
 */
class VideoConfigDialog(
    context: Context,
    private val toast: IToast,
    private val viewType: ViewTypeModel?,
    private val packList: List<DevicePack>?,
    private val onConfirm: (viewType: ViewTypeModel?, packList: List<DevicePack>?) -> Unit
) : Dialog(context, RR.style.commonDialog) {
    private val binding = Binding.inflate(layoutInflater)
    private val configAdapter = VideoConfigAdapter {
        changePackList = it.data
    }

    /**
     * 改变后的视图类型
     */
    private var changeViewType: ViewTypeModel? = null

    /**
     * 改变排序后的列表
     */
    private var changePackList: List<DevicePack>? = null

    init {
        setContentView(binding.root)
        val layoutParams = WindowManager.LayoutParams()
        window?.attributes?.let(layoutParams::copyFrom)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.dimAmount = 0.3f
        this.setCancelable(false)
        window?.attributes = layoutParams
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.initView()
        binding.initData()
    }

    private fun Binding.initView() {
        // 显示视图类型
        rgViewType.check(
            if (viewType == ViewTypeModel.MULTI) {
                R.id.rb_multi
            } else {
                R.id.rb_single
            }
        )
        // 初始化列表
        rvDevList.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            adapter = configAdapter
            val callback = ItemTouchCallback(
                recyclerView = this,
                onMove = {},
                afterMove = {
                    changePackList = configAdapter.data
                }
            )
            val helper = ItemTouchHelper(callback)
            helper.attachToRecyclerView(this)
        }
        // 点击取消
        tvCancel.setSingleClickListener { dismiss() }
        // 点击确认
        tvConfirm.setSingleClickListener {
            dismiss()
            onConfirm.invoke(changeViewType, changePackList)
            toast.show(RR.string.AA0201)
        }
        // 视图切换事件
        rgViewType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_multi -> changeViewType = ViewTypeModel.MULTI
                R.id.rb_single -> changeViewType = ViewTypeModel.SINGLE
            }
        }
    }

    private fun Binding.initData() {
        val oldList = packList ?: emptyList()
        val cloneList = MutableList(oldList.size) { index ->
            oldList[index].clone()
        }
        configAdapter.updateData(cloneList)
    }
}