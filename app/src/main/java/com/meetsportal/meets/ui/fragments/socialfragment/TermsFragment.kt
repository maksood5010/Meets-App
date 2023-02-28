package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meetsportal.meets.databinding.FragmentTermsBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.onClick
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TermsFragment : BaseFragment() {

    val TAG = TermsFragment::class.simpleName


    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_terms, container, false)
        _binding = FragmentTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.onClick({
            activity?.onBackPressed()
        })
        if(arguments != null) {
            val title = requireArguments().getString("title")
            val url = requireArguments().getString("url", "")
            binding.tvToolbar.text = title
            binding.webView.loadUrl(url)
            binding.webView.settings.javaScriptEnabled = true
        }
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}