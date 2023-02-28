package com.meetsportal.meets.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.registration.*
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import java.lang.RuntimeException
import javax.inject.Inject

@HiltViewModel
class OnboardRegistrationViewModel @Inject constructor(var repository: AppRepository) :
    ViewModel() {

    private val registrationResponse :MutableLiveData<ResultHandler<RegistrationResponse?>> = MutableLiveData();
    private val verifyOtpResponse :MutableLiveData<ResultHandler<OtpResponse?>> = MutableLiveData();
    private val verifyTokenResponse :MutableLiveData<ResultHandler<OtpResponse?>> = MutableLiveData();
    private val setUserNameResponse :MutableLiveData<ResultHandler<OtpResponse?>> = MutableLiveData();
    private val recoverResponse :MutableLiveData<ResultHandler<JsonObject?>> = MutableLiveData();
    private val recoverVerifyResponse :MutableLiveData<ResultHandler<JsonObject?>> = MutableLiveData();
    //private val isUserNameExist : MutableLiveData<Response<UserNameExistResponse?>> = MutableLiveData()
    private val isUserNameExist : MutableLiveData<ResultHandler<UserNameExistResponse?>> = MutableLiveData()

    private val errorResponse:MutableLiveData<String?> = MutableLiveData()

    var compositeDisposable : CompositeDisposable = CompositeDisposable()

    val TAG = OnboardRegistrationViewModel::class.java.simpleName



    fun registerUser(request: RegistrationRequest) {
        var d = repository.getOtp(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                {
                    if (it.isSuccessful)
                        Log.i(TAG," registrationResponse::: ${it.body()}")
                        registrationResponse.value = ResultHandler.Success(it.body())
                }, {
                    if (it is ApiException) {
                        if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                            Log.i(TAG," Sendingcrash to firebase::: ")
                            FirebaseCrashlytics.getInstance().log("error response 400 ${it.errorResponse?.errorCode}")
                            FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                            registrationResponse.value = ResultHandler.Failure(
                                it.errorResponse?.code?:"400",
                                it.errorResponse?.message?:"Something wend wrong",
                                it
                            )
                        } else {
                            FirebaseCrashlytics.getInstance().log("error Api Exception but not 400 exception ${it.errorResponse?.errorCode}")
                            FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                            registrationResponse.value = getUnknownError(it)
                            Log.e(TAG, "Something went Wrong ${it.printStackTrace()}")
                        }
                    } else {
                        FirebaseCrashlytics.getInstance().log("not APi exception something crash ${it.localizedMessage}")
                        FirebaseCrashlytics.getInstance().recordException(RuntimeException(it))
                        registrationResponse.value = getUnknownError(it)
                    }
                }
            )

        compositeDisposable.add(d!!)
    }

    fun verifyOtp(request: OtpRequest) {
        var d = repository.verifyOtp(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t ->
                    if(t.isSuccessful){
                        verifyOtpResponse.value = ResultHandler.Success(t.body())
                    }
                },
                {
                    //Log.e(TAG, " error:: ${it.message}")
                    Utils.handleException(it,verifyOtpResponse,"verifyOtp")
                    //verifyOtpResponse.value = ResultHandler.Failure(it)
                    //errorResponse.value = it.message
                },
                {
                    Log.i(TAG, " Completed:: ")
                }
            )
        compositeDisposable.add(d!!)
    }

    fun verifyGoogle(token: String?, countryCode: String?) {
        val request=JsonObject()
        request.addProperty("id_token",token)
        request.addProperty("region_code",countryCode)
        val d = repository.verifyGoogle(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t ->
                    if(t.isSuccessful){
                        verifyTokenResponse.value = ResultHandler.Success(t.body())
                    }
                },
                {
                    Utils.handleException(it,verifyTokenResponse,"verifyToken")
                },
                {
                    Log.i(TAG, " Completed:: ")
                }
            )
        compositeDisposable.add(d!!)
    }

    fun verifyFacebook(token: String?, countryCode: String?) {
        val request=JsonObject()
        request.addProperty("id_token",token)
        request.addProperty("region_code",countryCode)
        val d = repository.verifyFacebook(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t ->
                    if(t.isSuccessful){
                        verifyTokenResponse.value = ResultHandler.Success(t.body())
                    }
                },
                {
                    Utils.handleException(it,verifyTokenResponse,"verifyToken")
                },
                {
                    Log.i(TAG, " Completed:: ")
                }
            )
        compositeDisposable.add(d!!)
    }


    fun setUserName(username: String?) {
        val request=JsonObject()
        request.addProperty("username",username)
        val d = repository.setUserName(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t ->
                    if(t.isSuccessful){
                        setUserNameResponse.value = ResultHandler.Success(t.body())
                    }
                },
                {
                    Utils.handleException(it,setUserNameResponse,"setUserNameResponse")
                },
                {
                    Log.i(TAG, " Completed:: ")
                }
            )
        compositeDisposable.add(d!!)
    }
    fun recoverAccountOtp(old:String?,new:String?,email:String?) {
        val request=JsonObject()
        request.addProperty("old_phone_number",old)
        request.addProperty("new_phone_number",new)
        request.addProperty("email",email)
        Log.e(TAG, "recoverAccountOtp: called : 1")

        var d = repository.recoverAccountOtp(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t ->
                    if(t.isSuccessful){
                        recoverResponse.value = ResultHandler.Success(t.body())
                    }
                },
                {
                    Utils.handleException(it,recoverResponse,"recoverResponse")
                },
                {
                    Log.i(TAG, " Completed:: ")
                }
            )
        compositeDisposable.add(d!!)
    }
    fun recoverVerifyOtp(old_phone_number:String?,new_phone_number:String?,email:String?,email_code:Int?,new_phone_code:Int?) {
        val request=JsonObject()
        request.addProperty("old_phone_number",old_phone_number)
        request.addProperty("new_phone_number",new_phone_number)
        request.addProperty("email",email)
        request.addProperty("email_code",email_code)
        request.addProperty("new_phone_code",new_phone_code)

        var d = repository.recoverVerifyOtp(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t ->
                    if(t.isSuccessful){
                        recoverVerifyResponse.value = ResultHandler.Success(t.body())
                    }
                },
                {
                    Utils.handleException(it,recoverVerifyResponse,"recoverVerifyResponse")
                },
                {
                    Log.i(TAG, " Completed:: ")
                }
            )
        compositeDisposable.add(d!!)
    }

    fun getIsUserNameExist(username: String) {
        var d = repository.getIsUserNameExist(username)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isSuccessful)
                    isUserNameExist.value = ResultHandler.Success(it.body())
            }, {
                /*if (it is ApiException) {
                    if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                        Log.e(TAG, " logsent::: 1 checkname")
                        FirebaseCrashlytics.getInstance().log("userExist error response 400 ${it.errorResponse?.errorCode}")
                        FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                        isUserNameExist.value = ResultHandler.Failure(
                            it.errorResponse?.code,
                            it.errorResponse?.message,
                            it
                        )
                    } else {
                        Log.e(TAG, " logsent::: 2 checkname")
                        FirebaseCrashlytics.getInstance().log("userExist error Api Exception but not 400 exception ${it.errorResponse?.errorCode}")
                        FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                        isUserNameExist.value = getUnknownError(it)
                        Log.e(TAG, "Something went Wrong ${it.printStackTrace()}")
                    }
                } else {
                    Log.e(TAG, " logsent::: 3 checkname")
                    FirebaseCrashlytics.getInstance().log("userExist not APi exception something crash ${it.printStackTrace()}")
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                    isUserNameExist.value = getUnknownError(it)
                }*/
                handleException(it,isUserNameExist,"getIsUserNameExist")
            })
        compositeDisposable.add(d!!)
    }


    fun observerRegisterUserResponse(): MutableLiveData<ResultHandler<RegistrationResponse?>>{
        return registrationResponse
    }


    fun observeVerifyOtpResponse() :MutableLiveData<ResultHandler<OtpResponse?>>{
        return verifyOtpResponse
    }
    fun observeVerifyTokenResponse() :MutableLiveData<ResultHandler<OtpResponse?>>{
        return verifyTokenResponse
    }
    fun observeSetUserNameResponse() :MutableLiveData<ResultHandler<OtpResponse?>>{
        return setUserNameResponse
    }
    fun observeRecoverResponse() :MutableLiveData<ResultHandler<JsonObject?>>{
        return recoverResponse
    }
    fun observeRecoverVerifyResponse() :MutableLiveData<ResultHandler<JsonObject?>>{
        return recoverVerifyResponse
    }


    fun observeUserNameExist():MutableLiveData<ResultHandler<UserNameExistResponse?>>{
        return isUserNameExist
    }

    fun observeErrorResponse():MutableLiveData<String?>{
        return errorResponse
    }

    fun getUnknownError(it: Throwable): ResultHandler.Failure {
        return ResultHandler.Failure(
            "500",
            "Something went Wrong",
            it
        )
    }

    inline fun <reified T> handleException(it:Throwable,livedata: MutableLiveData<ResultHandler<T>>,apiName:String){

        if (it is ApiException) {
            if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                Log.e(TAG, " logsent::: 1 checkname")
                FirebaseCrashlytics.getInstance().log("$apiName 400 ${it.errorResponse?.errorCode}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = ResultHandler.Failure(
                    it.errorResponse?.code,
                    it.errorResponse?.message,
                    it
                )
            } else {
                Log.e(TAG, " logsent::: 2 checkname")
                FirebaseCrashlytics.getInstance().log("$apiName ${it.errorResponse?.errorCode}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = getUnknownError(it)
                Log.e(TAG, "Something went Wrong ${it.printStackTrace()}")
            }
        } else {
            Log.e(TAG, " logsent::: 3 checkname")
            FirebaseCrashlytics.getInstance().log("$apiName ${it.localizedMessage}")
            FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
            livedata.value = getUnknownError(it)
        }

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}