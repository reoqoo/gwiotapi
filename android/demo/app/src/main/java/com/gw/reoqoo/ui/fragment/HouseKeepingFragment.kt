package com.gw.reoqoo.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gw.reoqoo.R

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/28 10:22
 * Description: HouseKeepingFragment
 */
class HouseKeepingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.app_fragment_house_keeping, container, false)
    }
}