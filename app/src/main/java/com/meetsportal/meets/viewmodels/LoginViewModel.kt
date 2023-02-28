package com.meetsportal.meets.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(var repository: AppRepository) : ViewModel() {
    private val onLogin: MediatorLiveData<StateResource<String?>> = MediatorLiveData<StateResource<String?>>()
    private val disposable = CompositeDisposable()

    fun login(email: String?, password: String?) {
        /*repository.login(email, password, firebaseToken)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    //onLogin.value = StateResource.loading()
                }

                override fun onComplete() {
                    onLogin.value = StateResource.success(null)
                }

                override fun onError(e: Throwable) {
                    onLogin.value = StateResource.error(MyError(message = e.message))
                }
            })*/
    }

    fun observeLogin(): LiveData<StateResource<String?>> {
        return onLogin
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}