package com.meetsportal.meets.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment


class DummyA : BaseFragment() {

    lateinit var arrow: ImageButton
    lateinit var hiddenView: LinearLayout
    lateinit var cardView: CardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardView = view.findViewById(R.id.base_cardview);
        arrow = view.findViewById(R.id.arrow_button);
        hiddenView = view.findViewById(R.id.hidden_view);
        //setup()

        arrow.setOnClickListener(View.OnClickListener {
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if(hiddenView.getVisibility() == View.VISIBLE) {

                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                hiddenView.setVisibility(View.GONE)
                arrow.setImageResource(R.drawable.ccp_ic_arrow_drop_down)
            } else {
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                hiddenView.setVisibility(View.VISIBLE)
                arrow.setImageResource(R.drawable.bg_circular_corner_pending)
            }
        })

    }

    private fun setup() {
        /*imageA.setOnClickListener {
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            var ft: FragmentTransaction = fm.beginTransaction()

            var baseFragment = DummyB()
            baseFragment.sharedElementEnterTransition = DetailsTransition()
            baseFragment.enterTransition = Fade()
            baseFragment.exitTransition = Fade()
            baseFragment.sharedElementReturnTransition = DetailsTransition()


            ft.addSharedElement(imageA,"imageBBB")

            ft.replace(R.id.homeFram,baseFragment ,"tag")
            ft.addToBackStack(null)
            ft.commit()
        }*/
    }

    override fun onBackPageCome() {

    }
}