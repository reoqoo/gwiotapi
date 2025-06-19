package com.gw_reoqoo.cp_account.widget.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountDialogItemAvatarBinding
import com.gw_reoqoo.cp_account.databinding.AccountDialogListAvatarBinding
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.gw_reoqoo.lib_widget.dialog.base_dialog.BaseDialog
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.ui.DensityUtil

/**
 *@Description: 头像选择弹框
 *@Author: ZhangHui
 *@Date: 2022/4/27
 */
class AvatarSelectDialog : BaseDialog {

    companion object {
        private const val TAG = "AvatarSelectDialog"
    }

    /**
     * 点击事件
     */
    private var onAvatarClick: ((String) -> Unit)? = null

    /**
     * 列表适配器
     */
    private lateinit var mAdapter: AvatarListAdapter

    constructor(context: Context) : this(context, R.style.account_dialog)

    constructor(context: Context, theme: Int) : super(context, theme) {
        initView()
    }

    private fun initView() {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.account_dialog_list_avatar, null, false
        ) as AccountDialogListAvatarBinding
        setContentView(binding.root)
        val params = window?.attributes
        params?.width = DensityUtil.getScreenWidth(context)
        window?.attributes = params
        mAdapter = AvatarListAdapter()
        val avatarLM = GridLayoutManager(context, 5)
        avatarLM.orientation = RecyclerView.VERTICAL
        binding.rvAvatars.layoutManager = avatarLM
        binding.rvAvatars.adapter = mAdapter
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * 设置点击事件
     */
    fun setOnAvatarClick(onAvatarClick: ((String) -> Unit)?) {
        this.onAvatarClick = onAvatarClick
    }

    /**
     * 设置列表数据
     * @param data List<String>
     */
    fun setData(data: List<String>) {
        mAdapter.updateData(data)
    }

    var selectAvatar: String = ""
        set(value) {
            field = value
            mAdapter.notifyDataSetChanged()
        }

    inner class AvatarListAdapter : AbsDiffBDAdapter<AccountDialogItemAvatarBinding, String>() {

//        private var listener: ((String) -> Unit)? = null
//
//        fun setOnItemClick(onItemClick: ((String) -> Unit)?) {
//            listener = onItemClick
//        }

        override val layoutId: Int get() = R.layout.account_dialog_item_avatar

        override fun onCreateViewHolder(binding: AccountDialogItemAvatarBinding) {
            super.onCreateViewHolder(binding)
            binding.ivAvatar.setOnClickListener { v ->
                val avatarUrl = v.tag as String
                GwellLogUtils.i(TAG, "avatarUrl: $avatarUrl")
                onAvatarClick?.invoke(avatarUrl)
            }
        }

        override fun onBindViewHolder(binding: AccountDialogItemAvatarBinding, position: Int) {
            super.onBindViewHolder(binding, position)
            val avatarUrl = getItemData(position)
            binding.ivAvatar.tag = avatarUrl
            binding.ivAvatar.loadUrl(imageUrl = avatarUrl)
            GwellLogUtils.i(TAG, "selectAvatar: $selectAvatar, avatarUrl $avatarUrl")
            if (selectAvatar == avatarUrl) {
                binding.ivSelect.visibility = View.VISIBLE
            } else {
                binding.ivSelect.visibility = View.GONE
            }

        }

    }

}