package com.gw_reoqoo.house_watch.ui.active_card.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper
import com.gw_reoqoo.house_watch.entities.DeviceWrapper
import com.gw_reoqoo.house_watch.ui.active_card.adapter.DialogActiveTypeAdapter
import com.gw_reoqoo.house_watch.ui.active_card.adapter.DialogDeviceAdapter
import com.gw_reoqoo.house_watch.ui.active_card.vm.ActiveCardVM
import com.gw_reoqoo.lib_utils.ktx.getDrawableAndBounds
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.toast.IToast
import com.gwell.loglibs.GwellLogUtils
import java.util.LinkedList
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchDialogActiveConfigBinding as Binding
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 活动配置弹窗
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 *
 * @param activeCardVM 活动卡片的VM
 */
class ActiveConfigDialog(
    context: Context,
    private val toast: IToast,
    private val activeCardVM: ActiveCardVM,
) : Dialog(context, RR.style.commonDialog), LifecycleOwner {
    private val binding = Binding.inflate(layoutInflater)
    private val activeTypeAdapter = DialogActiveTypeAdapter()
    private val deviceAdapter = DialogDeviceAdapter()
    override val lifecycle = LifecycleRegistry(this)

    companion object {
        private const val TAG = "ActiveConfigDialog"

        /**
         * 设备列表筛选的展开状态
         */
        private var devExpand: Boolean = false
    }

    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        setContentView(binding.root)
        val layoutParams = WindowManager.LayoutParams()
        window?.attributes?.let(layoutParams::copyFrom)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.dimAmount = 0.3f
        window?.attributes = layoutParams
        setCancelable(false)
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setOnShowListener {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        setOnDismissListener {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        binding.initView()
        initData()
    }

    private fun Binding.initView() {
        // 点击取消
        tvCancel.setSingleClickListener { dismiss() }
        // 点击确认
        tvConfirm.setSingleClickListener {
            dismiss()
            // 事件
            val activeTypeList = activeTypeAdapter.data
            //设备
            val deviceList = deviceAdapter.data

            activeCardVM.setDataFromConfig(deviceList, activeTypeList)
            toast.show(RR.string.AA0201)
        }
        // 展开按钮
        cbExpand.setOnCheckedChangeListener { buttonView, isChecked ->
            devExpand = isChecked
            // 刷新设备
            loadDeviceList(devExpand)
            buttonView.setText(
                if (isChecked) {
                    RR.string.AA0504
                } else {
                    RR.string.AA0503
                }
            )
        }
        // 初始化列表
        initActiveTypeList()
        initDeviceList()
    }

    /**
     * 初始化事件类型列表
     */
    private fun Binding.initActiveTypeList() {
        val flexLayoutManager = FlexboxLayoutManager(context)
        //flexDirection 属性决定主轴的方向（即项目的排列方向）。类似 LinearLayout 的 vertical 和 horizontal。
        flexLayoutManager.flexDirection = FlexDirection.ROW;//主轴为水平方向，起点在左端。
        //flexWrap 默认情况下 Flex 跟 LinearLayout 一样，都是不带换行排列的，但是flexWrap属性可以支持换行排列。
        flexLayoutManager.flexWrap = FlexWrap.WRAP;//按正常方向换行
        //justifyContent 属性定义了项目在主轴上的对齐方式。
        flexLayoutManager.justifyContent = JustifyContent.FLEX_START;//交叉轴的起点对齐。

        rvSelectEvent.layoutManager = flexLayoutManager
        val divider = DividerItemDecoration(context, RecyclerView.VERTICAL)
        val drawable = context.getDrawableAndBounds(
            R.drawable.house_watch_active_dialog_grid_divider
        )
        drawable?.let(divider::setDrawable)
        rvSelectEvent.addItemDecoration(divider)
        rvSelectEvent.adapter = activeTypeAdapter
        activeTypeAdapter.setOnItemClick { activeTypeWrapper ->
            val newList = activeTypeAdapter.data
            GwellLogUtils.d("activeTypeWrapper", activeTypeWrapper)
            val text = activeTypeWrapper.type?.descRes
            if (activeTypeWrapper.checked) {
                // 如果本来是选中，则取消选中
                if (text == null || text == RR.string.AA0208) {
                    // 如果是全部按钮，默认将其他选项全部取消选中
                    for (wrapper in newList) {
                        wrapper.checked = false
                    }
                    activeTypeWrapper.checked = true
                } else {
                    // 不是全部按钮，则需要取消选中；还需要判断是否全部事件按钮非选中，如果全部取消，则将全部按钮选中
                    activeTypeWrapper.checked = false
                    var isAllUnchecked = true
                    var allWrapper: com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper? = null
                    for (wrapper in newList) {
                        val type = wrapper.type?.descRes
                        if (type == null || type == RR.string.AA0208) {
                            allWrapper = wrapper
                        }
                        if (wrapper.checked) {
                            isAllUnchecked = false
                            break
                        }
                    }
                    if (isAllUnchecked) {
                        allWrapper?.checked = true
                    }
                }
            } else {
                // 如果本来是取消选中，则选中
                if (text == null || text == RR.string.AA0208) {
                    // 如果是全部按钮，默认将其他选项全部取消
                    for (wrapper in newList) {
                        wrapper.checked = false
                    }
                    activeTypeWrapper.checked = true
                } else {
                    // 不是全部按钮，则需要选中；
                    var allWrapper: com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper? = null
                    for (wrapper in newList) {
                        val type = wrapper.type?.descRes
                        if (type == null || type == RR.string.AA0208) {
                            allWrapper = wrapper
                        }
                        break
                    }
                    allWrapper?.checked = false
                    activeTypeWrapper.checked = true
                }
            }
            activeTypeAdapter.updateData(newList, false)
        }
    }

    /**
     * 初始化设备列表
     */
    private fun Binding.initDeviceList() {
        val flexLayoutManager = FlexboxLayoutManager(context)
        //flexDirection 属性决定主轴的方向（即项目的排列方向）。类似 LinearLayout 的 vertical 和 horizontal。
        flexLayoutManager.flexDirection = FlexDirection.ROW;//主轴为水平方向，起点在左端。
        //flexWrap 默认情况下 Flex 跟 LinearLayout 一样，都是不带换行排列的，但是flexWrap属性可以支持换行排列。
        flexLayoutManager.flexWrap = FlexWrap.WRAP;//按正常方向换行
        //justifyContent 属性定义了项目在主轴上的对齐方式。
        flexLayoutManager.justifyContent = JustifyContent.FLEX_START;//交叉轴的起点对齐。

        rvSelectDevice.layoutManager = flexLayoutManager
        val divider = DividerItemDecoration(context, RecyclerView.VERTICAL)
        val drawable = context.getDrawableAndBounds(
            R.drawable.house_watch_active_dialog_grid_divider
        )
        drawable?.let(divider::setDrawable)
        rvSelectDevice.addItemDecoration(divider)
        rvSelectDevice.adapter = deviceAdapter
        deviceAdapter.setOnItemClick { deviceWrapper ->
            val newList = deviceAdapter.data
            if (deviceWrapper.device == null) {
                // 点击的是全部item
                for (wrapper in newList) {
                    wrapper.checked = false
                }
                deviceWrapper.checked = true
            } else {
                // 点击的是其他item
                if (deviceWrapper.checked) {
                    // 本来是选中状态
                    deviceWrapper.checked = false
                    var isAllUnchecked = true
                    var allDeviceWrapper: com.gw_reoqoo.house_watch.entities.DeviceWrapper? = null
                    for (wrapper in newList) {
                        if (wrapper.device == null) {
                            allDeviceWrapper = wrapper
                        }
                        if (wrapper.checked) {
                            isAllUnchecked = false
                            break
                        }
                    }
                    if (isAllUnchecked) {
                        allDeviceWrapper?.checked = true
                    }
                } else {
                    // 本来是非选中状态
                    for (wrapper in newList) {
                        if (wrapper.device == null) {
                            wrapper.checked = false
                            break
                        }
                    }
                    deviceWrapper.checked = true
                }
            }
            deviceAdapter.updateData(newList, false)
        }
    }

    private val cloneActiveTypeList = LinkedList<com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper>()
    private val cloneDeviceList = LinkedList<com.gw_reoqoo.house_watch.entities.DeviceWrapper>()

    private fun initData() {
        val dialog = this@ActiveConfigDialog
        // 事件
        activeCardVM.activeList.observe(dialog) { oldList ->
            cloneActiveTypeList.clear()
            cloneActiveTypeList.addAll(List(oldList.size) { index -> oldList[index].copy() })
            activeTypeAdapter.updateData(cloneActiveTypeList)
        }
        // 设备列表监听
        activeCardVM.deviceList.observe(dialog) { list ->
            GwellLogUtils.i(TAG, "loadDeviceList: list=$list")
            cloneDeviceList.clear()
            cloneDeviceList.addAll(List(list.size) { index -> list[index].copy() })
            loadDeviceList(devExpand)
        }
        binding.cbExpand.isChecked = devExpand
    }

    /**
     * 是否显示全部设备信息
     *
     * @param isShowAll Boolean
     */
    private fun loadDeviceList(isShowAll: Boolean) {
        GwellLogUtils.i(TAG, "loadDeviceList: cloneDeviceList=$cloneDeviceList")
        // 这里不知道什么时候cloneDeviceList会被置为null，所以增加了isNull的判断
        if (cloneDeviceList.isNullOrEmpty()) {
            deviceAdapter.updateData(mutableListOf())
            return
        }
        deviceAdapter.updateData(cloneDeviceList)
        deviceAdapter.setShowAll(isShowAll)
    }

}