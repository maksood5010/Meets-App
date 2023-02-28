package com.meetsportal.meets.networking

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.networking.registration.RefreshResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.PREFRANCE_OTPRESPONSE
import com.meetsportal.meets.utils.Logging
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private val TAG = ApiClient::class.java.simpleName

    var retrofit: Retrofit? = null
    var retrofit2: Retrofit? = null

    val cacheSize = (5*1024*1025).toLong()
    val myCache = Cache(MyApplication.getContext()!!.cacheDir,cacheSize)

    fun getRetrofit(): Api {


        Log.i(TAG," check:::::::: 1")
        if (retrofit == null) {
            val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    //Logging.i("OkHttp", "log: $message")
                    Logging.i("OkHttp", "log: $message")
                }

            })
            interceptor.level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }



            val client = OkHttpClient.Builder()
                .cache(myCache)
                .addInterceptor(HeaderInterceptor())
                //.addInterceptor(ResponseCodeCheckInterceptor())
                .addInterceptor(interceptor)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            Log.i(TAG," check:::::::: 12")

            //KeyStore.getInstance()
            val gson = GsonBuilder()
                .setLenient()
                .create()

            Log.i(TAG," check:::::::: 11")

            retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.SERVER_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            Log.i(TAG," check:::::::: 13")

        }
        return retrofit!!.create(Api::class.java)
    }

    fun getCacheRetrofit():Api{

        if (retrofit2 == null) {
            val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logging.i("OkHttp", "log: $message")
                }

            })
            interceptor.level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }

            /*val cacheSize = (5*1024*1025).toLong()
            val myCache = Cache(MyApplication.getContext()!!.cacheDir,cacheSize)*/

            val client = OkHttpClient.Builder()
                .cache(myCache)
                .addInterceptor(CacheHeaderInerceptor())
                //.addInterceptor(ResponseCodeCheckInterceptor())
                .addInterceptor(interceptor)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            Log.i(TAG," check:::::::: 12")

            //KeyStore.getInstance()
            val gson = GsonBuilder()
                .setLenient()
                .create()

            Log.i(TAG," check:::::::: 11")

            retrofit2 = Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.SERVER_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            Log.i(TAG," check:::::::: 13")

        }
        return retrofit2!!.create(Api::class.java)

    }

    class HeaderInterceptor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = chain.run {
            Log.i(TAG," check:::::::: 3")
            var requestBuilder = request()
                .newBuilder()
                .addHeader("Authorization", "Bearer " + "${
                    (PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE))?.auth?.access_token
                }")
                //.addHeader("Host","gateway-dev.shisheo.com")
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/json")



            var request = if(Utils.isOnline(MyApplication.getContext()!!)){
                requestBuilder
                    .addHeader("Cache-Control","public,max-age="+5)
                    .build()
            }
            else{
                requestBuilder
                    .addHeader("Cache-Control","public,only-if-cached,max-stale="+60*60*24*7)
                    .build()
            }

            var response = proceed(request)



//            var response  = proceed(
//                request()
//                    .newBuilder()
//                    .addHeader(
//                        "Authorization", "Bearer " + "${
//                            (ModelPreferencesManager.get<OtpResponse>(
//                                Constant.PREFRANCE_OTPRESPONSE
//                            ))?.auth?.access_token
//                        }"
//                    )
//                    .addHeader("Cache-Control", "no-cache")
//                    //.addHeader("Host","gateway-dev.shisheo.com")
//                    .addHeader("Accept", "*/*")
//                    .addHeader("Connection", "keep-alive")
//                    .addHeader("Content-Type", "application/json")
//                    //.removeHeader("User-Agent")
//                    .build()
//            )
            Log.i(TAG, " responseCode:: server ${response.code}")
            if (response.code == 401) {

                Log.i(TAG, " callRefreshToken::: 1")
                val client = OkHttpClient()
                val requestBody = FormBody.Builder()
                    .add("client_id", "api-service")
                    .add("client_secret", "54c7cc09-84d4-40cb-a140-8f4d97f10c61")
                    .add("scope", "openid")
                    .add("grant_type", "refresh_token")
                    .add(
                        "refresh_token", PreferencesManager.get<OtpResponse>(
                            Constant.PREFRANCE_OTPRESPONSE
                        )?.auth?.refresh_token!!
                    )
                    .build()

                val refreshRewquest = Request.Builder()
                    .url(BuildConfig.SERVER_URL.plus("user/auth/refresh"))
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(requestBody)
                    .build()

                val refreshResponse = client.newCall(refreshRewquest).execute()
                Log.i(TAG," refreshResponse.code:: ${refreshResponse.code} ")
                if(refreshResponse.code != 200){
                    Log.i(TAG," throwing APi Exceptipon ::: ")
                    throw ApiException(ErrorResponse(-1,refreshResponse.code.toString(),"Session Expire please login again!!!"))
                }
                var refreshJson = refreshResponse.body?.string()

                Log.i(TAG," refreshResponse.code:::: 0001")
                val refreshResponseObject = Gson().fromJson(
                    refreshJson,
                    RefreshResponse::class.java
                )
                PreferencesManager.put(
                    PreferencesManager.get<OtpResponse>(
                        PREFRANCE_OTPRESPONSE
                    )?.apply {
                        auth?.access_token = refreshResponseObject.access_token
                    }, PREFRANCE_OTPRESPONSE
                )

                response = proceed(
                    request()
                        .newBuilder()
                        .addHeader(
                            "Authorization",
                            "Bearer ".plus(refreshResponseObject.access_token)
                        )
                        .build()
                )
            }
            else if (response.code in 400..403) {
                var erroeresponse :ErrorResponse?
                try {
                    var json = response.body?.string()
                    Log.e(TAG,"Error 400 $json")
                    erroeresponse = MyApplication.getGsons()
                        ?.fromJson(json, ErrorResponse::class.java)?.apply {
                            errorCode = response.code
                        }

                }catch (e: Exception){
                    Log.e(TAG,"Error 400 Unable to  transform jsonto Object $e")
                    throw ApiException(null)
                }
                erroeresponse?.let {
                    throw ApiException(
                        erroeresponse
                    )
                }
            }else if(response.code == 504){
                Log.e(TAG," NoInternetCOnnection:: ")
                throw ApiException(
                    ErrorResponse(504,"504","No Internet Connection!!!")
                )
                Log.e(TAG," NoInternetCOnnection:: 1")
            }
            else if(response.code < 200 || response.code >299){
                Log.i(TAG," check:::::::: 4")
                Log.i(TAG," resposeCode:: ${response.code}")
                FirebaseCrashlytics.getInstance().log("${request.url} 400 ${response.code} ${MyApplication.SID}")
                throw ApiException(ErrorResponse(response.code,message = "Something went wrong please try again later",code = "Something went wrong please try again later"))
            }
            return response
        }
    }

    class CacheHeaderInerceptor:  Interceptor{
        override fun intercept(chain: Interceptor.Chain): Response = chain.run {
            Log.i(TAG," check:::::::: 3")
            var requestBuilder = request()
                .newBuilder()
                .addHeader(
                    "Authorization", "Bearer " + "${
                        (PreferencesManager.get<OtpResponse>(
                            Constant.PREFRANCE_OTPRESPONSE
                        ))?.auth?.access_token
                    }"
                )
                //.addHeader("Host","gateway-dev.shisheo.com")
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/json")



            var request : Request = requestBuilder
                    .addHeader("Cache-Control","public,only-if-cached,max-stale="+60*60*24*7)
                    .build()
            var response = proceed(request)
            Log.i(TAG, " responseCode:: cache ${response.code}")

            return response
        }

    }
}