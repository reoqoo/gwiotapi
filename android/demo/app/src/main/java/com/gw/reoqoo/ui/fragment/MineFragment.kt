package com.gw.reoqoo.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gw.reoqoo.R

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/28 10:23
 * Description: MineFragment
 */
class MineFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.app_fragment_mine, container, false)
    }
}