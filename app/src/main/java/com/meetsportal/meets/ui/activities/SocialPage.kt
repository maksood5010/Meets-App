package com.meetsportal.meets.ui.activities

import android.content.Intent
import android.os.Bundle
import com.meetsportal.meets.R
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.HomeFragment
import com.meetsportal.meets.ui.fragments.authfragment.LoginFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ExploreFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileFragment
import com.meetsportal.meets.ui.fragments.socialfragment.TimeLineFragment
import com.meetsportal.meets.utils.Constant.TAG_EXPLORE_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_NEWS_FEED_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_PROFILE_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_SHISHEO_FRAGMENT
import com.meetsportal.meets.utils.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_social_page.*
import javax.inject.Inject

//TODO Remove
@AndroidEntryPoint
class SocialPage : BaseActivity() {

    @Inject
    lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_page)
        supportActionBar?.hide()

        //Full Screen
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)



        initView()
        setUP()
    }

    fun initView(){
        navigateFragment(TimeLineFragment(), TAG_NEWS_FEED_FRAGMENT,false)
    }

    fun setUP(){

        var fragment: BaseFragment? = null
        var tag:String? = null
        repository.getCurrentUid()?.let {
            repository.firebaseAuthSource.getCurrentUserObject()
        }

        bottom_navigation.setOnNavigationItemSelectedListener {
            var selectedId = bottom_navigation.menu.findItem(bottom_navigation.selectedItemId).itemId
            if(selectedId == it.itemId && selectedId != R.id.action_shisheo)
                return@setOnNavigationItemSelectedListener true
            when(it.itemId){
                R.id.action_home -> {
                    fragment = TimeLineFragment()

                    tag = TAG_NEWS_FEED_FRAGMENT
                }
                R.id.action_search -> {
                    fragment = ExploreFragment()
                    tag = TAG_EXPLORE_FRAGMENT
                }
                /*R.id.action_camera -> {
                    fragment = CameraFragment()
                    tag = TAG_CAMERA_FRAGMENT
                }*/
                R.id.action_profile -> {
                    if(repository.getCurrentUid() == null) {
                        fragment = LoginFragment()
                        tag = TAG_PROFILE_FRAGMENT
                    }else{
                        fragment = ProfileFragment()
                        tag = TAG_PROFILE_FRAGMENT
                    }
                }
                R.id.action_shisheo -> {
                    fragment = HomeFragment()
                    tag = TAG_SHISHEO_FRAGMENT
                }
                else -> {
                    TimeLineFragment();
                    tag = TAG_NEWS_FEED_FRAGMENT

                }
            }
            navigateFragment(fragment!!,tag!!,false)
            return@setOnNavigationItemSelectedListener true
        }
    }


    private fun navigateFragment(fragment:BaseFragment,tag:String, isBack : Boolean){
        /*val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.replace(R.id.frame, fragment)
        if (isBack) {
            beginTransaction.addToBackStack(BACK_STACK)
        }
        beginTransaction.commit()*/
        /*if(fragment is ExploreFragment){
            Navigation.removeFragment(this,fragment,tag,R.id.frame,false,true)
            return
        }*/

        //Navigation.addFragment(this,fragment,tag,R.id.frame,true,true)
        Navigation.removeReplaceFragment(this,fragment,tag,R.id.frame,false,true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}