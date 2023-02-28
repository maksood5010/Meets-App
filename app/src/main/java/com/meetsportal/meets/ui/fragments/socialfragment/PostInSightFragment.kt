package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentPostInSightBinding
import com.meetsportal.meets.models.InsightResponse
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.post.SinglePostResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.VwPostInsightViewed
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class PostInSightFragment : BaseFragment() {

    private var postId: String? = null
    private var _binding: FragmentPostInSightBinding? = null
    private val binding get() = _binding!!
    val userViewModel: UserAccountViewModel by viewModels()
    val postViewModel: PostViewModel by viewModels()
    var postDetail: SinglePostResponse? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentPostInSightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments != null) {
            postId = arguments?.getString("post_id", null)
        }
        userViewModel.getInsight(postId)
        //TODO Cache it later
        postViewModel.fetchSinglePost(postId, null)
        setObserver()
        binding.ivBack.onClick({
            activity?.onBackPressed()
        })
        binding.tvFollowers.setOneSideBg(Color.parseColor("#93E0BA"), tLeft = 20f, bLeft = 20f)
        binding.tvNonFollow.setOneSideBg(Color.parseColor("#93E0BA"), tRight = 20f, bRight = 20f)
        binding.llAccount.setOneSideBg(Color.parseColor("#2EAA96"), tRight = 20f, tLeft = 20f)
        binding.tvSPV.setOneSideBg(Color.parseColor("#357FD3"), bLeft = 20f, tLeft = 20f)
        binding.tvPLikes.setOneSideBg(Color.parseColor("#357FD3"), tRight = 20f, bRight = 20f)
        binding.tvSPView.setOneSideBg(Color.parseColor("#1B5EAA"))
        binding.inMeetup.llBorder.setGradient(requireActivity(), GradientDrawable.Orientation.TL_BR, intArrayOf(
                ContextCompat.getColor(requireActivity(), R.color.bg_gray),
                ContextCompat.getColor(requireActivity(), R.color.bg_gray),
                ContextCompat.getColor(requireActivity(), R.color.bg_gray),
                                                                                                               ))
        binding.inMeetup.rlWhitebg.setRoundedColorBackground(requireActivity(), R.color.white, 10f, enbleDash = true, strokeColor = R.color.extraLightGray, Dashgap = 0f, strokeHeight = 1f)

    }

    private fun setObserver() {
        userViewModel.observeGetInsight().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {

                    populateView(it.value)
                }

                is ResultHandler.Failure -> {
                    showToast("Something Went Wrong")
                }
            }
        })
        postViewModel.observeSinglePost().observe(viewLifecycleOwner, { result ->

            when(result) {
                is ResultHandler.Success -> {
                    populatePost(result.value)
                }

                is ResultHandler.Failure -> {
                    showToast("Something Went Wrong")
                }
            }
        })
    }

    private fun populatePost(data: SinglePostResponse?) {
        postDetail = data
        when(data?.type) {
            Constant.Post.OPEN_MEET.type -> {
                bindOpenMeet(data)
            }

            Constant.Post.TEXT_POST.type -> {
                bindTextPost(data)
            }

            else                         -> {
                bindDefault(data)
            }
        }

    }

    private fun bindDefault(data: SinglePostResponse?) {
        binding.ivPost.visibility = View.VISIBLE
        binding.flText.visibility = View.GONE
        binding.inMeetup.rootCo.visibility = View.GONE
        binding.tvDate.text = "${data?.createdAt?.toDate()?.formatTo("dd MMMM yyyy - hh:mm a")}"
        if(data?.media?.size?.compareTo(0) == 1) {
            binding.ivPost.loadImage(requireContext(), data.media?.get(0))
        }
    }

    private fun bindTextPost(data: SinglePostResponse?) {
        binding.flText.visibility = View.VISIBLE
        binding.inMeetup.rootCo.visibility = View.GONE
        binding.ivPost.visibility = View.GONE
        binding.tvCaption.text = data?.body

        binding.tvDate.text = "${data?.createdAt?.toDate()?.formatTo("dd MMMM yyyy - hh:mm a")}"
        data?.body_obj?.text_post?.let { textPost ->
            var gradient = Constant.GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            //holder.postText.background = ContextCompat.getDrawable(myContext,BodyObj.gradArray.get(index))
            gradient?.let {
                binding.tvCaption.background = Utils.gradientFromColor(it.gradient)
            } ?: run {
                binding.tvCaption.background = Utils.gradientFromColor(Constant.GradientTypeArray.first().gradient)
            }
        } ?: run {
            binding.tvCaption.background = Utils.gradientFromColor(Constant.GradientTypeArray.first().gradient)
        }

    }

    private fun bindOpenMeet(data: SinglePostResponse?) {
        binding.inMeetup.rootCo.visibility = View.VISIBLE
        binding.flText.visibility = View.GONE
        binding.ivPost.visibility = View.GONE
        binding.tvDate.text = "${data?.createdAt?.toDate()?.formatTo("dd MMMM yyyy - hh:mm a")}"
        data?.body_obj?.open_meetup?.let { openMeet ->
            //holder.monthOfYear.text = openMeet.date?.toDate()?.formatTo("M")
            binding.inMeetup.dayOfMonth.text = openMeet.date?.toDate()?.formatTo("dd")
            //holder.year.text = openMeet.date?.toDate()?.formatTo("yy")
            binding.inMeetup.dayOfWeek.text = openMeet.date?.toDate()?.formatTo("EEEE")
            binding.inMeetup.Time.text = openMeet.date?.toDate()?.formatTo("hh:mm aa")
            binding.inMeetup.meetName.text = openMeet.name
        }
    }

    private fun populateView(value: InsightResponse?) {
        MyApplication.putTrackMP(VwPostInsightViewed, JSONObject(mapOf("postId" to arguments?.getString("post_id", null))))
        if(value?.is_boosted == false) {
            val response = value.default
            binding.groupDivider.visibility = View.GONE
            binding.tvLikes.text = Utils.prettyCount(response?.content_interactions?.likes)
            binding.tvComment.text = Utils.prettyCount(response?.content_interactions?.comments)
            binding.tvReached.text = Utils.prettyCount(response?.content_reach?.total)
            binding.tvContentInter.text = Utils.prettyCount(response?.content_interactions?.total)
            binding.tvProfileTotal.text = Utils.prettyCount(response?.profile_acivtity?.total)
            binding.tvFollow.text = Utils.prettyCount(response?.content_reach?.followers)
            binding.tvAccountReached.text = Utils.prettyCount(response?.content_reach?.total)
            binding.tvNonFollower.text = Utils.prettyCount(response?.content_reach?.non_followers)
            binding.tvLikeIt.text = Utils.prettyCount(response?.content_interactions?.likes)
            binding.tvCommentIt.text = Utils.prettyCount(response?.content_interactions?.comments)
            binding.tvProfileVisit.text = Utils.prettyCount(response?.profile_acivtity?.visits)
            binding.tvNewFollows.text = Utils.prettyCount(response?.profile_acivtity?.new_follows)
            binding.textView4.text = "This post is not currently promoted. promote it to reach more people."
            if(postDetail?.type != Constant.Post.CHECK_IN.type) {
                binding.superClick.onClick({
                    val superChargeFragment = SuperChargeFragment()
                    Navigation.setFragmentData(superChargeFragment, "post_id", postId)
                    Navigation.addFragment(requireActivity(), superChargeFragment, "SuperChargeFragment", R.id.homeFram, true, null)
                })
            }
        } else {
            val response = value?.boosted
            binding.groupDivider.visibility = View.VISIBLE
            binding.tvLikes.text = Utils.prettyCount(response?.content_interactions?.likes)
            binding.tvComment.text = Utils.prettyCount(response?.content_interactions?.comments)
            binding.tvReached.text = Utils.prettyCount(response?.content_reach?.total)
            binding.tvContentInter.text = Utils.prettyCount(response?.content_interactions?.total)
            binding.tvProfileTotal.text = Utils.prettyCount(response?.profile_acivtity?.total)
            binding.tvFollow.text = Utils.prettyCount(response?.content_reach?.followers)
            binding.tvAccountReached.text = Utils.prettyCount(response?.content_reach?.total)
            binding.tvNonFollower.text = Utils.prettyCount(response?.content_reach?.non_followers)
            binding.tvLikeIt.text = Utils.prettyCount(response?.content_interactions?.likes)
            binding.tvCommentIt.text = Utils.prettyCount(response?.content_interactions?.comments)
            binding.tvProfileVisit.text = Utils.prettyCount(response?.profile_acivtity?.visits)
            binding.tvProfileVisits.text = Utils.prettyCount(response?.profile_acivtity?.visits)
            binding.tvPostViews.text = Utils.prettyCount(response?.content_reach?.total)
            binding.tvPostLikes.text = Utils.prettyCount(response?.content_interactions?.likes)
            val total = (response?.profile_acivtity?.visits ?: 0) + (response?.content_reach?.total ?: 0) + (response?.content_interactions?.likes ?: 0)
            binding.tvTotal.text = Utils.prettyCount(total)
            binding.tvNewFollows.text = Utils.prettyCount(response?.profile_acivtity?.new_follows)

            binding.textView4.text = "This post is currently promoted."
            binding.superClick.onClick({})
        }
    }

    override fun onBackPageCome() {

    }
}