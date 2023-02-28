package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.FetchReviewResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable

@AndroidEntryPoint
class AddReview : BaseFragment() {

    lateinit var rating : RatingBar
    lateinit var review : EditText
    lateinit var reviewCounter: TextView
    lateinit var heading: TextView
    lateinit var post: TextView

    val  placeViewModel: PlacesViewModel by viewModels()

    var startChoosen : Boolean =false
    var toEditReview :Boolean = false
    var myReview : FetchReviewResponseItem? = null

    var compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
        setObserver()
    }

    private fun initView(view: View) {

        review = view.findViewById(R.id.et_review)
        reviewCounter = view.findViewById(R.id.tv_counter)
        heading = view.findViewById(R.id.tv_heading)
        post = view.findViewById(R.id.post)

        view.findViewById<ImageView>(R.id.cancel_button).apply {
            setOnClickListener {
                Utils.hideSoftKeyBoard(requireContext(),it)
                activity?.onBackPressed()
            }
        }

        rating = view.findViewById<RatingBar>(R.id.rating).apply {
            setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if(rating < 1.0f) {
                    ratingBar?.rating = 1.0f
                }
                startChoosen = true
            }
        }

    }

    private fun setUP(view: View) {

        placeViewModel.getMyReview(Navigation.getFragmentData(this,"id"),MyApplication.SID)
        heading.text = Navigation.getFragmentData(this,"placeName")

        review.count {
            reviewCounter.text = "$it/1000"
        }

        compositeDisposable.add(
            Utils.onClick(post,1000){
                Utils.hideSoftKeyBoard(requireContext(),post)
                validatePost()?.let {
                    showToast(it)
                }?:run{
                    val request = JsonObject()
                    request.addProperty("body",review.text.toString().trim())
                    request.addProperty("rating",rating.rating)

                    if(toEditReview)
                        placeViewModel.editReview(Navigation.getFragmentData(this,"id"),myReview?._id,request)
                    else
                        placeViewModel.reviewPlace(Navigation.getFragmentData(this,"id"),request)
                }
            }
        )

    }

    override fun hideNavBar(): Boolean {
        return true
    }

    private fun validatePost() :String?{
//        if(review.text.toString().trim().equals("") || review.text.toString().isBlank())
//            return "Review is required"
//        else
            if(!startChoosen){
            return "Please choose stars"
        }else if(rating.rating==0f){
                return "Minimum one star required"
            }
        return null
    }

    private fun setObserver() {
        placeViewModel.observeReview().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success ->{
                    Toast.makeText(requireContext(),"Review published",Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                }
                is ResultHandler.Failure ->{
                    activity?.onBackPressed()
                }
            }
        })

        placeViewModel.observeMyReview().observe(viewLifecycleOwner, Observer {
            if(it?.isEmpty() == true)
                toEditReview = false
            else{
                toEditReview = true
                myReview = it?.get(0)
                review.setText(it?.get(0)?.body)
                rating.rating = it?.get(0)?.rating?.toFloat()!!
            }

        })
    }

    override fun onBackPageCome(){

    }


}