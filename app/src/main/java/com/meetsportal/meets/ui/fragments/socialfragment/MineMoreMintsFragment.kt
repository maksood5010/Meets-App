package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.MineMoreMintsAdapter
import com.meetsportal.meets.databinding.FragmentMineMoreMintsBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MineMoreMintsFragment : BaseFragment() {

     val TAG =  MineMoreMintsFragment::class.simpleName

    val profileViewModel: UserAccountViewModel by viewModels()

    lateinit var mintsAdapter: MineMoreMintsAdapter
    private var _binding: FragmentMineMoreMintsBinding? = null
    private val binding get() = _binding!!
//

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMineMoreMintsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()

    }

    private fun initView() {
    }

    private fun setUp() {
        binding.rvMineMoreMints.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mintsAdapter = MineMoreMintsAdapter(requireActivity())
        binding.rvMineMoreMints.adapter = mintsAdapter
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}