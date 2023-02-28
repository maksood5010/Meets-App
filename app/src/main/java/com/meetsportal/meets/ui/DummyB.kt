package com.meetsportal.meets.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.transition.TransitionInflater
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment

class DummyB : BaseFragment() {

    val TAG = DummyB::class.simpleName

    lateinit var imageB: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_b, container, false)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.shared_image)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageB = view.findViewById(R.id.imgb)
        ViewCompat.setTransitionName(imageB, "imageBBB")


        Handler(Looper.getMainLooper()).postDelayed({
            startPostponedEnterTransition()
        }, 2000)


    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}