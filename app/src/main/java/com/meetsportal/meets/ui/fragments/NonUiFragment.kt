package com.meetsportal.meets.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.dialog.MeetUpInvitationAlert
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NonUiFragment : BaseFragment() {

    var meetUpInvitation: MeetUpInvitationAlert? = null

    val TAG = NonUiFragment::class.simpleName
    var meetsInvitationType : String? = ""

    val meetUpViewModel: MeetUpViewModel by viewModels()

    companion object {

        val MeetsInvitation  = "meetInvitation"

        val ALERT_TYPE = ""

        fun getInstance(para: String): NonUiFragment {
            return NonUiFragment().apply {
                arguments = Bundle().apply {
                    putString(ALERT_TYPE, para)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("NonUiFragment", " onCrete :::: NonUiFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_transparent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFrag()
        setUp()
        setObserver()
    }

    private fun initFrag() {
        /*arguments?.let {
            //meetsInvitationType = it.getString(ALERT_TYPE)
            meetsInvitationType = it.getString("ALERT_TYPE")
            Log.i(TAG," meetsInvitationType::: $meetsInvitationType")
        }*/

    }

    private fun setUp() {
        /*meetsInvitationType?.let {
            if(it == MeetsInvitation){
                meetUpInvitation = MeetUpInvitationAlert(requireActivity()){}
                arguments?.getString("MeetId")?.let {
                    //meetUpInvitation?.showDialog()
                    meetUpViewModel.getMeetUpDetail(it)
                }
            }
        }*/

    }

    private fun setObserver() {
        /*meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    //meetUpInvitation?.showDialog()
                }
            }
        })*/

    }

    override fun onBackPageCome(){

    }
}