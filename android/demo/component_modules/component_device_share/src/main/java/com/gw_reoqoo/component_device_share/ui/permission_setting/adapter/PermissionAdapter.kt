package com.gw_reoqoo.component_device_share.ui.permission_setting.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.lib_http.toJson
import com.gwell.loglibs.GwellLogUtils

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/7/13 15:40
 * Description: PermissionAdapter
 */
class PermissionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "PermissionAdapter"
    }

    private var permissions: List<PermissionEntity>? = null

    private var listener: OnSwitchChangeListener? = null

    fun setData(permissionList: List<PermissionEntity>?) {
        this.permissions = permissionList
        notifyDataSetChanged()
    }

    fun setListener(listener: OnSwitchChangeListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val view: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.dev_share_permission_item_title, parent, false)
            return TitleHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.dev_share_permission_item_function, parent, false)
            return FunctionHolder(view)
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        permissions?.let {
            for (group in it) {
                count += group.function.size + 1
            }
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        var count = 0
        permissions?.let {
            for (group in it) {
                val size: Int = group.function.size + 1
                if (position < count + size) {
                    return if (position == count) 0 else 1
                }
                count += size
            }
        }
        throw java.lang.IllegalArgumentException("Invalid position")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var count = 0
        permissions?.let {
            for (permissionEntity in it) {
                val size: Int = permissionEntity.function.size + 1
                if (position < count + size) {
                    GwellLogUtils.i(TAG, "position: $position, count: $count, size: $size")
                    if (position == count) {
                        val titleHolder: TitleHolder = holder as TitleHolder
                        titleHolder.tvTitle.text = permissionEntity.title
                    } else {
                        val functionHolder: FunctionHolder = holder as FunctionHolder
                        val funcEntity = permissionEntity.function[position - count - 1]
                        GwellLogUtils.i(TAG, "funcEntity: ${funcEntity.toJson()}")
                        functionHolder.tvName.text = funcEntity.functionName
                        functionHolder.switchStatus.isChecked = funcEntity.functionStatus
                        functionHolder.switchStatus.isEnabled = funcEntity.functionEnable
                        functionHolder.switchStatus.setOnClickListener {
                            listener?.onSwitchChange(
                                funcEntity,
                                functionHolder.switchStatus.isChecked
                            )
                        }
                        functionHolder.switchStatus.alpha = if (funcEntity.functionEnable) {
                            1.0f
                        } else {
                            0.6f
                        }
                        if (count + 1 == count + size - 1) {
                            // 说明只有一项，则设置背景
                            functionHolder.viewLayout.setBackgroundResource(R.drawable.dev_share_bg_white_r12)
                            functionHolder.line.visibility = View.GONE
                        } else if (position == count + 1) {
                            functionHolder.viewLayout.setBackgroundResource(R.drawable.dev_share_bg_white_r12_top)
                            functionHolder.line.visibility = View.VISIBLE
                        } else if (position == count + size - 1) {
                            functionHolder.viewLayout.setBackgroundResource(R.drawable.dev_share_bg_white_r12_bottom)
                            functionHolder.line.visibility = View.GONE
                        } else {
                            functionHolder.viewLayout.setBackgroundColor(holder.itemView.context.getColor(RR.color.white_100))
                            functionHolder.line.visibility = View.VISIBLE
                        }
                    }
                    return
                }
                count += size
            }
        }
        throw IllegalArgumentException("Invalid position")
    }

    class TitleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    }

    class FunctionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val switchStatus: CheckBox = itemView.findViewById(R.id.switch_status)
        val viewLayout: LinearLayout = itemView.findViewById(R.id.ll_layout)
        val line: View = itemView.findViewById(R.id.view_line)
    }

    interface OnSwitchChangeListener {
        fun onSwitchChange(item: FunctionEntity, isChecked: Boolean)
    }

}