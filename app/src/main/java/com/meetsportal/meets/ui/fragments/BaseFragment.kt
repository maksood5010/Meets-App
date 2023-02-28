package com.meetsportal.meets.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.Navigation
import android.view.ViewGroup

import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.ProceedDialog
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OtherProfileFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileFragment
import com.meetsportal.meets.utils.Constant


abstract class BaseFragment : Fragment() {
    var hideKey=true
    open fun hideBottom() {
        if(activity is MainActivity) {
            Log.i("TAG", "hideNavBar:: ${hideNavBar()} $this ")
            if(hideNavBar()) {
                getMain()?.binding?.tlTabs?.visibility = View.GONE
            } else {
                getMain()?.binding?.tlTabs?.visibility = View.VISIBLE
            }
        }
    }
    lateinit var imgTypeString: Array<String>
    fun navigate(fragment: BaseFragment, bundle: Bundle? = null, stack: Boolean = true, anim: Boolean = false) {
        if(fragment.arguments==null){ fragment.arguments = bundle }
        Navigation.addFragment(requireActivity(), fragment, fragment.javaClass.simpleName, R.id.homeFram, stack, anim)
    }
    fun getMain(): MainActivity? {
        return activity as MainActivity?
    }
    fun popBackStack(){
        getMain()?.popBackStack()
    }
    open fun hideNavBar(): Boolean {
        Log.d("TAG", "hideNavBar: from base")
        return false
    }
    fun openProfile(sid : String?,actionSource : String? = null){
        if(sid == MyApplication.SID){
            Navigation.addFragment(
                requireActivity(),
                ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }else {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(sid,actionSource)
            /*baseFragment = Navigation.setFragmentData(baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                data?.user_meta?.sid)*/
            Navigation.addFragment(
                requireActivity(), baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }
    }
    fun getViewPager(): MeetUpViewPageFragment? {
        return activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
    }

    fun supportImageInsersion(){
        imgTypeString = arrayOf(
            "image/png",
            "image/gif",
            "image/jpeg",
            "image/webp"
        )
    }



    fun openMyProfile(){

    }

    abstract fun onBackPageCome()

    open fun onBackPressed():Boolean{
        return true
    }

    fun getFData(key:String): String? {
        var data: String? = null
        if (arguments != null) {
            data = arguments?.getString(key, null)
        }
        return data
    }

    fun showAlert(onYes: () -> Unit) {
        val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
            if(b) {
                onYes()
            }
        }
    }
    fun showProceed(onYes: () -> Unit): ProceedDialog {
        val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
            if(b) {
                onYes()
            }
        }
        return proceedDialog
    }
    fun rationale() {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert)
        } else {
            AlertDialog.Builder(requireContext())
        }
        builder.setTitle("Mandatory Permissions")
                .setMessage("Manually allow permissions in App settings")
                .setPositiveButton("Proceed") { dialog, which ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 1)
                }.setCancelable(false).setIcon(android.R.drawable.ic_dialog_alert).show()
    }

    @Synchronized
    fun hideKeyboard() {
        var view: View? = requireActivity().currentFocus
        if (view == null) {
            view = View(requireContext())
        }
        hideKeyboard(view)
    }

    @Synchronized
    fun hideKeyboard(view: View?) {
        if (view == null) {
            return
        }
        val imm: InputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }
    @Synchronized
    fun showKeyboard(view: View?) {
        if (view == null) {
            return
        }
        val imm: InputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireActivity(), msg, length).show()
//        val inflater: LayoutInflater = activity?.layoutInflater ?: return
//        val layout: View = inflater.inflate(R.layout.custom_toast, LinearLayout(activity))
//        val text: TextView = layout.findViewById(com.meetsportal.meets.R.id.text)
//        text.text = msg
//
//        var shape = GradientDrawable()
//        shape.cornerRadii = floatArrayOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)
//        shape.setColor(ContextCompat.getColor(requireActivity(), R.color.blacktextColor))
//        text.background = shape
//
//        val toast = Toast(requireActivity())
//        toast.setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, Utils.dpToPx(60f, activity?.resources)) //toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
//        toast.duration = length
//        toast.view = layout
//        toast.show()
    }

    fun setupUI(view:View){
        if(hideKey){
            if(view !is EditText) {
                view.setOnTouchListener { v, event ->
                    hideSoftKeyboard(requireActivity())
                    false
                }
            }

            //If a layout container, iterate over children and seed recursion.

            //If a layout container, iterate over children and seed recursion.
            if(view is ViewGroup) {
                for(i in 0 until (view as ViewGroup).childCount) {
                    val innerView = (view as ViewGroup).getChildAt(i)
                    setupUI(innerView)
                }
            }
        }
    }

    fun hideSoftKeyboard(activity: Activity){
        val inputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus?.windowToken,
                0
            )
        }
    }

    fun showOption(sheet: BottomSheetDialogFragment?, fragment : BaseFragment) {
        // var fragment = sheet as BottomSheetDialogFragment?
        if(sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(fragment.childFragmentManager, sheet.tag)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Log.d(TAG, "onViewCreated: onClick.dispose")
        //hideKeyboard(view)
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard(view)
            setupUI(view)
    }

    override fun onDestroyView() {
        //hideKeyboard()
        super.onDestroyView()
    }



}