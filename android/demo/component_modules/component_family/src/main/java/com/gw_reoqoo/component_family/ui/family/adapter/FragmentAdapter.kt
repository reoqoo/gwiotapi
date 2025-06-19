package com.gw_reoqoo.component_family.ui.family.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gw_reoqoo.component_family.ui.family.bean.FragmentBeanWrapper
import com.gw_reoqoo.lib_router.createFragment
import java.util.LinkedList

/**
 * @Description: - family列表的适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
class FragmentAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {
    private val list = LinkedList<FragmentBeanWrapper>()
    override fun getItemId(position: Int): Long {
        return list[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return list.any { it.hashCode().toLong() == itemId }
    }
    override fun getItemCount() = list.size
    override fun createFragment(position: Int): Fragment {
        val wrapper = list[position]
        val uri = wrapper.fragmentUrl
        return uri.createFragment(params = wrapper.params)
            ?: throw RuntimeException("找不到$uri 对应的Fragment路由")
    }

    fun updateData(newData: List<FragmentBeanWrapper>) {
        list.clear()
        list.addAll(newData)
        this.notifyDataSetChanged()
    }
}