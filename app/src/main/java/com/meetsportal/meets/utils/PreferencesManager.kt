package com.meetsportal.meets.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.collection.LruCache
import com.google.gson.GsonBuilder
import com.meetsportal.meets.utils.Constant.PREFRANCE_OTPRESPONSE
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.Constant.PREFRENCE_CUISINE
import com.meetsportal.meets.utils.Constant.PREFRENCE_FACILITY
import com.meetsportal.meets.utils.Constant.PREFRENCE_INTEREST
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE


object PreferencesManager {

    private val TAG = PreferencesManager::class.java.simpleName
    //Shared Preference field used to save and retrieve JSON string
    lateinit var preferences: SharedPreferences

    //Name of Shared Preference file
    private const val PREFERENCES_FILE_NAME = "ShisheoPrefrence"
    var lru : LruCache<String, Any>? = null



    /**
     * Call this first before retrieving or saving object.
     *
     * @param application Instance of application class
     */
    fun with(application: Application) {
        preferences = application.getSharedPreferences(
            PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)


    }

    /**
     * Saves object into the Preferences.
     *
     * @param `object` Object of model class (of type [T]) to save
     * @param key Key with which Shared preferences to
     **/
    fun <T> put(`object`: T, key: String) {
        Log.i(TAG," key::  $key ")
        //Convert object to JSON String.
        val jsonString = GsonBuilder().create().toJson(`object`)
        //Save that String in SharedPreferences
        preferences.edit().putString(key, jsonString).apply()
    }
    fun putBoolean(boolean: Boolean,key: String) {
        preferences.edit().putBoolean(key, boolean).apply()
    }
    fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, true)
    }

    /**
     * Used to retrieve object from the Preferences.
     *
     * @param key Shared Preference key with which object was saved.
     **/
    inline fun <reified T> get(key: String): T? {
        //We read JSON String which was saved.
        val value = preferences.getString(key, null)
        //JSON String was found which means object can be read.
        //We convert this JSON String to model object. Parameter "c" (of
        //type Class < T >" is used to cast.
        Log.i("TAG"," get::: $key $value")
        return GsonBuilder().create().fromJson(value, T::class.java)
    }

    fun deleteSavedData(){
        val editor = preferences.edit()
        editor.remove(PREFRANCE_OTPRESPONSE)
        editor.remove(PREFRENCE_PROFILE)
        editor.remove(PREFRENCE_CATEGORY)
        editor.remove(PREFRENCE_INTEREST)
        editor.remove(PREFRENCE_FACILITY)
        editor.remove(PREFRENCE_CUISINE)
        //editor.clear()
        editor.apply()
    }

    fun getLruInstance():LruCache<String, Any>?{

        lru?.let {
            return it
        }?:run{
            lru = LruCache<String, Any>(1024)
            return lru
        }

    }

    fun <T> putCache(`object`: T, key: String){
        Log.i(TAG," key::  $key ")
        //Convert object to JSON String.
       // val jsonString = GsonBuilder().create().toJson(`object`)
        //val cache: LruCache<String, String> = LruCache<String, String>(1024)
        `object`?.let {
            getLruInstance()?.put(key, `object`)
        }
    }


    inline fun <reified T> getCache(key: String): T? {

        var  value : T? = null
        try{
            value = getLruInstance()?.get(key) as T?
            return value
        }catch (e :Exception){
            Log.e("TAG"," Exceprion_ACme:: ")
            e.printStackTrace()
        }
        return value
    }





}