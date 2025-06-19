package com.gw_reoqoo.house_watch.uitls

import java.sql.Timestamp
import java.util.Calendar

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/17 14:16
 * Description: TimeUtils
 */
object TimeUtils {

    /**
     * 判断当前时间戳是否是昨天的日期
     *
     * @param timestamp Long 10位数的时间戳，
     */
    fun isBeforeYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (timestamp.toString().length == 10) timestamp * 1000L else timestamp
        }
        val dataYear = calendar.get(Calendar.YEAR)
        val dataMonth = calendar.get(Calendar.MONTH)
        val dataDay = calendar.get(Calendar.DAY_OF_MONTH)
        val now = Calendar.getInstance()
        val nowYear = now.get(Calendar.YEAR)
        val nowMonth = now.get(Calendar.MONTH)
        val nowDay = now.get(Calendar.DAY_OF_MONTH)

        if (dataYear < nowYear) return true
        if (dataMonth < nowMonth) return true
        return dataDay < nowDay
    }

}