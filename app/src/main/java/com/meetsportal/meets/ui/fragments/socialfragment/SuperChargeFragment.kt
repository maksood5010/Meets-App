package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.android.billingclient.api.BillingClient
import com.meetsportal.meets.BillingManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.OpenMeetJoinRqPeopleStackAdapter
import com.meetsportal.meets.adapter.SuperChargeAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentSuperChargeBinding
import com.meetsportal.meets.models.PricingModelItem
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.RewardsResponseItem
import com.meetsportal.meets.networking.post.SinglePostResponse
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.overridelayout.CenterZoomLinearLayoutManager
import com.meetsportal.meets.ui.dialog.BuyMintsDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.BillingViewModal
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuperChargeFragment : BaseFragment() {

    private var mints: String? = null
    private val list: ArrayList<PricingModelItem> = ArrayList()
    private var adapter: SuperChargeAdapter? = null
    private var _binding: FragmentSuperChargeBinding? = null
    private val binding get() = _binding!!
    var buyMintsDialog: BuyMintsDialog? = null
    private var postId: String? = null
    var boostCount = 0
    val profileViewModel: UserAccountViewModel by viewModels()
    val postViewModel: PostViewModel by viewModels()
    val billingViewModel: BillingViewModal by viewModels()
    var position: Int = 1

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_terms, container, false)
        _binding = FragmentSuperChargeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null) {
            postId = arguments?.getString("post_id", null)
        }
        addObserver()
        setUp()
    }

    private fun addObserver() {

        billingViewModel.observerSkus().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    BillingManager.startProcess(requireActivity(), viewLifecycleOwner, billingViewModel, it.value)
                    // buyMintsDialog?.setDataInAdapter(it.value)
                }

                is ResultHandler.Failure -> {

                }
            }
        })
        billingViewModel.observeBuyPackage().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    buyMintsDialog?.setDataInAdapter(it.value)
                }

                is ResultHandler.Failure -> {

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

        profileViewModel.observeOnTopUp().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    if(result.value.isNullOrEmpty()) {
                        showToast("Something Went Wrong please contact admin")
                    } else {
                        showToast("Successfully Purchased Supercharge!")
                        profileViewModel.getChargeCount()
                        binding.viewFlipper.displayedChild = binding.viewFlipper.indexOfChild(binding.child3.child33)
                    }
                }

                is ResultHandler.Failure -> {
                    showToast("${result?.message}")
                }
            }
        })
        profileViewModel.observeOnConsumeProduct().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    binding.viewFlipper.displayedChild = binding.viewFlipper.indexOfChild(binding.child4.child44)
                    binding.child4.logo.setAnimation("supercharge.json")
                    binding.child4.logo.playAnimation()
                    binding.child4.tvSuccess.visibility = View.VISIBLE
                    binding.child4.tvTime.visibility = View.VISIBLE
//                    binding.child4.tvTitle4.text="Supercharge is Successfully activated"
                    boostCount--
                    binding.child4.tvLeft.text = "$boostCount Charges left"
                }

                is ResultHandler.Failure -> {
                    profileViewModel.getActiveBoost()
                    binding.viewFlipper.displayedChild = binding.viewFlipper.indexOfChild(binding.child5.child55)
                    binding.child5.tvClose.onClick({
                        activity?.onBackPressed()
                    })
//                    binding.child4.logo.setAnimation("supercharge.json")
//                    binding.child4.tvSuccess.visibility=View.VISIBLE
//                    binding.child4.tvSuccess.text="You already have an active super charge activated, please try again later"
//                    binding.child4.tvLeft.text="$boostCount Charges left"

//                    binding.child4.tvTitle4.text="You already have an active super charge activated, please try again later"
                }
            }
        })

        profileViewModel.observeOnChargeCount().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.get("count")?.asInt?.let { count ->
                        this.boostCount = count
                        if(count > 0) {
                            setUpWithCount(count)
                        } else {
                            setUpWithNoCount()
                        }
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went Wrong")
                    activity?.onBackPressed()
                }
            }
        })

        profileViewModel.observeOnGetActiveBoost().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let { it1 ->
                        binding.child5.tvViewPost.onClick({
                            if(postId != it1.meta?.product_meta?.post_id) {
                                val baseFragment: BaseFragment = DetailPostFragment()
                                Navigation.setFragmentData(baseFragment, "post_id", it1.meta?.product_meta?.post_id)
                                Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false)
                            } else {
                                activity?.onBackPressed()
                            }
                        })
                        val timeDifference = Utils.timeDifference(it1.terminating_at?.toDate())
                        binding.child5.tvRemaining.text = timeDifference
                    }
                }

                is ResultHandler.Failure -> {
                    if (it.code.equals("errors.inventory.notactive")){

                    }
                }
            }
        })
        profileViewModel.observeOnBoostPricing().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let { it1 ->
                        list.clear()
                        list.addAll(it1)
                        adapter?.notifyDataSetChanged()
                        binding.viewFlipper.displayedChild = binding.viewFlipper.indexOfChild(binding.child2.child22)
                        mints?.let { min ->
                            if(min.toDouble() < 100.0) {
                                binding.child2.tvSuperCharge1.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#F5F5F5"), Color.parseColor("#B5B3B5")), 30f)
                                binding.child2.textView2.text = "You do not have enough Mint cash to spend.\n First buy some Mint cash to be able \n to buy some charges"
                                binding.child2.tvSuperCharge1.isEnabled = false
                            }
                        }
                        binding.child2.tvSuperCharge1.onClick({
                            mints?.let { mi ->
                                position?.let { pos ->
                                    val item = list.getOrNull(pos)
                                    item?.amount?.let { it: Int ->
                                        if(it <= mi.toDouble()) {
                                            profileViewModel.topUpProduct(item._id)
                                        } else {
                                            showToast("Not Enough Mint Cash, please buy more mints")
                                        }
                                    } ?: run {
                                        showToast("Something went wrong")
                                    }
                                }
                            } ?: run {
                                showToast("Something Went wrong")
                            }
                        }, 2000)
                        binding.child2.getMore1.onClick({
                            buyMintsDialog = BuyMintsDialog(requireActivity(), true) {
                                it?.let { BillingManager.buyPackage(requireActivity(), viewLifecycleOwner, billingViewModel, it) } ?: run { MyApplication.showToast(requireActivity(), "Something went wrong!!!") }
                            }
                            buyMintsDialog?.show()
                            billingViewModel.getSkus()
                        })
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went Wrong")
                    activity?.onBackPressed()
                }
            }
        })
        billingViewModel.observePurchaseResponse().observe(viewLifecycleOwner, {
            Log.i("TAG", " observe ResponseCode in fragment")
            when(it) {
                is ResultHandler.Success -> {
                    when(it.value) {
                        BillingClient.BillingResponseCode.OK               -> MyApplication.showToast(requireActivity(), "Purchase Successful!!")
                        BillingClient.BillingResponseCode.USER_CANCELED    -> MyApplication.showToast(requireActivity(), "You cancelled transaction!!!")
                        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> MyApplication.showToast(requireActivity(), "Item is not available for purchase")
                        BillingClient.BillingResponseCode.SERVICE_TIMEOUT  -> MyApplication.showToast(requireActivity(), "Service timeout!!!")
                        else                                               -> MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                    }
                }

                is ResultHandler.Failure -> {

                }
            }
        })

        billingViewModel.observeBuyMintsResponse().observe(viewLifecycleOwner, {
            Log.i("TAG", "chekinfCondirmPurchaseModel:: $it")
            billingViewModel.verifyTransaction(it)
        })

        billingViewModel.observeVerifyTransaction().observe(viewLifecycleOwner, {
            Log.i("TAG", " verify transaction::: ")
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.get("msg")?.let {
                        profileViewModel.getRewardComp()
                        buyMintsDialog?.dismiss()
                        MyApplication.showToast(requireActivity(), it.asString)
//                        refresh()
                    } ?: run {
                        MyApplication.showToast(requireActivity(), "Processing Purchase..")
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!\nPlease contact with support team!!!", Toast.LENGTH_LONG)
                }
            }
        })

        profileViewModel.getReward().observe(viewLifecycleOwner, { response ->
            response?.let { datas ->
                val mintCash: RewardsResponseItem? = datas.firstOrNull { it.component_id == "mint_cash" }
                mintCash?.let { ri: RewardsResponseItem ->
                    ri.total_mints?.let {
                        mints = it.toString()
                        binding.child2.tvMints1.text = "$mints Mint cash"
                    }
                }
            }
        })
    }

    private fun populatePost(data: SinglePostResponse?) {
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
        binding.child3.tvSuperCharge2.onClick({
            if(boostCount > 0) {
                profileViewModel.consumeProduct(postId)
                binding.child4.tvLeft.text = "$boostCount Charges left"
            } else {
                showToast("You don't have enough super charges")
            }
        })
    }

    private fun bindDefault(data: SinglePostResponse?) {
        binding.child3.ivPost.visibility = View.VISIBLE
        binding.child3.flText.visibility = View.GONE
        binding.child3.inMeetup.rootCo.visibility = View.GONE
        if(data?.media?.size?.compareTo(0) == 1) {
            binding.child3.ivPost.loadImage(requireContext(), data.media?.get(0))
        }
    }

    private fun bindTextPost(data: SinglePostResponse?) {
        binding.child3.flText.visibility = View.VISIBLE
        binding.child3.inMeetup.rootCo.visibility = View.GONE
        binding.child3.ivPost.visibility = View.GONE
        binding.child3.tvCaption.text = data?.body
        data?.body_obj?.text_post?.let { textPost ->
            var gradient = Constant.GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            //holder.postText.background = ContextCompat.getDrawable(myContext,BodyObj.gradArray.get(index))
            gradient?.let {
                binding.child3.tvCaption.background = Utils.gradientFromColor(it.gradient)
            } ?: run {
                binding.child3.tvCaption.background = Utils.gradientFromColor(Constant.GradientTypeArray.first().gradient)
            }
        } ?: run {
            binding.child3.tvCaption.background = Utils.gradientFromColor(Constant.GradientTypeArray.first().gradient)
        }

    }

    private fun bindOpenMeet(data: SinglePostResponse?) {
        binding.child3.inMeetup.rootCo.visibility = View.VISIBLE
        binding.child3.flText.visibility = View.GONE
        binding.child3.ivPost.visibility = View.GONE
        binding.child3.inMeetup.join.visibility = View.INVISIBLE
        binding.child3.inMeetup.rlIntract.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.gray1))
//        binding.child3.inMeetup.cardView2.setRoundedColorBackground(requireActivity(),color = R.color.blacktextColor,corner = 20f)
        binding.child3.inMeetup.monthOfYear.setGradient(requireActivity(), GradientDrawable.Orientation.TL_BR, intArrayOf(ContextCompat.getColor(requireActivity(), R.color.primaryDark), ContextCompat.getColor(requireActivity(), R.color.gred_red), ContextCompat.getColor(requireActivity(), R.color.gred_red)))
        data?.body_obj?.open_meetup?.let { openMeet ->
            if(openMeet?.join_requests?.requests?.size?.compareTo(0) == 1) {
                binding.child3.inMeetup.llNoRequest.visibility = View.GONE
            } else {
                binding.child3.inMeetup.llNoRequest.visibility = View.VISIBLE
                binding.child3.inMeetup.tvFirst.text = "Requested\n will displayed here"
            }
            binding.child3.inMeetup.meetName.text = openMeet.name
            if(openMeet?.description?.isEmpty() == true) {
                binding.child3.inMeetup.desc.text = "No description added"
            } else {
                binding.child3.inMeetup.desc.text = openMeet?.description
            }
            binding.child3.inMeetup.dayOfMonth.text = openMeet.date?.toDate()?.formatTo("dd")
            //holder.year.text = openMeet.date?.toDate()?.formatTo("yy")
            binding.child3.inMeetup.dayOfWeak.text = openMeet.date?.toDate()?.formatTo("EEEE")
            binding.child3.inMeetup.timeOfDay.text = openMeet.date?.toDate()?.formatTo("hh:mm aa")
            when(openMeet?.chosen_place?.type) {
                Constant.PlaceType.MEET.label   -> {
                    binding.child3.inMeetup.address.text = openMeet?.places?.firstOrNull()?.name?.en
                }

                Constant.PlaceType.CUSTOM.label -> {
                    binding.child3.inMeetup.address.text = openMeet.custom_places?.firstOrNull()?.name
                }
            }
            binding.child3.inMeetup.countInterested.visibility = View.GONE
            binding.child3.inMeetup.view.visibility = View.GONE
            binding.child3.inMeetup.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(requireActivity(), openMeet) {

            }
        }


//        binding.child3.inMeetup.llBorder.setGradient(requireActivity(), GradientDrawable.Orientation.TL_BR, intArrayOf(
//                ContextCompat.getColor(requireActivity(), R.color.bg_gray),
//                ContextCompat.getColor(requireActivity(), R.color.bg_gray),
//                ContextCompat.getColor(requireActivity(), R.color.bg_gray), ))
//        binding.child3.inMeetup.rlWhitebg.setRoundedColorBackground(requireActivity(),
//                R.color.white,10f,enbleDash=true,strokeColor= R.color.extraLightGray,Dashgap=0f,strokeHeight=1f)
//        data?.body_obj?.open_meetup?.let { openMeet ->
//            //holder.monthOfYear.text = openMeet.date?.toDate()?.formatTo("M")
//            binding.child3.inMeetup.dayOfMonth.text = openMeet.date?.toDate()?.formatTo("dd")
//            //holder.year.text = openMeet.date?.toDate()?.formatTo("yy")
//            binding.child3.inMeetup.dayOfWeek.text = openMeet.date?.toDate()?.formatTo("EEEE")
//            binding.child3.inMeetup.Time.text = openMeet.date?.toDate()?.formatTo("hh:mm aa")
//            binding.child3.inMeetup.meetName.text = openMeet.name
//        }
    }

    private fun setUp() {
        profileViewModel.getChargeCount()
        profileViewModel.getRewardComp()

        binding.root.onClick({
            activity?.onBackPressed()
        })
        binding.child3.textView3.onClick({
            activity?.onBackPressed()
        })
        binding.child4.tvClose.onClick({
            activity?.onBackPressed()
        })
        binding.rootView.onClick({})
        binding.child1.tvSuperCharge.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#1D55A2")), 30f)
        binding.child2.tvSuperCharge1.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#1D55A2")), 30f)
        binding.child3.tvSuperCharge2.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#1D55A2")), 30f)

        val layoutManager = CenterZoomLinearLayoutManager(requireActivity()) { child, position, selected ->
            if(selected) {
                this.position = position
                child.findViewById<CardView>(R.id.cvMintPackage)?.apply {
                    foreground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_stroke_boost)
                }
                Log.d("TAG", "onViewCreated: selected position $position")
            } else {
                child.findViewById<CardView>(R.id.cvMintPackage)?.apply {
                    foreground = null
                }
            }
        }
        binding.child2.rvChargePackage.layoutManager = layoutManager
        adapter = SuperChargeAdapter(requireActivity(), list) {
            binding.child2.rvChargePackage.smoothScrollToPosition(it)
        }
        binding.child2.rvChargePackage.adapter = adapter
        val helper: SnapHelper = PagerSnapHelper()
        helper.attachToRecyclerView(binding.child2.rvChargePackage)
//        binding.child2.rvChargePackage.smoothScrollToPosition(position)

//        layoutManager.scrollToPositionWithOffset(position,binding.child2.rvChargePackage.computeHorizontalScrollOffset())

    }

    private fun setUpMints(profileGetResponse: ProfileGetResponse) {
        mints = profileGetResponse?.social?.getmints()
        binding.child2.tvMints1.text = "$mints Mint cash"
        /*You do not have enough Mint cash to spend.First buy some Mint cash to be able to buy some charges*/
    }

    private fun setUpWithNoCount() {

        binding.child1.tvCharges2.text = "No Charges"
        binding.child3.tvCharges3.text = "No Charges"
        binding.child1.tvSuperCharge.onClick({
            profileViewModel.getBoostPricing()
        })
        binding.child1.tvNotNow.text = "Not Now"
        binding.child1.tvNotNow.onClick({
            activity?.onBackPressed()
        })

    }

    private fun setUpWithCount(count: Int) {
        binding.child1.tvCharges2.text = "$count Charges"
        binding.child3.tvCharges3.text = "$count Charges"
        postViewModel.fetchSinglePost(postId, null)
        binding.child1.tvSuperCharge.onClick({
            binding.viewFlipper.displayedChild = binding.viewFlipper.indexOfChild(binding.child3.child33)
        })
        binding.child1.tvNotNow.text = "Buy more Charges"
        binding.child1.tvNotNow.onClick({
            profileViewModel.getBoostPricing()
        })

    }

    override fun onBackPageCome() {

    }
}