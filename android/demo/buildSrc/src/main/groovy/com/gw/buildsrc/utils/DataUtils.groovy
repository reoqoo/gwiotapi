package com.gw.buildsrc.utils;

class DataUtils {
    /**
     * 获取当前时间
     */
    static def getDay() {
        return new Date().format("yyyy-MM-dd", TimeZone.getTimeZone("UTC"))
    }

    static String getTime() {
        Date date = new Date()
        return "\"" + date.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+08:00")) + "\""
    }

    static String getCurrentTime() {
        Date date = new Date()
        return date.format("MMddHHmm", TimeZone.getTimeZone("GMT+08:00"))
    }
}