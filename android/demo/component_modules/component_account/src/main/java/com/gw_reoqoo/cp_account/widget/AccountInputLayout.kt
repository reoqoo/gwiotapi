//package com.jwkj.account.widget
//
//import android.content.Context
//import android.text.Editable
//import android.text.TextUtils
//import android.text.TextWatcher
//import android.util.AttributeSet
//import android.view.LayoutInflater
//import android.widget.EditText
//import android.widget.LinearLayout
//import androidx.databinding.DataBindingUtil
//import com.gwell.loglibs.GwellLogUtils
//import com.jwkj.compo_impl_account.R
//import com.jwkj.compo_impl_account.databinding.LayoutAccountInputBinding
//
///**
// * Author: yanzheng@gwell.cc
// * Time: 2022/12/27 11:30
// * Description: 账号输入框
// */
//class AccountInputLayout : LinearLayout {
//
//    companion object {
//        private const val TAG = "AccountInputLayout"
//    }
//
//    private lateinit var binding: LayoutAccountInputBinding
//
//    constructor(context: Context) : this(context, null)
//
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
//
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        init(context, attrs)
//    }
//
//    private var watcher: TextChangeWatcher? = null
//
//    /**
//     *
//     * @param context Context
//     * @param attrs AttributeSet?
//     */
//    private fun init(context: Context, attrs: AttributeSet?) {
//        if (!isInEditMode) {
//            binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_account_input, this, true)
//            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GwCommonInputView)
//            val title = typedArray.getString(R.styleable.GwCommonInputView_android_hint)
//            title?.let {
//                binding.etAccountInput.hint = title
//            }
//            binding.etAccountInput.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    if (TextUtils.isEmpty(s)) {
//                        binding.ivAccountClear.visibility = INVISIBLE
//                    } else {
//                        binding.ivAccountClear.visibility = VISIBLE
//                    }
//                }
//
//                override fun afterTextChanged(s: Editable?) {
//                    watcher?.onTextChange(s.toString())
//                }
//            })
//            binding.etAccountInput.setOnFocusChangeListener { v, hasFocus ->
//                binding.llAccount.isSelected = hasFocus
//                if (!hasFocus) {
//                    binding.ivAccountClear.visibility = INVISIBLE
//                } else {
//                    val account: String = (v as EditText).text.toString()
//                    if (!TextUtils.isEmpty(account)) {
//                        binding.ivAccountClear.visibility = VISIBLE
//                    }
//                }
//            }
//            binding.etAccountInput.requestFocus()
//
//            binding.ivAccountClear.visibility = GONE
//            binding.ivAccountClear.setOnClickListener {
//                // 清除输入的账号数据
//                binding.etAccountInput.text = null
//            }
//        }
//    }
//
//    /**
//     * 获取输入的string
//     *
//     * @return String input的值
//     */
//    fun getText(): String {
//        return binding.etAccountInput.text.toString().trim()
//    }
//
//    /**
//     * 更新text
//     *
//     * @param text String?  字符串
//     */
//    fun setText(text: String?) {
//        if (text.isNullOrEmpty()) {
//            binding.etAccountInput.setText("")
//        } else {
//            binding.etAccountInput.setText(text)
//        }
//    }
//
//    /**
//     * 设置et的hint text
//     *
//     * @param text String 提示语
//     */
//    fun setHintText(text: String) {
//        binding.etAccountInput.hint = text
//    }
//
//    fun addTextChangeWatcher(watcher: TextChangeWatcher) {
//        this.watcher = watcher
//    }
//
//    interface TextChangeWatcher {
//        fun onTextChange(s: String)
//    }
//
//    override fun setFocusable(focusable: Boolean) {
//        super.setFocusable(focusable)
//        GwellLogUtils.i(TAG, "setFocusable: focusable = $focusable")
//        binding.etAccountInput.isFocusable = focusable
//    }
//
//}