package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.FullSuggetionAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.SuggestionResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuggestionFragment : BaseFragment() {

    val TAG = SuggestionFragment::class.java.simpleName

    val profileViewmodel: UserAccountViewModel by viewModels()

    lateinit var rvSuggestion: RecyclerView
    lateinit var noDataLayout: ConstraintLayout
    lateinit var adapter: FullSuggetionAdapter
    var suggestionResponse = SuggestionResponse()
    lateinit var shimmer: ShimmerFrameLayout
    lateinit var retry: ConstraintLayout
    lateinit var retryButton: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_suggestion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MyApplication.putTrackMP("SuggestedPage", null)
        initView(view)
        setUp()
        setObserver()
    }

    private fun initView(view: View) {
        rvSuggestion = view.findViewById(R.id.rv_suggestion)
        noDataLayout = view.findViewById<ConstraintLayout>(R.id.no_data_root)
        view.findViewById<TextView>(R.id.no_data_heading).apply {
            text = "No Suggestion"
        }
        shimmer = view.findViewById(R.id.shimmer)
        retry = view.findViewById(R.id.retry)
        retryButton = view.findViewById<TextView>(R.id.bt_retry).apply {
            setOnClickListener {
                retry.visibility = View.GONE; profileViewmodel.getSuggestion()
            }
        }
        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            activity?.onBackPressed()
        }

        rvSuggestion.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = FullSuggetionAdapter(requireActivity(), suggestionResponse, { sid, isFollow ->
            followuser(sid, isFollow)
        }, {
            openProfile(it, Constant.Source.SUGGESTION.sorce.plus(MyApplication.SID))
        })


    }

    private fun setUp() {
        rvSuggestion.adapter = adapter
        profileViewmodel.getSuggestion()
    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow) {
            profileViewmodel.followUser(id)
        } else {
            profileViewmodel.unfollowUser(id)
        }
    }

    private fun setObserver() {
        profileViewmodel.observeOnSuggestion().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " suggestionChanges:: 0 $it")
            shimmer.stopShimmer()
            shimmer.visibility = View.GONE
            when(it){
                is ResultHandler.Success-> {
                    it?.value?.let { it1 ->
                        suggestionResponse.clear()
                        suggestionResponse.addAll(it1)
                        adapter.notifyDataSetChanged()
                    }
                }
                is ResultHandler.Failure->{
                    Log.e(TAG, "setObserver:ResultHandler.Failure in suggestion api")
                }
            }
            if(suggestionResponse.isEmpty()) {
                noDataLayout.visibility = View.VISIBLE
            } else {
                noDataLayout.visibility = View.GONE
            }
        })

        profileViewmodel.observeFollowUnFollow().observe(viewLifecycleOwner, Observer {
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager,0) as TimeLineFragment).updateSuggestion()
        })

        profileViewmodel.observeException().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "show retryApi:: ")
            retry.visibility = View.VISIBLE
        })
    }

    override fun onStart() {
        super.onStart()
        shimmer.startShimmer()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}