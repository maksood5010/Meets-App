package com.meetsportal.meets.ui.fragments.authfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meetsportal.meets.R
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.UserProfile
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.showSnackBar
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import dagger.hilt.android.AndroidEntryPoint

//TODO Remove
@AndroidEntryPoint
class RegisterFragment : BaseFragment() {

    val registerViewmodel: FireBaseViewModal by viewModels()

    lateinit var firstName: EditText
    lateinit var lastName: EditText
    lateinit var email: EditText
    lateinit var phone: EditText
    lateinit var password: EditText
    lateinit var confirmPassword: EditText
    lateinit var term: CheckBox
    lateinit var create: Button
    lateinit var faceBook: LinearLayout
    lateinit var google: LinearLayout
    lateinit var parent: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        subscribeObservers()

    }

    private fun initView(view: View) {
        parent = view
        firstName = view.findViewById(R.id.ed_fname)
        lastName = view.findViewById(R.id.ed_lname)
        email = view.findViewById(R.id.ed_email)
        phone = view.findViewById(R.id.ed_phone)
        password = view.findViewById(R.id.ed_pass)
        confirmPassword = view.findViewById(R.id.ed_confirm)
        term = view.findViewById(R.id.termCheck)
        create = view.findViewById(R.id.signUpButton)
        faceBook = view.findViewById(R.id.llfacebookLogin)
        google = view.findViewById(R.id.llgoogleLogin)

        view.findViewById<ImageView>(R.id.ivBack)
                .setOnClickListener { requireActivity().onBackPressed() }
        view.findViewById<TextView>(R.id.moveToLogin)
                .setOnClickListener { requireActivity().onBackPressed() }

    }

    private fun setUp() {
        create.setOnClickListener {
            validateField()?.let {
                parent.showSnackBar(it)
                return@setOnClickListener
            }
            registerUser()
            Log.i("TAG", " Form validated::  ")
        }
    }

    private fun subscribeObservers() {
        registerViewmodel.observeRegister()
                ?.observe(viewLifecycleOwner, Observer<StateResource<String?>> {
                    it?.let {
//                when (it.status) {
//                    Status.LOADING -> loadingDialog.show(activity?.supportFragmentManager!!,"loadingDialog")
//                    Status.SUCCESS -> {
//                        loadingDialog.dismiss()
//                    }
//                    Status.ERROR ->{
//                        loadingDialog.dismiss()
//                        parent.showSnackBar(it.message!!)
//                    }
//                }
                    }
                })
    }


    private fun registerUser() {
        var userProfile = UserProfile(firstName = firstName.text.toString(), lastName = lastName.text.toString(), displayName = firstName.text.toString(), phoneNumber = phone.text.toString(), email = email.text.toString(), verified = false, loginType = "Email")

        registerViewmodel.register(email.text.toString().trim(), password.text.toString()
                .trim(), userProfile)
    }

    private fun validateField(): String? {
        if(firstName.text.trim().isNullOrEmpty() || lastName.text.trim()
                    .isNullOrEmpty() || email.text.trim().isNullOrEmpty() || phone.text.trim()
                    .isNullOrEmpty() || password.text.trim()
                    .isNullOrEmpty() || confirmPassword.text.trim()
                    .isNullOrEmpty()) return "Please fill all field!!"
        if(!Utils.isValidEmail(email.text, requireContext())) return "Please Enter valid Email address"
        if(password.text.length < 6 || !Utils.isValidPassword(password.text)) return "Password should be at least 6 letter and contain a Capital a Small letter and a number"
        if(!password.text.trim()
                    .equals(confirmPassword.text.trim())) return "Password should match with Confirm Password!!"
        if(!term.isChecked) return "Please accept Term & Condition!!"
        if(!Utils.isNetworkConnected(requireActivity())) return "Please Check your internet connection!!"
        return null
    }

    override fun onBackPageCome() {

    }

}