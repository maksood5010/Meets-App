package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.android.billingclient.api.BillingClient
import com.meetsportal.meets.BillingManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.CalenderMiningAdapter
import com.meetsportal.meets.adapter.MiningDetailAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMySafeBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.RewardsResponseItem
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.dialog.BuyMintsDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.VwMySafeView
import com.meetsportal.meets.viewmodels.BillingViewModal
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MySafeFragment : BaseFragment() {

    val TAG = MineMoreMintsFragment::class.simpleName

    private lateinit var miningDetailAdapter: MiningDetailAdapter
    val profileViewModel: UserAccountViewModel by viewModels()
    val billingViewModel: BillingViewModal by viewModels()

    val list = ArrayList<RewardsResponseItem>()

    private var _binding: FragmentMySafeBinding? = null
    private val binding get() = _binding!!
    var buyMintsDialog: BuyMintsDialog? = null
//    lateinit var mintsAdapter : MineMoreMintsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMySafeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        setObserver()

    }


    private fun initView() {
        MyApplication.putTrackMP(VwMySafeView, null)
        miningDetailAdapter = MiningDetailAdapter(requireActivity(), list)
        binding.rvMiningDetail.adapter = miningDetailAdapter

        binding.refresh.setOnRefreshListener {
            refresh()
            //binding.refresh.isRefreshing = false
        }

        binding.linearLayout3.setRoundedColorBackground(requireActivity(), Color.parseColor("#F0EEEE"), 10f)
        binding.linearLayout4.setRoundedColorBackground(requireActivity(), Color.parseColor("#F0EEEE"), 10f)
        val startDate = Calendar.getInstance()
        val yearAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_text_item, resources.getStringArray(R.array.year))
        val monthAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_text_item, resources.getStringArray(R.array.months))
        monthAdapter.setDropDownViewResource(R.layout.spinner_text)
        yearAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.spMonth.adapter = monthAdapter
        binding.spYear.adapter = yearAdapter
        var selectedMonth = 0
        var selectedYear = 0
        startDate[Calendar.MONTH] = selectedMonth
        startDate[Calendar.DAY_OF_MONTH] = selectedMonth
        binding.spMonth.setSelection(selectedMonth)
        binding.spYear.setSelection(selectedYear)
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.buyMint.setOnClickListener {
            buyMintsDialog = BuyMintsDialog(requireActivity(), false) {
                it?.let { BillingManager.buyPackage(requireActivity(), viewLifecycleOwner, billingViewModel, it) } ?: run { MyApplication.showToast(requireActivity(), "Something went wrong!!!") }
            }
            buyMintsDialog?.show()
            billingViewModel.getSkus()
        }

        val calenderMiningAdapter = CalenderMiningAdapter(requireActivity(), startDate) {

        }
        binding.rvCalender.adapter = calenderMiningAdapter

        binding.spMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position
                startDate[Calendar.MONTH] = selectedMonth
                calenderMiningAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        binding.spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedYear = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        binding.upMonth.setOnClickListener {
            selectedMonth++
            if(selectedMonth >= 11) selectedMonth = 11
            startDate[Calendar.MONTH] = selectedMonth
            binding.spMonth.setSelection(selectedMonth)
            calenderMiningAdapter.notifyDataSetChanged()
        }
        binding.downMonth.setOnClickListener {
            selectedMonth--
            if(selectedMonth <= 0) selectedMonth = 0
            startDate[Calendar.MONTH] = selectedMonth
            binding.spMonth.setSelection(selectedMonth)
            calenderMiningAdapter.notifyDataSetChanged()
        }

        binding.upYear.setOnClickListener {
            selectedYear++
            if(selectedYear >= 2) selectedYear = 2
            binding.spYear.setSelection(selectedYear)
        }

        binding.downYear.setOnClickListener {
            selectedYear--
            if(selectedYear <= 0) selectedYear = 0
            binding.spYear.setSelection(selectedYear)
        }

        binding.sivBg.setGradient(requireActivity(), GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(ContextCompat.getColor(requireContext(), R.color.mining_s), ContextCompat.getColor(requireContext(), R.color.mining_e)))

    }

    private fun setUp() {
        profileViewModel.getRewardComp()
        val profileGetResponse = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        profileGetResponse?.let { profile ->
            profile.social?.getbadge()?.let { badge: String ->
                val firstOrNull = Utils.getBadge(badge)
                binding.ivDpBadge.setImageResource(firstOrNull.foreground)
                binding.ivDpBadge.setImageResource(firstOrNull.foreground)
                binding.tvBadgeNme.text = firstOrNull.name
                binding.tvBadgeLevel.text = "Level ${firstOrNull.level}"
            }
            binding.tvMints.text = String.format("%.2f", profile.social?.getmints()
                    ?.toDoubleOrNull())

        }
    }

    fun refresh() {
        profileViewModel.getFullProfile()
    }

    private fun setObserver() {

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
            Log.i(TAG, "chekinfCondirmPurchaseModel:: $it")
            billingViewModel.verifyTransaction(it)
        })

        billingViewModel.observeVerifyTransaction().observe(viewLifecycleOwner, {
            Log.i(TAG, " verify transaction::: ")
            buyMintsDialog?.hideDialog()
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.get("msg")?.let {
                        MyApplication.showToast(requireActivity(), it.asString)
                        refresh()
                    } ?: run {
                        MyApplication.showToast(requireActivity(), "Processing Purchase..")
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!\nPlease contact wot support team!!!", Toast.LENGTH_LONG)
                }
            }
        })

        profileViewModel.observeFullProfileChange().observe(viewLifecycleOwner, {
            binding.refresh.isRefreshing = false
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        Log.i(TAG, "ProfileGetResponse:: their response")
                        // loader?.hideDialog()
                        Log.i(TAG, " chekingPause::: 1")
                        PreferencesManager.put(it, Constant.PREFRENCE_PROFILE)
                        Log.i(TAG, " chekingPause::: 2")
                        setUp()
                        Log.i(TAG, " chekingPause::: 3")
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        profileViewModel.getReward().observe(viewLifecycleOwner, { response ->
            response?.let { datas ->
                Log.d("TAG", "setUp:json object $datas")
                list.clear()
                list.addAll(datas)
                miningDetailAdapter.notifyDataSetChanged()
                val mintCash = list.firstOrNull { it.component_id == "mint_cash" }
                mintCash?.let { ri: RewardsResponseItem ->
                    ri.total_mints?.let {
                        binding.tvMintCash.text = it.toString()
                        PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.social?.getmints()
                                ?.let { mints: String ->
                                    Log.d(TAG, "setObserver: mint_cash1: $mints")
                                    Log.d(TAG, "setObserver: mint_cash2: $it")
                                    val minedMint = (mints.toDouble() - it)
                                    Log.d(TAG, "setObserver: mint_cash3: $minedMint")
                                    Log.d(TAG, "setObserver: profile::: ${PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)}")
                                    binding.tvMinedMint.text = String.format("%.2f", minedMint)
                                }
                    } ?: run {
                        binding.tvMintCash.text = "Worth: 0 mints"
                        binding.tvMinedMint.text = "0"
                    }
                }
            }
        })

    }


    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}
