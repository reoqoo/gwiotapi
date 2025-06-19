package com.gw.cp_msg.manger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw.cp_msg.api.kapi.IBenefitsApi
import com.gw.cp_msg.entity.http.EventBenefitsEntity.Companion.STATUS_HAVE_READ
import com.gw.cp_msg.entity.http.EventBenefitsEntity.Companion.STATUS_UNREAD_NOTICE
import com.gw.cp_msg.entity.http.Notice
import com.gw.cp_msg.repository.NoticeRepository
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/3/1 18:14
 * Description: BenefitsMgrImpl
 */
@Singleton
class BenefitsMgrImpl @Inject constructor(
    private val repository: NoticeRepository
) : IBenefitsApi {

    companion object {
        private const val TAG = "BenefitsMgrImpl"
    }

    private val benefitsLiveData: MutableLiveData<Int> = MutableLiveData()

    private var benefitsList: List<Notice>? = emptyList()

    private val scope by lazy {
        MainScope()
    }

    /**
     * 获取活动福利列表
     */
    override fun loadBenefits() {
        loadBenefits(null, null, null)
    }

    /**
     * 获取活动福利列表
     */
    fun loadBenefits(
        onStart: (() -> Unit)? = null,
        onSuccess: ((List<Notice>?) -> Unit)? = null,
        onFail: ((Throwable) -> Unit)? = null
    ) {
        scope.launch {
            repository.getEventBenefits().collect {
                when (it) {
                    is HttpAction.Loading -> {
                        onStart?.invoke()
                    }

                    is HttpAction.Success -> {
                        GwellLogUtils.i(TAG, "eventBenefits ${it.data}")
                        it.data?.noticeList?.let { list ->
                            benefitsList = list.ifEmpty {
                                emptyList()
                            }
                            val unReadNotice = benefitsList?.filter { notice ->
                                notice.status == STATUS_UNREAD_NOTICE
                            }
                            benefitsLiveData.postValue(unReadNotice?.size ?: 0)
                        } ?: run {
                            benefitsList = emptyList()
                        }
                        onSuccess?.invoke(benefitsList)
                    }

                    is HttpAction.Fail -> {
                        GwellLogUtils.e(TAG, "eventBenefits fail: error ${it.t.message}")
                        onFail?.invoke(it.t)
                    }
                }
            }
        }
    }

    /**
     * 设置活动福利状态为已读
     */
    fun setBenefitsStatusRead() {
        val benefits = benefitsList ?: emptyList()
        if (benefits.isEmpty()) {
            return
        }
        val unReadIds = benefits.filter { notice ->
            notice.status == STATUS_UNREAD_NOTICE
        }.map { it.id }.toLongArray()
        scope.launch {
            val success = repository.setBenefitsStatusRead(unReadIds)
            if (success == true) {
                benefitsList?.forEach { notice ->
                    if (unReadIds.contains(notice.id)) {
                        notice.status = STATUS_HAVE_READ
                    }
                }
            } else {
                GwellLogUtils.e(TAG, "setBenefitsStatusRead fail")
            }
            val unReadNotice = benefitsList?.filter { notice ->
                notice.status == STATUS_UNREAD_NOTICE
            }
            benefitsLiveData.postValue(unReadNotice?.size ?: 0)
        }
    }

    /**
     * 获取未读福利数量
     *
     * @return Int? 数量
     */
    override fun getUnReadBenefitsCount(): LiveData<Int> {
        return benefitsLiveData
    }

}