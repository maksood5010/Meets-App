package com.meetsportal.meets.ui.fragments.authfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meetsportal.meets.R
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.Status
import com.meetsportal.meets.ui.activities.SocialPage
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant.TAG_FORGET_PASSWORD
import com.meetsportal.meets.utils.Constant.TAG_REGISTRATION
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.showSnackBar
import com.meetsportal.meets.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_social_page.*
import kotlinx.android.synthetic.main.fragment_login.*

//TODO Remove
@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    val TAG = LoginFragment::class.java.simpleName
    val viewModel: LoginViewModel by viewModels()

    lateinit var skip: TextView
    lateinit var email: EditText
    lateinit var pass: EditText
    lateinit var forget: TextView
    lateinit var createAccount: TextView
    lateinit var login: Button
    lateinit var facebookLogin: LinearLayout
    lateinit var googleLogin: LinearLayout

    lateinit var parent: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        subscribeObservers()
    }

    private fun initView(view: View) {
        parent = view
        skip = view.findViewById(R.id.skipButton)
        email = view.findViewById(R.id.ed_mail)
        pass = view.findViewById(R.id.ed_pass)
        forget = view.findViewById(R.id.forgot_text)
        createAccount = view.findViewById(R.id.moveToSignUp)
        login = view.findViewById(R.id.buttonLogin)
        facebookLogin = view.findViewById(R.id.llfacebookLogin)
        googleLogin = view.findViewById(R.id.llgoogleLogin)

    }

    private fun setUp() {

        login.setOnClickListener {
            validateField()?.let {
                parent.showSnackBar(it)
                return@setOnClickListener
            }
            viewModel.login(email.text.toString().trim(), pass.text.toString().trim())
        }
        createAccount.setOnClickListener {
            val baseFragment: BaseFragment = Navigation.setFragmentData(RegisterFragment(), "", "");
            Navigation.addFragment(requireActivity(), baseFragment, TAG_REGISTRATION, R.id.frame, true, false);
        }
        forget.setOnClickListener {
            val baseFragment: BaseFragment = Navigation.setFragmentData(ForgetPasswordFragment(), "", "");
            Navigation.addFragment(requireActivity(), baseFragment, TAG_FORGET_PASSWORD, R.id.frame, true, false);
        }
    }

    private fun subscribeObservers() {
        viewModel.observeLogin()?.observe(viewLifecycleOwner, Observer<StateResource<String?>> {
            it?.let {
                when(it.status) {
                    Status.LOADING -> {
                        //TODO(" add loader")
                    }

                    Status.SUCCESS -> {
                        //TODO(" add loader move to newsFeeed ")
                        parent.showSnackBar("Signed in Successfull")
                        (activity as SocialPage).bottom_navigation.selectedItemId = R.id.action_home

                    }

                    Status.ERROR   -> {
                        //TODO(" dissmiss Loader ")
                        Log.e(TAG, it.message!!)
                        parent.showSnackBar("Check your email and password!!")
                    }
                }
            }
        })
    }

    private fun validateField(): String? {
        if(!Utils.isValidEmail(email.text, requireContext())) return "Please enter valid Email address!!"
        if(pass.text.length <= 0) return "Password should not be Empty!!"
        return null
    }

    override fun onBackPageCome() {

    }

}