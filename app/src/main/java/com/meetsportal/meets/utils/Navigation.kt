package com.meetsportal.meets.utils

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.TimeLineFragment
import com.meetsportal.meets.utils.Constant.BACK_STACK

class Navigation {


    companion object {
        private val TAG = Navigation::class.java.simpleName

        fun setFragmentData(baseFragment: BaseFragment,key: String ,value: String?): BaseFragment {
            var b = baseFragment.arguments
            if(b == null){
                var bundle: Bundle = Bundle()
                bundle.putString(key, value)
                baseFragment.arguments = bundle
            }else{
                b.putString(key,value)
                baseFragment.arguments = b
            }

            return baseFragment
        }

        fun getFragmentData(baseFragment: BaseFragment, key:String): String {
            var data: String = ""
            var bundle: Bundle? = baseFragment.arguments
            if (bundle != null) {
                data = bundle.getString(key, "")
            }
            return data
        }



        /*fun removeReplaceFragment(activity: Activity, toFragment: BaseFragment,tag:String, layoutid: Int, stack: Boolean, needAnimation:Boolean) {
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            var ft: FragmentTransaction = fm.beginTransaction()

            val currFragment = fm.findFragmentById(layoutid)
            if (!currFragment?.tag.equals(tag)) {
                currFragment?.let { frag ->

                    //save state of current fragment

                    fragmentSavedState.put(
                        frag.tag!!,
                        fm.saveFragmentInstanceState(frag)
                    )


                }

                //set initial state of fragment if there is any


                toFragment?.setInitialSavedState(fragmentSavedState[fragmentTag])
                toFragment?.let {
                    fm.beginTransaction()
                        .replace(layoutid, it, tag
                        )
                        .commit()
                }
            }
            if (needAnimation)
                //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
            ft.replace(layoutid, toFragment,tag)
            if (stack) {
                ft.addToBackStack(BACK_STACK)
            }
            ft.commit()
            //fragDetail(activity)
        }*/

        fun replaceFragment(activity: Activity, toFragment: BaseFragment, tag:String, layoutid: Int, stack: Boolean, needAnimation:Boolean?,exitAnim : Int = R.anim.fragment_slide_out,thisFragment : Fragment? = null) {
            var ft: FragmentTransaction? = (activity as FragmentActivity?)?.supportFragmentManager?.beginTransaction()
            ft?.replace(R.id.homeFram, toFragment, toFragment.javaClass.simpleName)
            if(stack) {
                ft?.addToBackStack(BACK_STACK)
            }
            ft?.commit()
            //fragDetail(activity)
        }

        fun removeFragment(
            activity: Context,
            tag: String,
        ) {
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            var fragment = fm.findFragmentByTag(tag)
            var ft: FragmentTransaction = fm.beginTransaction()
            fragment?.let {
                Log.i(TAG," fragment:: removed ${fragment::class.java.simpleName}" )
                ft.remove(fragment)
            }
            ft.commit()
            //fragDetail(activity)
        }




        fun removeReplaceFragment(activity: Activity, toFragment: BaseFragment,tag:String, layoutid: Int, stack: Boolean, needAnimation:Boolean) {
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            fm.popBackStack(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            var ft: FragmentTransaction = fm.beginTransaction()
            /*if (needAnimation)
            //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
          */
            ft.replace(layoutid, toFragment,tag)
            if (stack) {
                ft.addToBackStack(BACK_STACK)
            }
            ft.commit()
            //fragDetail(activity)
        }

        fun addFragment(activity: Activity, toFragment: BaseFragment, tag:String, layoutid: Int, stack: Boolean, needAnimation:Boolean?,exitAnim : Int = R.anim.fragment_slide_out,thisFragment : Fragment? = null) {
            MyApplication.smallVibrate()
//            if(activity is HomeActivityNew){
//                activity.hideMyToast()
//            }
            Log.i("Navigation"," ${toFragment.javaClass.simpleName} added")
           // val fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            val fm: FragmentManager? = thisFragment?.parentFragmentManager?:(activity as FragmentActivity).supportFragmentManager
            val ft: FragmentTransaction? = fm?.beginTransaction()
            needAnimation?.let {
                if (needAnimation == true)
                //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
                    ft?.setCustomAnimations(R.anim.slide_in_up,R.anim.slide_out_bottom,R.anim.slide_in_up,R.anim.slide_out_bottom)
                else
                //ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out,R.anim.zoom_in,R.anim.zoom_out)
                    ft?.setCustomAnimations(R.anim.fragment_slide_in,R.anim.fragment_slide_out,R.anim.fragment_slide_in,exitAnim)
                //ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out)
            }?: run {

                ft?.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out)
            }
            ft?.add(layoutid, toFragment,tag)
            if (stack) {
                ft?.addToBackStack(BACK_STACK)
            }
            ft?.commit()
            //fragDetail(activity)
        }

        fun removeNAddFragment(activity: Context, toFragment: BaseFragment, tag:String, layoutid: Int, stack: Boolean, needAnimation:Boolean,exitAnim : Int = R.anim.fragment_slide_out){
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            //fm.popBackStack(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fm.popBackStackImmediate()
            var ft: FragmentTransaction = fm.beginTransaction()
            /*if (needAnimation)
            //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
          */
            ft.add(layoutid, toFragment,tag)
            if (stack) {
                ft.addToBackStack(null)
            }
            ft.commit()
            //fragDetail(activity)
        }
        fun removeReplaceFragment2(activity: Activity, toFragment: Class<out Fragment?>,tag:String, layoutid: Int, stack: Boolean, needAnimation:Boolean) {
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            //fm.popBackStack(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            var ft: FragmentTransaction = fm.beginTransaction()
                .setReorderingAllowed(true)
            if (needAnimation)
            //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
                ft.replace(layoutid, toFragment,null)
            if (stack) {
                ft.addToBackStack(BACK_STACK)
            }
            ft.commit()
            //fragDetail(activity)
        }

        fun addFragment2(
            activity: Context,
            toFragment: BaseFragment,
            tag: String,
            layoutid: Int,
            stack: Boolean,
            needAnimation: Boolean
        ) {
            Log.i("Navigation", " ${toFragment.javaClass.simpleName} added")
            var fm: FragmentManager = (activity as FragmentActivity).supportFragmentManager
            var ft: FragmentTransaction = fm.beginTransaction()
                .setReorderingAllowed(true)
            //if (needAnimation)
            //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
            //ft.add(layoutid, toFragment,tag)
            ft.add(layoutid, TimeLineFragment::class.java, null)
            if (stack) {
                ft.addToBackStack(BACK_STACK)
            }
            ft.commit()
            //fragDetail(activity)
        }

    }

}