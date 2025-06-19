package com.gw_reoqoo.house_watch.ui.active_card.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.lib_http.entities.ActiveBean
import com.gw_reoqoo.lib_http.entities.TimeBean
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import java.util.LinkedList
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemActiveListBinding as ActiveBinding
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemActiveMenuBinding as MenuBinding
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemActiveTimeBinding as TimeBinding

/**
 * @Description: - 活动列表适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 *
 * @param list 列表数据
 * @param getDeviceName 根据设备ID获取对应的名称
 * @param showConfigDialog 显示配置弹窗
 * @param onClickActiveItem 点击某个事件item
 */
abstract class ActiveBaseAdapter(
    private val list: LinkedList<Any> = LinkedList(),
    private val showConfigDialog: () -> Unit,
    private val onClickActiveItem: (ActiveBean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        /**
         * 活动
         */
        const val TYPE_ACTIVE = 0x01

        /**
         * 日期
         */
        private const val TYPE_TIME = 0x02

        /**
         * 底部调整范围按钮
         */
        private const val TYPE_MENU = 0x03
    }

    /**
     * 覆盖更新数据
     * @param list 活动
     */
    fun update(list: List<ActiveBean>) {
        this.list.clear()
        this.list.addAll(list)
        this.notifyDataSetChanged()
    }

    fun updateAll(list: List<Any>) {
        this.list.clear()
        this.list.addAll(list)
        this.notifyDataSetChanged()
    }

    /**
     * 添加数据
     * @param list 活动
     */
    fun add(list: List<ActiveBean>) {
        this.list.addAll(list)
        this.notifyDataSetChanged()
    }

    /**
     * 底部调整范围按钮
     * @param bean 底部调整范围按钮
     */
    fun add(bean: Unit) {
        this.list.add(bean)
        this.notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is ActiveBean -> TYPE_ACTIVE
            is com.gw_reoqoo.lib_http.entities.TimeBean -> TYPE_TIME
            else -> TYPE_MENU
        }
    }

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ACTIVE -> {
                val binding = ActiveBinding.inflate(inflater, parent, false)
                binding.root.setSingleClickListener { v ->
                    val tag = v.tag
                    if (tag is ActiveBean) {
                        onClickActiveItem(tag)
                    }
                }
                binding.rvActiveIcons.run {
                    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    adapter = ActiveIconsAdapter()
                }
                ActiveViewHolder(binding)
            }

            TYPE_TIME -> {
                val binding = TimeBinding.inflate(inflater, parent, false)
                TimeViewHolder(binding)
            }

            else -> {
                val binding = MenuBinding.inflate(inflater, parent, false)
                binding.root.setOnClickListener {
                    showConfigDialog.invoke()
                }
                MenuViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = list[position]
        when (holder) {
            is ActiveViewHolder -> {
                if (bean is ActiveBean) {
                    onBindActiveViewHolder(holder.binding, position, bean)
                }
            }

            is TimeViewHolder -> {
                if (bean is com.gw_reoqoo.lib_http.entities.TimeBean) {
                    onBindTimeViewHolder(holder.binding, position, bean)
                }
            }

            is MenuViewHolder -> {
                onBindMenuViewHolder(holder.binding, position)
            }
        }
    }

    abstract fun onBindActiveViewHolder(binding: ViewDataBinding, position: Int, bean: ActiveBean)

    /**
     * 日期的item
     */
    private fun onBindTimeViewHolder(binding: TimeBinding, position: Int, bean: com.gw_reoqoo.lib_http.entities.TimeBean) {

    }

    /**
     * 绑定底部更多
     */
    private fun onBindMenuViewHolder(binding: MenuBinding, position: Int) {

    }

    /**
     * 活动
     */
    class ActiveViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 时间title
     *
     * @property binding HouseWatchItemActiveTimeBinding
     * @constructor
     */
    class TimeViewHolder(val binding: TimeBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 底部调整范围
     */
    class MenuViewHolder(val binding: MenuBinding) : RecyclerView.ViewHolder(binding.root)
}