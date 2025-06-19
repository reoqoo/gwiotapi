package com.gw.cp_config.data.datasource

import com.gw_reoqoo.cp_account.api.kapi.IInterfaceSignApi
import com.gw.cp_config.impl.AppParamApiImpl
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.entities.AppConfigEntity
import com.gw_reoqoo.lib_http.typeSubscriber
import com.gw_reoqoo.lib_http.wrapper.HttpServiceWrapper
import com.gwell.loglibs.GwellLogUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.channels.Channel
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/6 20:19
 * Description: 远程获取配置信息的数据源
 */
class RemoteConfigDataSource @Inject constructor(
    private val httpService: HttpServiceWrapper,
    private val appParamApiImpl: AppParamApiImpl,
    private val signApi: IInterfaceSignApi
) {

    companion object {
        private const val TAG = "RemoteConfigDataSource"
    }

    suspend fun getAppConfig(isLogin: Boolean): RespResult<AppConfigEntity> {
        val result = Channel<RespResult<AppConfigEntity>>(1)
        val listener = typeSubscriber<AppConfigEntity>(
            onSuccess = {
                result.trySend(RespResult.Success(it))
            },

            onFail = { t ->
                if (t is ResponseNotSuccessException) {
                    result.trySend(RespResult.ServerError(t.code, t.message))
                } else {
                    GwellLogUtils.e(TAG, "checkEmailExist onFail: error = ${t.message}")
                    result.trySend(RespResult.LocalError(t))
                }
            }
        )
        GwellLogUtils.i(TAG, "getAppConfig isLogin = $isLogin")
        if (isLogin) {
            httpService.getReoqooConfigWithLogin(listener)
        } else {
            httpService.getReoqooConfig(listener)
        }
        return result.receive()
    }

    suspend fun downloadConfig(url: String, filePath: String): Boolean {
        val result = Channel<Boolean>(1)

        httpService.downloadFile(url, object : Observer<ResponseBody> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
                GwellLogUtils.e(TAG, "downloadConfig onFail: ${e.message}")
            }

            override fun onComplete() {
                GwellLogUtils.i(TAG, "downloadConfig onComplete")
            }

            override fun onNext(t: ResponseBody) {
                GwellLogUtils.i(TAG, "downloadConfig onNext")
                // 文件下载成功
                try {
                    // 创建文件
                    val file = File(filePath)
                    // 将ResponseBody写入文件
//                    GwellLogUtils.i(TAG, "downloadFile onSuccess: $t")
                    result.trySend(writeResponseBodyToDisk(t, file))
                } catch (e: IOException) {
                    GwellLogUtils.e(TAG, "downloadFile IOException: ${e.message}")
                    result.trySend(false)
                }
            }
        })
        return result.receive()
    }

    private fun writeResponseBodyToDisk(body: ResponseBody, file: File): Boolean {
        var sink: BufferedSink? = null
        return try {
            sink = file.sink().buffer()
            sink.writeAll(body.source())
            true
        } catch (e: IOException) {
            GwellLogUtils.e(TAG, "writeResponseBodyToDisk IOException: ${e.message}")
            false
        } finally {
            sink?.close()
        }
    }

}