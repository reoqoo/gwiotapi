package com.gw.component_push.manager;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gw.component_push.service.AlarmPushService;
import com.gw.player.entity.ErrorInfo;
import com.gwell.loglibs.GwellLogUtils;
import com.jwkj.iotvideo.constant.IoTError;
import com.jwkj.iotvideo.httpviap2p.HttpViaP2PProxy;
import com.jwkj.iotvideo.init.IoTVideoInitializer;
import com.jwkj.iotvideo.player.api.IIoTCallback;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * T平台 设备报警推送
 */
public class IoTAlarmPushManager {
    private static final String TAG = "IoTAlarmPushManager";
    private AlarmPushService pushService;

    private HttpViaP2PProxy serviceProxy;

    private static class IoTAlarmPushManagerHolder {
        private static final IoTAlarmPushManager INSTANCE = new IoTAlarmPushManager();
    }

    private IoTAlarmPushManager() {
        serviceProxy = new HttpViaP2PProxy();
        pushService = serviceProxy.create(AlarmPushService.class);
    }

    public static IoTAlarmPushManager instance() {
        return IoTAlarmPushManagerHolder.INSTANCE;
    }

    public void init(Map<String, Object> publicParams) {
        serviceProxy.clearPublicParams();
        serviceProxy.setPublicParams(publicParams);
    }

    /**
     * 厂商 绑定 设备推送
     *
     * @param jpushId        极光推送的token
     * @param appVer         app版本（类似：00.46.00.84）需要从其他地方传进来
     * @param registrationID 厂商的token值。只有获取了token才会获取得到（在这里不会为空）
     * @param mfrName        制造商名称(小米: XIAOMI, 华为: HUAWEI, 荣耀: HONOR, vivo: VIVO, oppo: OPPO )
     * @param mfrDevModel    制造商手机型号
     */
    public void registerIoTPush(String termId, int osType, int timeZone, String language, String appId,
                                String phoneId, String jpushId, String osVer, String sdkVer, String appVer, String osPushId,
                                String registrationID, String mfrName, String mfrDevModel, IIoTCallback<String> callback) {
        GwellLogUtils.i(TAG, "registerIoTPush: 推送厂商通道的token开始上传, registrationID = "
                + registrationID + ", mfrPushId = " + mfrName + ", jPushId = " + jpushId + ", mfrDevModel: " + mfrDevModel);
        pushService.registerPush(termId, osType, timeZone, language, appId,
                phoneId, jpushId, osVer, sdkVer, appVer, osPushId,
                registrationID, mfrName, mfrDevModel,
                callback);
    }

    /**
     * 注销 设备推送
     *
     * @param unregisterSdk 是否需要注销SDK
     */
    public void unRegisterPush(String terminalId, boolean unregisterSdk) {
        pushService.unRegister(terminalId,
                new IIoTCallback<String>() {

                    @Override
                    public void onError(@Nullable ErrorInfo errorInfo) {

                    }

                    @Override
                    public void onSuccess(String result) {
                        GwellLogUtils.d(TAG, "unregister push success");
                        if (unregisterSdk) {
                            GwellLogUtils.d(TAG, "unregister sdk");
                            IoTVideoInitializer.INSTANCE.unregister();
                        }
                    }

                    @Override
                    public void onStart() {
                        GwellLogUtils.d(TAG, "unregister push start");
                        GwellLogUtils.d(TAG, "registerIoTPush: 厂商, IoT register push start");
                    }

                    @Override
                    public void onError(@NonNull IoTError ioTError) {
                        GwellLogUtils.e(TAG, "unregister push failed:" + ioTError);
                    }
                });
    }

}
