package com.meetsportal.meets.networking.registration

import com.google.gson.JsonObject
import com.meetsportal.meets.networking.Api
import com.meetsportal.meets.networking.ApiClient
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistrationApiService @Inject constructor() {

    val api:Api = ApiClient.getRetrofit()

    private val TAG = RegistrationApiService::class.java.simpleName

    //private val api : Api
    private var isRequestingAccount = false


    fun getOTP(request: RegistrationRequest): Flowable<Response<RegistrationResponse?>> {
        return api.getOTP(request)
            .doOnSubscribe { disposable -> isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //?.doOnError { throwable: Throwable? -> this.handleAccountError(throwable) }
            .toFlowable(BackpressureStrategy.BUFFER)
    }


    fun verifyOtp(request: OtpRequest): Flowable<Response<OtpResponse?>> {
        return api.verifyOTP(request)
            .doOnSubscribe { disposable -> isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

            /*.doOnError { throwable: Throwable? ->
                Log.i(TAG," verifyOtpError:: ${throwable?.toString()}")
                this.handleAccountError(throwable)
            }*/
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun verifyFacebook(request: JsonObject): Flowable<Response<OtpResponse?>> {
        return api.verifyFacebook(request)
            .doOnSubscribe { isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun verifyGoogle(request: JsonObject): Flowable<Response<OtpResponse?>> {
        return api.verifyGoogle(request)
            .doOnSubscribe { isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun setUserName(request: JsonObject): Flowable<Response<OtpResponse?>> {
        return api.setUserName(request)
            .doOnSubscribe { isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }
    fun recoverAccountOtp(request: JsonObject): Observable<Response<JsonObject?>> {
        return api.recoverAccountOtp(request)
            .doOnSubscribe { isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    fun recoverVerifyOtp(request: JsonObject): Observable<Response<JsonObject?>> {
            return api.recoverVerifyOtp(request)
            .doOnSubscribe { isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getIsUserNameExist(username : String): Flowable<Response<UserNameExistResponse?>> {
        return api.getIsUserNameExist(username,"username")
            .doOnSubscribe { disposable -> isRequestingAccount = true }
            .doOnTerminate { isRequestingAccount = false }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            /*.doOnError { throwable: Throwable? ->
                //Log.i(TAG," getIsUserNameExistError:: ${throwable?.toString()}")
                this.handleAccountError(throwable)
            }*/
            .toFlowable(BackpressureStrategy.BUFFER)

    }

   /* private fun handleAccountError(throwable: Throwable?) {
        Log.i("TAG"," error:: ${throwable.toString()}")
        throw RegistrationTechFailureException(throwable?.message)
    }*/

}