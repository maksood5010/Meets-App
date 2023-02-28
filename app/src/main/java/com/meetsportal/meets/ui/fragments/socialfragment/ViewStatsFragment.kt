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
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.android.billingclient.api.BillingClient
import com.meetsportal.meets.BillingManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.ViewStatsAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentBuyStatusBinding
import com.meetsportal.meets.models.PricingModelItem
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.RewardsResponseItem
import com.meetsportal.meets.overridelayout.CenterZoomLinearLayoutManager
import com.meetsportal.meets.ui.dialog.BuyMintsDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setGradient
import com.meetsportal.meets.viewmodels.BillingViewModal
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewStatsFragment : BaseFragment() {

    private var mints: String? = null
    var adapter: ViewStatsAdapter? = null
    private var _binding: FragmentBuyStatusBinding? = null
    private val binding get() = _binding!!
    val profileViewModel: UserAccountViewModel by viewModels()
    val billingViewModel: BillingViewModal by viewModels()
    private val list: ArrayList<PricingModelItem> = ArrayList()
    var position: Int = 1
    var buyMintsDialog: BuyMintsDialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBuyStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addObserver()
        setUp()
    }

    private fun setUp() {
        profileViewModel.getRewardComp()
        profileViewModel.getBoostPricing("profile_stats")
        binding.ivAddMints.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#FF84EF"), Color.parseColor("#FF30EA")), 10f)

        binding.tvBuyNow.setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#FFD87E"), Color.parseColor("#FF4949")), 20f)

        binding.root.onClick({
            activity?.onBackPressed()
        })
        binding.rootView.onClick({})
        binding.tvNotNow.onClick({
            activity?.onBackPressed()
        })
        binding.ivAddMints.onClick({
            buyMintsDialog = BuyMintsDialog(requireActivity(), true) {
                it?.let { BillingManager.buyPackage(requireActivity(), viewLifecycleOwner, billingViewModel, it) } ?: run { MyApplication.showToast(requireActivity(), "Something went wrong!!!") }
            }
            buyMintsDialog?.show()
            billingViewModel.getSkus()
        })
        val layoutManager = CenterZoomLinearLayoutManager(requireActivity()) { child, position, selected ->
            if(selected) {
                this.position = position
                child.findViewById<CardView>(R.id.cvMintPackage)?.apply {
                    foreground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_stroke_stats)
                }
                Log.d("TAG", "onViewCreated: selected position $position")
            } else {
                child.findViewById<CardView>(R.id.cvMintPackage)?.apply {
                    foreground = null
                }
            }
        }

        adapter = ViewStatsAdapter(requireActivity(), list) {
            binding.rvChargePackage.smoothScrollToPosition(it)
        }
        binding.rvChargePackage.layoutManager = layoutManager
        val helper: SnapHelper = PagerSnapHelper()
        helper.attachToRecyclerView(binding.rvChargePackage)
        binding.rvChargePackage.adapter = adapter
    }

    private fun addObserver() {
        profileViewModel.getReward().observe(viewLifecycleOwner, { response ->
            response?.let { datas ->
                val mintCash: RewardsResponseItem? = datas.firstOrNull { it.component_id == "mint_cash" }
                mintCash?.let { ri: RewardsResponseItem ->
                    ri.total_mints?.let {
                        mints = it.toString()
                        binding.tvMints.text = "$mints Mint cash"
                    }
                }
            }
        })
        profileViewModel.observeOnBoostPricing().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let { it1 ->
                        it1.sortedBy {
                            it.amount
                        }.let {
                            list.clear()
                            list.addAll(it)
                        }
                        adapter?.notifyDataSetChanged()
                        binding.tvBuyNow.onClick({
                            position?.let { pos ->
                                mints?.let {
                                    val item = list.get(pos)
                                    profileViewModel.topUpProduct(item._id)
                                }
                            }
                        })
                    }
                }

                is ResultHandler.Failure -> {
                    showToast("Something went Wrong")
                    activity?.onBackPressed()
                }
            }
        })
        profileViewModel.observeOnTopUp().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    profileViewModel.consumeProduct(null, "profile_stats")
                    showToast("Successfully Purchased Package!")
                }

                is ResultHandler.Failure -> {
                    showToast("${result?.message}")
                }
            }
        })
        profileViewModel.observeOnConsumeProduct().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    showToast("Successfully Applied!")

                    var bundle = Bundle().apply {
                        putBoolean("reload", true)
                    }
                    setFragmentResult("value", bundle)
                    activity?.onBackPressed()
                }

                is ResultHandler.Failure -> {
                    showToast("${result?.message}")
                    activity?.onBackPressed()
                }
            }
        })
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
    }


    override fun onBackPageCome() {

    }

}