package com.gw.component_push.service;

import com.jwkj.iotvideo.httpviap2p.Field;
import com.jwkj.iotvideo.httpviap2p.HttpViaP2PMethod;
import com.jwkj.iotvideo.player.api.IIoTCallback;

/**
 * 离线推送接口
 *
 * @date 2020/11/23
 */
public interface AlarmPushService {


    /**
     * 离线推送注册
     *
     * @param termId   终端ID
     * @param osType   系统类型
     * @param timeZone 时区（单位秒）
     * @param language 语言
     * @param appId    appId
     * @param phoneId  手机唯一Id
     * @param jPushId  极光推送Id
     */
    @HttpViaP2PMethod(url = "offlinePush/register", method = "POST")
    void registerPush(@Field("TermId") String termId,
                      @Field("OsType") int osType,
                      @Field("Zone") int timeZone,
                      @Field("Lang") String language,
                      @Field("AppId") String appId,
                      @Field("PhoneId") String phoneId,
                      @Field("JpushId") String jPushId,
                      IIoTCallback listener);


    /**
     * 离线推送注册
     *
     * @param termId      终端ID
     * @param osType      系统类型
     * @param timeZone    时区（单位秒）
     * @param language    语言
     * @param appId       appId
     * @param phoneId     手机唯一Id
     * @param jPushId     极光推送Id
     * @param osVer       系统版本
     * @param sdkVer      sdk版本
     * @param appVer      app版本
     * @param osPushId    系统推送Id
     * @param mfrPushId   制造商推送Id
     * @param mfrName     制造商名称
     * @param mfrDevModel 手机型号
     */
    @HttpViaP2PMethod(url = "offlinePush/register", method = "POST")
    void registerPush(@Field("TermId") String termId,
                      @Field("OsType") int osType,
                      @Field("Zone") int timeZone,
                      @Field("Lang") String language,
                      @Field("AppId") String appId,
                      @Field("PhoneId") String phoneId,
                      @Field("JpushId") String jPushId,
                      @Field("OsVer") String osVer,
                      @Field("SdkVer") String sdkVer,
                      @Field("AppVer") String appVer,
                      @Field("OsPushId") String osPushId,
                      @Field("MfrPushId") String mfrPushId,
                      @Field("MfrName") String mfrName,
                      @Field("MfrDevModel") String mfrDevModel,
                      IIoTCallback listener);


    /**
     * 注销推送
     *
     * @param termId    终端Id
     */
    @HttpViaP2PMethod(url = "offlinePush/unregister", method = "POST")
    void unRegister(@Field("TermId") String termId,
                    IIoTCallback listener);


}
