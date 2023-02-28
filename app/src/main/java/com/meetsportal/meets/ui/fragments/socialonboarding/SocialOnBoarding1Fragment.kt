package com.meetsportal.meets.ui.fragments.socialonboarding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import com.airbnb.lottie.LottieAnimationView
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import kotlinx.android.synthetic.main.fragment_social_onboarding1.*

class SocialOnBoarding1Fragment : BaseFragment(),View.OnClickListener {


    val TAG = SocialOnBoarding1Fragment::class.simpleName

    lateinit var lotti : LottieAnimationView
    lateinit var radioGroup : RadioGroup

    companion object{
        private const val ARG_POSITION = "ARG_POSITION"

        fun getInstance(position: Int) = SocialOnBoarding1Fragment().apply {
            arguments = bundleOf(ARG_POSITION to position)
        }
    }

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
        setUP()
        //findNavController().navigate(R.id.action_socialOnBoarding1_to_socialOnBoarding2)
    }



    private fun initView(view: View){

        lotti = view.findViewById(R.id.lav_main)
        radioGroup = view.findViewById(R.id.radioGroup)
        val position = requireArguments().getInt(ARG_POSITION)
        val onBoardingTitles = requireContext().resources.getStringArray(R.array.on_boarding_titles)
        val onBoardingTexts = requireContext().resources.getStringArray(R.array.on_boarding_texts)

        tv_header.text = onBoardingTitles.get(position)
        tv_description.text = onBoardingTexts.get(position)


    }

    private fun setUP() {
        val position = requireArguments().getInt(ARG_POSITION)
        when(position){
            0->lotti.setAnimation("data_01.json")
            1->lotti.setAnimation("data_02.json")
            2->lotti.setAnimation("data_03.json")
            3->lotti.setAnimation("data_04.json")
            4->lotti.setAnimation("data_05.json")
        }
        //radioGroup.check(radioGroup.getChildAt(position).getId())

        lotti.playAnimation()

    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}