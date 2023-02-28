package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SavedAddressListAdapter
import com.meetsportal.meets.adapter.SavedPlacesListAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentSavedPlaceListBinding
import com.meetsportal.meets.models.AddressModelResponse
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.SavedPlaceFragment
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedPlaceListFragment : BaseFragment() {

     val TAG = SavedPlaceListFragment::class.simpleName
    private var addressList: AddressModelResponse? = null
    private var list: FetchPlacesResponse? = null
    private var _binding: FragmentSavedPlaceListBinding? = null
    private val binding get() = _binding!!
    lateinit var savedPlacesListAdapter: SavedPlacesListAdapter
    lateinit var savedAddressListAdapter: SavedAddressListAdapter

    val placeViewModel: PlacesViewModel by viewModels()
    val viewModel: UserAccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSavedPlaceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun deleteAddress(id: String) {
        showProceed{
            viewModel.deleteAddress(id)
        }?.setMessage("Delete","Are you sure you want to delete this place?")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        getAddress()
        clickViews()
        setObserver()
    }

    fun getAddress() {
        placeViewModel.getSavedPlaceWOpaging(10)
        viewModel.getAddresses(null)
    }

    private fun setObserver() {
        binding.viewAll1.onClick({
//            navigate(SavedPlacesAllFragment())
             Navigation.addFragment(requireActivity(), SavedPlaceFragment(),"SavedPlaceFragment",R.id.homeFram,true,false)
        })

        viewModel.observeGetAddress().observe(viewLifecycleOwner, {

            Log.i("TAG", " observeGetAddress:: ")
            binding.shimmer.stopShimmer()
            binding.shimmer.visibility=View.GONE
            when(it){
                is ResultHandler.Success -> {
                    it.value?.let {
                        savedAddressListAdapter.setListData(it.first)

                    }
                }
                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(),"Something went Wrong")
                }
            }
        })
        viewModel.observeDeleteAddress().observe(viewLifecycleOwner, {
            when (it) {
                is ResultHandler.Success -> {
                    val id = it.value?.get("_id")?.asString ?: ""
                    savedAddressListAdapter.deleteItem(id)
                    showToast(getString(R.string.deleted_success))
                }
                is ResultHandler.Failure -> {
                    showToast(getString(R.string.sorry_try_again))
                }

            }
        })
        placeViewModel.observerSavedPlace().observe(viewLifecycleOwner, {
            Log.i("TAG", " observerSavedPlace:: ")
            savedPlacesListAdapter.setListData(it)
        })

    }

    private fun clickViews() {
        binding.ivBack.onClick({
            requireActivity().onBackPressed()
        })
        binding.viewAll2.onClick({

        })
        binding.tvAddNew.onClick({
            val inviteMap: BaseFragment = InviteMeetMapFragment( this@SavedPlaceListFragment)
//            val inviteMap: BaseFragment = InviteMeetMapFragment<SavedPlaceListFragment>( )
            Navigation.addFragment(
                requireActivity(),
                inviteMap,
                inviteMap.javaClass.simpleName,
                R.id.homeFram,
                stack = true, needAnimation = false
            )
        })
    }

    private fun initViews() {
        binding.noDataAddress.noDataImage.setImageResource(R.drawable.no_saved_places)
        binding.noDataPlaces.noDataImage.setImageResource(R.drawable.no_saved_places)
        binding.noDataAddress.noDataRoot.visibility=View.GONE
        binding.noDataPlaces.noDataRoot.visibility=View.GONE
        binding.noDataPlaces.noDataHeading.text="No Saved Places"
        binding.noDataPlaces.noDataDesc.text="All Your saved Places will appear here"
        binding.noDataAddress.noDataHeading.text="No Custom places"
        binding.noDataAddress.noDataDesc.text="All Your saved custom places will appear here"
        savedPlacesListAdapter = SavedPlacesListAdapter(requireActivity(), list) {
            if(it){
                binding.noDataPlaces.noDataRoot.visibility=View.VISIBLE
                binding.viewAll1.visibility=View.GONE
            }else{
                binding.noDataPlaces.noDataRoot.visibility=View.GONE
                binding.viewAll1.visibility=View.VISIBLE
            }
        }
        savedAddressListAdapter =
            SavedAddressListAdapter(requireContext(), addressList, this@SavedPlaceListFragment) {
                if(it){
                    binding.noDataAddress.noDataRoot.visibility=View.VISIBLE
                }else{
                    binding.noDataAddress.noDataRoot.visibility=View.GONE
                }
            }

        binding.rvPlacesSaved.adapter = savedPlacesListAdapter
        binding.rvAddress.adapter = savedAddressListAdapter
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}