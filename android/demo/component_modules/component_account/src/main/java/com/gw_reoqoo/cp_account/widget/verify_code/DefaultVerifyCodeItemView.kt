package com.gw_reoqoo.cp_account.widget.verify_code

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.jwkj.base_utils.ui.DensityUtil


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/1/10 10:14
 * Description: 默认验证码itemView实现
 */
open class DefaultVerifyCodeItemView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), VerifyCodeItemViewStyle {

    companion object {
        private const val TAG = "DefaultVerifyCodeItemView"
    }

    /**
     * 显示的textView
     */
    private val tvValue = AppCompatTextView(context)

    /**
     * 控制光标的显示与隐藏
     */
    var cursorControl = false

    /**
     * 光标画笔
     */
    private val mPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, com.gw_reoqoo.resource.R.color.color_000000_90)
    }

    /**
     * 时间戳闪动控制器
     */
    private var timer: CountDownTimer? = null

    init {
        //自身相关属性设置
        orientation = VERTICAL
        gravity = Gravity.CENTER
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 对应activity onResume的适合被调用，即DecorView被添加到ViewRootImpl时，适合初始化操作
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //默认ViewGroup不会进行绘制 所以需要允许进行绘制
        setWillNotDraw(false)

        //textView 相关属性设置
        val width = DensityUtil.dip2px(context, 42F)
        val height = DensityUtil.dip2px(context, 60F)
        val lpt = LayoutParams(width, height)
        tvValue.setBackgroundColor(ContextCompat.getColor(context, com.gw_reoqoo.resource.R.color.transparent))
        tvValue.gravity = Gravity.CENTER
        tvValue.setTextColor(ContextCompat.getColor(context, com.gw_reoqoo.resource.R.color.color_000000))

        tvValue.textSize = 26f
        tvValue.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        //添加进容器
        addView(tvValue, lpt)

        //添加下划线
        val heightLine = DensityUtil.dip2px(context, 1f)
        val line = View(context).apply {
            setBackgroundColor(ContextCompat.getColor(context, com.gw_reoqoo.resource.R.color.color_000000_20))
        }
        val lll = LayoutParams(width, heightLine)
        //添加进容器
        addView(line, lll)
    }

    /**
     * 绘制光标
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        customOnDraw(canvas)
    }

    open fun customOnDraw(canvas: Canvas?) {
        if (cursorControl) {
            //cursor 宽度
            val cursorWidth = DensityUtil.dip2px(context, 1F).toFloat()

            //绘制坐标
            val xStart = width / 2 - cursorWidth / 2
            val xEnd = width / 2 + cursorWidth / 2
            val hStart = DensityUtil.dip2px(context, 7).toFloat()
            val hEnd = height.toFloat() - DensityUtil.dip2px(context, 7F)

            canvas?.drawRect(xStart, hStart, xEnd, hEnd, mPaint)
        }
    }

    override fun displayNumStyle(char: Char) {
        cursorControl = false
        timer?.cancel()
        tvValue.visibility = View.VISIBLE
        tvValue.text = char.toString()
        invalidate()
    }

    override fun cursorBlinksStyle() {
        tvValue.visibility = View.INVISIBLE
        //光标闪烁
        if (timer == null) {
            timer = object : CountDownTimer(Int.MAX_VALUE.toLong(), 600) {
                override fun onTick(millisUntilFinished: Long) {
                    cursorControl = !cursorControl
                    invalidate()
                }

                override fun onFinish() {

                }

            }
        }
        timer?.start()
    }

    override fun defaultStyle() {
        timer?.cancel()
        cursorControl = false
        tvValue.visibility = View.INVISIBLE
        invalidate()
    }

    /**
     * 对应activity destory的时候被调用，即ViewRootImpl做doDie时，适合销毁操作
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer?.cancel()
        timer = null
    }

}
