package com.meetsportal.meets.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePage : BaseActivity() , View.OnClickListener{

    val TAG = HomePage::class.simpleName

    lateinit var container:FrameLayout
    lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        //Full Screen
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        initView()
        setUp()

    }

    private fun initView(){
        container = findViewById(R.id.frame)
        bottomNavigationView = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView

        navigateFragment(HomeFragment(),false)
    }

    private fun setUp(){

        var fragment: Fragment? = null

        bottomNavigationView.setOnNavigationItemSelectedListener {
            var selectedId = bottomNavigationView.menu.findItem(bottomNavigationView.selectedItemId).itemId
            if(selectedId == it.itemId && selectedId != R.id.action_shisheo)
                 return@setOnNavigationItemSelectedListener true
            when(it.itemId){
                R.id.action_shisheo -> {
                    intent = Intent(this, AuthenticationActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_search -> {
                    fragment = HomeFragment();}
//                R.id.action_map ->
//                    fragment = MapFragment()
//                R.id.action_offers ->
//                    fragment = OfferFragment()
                else ->  HomeFragment();
            }
            navigateFragment(fragment!!,false)
            return@setOnNavigationItemSelectedListener true
        }

    }

    private fun navigateFragment(fragment:Fragment, isBack : Boolean){
        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.replace(R.id.frame, fragment)
        if (isBack) {
            beginTransaction.addToBackStack("frag")
        }
        beginTransaction.commit()
    }

    override fun onClick(p0: View?) {
    }

}