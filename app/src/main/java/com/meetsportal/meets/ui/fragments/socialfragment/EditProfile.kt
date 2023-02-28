package com.meetsportal.meets.ui.fragments.socialfragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.Status
import com.meetsportal.meets.models.UserProfile
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.CustomerData
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.profile.ProfileUpdateRequest
import com.meetsportal.meets.networking.profile.SettingsRequest
import com.meetsportal.meets.ui.activities.AuthenticationActivity
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import com.onesignal.OneSignal
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditProfile : BaseFragment(), View.OnClickListener {

    private val TAG = EditProfile::class.java.simpleName

    lateinit var language: Spinner
    lateinit var ll_userprofile: LinearLayout
    lateinit var llTerms: LinearLayout
    lateinit var llShareApp: LinearLayout
    lateinit var llPrivacy: LinearLayout
    lateinit var back: ImageView
    lateinit var iv_profile: ImageView
    lateinit var logOut: TextView
    lateinit var tvUserName: TextView

    //    lateinit var bioTextCouter: TextView
//    lateinit var bio: EditText
//    lateinit var fName: EditText
//    lateinit var lName: EditText
//    lateinit var uName: EditText
//    lateinit var phone: EditText
//    lateinit var email: EditText
//    lateinit var emergency: EditText
//    lateinit var countryPicker : CountryCodePicker
    lateinit var notification: CompoundButton
    lateinit var interest: CompoundButton
    lateinit var discover: CompoundButton

    //    lateinit var publicPost: SwitchCompat
//    lateinit var followingPost: SwitchCompat
//    lateinit var sharePost: SwitchCompat
//    lateinit var memeIt: SwitchCompat
//    lateinit var checkIn: SwitchCompat
//    lateinit var meetUP: SwitchCompat
    lateinit var parent: View
    var userProfile: UserProfile? = null
    var isUpdateEnable = false

    var loader: LoaderDialog? = null

    private val paths = arrayOf("English")
    lateinit var spinnerAdapter: ArrayAdapter<String>


    val viewModel: UserAccountViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
        //observerInfoChange()
        observerProfileChange()

    }

    private fun initView(view: View) {

        language = view.findViewById(R.id.spn_language)
        llTerms = view.findViewById(R.id.llTerms)
        llShareApp = view.findViewById(R.id.llShareApp)
        llPrivacy = view.findViewById(R.id.llPrivacy)
        ll_userprofile = view.findViewById(R.id.ll_userprofile)
        back = view.findViewById(R.id.iv_back)
        iv_profile = view.findViewById(R.id.iv_profile)
//        bio = view.findViewById(R.id.et_bio)
//        bioTextCouter = view.findViewById(R.id.tv_counter)
//        fName = view.findViewById(R.id.et_first_name)
//        lName = view.findViewById(R.id.et_last_name)
//        uName = view.findViewById(R.id.et_username)
//        phone = view.findViewById(R.id.et_phone)
//        email = view.findViewById(R.id.et_email)
//        emergency = view.findViewById(R.id.et_emergency)
//        countryPicker = view.findViewById(R.id.country_code_picker)
//        countryPicker.registerCarrierNumberEditText(emergency)

        notification = view.findViewById(R.id.stc_notification)
        interest = view.findViewById(R.id.stc_interest)
        discover = view.findViewById(R.id.stc_discover)
//        publicPost = view.findViewById(R.id.stc_public_post)
//        followingPost = view.findViewById(R.id.stc_following_post)
//        sharePost = view.findViewById(R.id.stc_share_post)
//        memeIt = view.findViewById(R.id.stc_meme_it)
//        checkIn = view.findViewById(R.id.stc_check_in)
//        meetUP = view.findViewById(R.id.stc_meet_up)
        logOut = view.findViewById(R.id.log_out)
        tvUserName = view.findViewById(R.id.tvUserName)
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        tvVersion.text = "version ${BuildConfig.VERSION_NAME}"

        loader = LoaderDialog(requireActivity())
        parent = view

    }

    private fun setUp(view: View) {
        populateView(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE))
        //view.findViewById<TextView>(R.id.sign_out).setOnClickListener(this)

        spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_text, paths)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language.adapter = spinnerAdapter


        addListener(view)
        setObserver()

    }

    private fun addListener(view: View) {

//        fName.count { enableUpdate(view) }
//        lName.count {enableUpdate(view) }
//        phone.count { enableUpdate(view) }
//        email.count{ enableUpdate(view) }
//        emergency.count{ enableUpdate(view) }
//        bio.count {
//            enableUpdate(view)
//            bioTextCouter.text = "$it/150"
//        }
        notification.isChecked = PreferencesManager.getBoolean(Constant.notification_pref)
        back.setOnClickListener(this)
        notification.enabling { enable: Boolean ->
            Log.d(TAG, "addListener: $enable ")
            if(enable) {
                MyApplication.SID?.let {
                    OneSignal.setExternalUserId(it)
                    OneSignal.disablePush(false)
                    PreferencesManager.putBoolean(true, Constant.notification_pref)
                }
            } else {
                MyApplication.SID?.let {
                    OneSignal.setExternalUserId(it)
                }
                OneSignal.disablePush(true)
                PreferencesManager.putBoolean(false, Constant.notification_pref)
            }
            Log.d(TAG, "addListener: :: ${PreferencesManager.getBoolean(Constant.notification_pref)}")
//            viewModel.updateProfile(
//                ProfileUpdateRequest(
//                    cust_data = CustomerData(
//                        settings = SettingsRequest(notifications_enabled = it)
//                    )
//                )
//            )
        }
        interest.enabling {
            viewModel.updateProfile(ProfileUpdateRequest(cust_data = CustomerData(settings = SettingsRequest(show_interests = it))))
        }
//        publicPost.enabling { viewModel.updateProfile(ProfileUpdateRequest(cust_data = CustomerData(settings = SettingsRequest(public_posts_enabled = it)))) }
//        followingPost.enabling { viewModel.updateProfile(ProfileUpdateRequest(cust_data = CustomerData(settings = SettingsRequest(followings_posts_enabled = it)))) }
//        checkIn.enabling { viewModel.updateProfile(ProfileUpdateRequest(cust_data = CustomerData(location_check_in_enabled = it))) }
        discover.enabling {
            viewModel.updateProfile(ProfileUpdateRequest(cust_data = CustomerData(location_meet_up_enabled = it)))
        }
        logOut.setOnClickListener {
            val showProceed = showProceed {
                viewModel.signOut()
                /*PreferencesManager.deleteSavedData()
                OneSignal.disablePush(true)*/
                activity?.startActivity(Intent(requireContext(), AuthenticationActivity::class.java))
                activity?.finish()
            }
            showProceed.setMessage("Log out", "Are you sure you want to log out?")

        }
        ll_userprofile.onClick({
            val edit: BaseFragment = EditInfoFragment()
            Navigation.addFragment(activity as Activity, edit, Constant.TAG_EDIT_INFO, R.id.homeFram, stack = true, needAnimation = false)
        })

        llShareApp.onClick({
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Meets app")
                var shareMessage = "\nI just found this cool application\n"
                shareMessage = "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "Share app"))
            } catch(e: Exception) {
                showToast("No application found to share")
            }
        })
        llTerms.setOnClickListener {
            val terms: BaseFragment = TermsFragment()
            Navigation.setFragmentData(terms, "url", Constant.TermsUrl)
            Navigation.setFragmentData(terms, "title", getString(R.string.terms))
            Navigation.addFragment(activity as Activity, terms, "TermsFragment", R.id.homeFram, stack = true, needAnimation = false)
        }
        llPrivacy.setOnClickListener {
            val terms: BaseFragment = TermsFragment()
            Navigation.setFragmentData(terms, "url", Constant.PrivacyUrl)
            Navigation.setFragmentData(terms, "title", getString(R.string.privacy))
            Navigation.addFragment(activity as Activity, terms, "TermsFragment", R.id.homeFram, stack = true, needAnimation = false)
        }
    }

    private fun setObserver() {
        viewModel.observeFullProfileChange().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {

                        Log.i(TAG, "ProfileGetResponse:: their response")
                        loader?.hideDialog()
                        populateView(it)
                        PreferencesManager.put(it, PREFRENCE_PROFILE)
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }

            /*it?.let {

                Log.i(TAG, "ProfileGetResponse:: their response")
                loader?.hideDialog()
                populateView(it)
                PreferencesManager.put(it, PREFRENCE_PROFILE)
            }*/
        })

        viewModel.observeErro().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "ProfileGetResponse:: error")
            loader?.hideDialog()
            parent.showSnackBar("it")
        })

        viewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
            parent.showSnackBar(it?.message)
        })
    }

//    private fun enableUpdate(view: View){
//        isUpdateEnable = true
//        view.findViewById<TextView>(R.id.tv_update).setTextColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
//    }

    private fun populateView(profile: ProfileGetResponse?) {
        profile?.let {
//            fName.setText(it.cust_data.first_name)
//            lName.setText(it.cust_data.last_name)
//            uName.setText(it.cust_data.username)
//            email.setText(it.cust_data.email)
//            countryPicker.fullNumber = it.cust_data.emergency_contact
//            //emergency.setText(it.cust_data.emergency_contact)
//            bio.setText(it.social.bio)
//            phone.setText(it.cust_data.phone_number)
            Log.i(TAG, " bhbcdhc:: $it")
//            publicPost.isChecked = it.cust_data.settings.public_posts_enabled ?: false
            interest.isChecked = it.cust_data?.settings?.show_interests ?: false
//            followingPost.isChecked = it.cust_data.settings.followings_posts_enabled ?: false
//            checkIn.isChecked = it.cust_data.location_check_in_enabled ?: false
            discover.isChecked = it.cust_data?.location_meet_up_enabled ?: false
            Utils.stickImage(requireContext(), iv_profile, it.cust_data?.profile_image_url, null)
            iv_profile.onClick({

                var baseFragment: BaseFragment = ProfileEditPicPage()
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.TYPE, ProfileEditPicPage.PROFILE_PIC)
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, it.cust_data?.profile_image_url)
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OWN)
                Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)
            })
            tvUserName.text = it.cust_data?.username

        }
    }

    /*private fun observerInfoChange() {
        viewModel.getUserInfo()?.observe(viewLifecycleOwner, Observer<UserProfile?> {
            userProfile = it
            bindData(it)
        })
    }*/

//    private fun bindData(it: UserProfile?) {
//        fName.setText(it?.firstName)
//        lName.setText(it?.lastName)
//        uName.setText(it?.displayName)
//        phone.setText(it?.phoneNumber)
//        email.setText(it?.email)
//        emergency.setText(it?.email)
//        bio.setText(it?.bio)
//        setLanguage(it)
//        it?.isNotificationOn?.let {
//            notification.isChecked = it
//        }
//
//    }

    private fun setLanguage(it: UserProfile?) {
        when(it?.language) {
            null      -> language.setSelection(0)
            "English" -> language.setSelection(0)
            "Spanish" -> language.setSelection(1)
            "Hindi"   -> language.setSelection(2)
        }
    }
//
//    private fun updateProfile() {
//
//        validateForm()?.let {
//            parent.showSnackBar(it)
//            return
//        }
//        Log.i(TAG," fire Api::: ")
//        loader?.showDialog()
//        viewModel.updateProfile(getRequest())
//
//        //updateUserProfile()
//        //viewModel.updateProfile(userProfile)
//
//    }

//    private fun getRequest():ProfileUpdateRequest{
//        return ProfileUpdateRequest(
//            cust_data = CustomerData(
//                first_name = fName.text.toString(),
//                last_name = lName.text.toString(),
//                email = email.text.toString(),
//                emergency_contact = "+".plus(countryPicker.fullNumber)
//            ),
//            bio = bio.text.toString()
//        )
//    }
//
//    private fun updateUserProfile() {
//        userProfile?.firstName = fName.text.toString()
//        userProfile?.lastName = lName.text.toString()
//        userProfile?.displayName = uName.text.toString()
//        userProfile?.phoneNumber = phone.text.toString()
//        userProfile?.email = email.text.toString()
//        userProfile?.bio = bio.text.toString()
//        userProfile?.firstName = fName.text.toString()
//        userProfile?.isNotificationOn = notification.isChecked
//        userProfile?.language = language.selectedItem.toString()
//    }

    private fun observerProfileChange() {
        viewModel.observerProfileChange()
                ?.observe(viewLifecycleOwner, Observer<StateResource<String>> {
                    it?.let {
                        when(it.status) {
                            Status.LOADING -> {
                                //TODO(" add loader ")
                            }

                            Status.SUCCESS -> {
                                //TODO(" dismiss loader ")
                                parent.showSnackBar("Profile Updated Successfully")
                            }

                            Status.ERROR   -> {
                                //TODO(" dismiss loader ")
                                parent.showSnackBar(it.message!!)
                            }
                        }
                    }
                })


    }

//    private fun validateForm():String?{
//        /*if(fName.text.trim().isNullOrEmpty() || lName.text.trim().isNullOrEmpty() ||
//            lName.text.trim().isNullOrEmpty()|| email.text.trim().isNullOrEmpty() ||
//            phone.text.trim().isNullOrEmpty() )
//                return "Please Fill field"*/
//
//        if(!email.text.toString().equals("") && !Utils.isValidEmail(email.text))
//            return "Please Enter valid Email address"
//
//        if(emergency.text.toString().equals("")){
//            countryPicker.fullNumber = ""
//        }
//        else if(!countryPicker.isValidFullNumber)
//            return "Please fill correct emergency number"
//
//        return null
//    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_back -> {
                Log.i("EditProfile", " Clicked:: 757")
                activity?.onBackPressed()
            }
//            R.id.tv_update -> {
//                if(isUpdateEnable){
//                    MyApplication.smallVibrate()
//                    updateProfile()
//                }
//            }
            /*R.id.sign_out -> {
                viewModel.signOut()
                parent.showSnackBar("Signed Out")
                (activity as SocialPage).bottom_navigation.selectedItemId = R.id.action_home
            }*/
        }
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
        populateView(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE))
    }


}