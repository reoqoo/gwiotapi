package com.gw.reoqoo.app

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.gw_reoqoo.module_mount.initializetask.AInitializeTask
import dagger.hilt.android.qualifiers.ApplicationContext
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import me.jessyan.autosize.utils.ScreenUtils
import javax.inject.Inject

/**
 * @Description: - 初始化AutoSize的配置
 * @Author: XIAOLEI
 * @Date: 2023/10/31
 *
 * @param context app
 */
class AutoSizeInitTask @Inject constructor(
    @ApplicationContext private val context: Context
) : AInitializeTask() {
    companion object {
        /**
         * 设计图纸的宽度基准单位
         */
        const val DESIGN_WITH_DP = 360

        /**
         * 设计图纸的高度基准单位
         */
        const val DESIGN_HEIGHT_DP = 780
    }

    private val autoSizeConfig by lazy { AutoSizeConfig.getInstance() }
    override fun run() {
        autoSizeConfig.isExcludeFontScale = true
        autoSizeConfig.onAdaptListener = object : onAdaptListener {
            override fun onAdaptBefore(target: Any?, activity: Activity?) {
                activity ?: return
                //使用以下代码, 可以解决横竖屏切换时的屏幕适配问题
                //使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时系统会重绘当前的页面, 
                //经测试在某些机型, 某些情况下系统不会重绘当前页面, ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
                val screenSize = ScreenUtils.getScreenSize(activity)
                if (screenSize.size < 2) return
                autoSizeConfig.screenWidth = screenSize.first()
                autoSizeConfig.screenHeight = screenSize.last()
                //根据屏幕方向，设置设计尺寸
                when (activity.resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        //设置横屏设计尺寸
                        autoSizeConfig.designWidthInDp = DESIGN_HEIGHT_DP
                        autoSizeConfig.designHeightInDp = DESIGN_WITH_DP
                    }

                    else -> {
                        //设置竖屏设计尺寸
                        autoSizeConfig.designWidthInDp = DESIGN_WITH_DP
                        autoSizeConfig.designHeightInDp = DESIGN_HEIGHT_DP
                    }
                }
            }

            override fun onAdaptAfter(target: Any?, activity: Activity?) = Unit
        }
    }
}