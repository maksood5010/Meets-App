package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.databinding.FragmentCreateTextPostBinding
import com.meetsportal.meets.ui.fragments.BaseFragment

class CreateTextPost : BaseFragment() {


    private var _binding: FragmentCreateTextPostBinding? = null
    private val binding get() = _binding!!


    companion object {

        val TAG = CreateTextPost::class.simpleName!!
        fun getInstance(): CreateTextPost {
            return CreateTextPost()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return inflater.inflate(R.layout.fragment_create_text_post, container, false)

        _binding = FragmentCreateTextPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}