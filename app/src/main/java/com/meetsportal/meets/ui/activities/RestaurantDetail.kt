package com.meetsportal.meets.ui.activities

import android.os.Bundle
import android.util.Log
import com.meetsportal.meets.R
import com.meetsportal.meets.models.Restaurant
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.Constant.RESTAURANT_EXTRA
import com.meetsportal.meets.utils.Navigation

class RestaurantDetail : BaseActivity() {

    private val TAG = RestaurantDetail::class.java.simpleName

    public var restaurant : Restaurant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        supportActionBar?.hide()

        initView()
        setUp()
    }

    private fun initView() {
        val bundle: Bundle? =  intent.extras
        bundle?.let {
             bundle.apply {
                 restaurant = getParcelable(RESTAURANT_EXTRA)
             }
        }
    }

    private fun setUp() {
        navigateFragment(RestaurantDetailFragment(),RestaurantDetailFragment.TAG)
        supportFragmentManager.addOnBackStackChangedListener {
            Log.i(TAG," fragmentCount:: ${supportFragmentManager.backStackEntryCount}")
        }
    }

    private fun navigateFragment(fragment: BaseFragment, tag:String){

        Navigation.removeReplaceFragment(this,fragment,tag,R.id.restrauntFrame,false,true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*var fragment = supportFragmentManager.findFragmentById(R.id.restrauntFrame)
        if(fragment is MyTableBookingFragment){
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }else{
            super.onBackPressed()
        }*/



    }

}