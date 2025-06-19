package com.gw.cp_msg.utils

import android.app.Activity
import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.gwell.loglibs.GwellLogUtils

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/10/7 15:02
 * Description: PushUtils
 */
object PushUtils {

    private const val TAG = "PushUtils"

    const val PUSH_SETTINGS_CODE = 1000

    /**
     * 获取通知开关状态 方法名
     */
    private const val CHECK_OP_NO_THROW = "checkOpNoThrow"

    /** 获取通知开关状态 属性名 */
    private const val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"

    /**
     * 判断当前App是否打开通知
     *
     * @return true：已开启， false：未开启
     */
    fun isNotificationEnabled(app: Application): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManagerCompat.from(app).areNotificationsEnabled()
        } else {
            val manager = app.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val packageName: String = app.packageName
            val uid: Int = app.applicationInfo.uid
            var appOpsClass: Class<*>?
            try {
                appOpsClass = Class.forName(AppOpsManager::class.java.name)
                val checkOpNoThrowMethod = appOpsClass.getMethod(
                    CHECK_OP_NO_THROW, Integer.TYPE,
                    String::class.java
                )
                val opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
                val value = opPostNotificationValue[Int::class.java] as Int
                return checkOpNoThrowMethod.invoke(
                    manager,
                    value,
                    uid,
                    packageName
                ) as Int == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                GwellLogUtils.e(TAG, "isNotificationEnabled error: msg is ${e.message}")
                false
            }
        }
    }

    /**
     * 前往设置页打开通知权限
     */
    fun openNotification(context: Context) {
        val intent = Intent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
            } else {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
        } catch (exception: java.lang.Exception) {
            GwellLogUtils.e(TAG, "openNotification error:" + exception.message)
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
        }
        if (context is Activity) {
            context.startActivityForResult(intent, PUSH_SETTINGS_CODE)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

}