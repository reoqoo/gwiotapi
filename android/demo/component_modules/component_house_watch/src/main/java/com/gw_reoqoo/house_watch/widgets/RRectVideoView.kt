package com.gw_reoqoo.house_watch.widgets

import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.gw.player.render.GwVideoView
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.lib_utils.ktx.dp

/**
 * 支持圆角的VideoView的再次封装
 */
class RRectVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    /**
     * 圆角半径
     */
    var radius = 7f.dp
        set(value) {
            field = value
            invalidateOutline()
        }

    /**
     * 真实的VideoView
     */
    val videoView: GwVideoView
        get() {
            val videoView = this.findViewById<View>(R.id.video_view_sub)
            return videoView as GwVideoView
        }

    init {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val rect = Rect(0, 0, view.measuredWidth, view.measuredHeight)
                outline.setRoundRect(rect, radius)
            }
        }
        clipToOutline = true
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(
            R.layout.house_watch_gw_video_view,
            this,
            true
        )
    }
}