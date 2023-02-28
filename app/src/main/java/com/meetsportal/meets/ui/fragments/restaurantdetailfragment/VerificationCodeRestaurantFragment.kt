package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.singleCharector

class VerificationCodeRestaurantFragment :BaseFragment() {

    private val TAG = VerificationCodeRestaurantFragment::class.java.simpleName

    lateinit var et1 :EditText
    lateinit var et2 :EditText
    lateinit var et3 :EditText
    lateinit var et4 :EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verification_code,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP()

    }

    private fun initView(view: View) {
        et1 =  view.findViewById(R.id.et1)
        et2 =  view.findViewById(R.id.et2)
        et3 =  view.findViewById(R.id.et3)
        et4 =  view.findViewById(R.id.et4)
    }

    private fun setUP() {
        et1.singleCharector { count -> if (count==1) et2.requestFocus()}
        et2.singleCharector { count -> if (count==1) et3.requestFocus() else et1.requestFocus()}
        et3.singleCharector { count -> if (count==1) et4.requestFocus() else et2.requestFocus()}
        et4.singleCharector { count -> if (count==1) showPopup()}
    }

    private fun showPopup() {
        Log.i(TAG,"show Text")
        clearTextBox()

        //var b  = ForceUpdateAlert(requireContext(),true)
            //b.show()

        //Navigation.replaceFragment(requireActivity(), MyTableBookingFragment(),"MyTableBookingFragment",
          //  R.id.restrauntFrame, stack = true, needAnimation = false)

        Navigation.addFragment(
            activity as Activity,
            MyTableBookingFragment(),
            Constant.TAG_EDIT_PROFILE,
            R.id.restrauntFrame,
            stack = true,
            needAnimation = false
        )

        //Navigation.removeFragment(requireContext(),"verifingCode")

    }

    private fun clearTextBox() {
        et1.setText("")
        et2.setText("")
        et3.setText("")
        et4.setText("")
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}