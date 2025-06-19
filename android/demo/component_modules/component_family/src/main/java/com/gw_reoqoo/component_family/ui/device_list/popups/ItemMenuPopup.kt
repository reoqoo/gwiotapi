package com.gw_reoqoo.component_family.ui.device_list.popups

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupWindow
import androidx.core.view.isVisible
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.databinding.FamilyPopupItemMenuBinding
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.ktx.isMaster
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gwell.loglibs.GwellLogUtils
import kotlin.math.roundToInt

/**
 * @Description: - 设备长按弹窗
 * @Author: XIAOLEI
 * @Date: 2023/8/8
 * @param onShareClick 点击共享按钮
 * @param onDeleteClick 点击删除按钮
 */
class ItemMenuPopup(
    context: Context,
    private val deviceInfo: DeviceInfo,
    private val onShareClick: () -> Unit,
    private val onDeleteClick: () -> Unit,
) : PopupWindow() {
    companion object {
        private const val TAG = "ItemMenuPopup"
    }

    private val binding: FamilyPopupItemMenuBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = FamilyPopupItemMenuBinding.inflate(inflater)
        this.contentView = binding.root
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        isFocusable = true
        initView()
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        initData()
    }

    private fun initView() {
        // 点击共享
        binding.llShared.setSingleClickListener {
            dismiss()
            onShareClick.invoke()
        }
        // 点击删除
        binding.llDelete.setSingleClickListener {
            dismiss()
            onDeleteClick.invoke()
        }
        binding.llShared.isVisible = deviceInfo.isMaster
        binding.lineShare.isVisible = deviceInfo.isMaster
    }


    private fun initData() {

    }

    fun show(anchor: View, anchorActivity: Activity) {
        val itemPadding = 7.5f.dp.roundToInt()
        val dialogPadding = 4.dp

        val popupLocation = IntArray(2)
        contentView.getLocationOnScreen(popupLocation)
        val popupX = popupLocation.first()
        val popupY = popupLocation.last()
        val popupW = contentView.measuredWidth
        val popupH = contentView.measuredHeight
        GwellLogUtils.i(TAG, "popupX:$popupX,popupY:$popupY")
        GwellLogUtils.i(TAG, "popupW:$popupW,popupH:$popupH")

        val anchorLocation = IntArray(2)
        anchor.getLocationOnScreen(anchorLocation)
        val anchorX = anchorLocation.first()
        val anchorY = anchorLocation.last()
        val anchorW = anchor.width
        val anchorH = anchor.height
        GwellLogUtils.i(TAG, "anchorX:$anchorX,anchorY:$anchorY")
        GwellLogUtils.i(TAG, "anchorW:$anchorW,anchorH:$anchorH")
        if (anchorActivity.isFinishing) return
        
        val display = anchorActivity.windowManager.defaultDisplay
        val windowWidth = display.width
        val windowHeight = display.height
        GwellLogUtils.i(TAG, "windowWidth:$windowWidth,windowHeight:$windowHeight")

        if (windowHeight < (anchorY + anchorH + popupH)) {
            binding.rlLayout.setBackgroundResource(R.drawable.family_device_item_popup_revers_bg)
            super.showAsDropDown(anchor, 0, -anchorH - popupH + itemPadding - dialogPadding)
        } else {
            super.showAsDropDown(anchor, 0, -itemPadding + dialogPadding)
        }
    }
}