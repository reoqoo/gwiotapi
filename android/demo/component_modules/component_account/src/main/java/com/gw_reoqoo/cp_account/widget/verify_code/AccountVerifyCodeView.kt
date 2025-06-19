package com.gw_reoqoo.cp_account.widget.verify_code

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.gw_reoqoo.cp_account.R
import com.gwell.loglibs.GwellLogUtils
import java.lang.reflect.Field

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/1/10 10:24
 * Description: 验证码容器
 */
open class AccountVerifyCodeView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "AccountVerifyCodeView"
    }

    /**
     * 验证码item容器
     */
    private val itemContainer = LinearLayout(context)

    /**
     * 输入控件
     */
    private val editText = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        isLongClickable = true
        setBackgroundColor(ContextCompat.getColor(context, com.gw_reoqoo.resource.R.color.transparent))
        setTextColor(ContextCompat.getColor(context, com.gw_reoqoo.resource.R.color.transparent))
        isCursorVisible = true
    }

    /**
     * 验证码位数，默认为6，允许更改范围为1-8
     */
    private var verifyNum: Int = 6
        set(value) {
            field = if (value >= 1 || value <= 8) {
                value
            } else {
                6
            }

            //设置EditText的最大输入数量
            editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(field))
        }

    /**
     * 完整监听
     */
    var inputCompleteListener: InputCompleteListener? = null

    init {
        // 解析xml属性
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AccountVerifyCodeView)
        // 验证码位数，默认为6，允许更改
        verifyNum = attributes.getInt(R.styleable.AccountVerifyCodeView_accountMaxLength, 6)
        attributes.recycle()

        // itemContainer 填充默认的itemView
        fillUpDefaultItemView()
        isFocusable = true
        editText.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cursor: Drawable? = context.getDrawable(R.drawable.gw_reoqoo_edit_cursor_bg_transparent)
            editText.textCursorDrawable = cursor
        } else {
            try {
                val field: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                field.isAccessible = true
                field.set(editText, R.drawable.gw_reoqoo_edit_cursor_bg_transparent)
            } catch (e: Exception) {
                GwellLogUtils.e(TAG, "mCursorDrawableRes: ${e.message}")
            }
        }
    }

    /**
     * 初始化相关操作
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 添加 容器
        addView(itemContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        // 添加 输入框EditText
        addView(editText, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // editText监听处理
        editTextListener()

        // 默认第一个为itemView 进行光标闪烁
        itemContainer.takeIf { it.childCount > 0 }?.let { linearLayout ->
            val itemView = try {
                linearLayout.getChildAt(0) as VerifyCodeItemViewStyle
            } catch (e: Exception) {
                throw RuntimeException("VerifyItemView 必须是 VerifyCodeItemViewStyle 类型的", e)
            }
            itemView.cursorBlinksStyle()
        }
    }

    /**
     * editTextListener 监听处理
     */
    private fun editTextListener() {
        editText.afterTextChanged { editable: Editable? ->
            val container = itemContainer
            val childCount = container.childCount
            val content = editable.toString()
            val inputCount = content.length

            for (i in 0 until childCount) {
                // 获取itemView
                val itemView = try {
                    container.getChildAt(i) as VerifyCodeItemViewStyle
                } catch (e: Exception) {
                    throw RuntimeException(
                        "VerifyItemView 必须是 VerifyCodeItemViewStyle 类型的",
                        e
                    )
                }

                if (i < inputCount) {
                    // 设置显示的字体
                    val singleChar = content[i]
                    itemView.displayNumStyle(singleChar)
                    continue
                }

                // 光标闪烁
                if (i == inputCount) {
                    itemView.cursorBlinksStyle()
                    continue
                }

                // 什么都不显示
                itemView.defaultStyle()
            }

            // 验证码是否完成回调
            if (inputCount >= childCount) {
                inputCompleteListener?.inputComplete()
            } else {
                inputCompleteListener?.invalidContent()
            }
        }
    }

    /**
     * 对外提供一个getEditContent方法
     */
    open fun getEditContent(): String? = editText.text.toString()

    /**
     * 对外提供清空验证码能力,若为清空就弹出键盘
     */
    open fun setText(activity: Activity?, inputContent: String?) {
        editText.setText(inputContent)
        if (TextUtils.isEmpty(inputContent)) {
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            if (editText.context is Activity) {
                (editText.context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            } else {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
            }
        }
    }

    /**
     * 填充默认的itemView
     */
    private fun fillUpDefaultItemView() {
        for (i in 0 until verifyNum) {
            val defaultVerifyCodeItemView = DefaultVerifyCodeItemView(context)
            itemContainer.addView(
                defaultVerifyCodeItemView, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F
                )
            )
        }
    }

    /**
     * 填充自己个性化的itemView
     */
    open fun <T : VerifyCodeItemViewStyle> fillUpCustomItemView(customItemView: Class<T>) {
        if (customItemView == VerifyCodeItemViewStyle::class.java) {
            // 先移除所有的View
            itemContainer.removeAllViews()
            for (i in 0 until verifyNum) {
                val verifyCodeItemView = try {
                    customItemView.getConstructor(Context::class.java).newInstance(context) as View
                } catch (e: Exception) {
                    throw RuntimeException(
                        "不能够创建一个实例 $customItemView , 或者不是一个View类型",
                        e
                    )
                }
                //填充View
                itemContainer.addView(
                    verifyCodeItemView, LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F
                    )
                )
            }
        } else {
            // 如果不是VerifyCodeItemViewStyle类型，则抛出异常。否则运行时会崩溃
            throw RuntimeException(
                "VerifyItemView 必须是 VerifyCodeItemViewStyle 类型的"
            )
        }
    }


    /**
     * afterTextChanged
     */
    inline fun EditText.afterTextChanged(crossinline block: (Editable?) -> Unit = {}): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                block(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }.also {
            addTextChangedListener(it)
        }

    }
}
