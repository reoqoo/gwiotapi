package com.gw.reoqoo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gw.reoqoo.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/28 10:22
 * Description: FamilyFragment
 */
@AndroidEntryPoint
class FamilyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.app_fragment_family, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
    }

}