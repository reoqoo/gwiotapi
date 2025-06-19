package com.gw.cp_mine.ui.fragment.mine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.gw.cp_mine.R
import com.gw.cp_mine.entity.MenuListEntity
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.ktx.visible

/**
 * @author: xuhaoyuan
 * @date: 2023/8/18
 * description:
 * 1. 我的页面中的recyclerView需要使用到的适配器，数据从mineActivity的VM中传入
 */
class MenuListAdapter : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "MenuListAdapter"
    }

    private var menuList: List<MenuListEntity>? = null
    private var mListener: ItemClickListener? = null

    /**
     * 在view更新完后等initData开始后再更新adapter的列表和更新视图
     * @param menuEntityList recyclerView的列表数据
     */
    fun updateData(menuEntityList: List<MenuListEntity>) {
        menuList = menuEntityList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.mine_rv_menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entity = menuList?.get(position)
        val isLastItem = position == (menuList?.size?.minus(1) ?: -1)
        if (isLastItem) {
            holder.line.visibility = View.GONE
        } else {
            holder.line.visibility = View.VISIBLE
        }
        val menuListEntity = menuList?.get(position)
        menuListEntity?.let {
            it.iconId?.let { resId ->
                holder.ivIcon.visible(true)
                holder.ivIcon.setImageResource(resId)
            } ?: let { holder.ivIcon.visible(false) }
            holder.tvFunction.setText(it.functionName)
        }
        holder.notice.visible(menuListEntity?.showNotice == true)
        holder.clItem.setSingleClickListener {
            entity?.let {
                mListener?.onItemClick(entity)
            }
        }
    }

    fun setOnItemClickListener(listener: ItemClickListener) {
        this.mListener = listener
    }

    override fun getItemCount(): Int {
        return menuList?.size ?: 0
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView
        val tvFunction: TextView
        val notice: View
        val line: View
        val clItem: ConstraintLayout

        init {
            line = view.findViewById(R.id.view_line)
            ivIcon = view.findViewById(R.id.iv_icon)
            tvFunction = view.findViewById(R.id.function_name)
            notice = view.findViewById(R.id.view_notice)
            clItem = view.findViewById(R.id.cl_item)
        }
    }

    /**
     * ItemClickListener: 用于我的页面中recyclerView的点击事件
     * position：点击的item的下标
     */
    interface ItemClickListener {
        fun onItemClick(item: MenuListEntity)
    }
}