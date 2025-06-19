package com.gw_reoqoo.cp_account.widget

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountWidgetSearchLayoutBinding

/**
 * Author: yanzheng@gwell.cc
 * Time: 2022/12/29 11:36
 * Description: 地区搜索框
 */
class SearchAreaInputLayout : LinearLayout {

    /**
     * 搜索框layout
     */
    private lateinit var binding: AccountWidgetSearchLayoutBinding

    /**
     * 输入结果回调
     */
    private var mCallback: TextChangeCallback? = null

    /**
     * 输入框监听
     */
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (TextUtils.isEmpty(s)) {
                binding.ibSearchClear.visibility = INVISIBLE
            } else {
                binding.ibSearchClear.visibility = VISIBLE
            }
        }

        override fun afterTextChanged(s: Editable?) {
            mCallback?.onTextChange(s.toString())
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    /**
     * 添加输入结果监听
     *
     * @param callback TextChangeWatcher 监听器
     */
    fun setTextChangeCallback(callback: TextChangeCallback) {
        this.mCallback = callback
    }

    /**
     * 销毁监听
     */
    fun onDestroy() {
        binding.etSearchArea.removeTextChangedListener(inputWatcher)
        this.mCallback = null
    }

    /**
     * 初始化
     *
     * @param context Context 上下文
     * @param attrs AttributeSet? 属性
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.account_widget_search_layout,
            this,
            true
        )

        binding.etSearchArea.addTextChangedListener(inputWatcher)

        binding.ibSearchClear.visibility = INVISIBLE

        binding.ibSearchClear.setOnClickListener {
            // 清除输入的账号数据
            binding.etSearchArea.text = null
        }
    }

    /**
     * 输入结果回调
     */
    interface TextChangeCallback {
        fun onTextChange(content: String)
    }

}