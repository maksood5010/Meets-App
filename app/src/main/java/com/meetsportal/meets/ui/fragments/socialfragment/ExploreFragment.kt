package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment

class ExploreFragment : BaseFragment() {

    val TAG = ExploreFragment::class.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}