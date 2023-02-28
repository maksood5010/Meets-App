package com.meetsportal.meets.ui.fragments.authfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.LayoutMinedMintsCelebBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground

class MinedMintsCeleb : BaseFragment() {

    companion object {

        val TAG = MinedMintsCeleb::class.simpleName!!
    }


    private var _binding: LayoutMinedMintsCelebBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutMinedMintsCelebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setUp()
        setObserver()
    }

    private fun initView() {
        binding.parent.setRoundedColorBackground(requireActivity(), R.color.golden)
        binding.stroke.setRoundedColorBackground(requireActivity(), R.color.transparent, enbleDash = true, stripSize = 10f, strokeColor = R.color.golder_strip, Dashgap = 0f, strokeHeight = 3f)
        binding.tvMintConut.setRoundedColorBackground(requireActivity(), R.color.transparent, enbleDash = true, stripSize = 10f, strokeColor = R.color.yellow_color_picker, Dashgap = 0f, corner = 10f)
        binding.root.onClick({ activity?.onBackPressed() })

    }

    private fun setUp() {


    }

    private fun setObserver() {


    }

    override fun onBackPageCome() {

    }
}