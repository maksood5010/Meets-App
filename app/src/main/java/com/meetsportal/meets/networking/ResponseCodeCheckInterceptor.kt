package com.meetsportal.meets.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ErrorMessages.ErrorMessagesMap
import com.meetsportal.meets.networking.ErrorMessages.UNKNOWN_HOST_EXCEPTION
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class ResponseCodeCheckInterceptor : Interceptor {

    private val TAG = ResponseCodeCheckInterceptor::class.java.simpleName


    override fun intercept(chain: Interceptor.Chain): Response {

        Log.i(TAG," ResponseCodeCheckInterceptor:: 0 ${(PreferencesManager.get<OtpResponse>(
            Constant.PREFRANCE_OTPRESPONSE))?.auth?.access_token}")
        val newRequest = chain.request().newBuilder()
            .addHeader("Accept", "*/*")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
//            .addHeader("Connection", "keep-alive")
            .addHeader("Content-Type", "application/json")
//            .addHeader("Host","gateway-dev.shisheo.com")
            .addHeader("version", "1.0")
//                        .addHeader("X-localization", App.appPreference?.GetString(ConstantClass.SELECT_LANGAGE).toString())
            .addHeader("Authorization", "Bearer " + "${(PreferencesManager.get<OtpResponse>(
                Constant.PREFRANCE_OTPRESPONSE))?.auth?.access_token}")
            .build()
        if(!isInternetAvailable()){
            throw ApiException(ErrorResponse(message = ErrorMessagesMap.get(UNKNOWN_HOST_EXCEPTION),))
        }
        Log.i(TAG," ResponseCodeCheckInterceptor:: 3")
        var response = chain.proceed(chain.request())
        val originalRequest: Request = chain.request()
        if(response.code == 404){
            var data = response.body?.string()
            Log.i(TAG," Service not found ${response.code}")
            /*throw ApiException(
                ErrorResponse(code = "404",data)
            )*/
        }
        else if(response.code != 200){
            var data = response.body?.string()
            Log.e(TAG," responseCode:: ${response.code}")
            Log.e("DATA:", "log: $data" )
            /*throw ApiException(
                Gson().fromJson(
                    data,
                    ErrorResponse::class.java
                )
            )*/
        }
        return response
    }

    fun isInternetAvailable():Boolean{
        var result = false
        val connectivityManager =
            MyApplication.getContext()?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }



}