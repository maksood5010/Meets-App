package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.LeaderboardAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentLeaderBoardBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.LeaderboardResponse
import com.meetsportal.meets.networking.places.LeaderboardResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.VwLeaderBoardView
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class LeaderBoardFragment : BaseFragment() {

    private val list: ArrayList<LeaderboardResponseItem> = arrayListOf()
    val profileViewModel: UserAccountViewModel by viewModels()
    lateinit var adapter: LeaderboardAdapter
    private var otherUserID: String? = null
    private var _binding: FragmentLeaderBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments != null) {
            otherUserID = arguments?.getString("otherProfileSID", null)
        }
        Log.d("TAG", "onViewCreated:data $otherUserID ")
        profileViewModel.getLeaders(null, otherUserID)
        initView()
        setObserver()
    }

    private fun initView() {
        MyApplication.putTrackMP(VwLeaderBoardView, null)
        binding.ivOption.isVisible = otherUserID == null
        adapter = LeaderboardAdapter(requireActivity(), list) {
            openProfile(it, Constant.Source.LEADERBOARD.sorce.plus(arguments?.getString("otherProfileSID", MyApplication.SID)))
        }
        if(list.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }
        binding.root.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#F0C0D2"), Color.parseColor("#4FE4E7")), 0f)
        binding.card1.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#FDF3F3"), Color.parseColor("#E99B9B")), 10f)
        binding.card2.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#FC9797"), Color.parseColor("#D287B9")), 10f)
        binding.card3.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#97AFFC"), Color.parseColor("#EA94A0")), 10f)
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.ivShowSearch.setOnClickListener {
            binding.viewFlipper.displayedChild = 1
        }
        binding.ivClearEdit.setOnClickListener {
            binding.etSearch.text = null
            binding.viewFlipper.displayedChild = 2
            profileViewModel.getLeaders(null, otherUserID)
        }
        binding.ivSearch.setOnClickListener {
            hideKeyboard(binding.ivSearch)
            binding.ivSearch.clearFocus()
            if(binding.etSearch.text.toString().trim().isNotEmpty()) {
                profileViewModel.getLookUserName(binding.etSearch.text.toString())
            }
        }
        binding.etSearch.onText {

        }

        binding.rvRanks.adapter = adapter
        val filterBottomSheet = FilterBottomSheet(requireActivity(), arrayListOf("Worldwide Leaderboard", "National Leaderboard")) {
            when(it) {
                0 -> {
                    profileViewModel.getLeaders(null, otherUserID)
                }

                1 -> {
                    profileViewModel.getLeaders(Utils.getCountryCode(requireContext()), otherUserID)

                }
            }
        }
        binding.ivOption.setOnClickListener {
            filterBottomSheet.show()
        }
    }

    private fun setObserver() {
        profileViewModel.observeLeaderDataSource().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    result.value?.let {
                        Log.i("TAG", " from::: 3")
                        val filter = it.leaderboard.filter { it.user_meta?.username != null }
                        if(filter.size > 3) {
                            list.clear()
                            list.addAll(filter.subList(3, filter.size))
                            adapter.notifyDataSetChanged()
                            if(list.isEmpty()) {
                                binding.tvNoData.visibility = View.VISIBLE
                            } else {
                                binding.tvNoData.visibility = View.GONE
                            }
                            updateUI(it)
                        } else {
                            showToast("Something Went Wrong")
                        }
                    }

                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went Wrong")
                }
            }
//            leaderBoardPagingAdapter.submitData(lifecycle, it)
        })

        profileViewModel.observeLookUpUsername().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    result.value?.let {
                        list.clear()
                        list.addAll(it?.leaderboard)
                        adapter.notifyDataSetChanged()
                        if(list.isEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        } else {
                            binding.tvNoData.visibility = View.GONE
                        }
                    }

                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went Wrong")
                }
            }
//            leaderBoardPagingAdapter.submitData(lifecycle, it)
        })

    }

    private fun updateUI(response: LeaderboardResponse) {
        if(otherUserID != null) {
            binding.textView.text = "${response.user.user_meta.username}'s current rank"
        }
        binding.ivDp.loadImage(requireContext(), response.user.user_meta.profile_image_url, R.drawable.ic_default_person, showSimmer = false)
        binding.ivDpBadge.setImageResource(Utils.getBadge(response.user.badge).foreground)
        binding.tvUserName.text = response.user.user_meta.username
        binding.tvStatus.text = Utils.getBadge(response.user.badge).name
        binding.tvRank.text = "${response.user.rank}"
        binding.tvMints.text = Utils.prettyCount(response.user.mints)


        if(response.leaderboard.size > 3) {
            val first = response.leaderboard[0]
            val second = response.leaderboard[1]
            val third = response.leaderboard[2]
            binding.circleImageView1.loadImage(requireActivity(), first.user_meta.profile_image_url, R.drawable.ic_default_person, showSimmer = false)
            binding.tvName1.text = first.user_meta.username
            binding.tvMints1.text = "${Utils.prettyCount(first.mints)} Mints"
            binding.ivDpBadge1.setImageResource(Utils.getBadge(first.badge).foreground)
            binding.circleImageView1.onClick({
                openProfile(first.sid, Constant.Source.LEADERBOARD.sorce.plus(otherUserID))
                /* var baseFragment : BaseFragment = OtherProfileFragment.getInstance(first.sid)
                 *//*baseFragment = Navigation.setFragmentData(baseFragment,
                    OtherProfileFragment.OTHER_USER_ID,
                    it)*//*
                Navigation.addFragment(requireActivity(),baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT,
                    R.id.homeFram,true,false)

*/
                /*val bundle=Bundle()
                bundle.putString(OtherProfileFragment.OTHER_USER_ID,first.sid)
                navigate(OtherProfileFragment(),bundle)*/
            })

            binding.circleImageView2.loadImage(requireActivity(), second.user_meta.profile_image_url, R.drawable.ic_default_person, showSimmer = false)
            binding.tvName2.text = second.user_meta.username
            binding.tvMints2.text = "${Utils.prettyCount(second.mints)} Mints"
            binding.ivDpBadge2.setImageResource(Utils.getBadge(second.badge).foreground)

            binding.circleImageView2.onClick({
                openProfile(second.sid, Constant.Source.LEADERBOARD.sorce.plus(otherUserID))

//                var baseFragment : BaseFragment = OtherProfileFragment.getInstance(second.sid)
//                /*baseFragment = Navigation.setFragmentData(baseFragment,
//                    OtherProfileFragment.OTHER_USER_ID,
//                    it)*/
//                Navigation.addFragment(requireActivity(),baseFragment,
//                    Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)


                /*val bundle=Bundle()
                bundle.putString(OtherProfileFragment.OTHER_USER_ID,second.sid)
                navigate(OtherProfileFragment(),bundle)*/
            })

            binding.circleImageView3.loadImage(requireActivity(), third.user_meta.profile_image_url, R.drawable.ic_default_person, showSimmer = false)
            binding.tvName3.text = third.user_meta.username
            binding.tvMints3.text = "${Utils.prettyCount(third.mints)} Mints"
            binding.ivDpBadge3.setImageResource(Utils.getBadge(third.badge).foreground)

            binding.circleImageView3.onClick({
                openProfile(third.sid, Constant.Source.LEADERBOARD.sorce.plus(otherUserID))
//                var baseFragment : BaseFragment = OtherProfileFragment.getInstance(third.sid)
//                /*baseFragment = Navigation.setFragmentData(baseFragment,
//                    OtherProfileFragment.OTHER_USER_ID,
//                    it)*/
//                Navigation.addFragment(requireActivity(),baseFragment,
//                    Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)

                /* val bundle=Bundle()
                 bundle.putString(OtherProfileFragment.OTHER_USER_ID,third.sid)
                 navigate(OtherProfileFragment(),bundle)*/
            })
        }
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {

    }
}