package com.meetsportal.meets.ui.fragments.socialonboarding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment

class SocialOnBoarding2Fragment : BaseFragment(), View.OnClickListener {


    val TAG= SocialOnBoarding2Fragment::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_social_onboarding1,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)

    }

    private fun initView(view: View){

    }

    override fun onClick(v: View?) {

    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }


}