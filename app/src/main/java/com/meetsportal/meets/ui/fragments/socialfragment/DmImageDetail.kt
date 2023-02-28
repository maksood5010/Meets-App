package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.databinding.FragmentDmImageDetailBinding
import com.meetsportal.meets.networking.directmessage.DMModel
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Utils

class DmImageDetail : BaseFragment() {

    val TAG = DmImageDetail::class.java.simpleName

    private var _binding: FragmentDmImageDetailBinding? = null
    private val binding get() = _binding!!

    var myDm: DMModel.MyDM? = null

    companion object {

        var IMAGE = "imageLink"
        var NAME = "Name"
        var PASS = "Pass"
        fun getInstance(item: DMModel.MyDM, name: String, passPhrase: String): BaseFragment {
            return DmImageDetail().apply {
                arguments = Bundle().apply {
                    putParcelable(IMAGE, item)
                    putString(NAME, name)
                    putString(PASS, passPhrase)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDmImageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp(view)
        addObserver()

    }

    private fun initView(view: View) {
        arguments?.let {
            myDm = it.getParcelable<DMModel.MyDM>(IMAGE)
            Log.i(TAG, " data::: $myDm")
        }

    }

    private fun setUp(view: View) {
        Utils.stickImage(requireContext(), binding.detailImage, arguments?.getString(PASS), null)
        binding.name.text = arguments?.getString(NAME)
        binding.time.text = Utils.fireTimeStamptoAgo(myDm?.timestamp?.toDate()!!)
        binding.back.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    override fun hideNavBar(): Boolean {
        return true
    }

    private fun addObserver() {

    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }


}