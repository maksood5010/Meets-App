package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentEditInfoBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.CustomerData
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.profile.ProfileUpdateRequest
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditInfoFragment : BaseFragment() {

    private lateinit var parent: View
    private val TAG: String = EditInfoFragment::class.java.simpleName
    private var isUpdateEnable: Boolean = false
    private var _binding: FragmentEditInfoBinding? = null
    private val binding get() = _binding!!
    val viewModel: UserAccountViewModel by viewModels()
    var loader: LoaderDialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentEditInfoBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = view
        loader = LoaderDialog(requireActivity())
        setUp(view)
    }

    private fun setUp(view: View) {
        binding.countryCodePicker.registerCarrierNumberEditText(binding.etEmergency)

        populateView(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE))

        addListener(view)
        setObserver()

    }

    private fun addListener(view: View) {

        binding.etFirstName.count { enableUpdate(view) }
        binding.etLastName.count { enableUpdate(view) }
//        binding.etEmail.count { enableUpdate(view) }
        binding.etEmergency.count { enableUpdate(view) }
        binding.etBio.count {
            enableUpdate(view)
            binding.tvCounter.text = "$it/150"
        }
        binding.tvUpdate.onClick({
            if(isUpdateEnable) {
                MyApplication.smallVibrate()
                updateProfile()
            }
        })
        binding.tvChangeMail.onClick({

            var baseFragment = UpdateMailFragment.getInstance("email")
            Navigation.addFragment(requireActivity(), baseFragment, UpdateMailFragment.TAG, R.id.homeFram, true, false)

/*
            val updateMailFragment = UpdateMailFragment()
            Navigation.setFragmentData(updateMailFragment,"mail_id",PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.email?:"")
            Navigation.setFragmentData(updateMailFragment,"channel","email")
            Navigation.addFragment(requireActivity(), updateMailFragment, "UpdateMailFragment", R.id.homeFram, true, false)*/
        })
        binding.tvChangePhone.onClick({

            var baseFragment = UpdateMailFragment.getInstance("phone_number")
            Navigation.addFragment(requireActivity(), baseFragment, UpdateMailFragment.TAG, R.id.homeFram, true, false)

            /*val updateMailFragment = UpdateMailFragment()
            Navigation.setFragmentData(updateMailFragment,"phone",PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.phone_number?:"")
            Navigation.setFragmentData(updateMailFragment,"channel","phone_number")
            Navigation.addFragment(requireActivity(), updateMailFragment, "UpdateMailFragment", R.id.homeFram, true, false)
*/
        })
        binding.ivBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun getRequest(): ProfileUpdateRequest {
        Log.i(TAG, " countryPicker.fullNumber with plus ${binding.countryCodePicker.fullNumberWithPlus}")

        Log.i(TAG, "getRequest: ${binding.etFirstName.text.toString().trim().isNullOrBlank()}")
        Log.i(TAG, "getRequest: ${
            if(binding.etFirstName.text.toString().trim()
                        .isNullOrBlank()) null else binding.etFirstName.text.toString().trim()
        }")
        return ProfileUpdateRequest(cust_data = CustomerData(first_name = if(binding.etFirstName.text.toString()
                    .trim().isNullOrBlank()) null else binding.etFirstName.text.toString()
                .trim(), last_name = if(binding.etLastName.text.toString().trim()
                    .isNullOrBlank()) null else binding.etLastName.text.toString(), email = binding.etEmail.text.toString(), emergency_contact = binding.countryCodePicker.fullNumberWithPlus), bio = binding.etBio.text.toString())
    }

    private fun updateProfile() {

        validateForm()?.let {
            parent.showSnackBar(it)
            return
        }
        Log.i(TAG, " fire Api::: ")
        loader?.showDialog()
        disableUpdate()
        viewModel.updateProfile(getRequest())
    }

    private fun validateForm(): String? {
        /*if(fName.text.trim().isNullOrEmpty() || lName.text.trim().isNullOrEmpty() ||
            lName.text.trim().isNullOrEmpty()|| email.text.trim().isNullOrEmpty() ||
            phone.text.trim().isNullOrEmpty() )
                return "Please Fill field"*/

//        if(!binding.etEmail.text.toString()
//                .equals("") && !Utils.isValidEmail(binding.etEmail.text,requireContext())) return "Please Enter valid Email address"

        if(binding.etEmergency.text.toString().equals("")) {
            binding.countryCodePicker.fullNumber = ""
        } else if(!binding.countryCodePicker.isValidFullNumber) return "Please fill correct emergency number"

        return null
    }


    private fun enableUpdate(view: View) {
        isUpdateEnable = true
        binding.tvUpdate.isEnabled = true
//        view.findViewById<TextView>(R.id.tv_update).setTextColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
    }

    private fun disableUpdate() {
        isUpdateEnable = false
        binding.tvUpdate.isEnabled = false
//        view.findViewById<TextView>(R.id.tv_update).setTextColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
    }

    private fun setObserver() {
        viewModel.observeFullProfileChange().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        Log.i(TAG, "ProfileGetResponse:: their response")
                        parent.showSnackBar("Profile Updated Successfully")
                        loader?.hideDialog()
                        populateView(it)
                        PreferencesManager.put(it, Constant.PREFRENCE_PROFILE)
                    }
                }

                is ResultHandler.Failure -> {
                    loader?.hideDialog()
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }


        })

        viewModel.observeErro().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "ProfileGetResponse:: error")
            loader?.hideDialog()
            parent.showSnackBar(it)
        })

        viewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
            parent.showSnackBar(it?.message)
        })
        setFragmentResultListener("update") { key, result ->
            viewModel.getFullProfile()
        }
    }


    private fun populateView(profile: ProfileGetResponse?) {
        profile?.let {
            binding.etFirstName.setText(it.cust_data?.first_name)
            binding.etLastName.setText(it.cust_data?.last_name)
            binding.etUsername.setText(it.cust_data?.username)
            binding.etEmail.setText(it.cust_data?.email)
            binding.countryCodePicker.fullNumber = it.cust_data?.emergency_contact
            binding.etBio.setText(it.social?.bio)
            binding.etPhone.setText(it.cust_data?.phone_number)

        }
        if(profile?.cust_data?.account_type=="phone_number"){
            binding.tvChangeMail.visibility=View.VISIBLE
        }else{
            binding.tvChangeMail.visibility=View.GONE
        }
        disableUpdate()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}