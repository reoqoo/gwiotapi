package com.gw_reoqoo.cp_account.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountDialogListInputBinding
import com.gw_reoqoo.lib_widget.dialog.base_dialog.BaseDialog
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.ui.DensityUtil
import com.gw_reoqoo.resource.R as RR

/**
 *@Description: 通用字符串列表弹窗
 *@Author: ZhangHui
 *@Date: 2022/4/27
 */
class StringListInputDialog : BaseDialog {

    companion object {
        private const val TAG = "CommonStringListDialog"
    }

    /**
     * 列表适配器
     */
    private lateinit var mAdapter: StringListAdapter

    /**
     * 列表点击监听回调
     */
    private var mItemClickListener: OnReasonItemClickListener? = null

    lateinit var binding: AccountDialogListInputBinding

    constructor(context: Context) : this(context, R.style.account_dialog)
    constructor(context: Context, theme: Int) : super(context, theme) {
        initView()
    }

    private fun initView() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.account_dialog_list_input, null, false
        ) as AccountDialogListInputBinding
        setContentView(binding.root)
        val params = window?.attributes
        params?.width = DensityUtil.getScreenWidth(context)
        window?.attributes = params
        mAdapter = StringListAdapter(null)
        mAdapter.setOnItemClickListener { _, _, position ->
            binding.etOther.setText("")
            mAdapter.setSelectedIndex(position)
            checkBtnState()
        }
        binding.rvReasons.layoutManager = LinearLayoutManager(context)
        binding.rvReasons.adapter = mAdapter
        binding.etOther.addTextChangedListener {
            it?.run {
                if (isNotEmpty()) {
                    binding.tvSure.isEnabled = true
                    setSelectedIndex(-1)
                }
            }
            checkBtnState()
        }
        binding.tvCancel.setOnClickListener {
            setSelectedIndex(-1)
            dismiss()
        }
        binding.tvSure.setOnClickListener {
            val reason = if (mAdapter.getSelectedIndex() == -1) {
                binding.etOther.text.toString()
            } else {
                mAdapter.data[mAdapter.getSelectedIndex()]
            }
            mItemClickListener?.onItemClick(mAdapter.getSelectedIndex(), reason)
        }
    }

    /**
     * 设置列表数据
     *
     * @param data List<String>
     */
    fun setData(data: MutableList<String>) {
        mAdapter.setList(data)
    }

    /**
     * 设置列表点击监听
     *
     * @param itemClickListener OnReasonItemClickListener
     */
    fun setItemClickListener(itemClickListener: OnReasonItemClickListener) {
        mItemClickListener = itemClickListener
    }

    /**
     * 设置选中角标
     *
     * @param index Int
     */
    private fun setSelectedIndex(index: Int) {
        GwellLogUtils.i(TAG, "setSelectedIndex index:$index")
        mAdapter.setSelectedIndex(index)
    }

    private fun checkBtnState() {
        binding.tvSure.isEnabled = !(mAdapter.getSelectedIndex() == -1 &&
                binding.etOther.text.toString().isEmpty())
    }

    inner class StringListAdapter(data: List<String>?) :
        BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.account_dialog_item_list_input,
            data as MutableList<String>?
        ) {

        /**
         * 选中角标
         */
        private var mSelectedIndex = -1

        override fun convert(holder: BaseViewHolder, item: String) {
            holder.setText(R.id.tv_title, item)
            holder.getView<CheckBox>(R.id.cb_choose).isChecked =
                mSelectedIndex == data.indexOf(item)
            holder.setTextColor(
                R.id.tv_title,
                ContextCompat.getColor(context, RR.color.color_000000_90)
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setSelectedIndex(index: Int) {
//            if (index !in 0 until data.size) {
//                return
//            }
            mSelectedIndex = index
            notifyDataSetChanged()
        }

        fun getSelectedIndex(): Int {
            return mSelectedIndex
        }
    }

    interface OnReasonItemClickListener {
        fun onItemClick(index: Int, value: String)
    }
}