package com.gw_reoqoo.cp_account.ui.fragment.area_list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountWidgetAreaItemBinding
import com.gw_reoqoo.lib_http.entities.DistrictEntity

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/1/3 14:38
 * Description: DistrictAreaListAdapter
 */
class DistrictAreaListAdapter(private var mDistrictList: List<DistrictEntity>?) :

    RecyclerView.Adapter<DistrictAreaListAdapter.DistrictViewHolder>(), Filterable {

    /**
     * 搜索过滤之后的数据源
     */
    private var mFilterList: List<DistrictEntity>? = mDistrictList

    /**
     * item点击监听
     */
    private var mListener: OnDistrictItemClickListener? = null

    /**
     * 更新地区列表信息
     *
     * @param list List<DistrictEntity>? 地区列表
     */
    fun updateDistrictList(list: List<DistrictEntity>?) {
        list?.let {
            mDistrictList = it
            mFilterList = it
            notifyDataSetChanged()
        } ?: let {
            mDistrictList = emptyList()
            mFilterList = emptyList()
            notifyDataSetChanged()
        }
    }

    /**
     * 设置item点击监听
     *
     * @param listener OnDistrictItemClickListener 监听
     */
    fun setOnDistrictItemClickListener(listener: OnDistrictItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistrictViewHolder {
        val binding: AccountWidgetAreaItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.account_widget_area_item, parent, false)
        return DistrictViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DistrictViewHolder, position: Int) {
        mFilterList?.get(position)?.let {
            holder.bindData(it)
        }
    }

    override fun getItemCount(): Int {
        return mFilterList?.size ?: let { 0 }
    }

    inner class DistrictViewHolder(private var binding: AccountWidgetAreaItemBinding) : ViewHolder(binding.root) {

        fun bindData(bean: DistrictEntity) {
            binding.tvDistrictArea.text = bean.districtName
            binding.tvDistrictCode.text = buildString {
                append("+")
                append(bean.districtCode)
            }
            binding.clDistrictItem.setOnClickListener {
                mListener?.onItemClicked(bean)
            }
        }

    }

    /**
     * item点击事件接口
     */
    interface OnDistrictItemClickListener {
        fun onItemClicked(bean: DistrictEntity)
    }

    /**
     * 搜索过滤器
     *
     * @return Filter过滤器
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                mFilterList = if (charString.isEmpty()) {
                    mDistrictList
                } else {
                    val filteredList: MutableList<DistrictEntity> = ArrayList()
                    mDistrictList?.let {
                        for (str in it) {
                            //这里根据需求，添加匹配规则
                            if (str.districtName.contains(charSequence, true) || str.districtCode.contains(charSequence, true)) {
                                filteredList.add(str)
                            }
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = mFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                mFilterList = filterResults.values as List<DistrictEntity>?
                //刷新数据
                notifyDataSetChanged()
            }
        }
    }

}