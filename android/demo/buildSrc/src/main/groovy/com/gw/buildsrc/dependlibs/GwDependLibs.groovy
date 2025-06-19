package com.gw.buildsrc.dependlibs

class GwDependLibs {

    static class GwLibs {
        /**
         * IoTVideo库
         */
//        public static final String DEPEND_LIB_IOT_VIDEO = "com.tencentcs:iotvideo_beta"
        public static final String DEPEND_LIB_IOT_VIDEO = "com.gwell:iotvideo-multiplatform"

        /**
         * IoTVideo网络请求库
         */
//        public static final String DEPEND_LIB_IOT_ACCOUNT_MGR = "com.tencentcs:accountmgr"
        public static final String DEPEND_LIB_IOT_ACCOUNT_MGR = "com.tencentcs:accountmgr"

        /**
         * 蓝牙配网库
         */
        public static final String DEPEND_LIB_BLE_CONFIG_AAR = "com.gwell:bleconfig"
//        public static final String DEPEND_LIB_BLE_CONFIG_AAR = "com.gwell:bleconfig_beta_debug"

        /**
         * VAS库
         */
        public static final String DEPEND_LIB_IOT_VAS = "com.tencentcs:iotvas_beta"
//        public static final String DEPEND_LIB_IOT_VAS = "com.tencentcs:iotvas_beta_debug"

        /**
         * 技威数据统计
         */
        public static final String DEPEND_LIB_BASE_STATISTICS = "com.gwell:base_statistics"
//        public static final String DEPEND_LIB_BASE_STATISTICS = "com.gwell:base_statistics_snapshot"

        /**
         * 技威数生命周期
         */
        public static final String DEPEND_LIB_BASE_LIFECYCLE = "com.gwell:base_lifecycle"
//        public static final String DEPEND_LIB_BASE_LIFECYCLE = "com.gwell:base_lifecycle_snapshot"

        /**
         * IOT-SAAS库
         */
        public static final String DEPEND_LIB_IOT_SAAS = "com.gwell:iot-saas"
//        public static final String DEPEND_LIB_IOT_SAAS = "com.gwell:iot-saas-debug"

        /**
         * lifecycle-common库
         */
        public static final String LIB_LIFECYCLE_COMMON = "androidx.lifecycle:lifecycle-runtime-ktx"

        /**
         * 技威xlog日志库
         */
        public static final String DEPEND_LIB_X_LOG = "com.tencentcs:iotxlog"

        /**
         * 技威云播放器库
         */
        public static final String DEPEND_LIB_CLOUD_PLAYER = "com.gwell:cloud_player"
    }

    /**
     * 引入ReoqooSdk库
     */
    static class ReoqooSdkLibs {
        /**
         * 组件库
         */
        public static final String COMP_NAME = "com.reoqoo_comp:"
//        public static final String COMP_NAME = "com.gwell:reoqoo_comp:"

        /**
         * library库
         */
        public static final String LIB_NAME = "com.reoqoo_lib:"
//        public static final String LIB_NAME = "com.gwell:reoqoo_lib:"

    }
}
