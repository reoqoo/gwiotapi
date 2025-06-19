package com.gw.cp_mine.ui.activity.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gw.cp_mine.R
import com.gw.cp_mine.entity.SettingsEntity

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 14:28
 * Description: SettingsAdapter
 */
class SettingsAdapter(
    private var items: List<SettingsEntity>,
    private var listener: ItemClickListener
) :
    RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    /**
     * 在view更新完后等initData开始后再更新adapter的列表和更新视图
     * @param menuEntityList recyclerView的列表数据
     */
    fun updateData(menuEntityList: List<SettingsEntity>) {
        items = menuEntityList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.mine_list_setting_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.findViewById<View>(R.id.cl_item).setOnClickListener {
            listener.onItemClick(items[position])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SettingsEntity) {
            itemView.findViewById<TextView>(R.id.title).setText(item.title)
            itemView.findViewById<TextView>(R.id.description).setText(item.description)
            itemView.findViewById<TextView>(R.id.status).text =
                when (item.status) {
                    1 -> {
                        itemView.context.getString(RR.string.AA0618)
                    }

                    0 -> {
                        itemView.context.getString(RR.string.AA0617)
                    }

                    else -> {
                        ""
                    }
                }
        }
    }

    /**
     * ItemClickListener: 用于我的页面中recyclerView的点击事件
     * position：点击的item的下标
     */
    interface ItemClickListener {
        fun onItemClick(item: SettingsEntity)
    }

}