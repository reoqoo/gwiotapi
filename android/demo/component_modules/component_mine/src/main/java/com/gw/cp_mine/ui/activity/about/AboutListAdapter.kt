package com.gw.cp_mine.ui.activity.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.gw.cp_mine.R
import com.gw_reoqoo.lib_utils.ktx.visible

/**
 * @author: xuhaoyuan
 * @date: 2023/8/30
 * description:
 * 1. 用于加载关于里面的recyclerView列表
 */
class AboutListAdapter(private val mDataList: MutableList<AboutEnum>) :
    RecyclerView.Adapter<AboutListAdapter.ViewHolder>() {

    var mOnItemClickListener: OnItemClickListener? = null

    companion object {
        private const val TAG = "AboutListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mine_about_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvItemName.setText(mDataList[position].strRes)
        holder.clItem.setOnClickListener {
            mOnItemClickListener?.onItemClick(mDataList[position])
        }
        holder.lineView.visible(position != mDataList.size - 1)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivArrow: ImageView = view.findViewById(R.id.iv_arrow)
        val tvItemName: TextView = view.findViewById(R.id.tv_item_name)
        val lineView: View = view.findViewById(R.id.v_line)
        val clItem: ConstraintLayout = view.findViewById(R.id.cl_item)
    }

    fun interface OnItemClickListener {
        fun onItemClick(item: AboutEnum)
    }

}