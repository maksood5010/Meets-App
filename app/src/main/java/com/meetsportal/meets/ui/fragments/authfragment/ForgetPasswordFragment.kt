package com.meetsportal.meets.ui.fragments.authfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment

class ForgetPasswordFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forget_password, container, false)
    }

    override fun onBackPageCome() {

    }

}