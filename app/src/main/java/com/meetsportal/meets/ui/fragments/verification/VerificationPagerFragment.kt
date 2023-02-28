package com.meetsportal.meets.ui.fragments.verification

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.meetsportal.meets.databinding.FragmentVerificationPagerBinding


class VerificationPagerFragment : BaseVerificationFragment() {

    companion object {

        val TAG = VerificationPagerFragment::class.simpleName!!
        val IMAGE_TYPE = "ImageType"
    }

    lateinit var pagerAdapter: ViewPagerAdapter

    private var _binding: FragmentVerificationPagerBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentVerificationPagerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        setObserver()

    }

    private fun initView() {

        binding.verifyPager.offscreenPageLimit = 7
        pagerAdapter = ViewPagerAdapter(childFragmentManager, this)
        binding.verifyPager.adapter = pagerAdapter

    }

    private fun setUp() {

    }

    private fun setObserver() {

    }

    override fun image1UriCame(uri: Uri?) {

    }

    override fun image2UriCame(uri: Uri?) {

    }

    class ViewPagerAdapter(fm: FragmentManager, var fragment: Fragment) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 7
        override fun getItem(position: Int): Fragment {


            return when(position) {
                0    -> VerificationDescFragment.getInstance(0)
                1    -> VerificationStep.getInstance(0)
                2    -> VerificationStepResult.getInstance(0)
                3    -> VerificationStep.getInstance(1)
                4    -> VerificationStepResult.getInstance(1)
                5    -> VerificationDescFragment.getInstance(1)
                6    -> VerificationDescFragment.getInstance(2)
                else -> VerificationDescFragment()
            }


        }
    }


    override fun onBackPageCome() {

    }

}