package com.gw.cp_mine.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineBtnCustomBinding
import com.gw_reoqoo.lib_utils.ktx.visible

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/9/2 11:19
 * Description: IconTextButton
 */
class MineIconTextButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: MineBtnCustomBinding

    init {
        // 使用ViewBinding加载布局
        val inflater = LayoutInflater.from(context)
        binding = MineBtnCustomBinding.inflate(inflater, this, true)

        // 读取自定义属性并应用
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.MineIconTextButton, 0, 0)
            val text = typedArray.getString(R.styleable.MineIconTextButton_mine_text)
            val icon = typedArray.getDrawable(R.styleable.MineIconTextButton_mine_icon)

            binding.tvText.text = text
            icon?.let {
                binding.imgIcon.setImageDrawable(icon)
            }

            typedArray.recycle()
        }
    }

    /**
     * 设置文本的方法
     *
     * @param text String
     */
    fun setText(text: String) {
        binding.tvText.text = text
    }

    /**
     * 设置图标的方法
     *
     * @param resId Int
     */
    fun setIcon(resId: Int) {
        binding.imgIcon.setImageResource(resId)
    }

    /**
     *  设置文本颜色的方法
     *
     * @param color Int
     */
    fun setTextColor(color: Int) {
        binding.tvText.setTextColor(color)
    }

    /**
     * 设置红点的可见性
     *
     * @param visible Boolean
     */
    fun setPointVisible(visible: Boolean) {
        binding.viewRedPoint.visible(visible)
    }
}