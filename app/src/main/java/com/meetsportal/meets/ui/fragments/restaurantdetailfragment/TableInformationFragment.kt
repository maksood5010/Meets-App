package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation

class TableInformationFragment : BaseFragment() {

    private val TAG = TableInformationFragment::class.java.simpleName

    lateinit var next : Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_table_information,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP()
    }

    private fun initView(view: View) {
        next = view.findViewById(R.id.btn_next_table_information)
    }

    private fun setUP() {

        next.setOnClickListener {
            Log.i(TAG," clickec:: next")
            Navigation.addFragment(requireActivity(),VerificationCodeRestaurantFragment(),"verifingCode",R.id.restrauntFrame,false,false)
        }
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}