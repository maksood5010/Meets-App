package com.meetsportal.meets.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.meetsportal.meets.models.UserProfile
import com.meetsportal.meets.utils.Constant
import io.reactivex.Completable
import javax.inject.Inject

class FirebaseAuthSource @Inject constructor(
    var firebaseAuth: FirebaseAuth,
    var firebaseFirestore: FirebaseFirestore
){

    val TAG  = FirebaseAuthSource::class.java.simpleName

    //get current user uid
    fun getCurrentUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    //get current user
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun register(email: String?, password: String?, profile: UserProfile?): Completable {
        return Completable.create { emitter ->
            firebaseAuth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnFailureListener { e -> emitter.onError(e) }
                .addOnCompleteListener { //create new user
                    profile?.id = getCurrentUid()

                    firebaseFirestore.collection(Constant.USERS_NODE)
                        .document(getCurrentUid()!!).set(profile!!)
                        .addOnFailureListener { e -> emitter.onError(e) }
                        .addOnSuccessListener { emitter.onComplete() }
                }
        }

    }

    /*fun getUserById(uid: String):Flowable<DocumentSnapshot>{
       return Flowable.create(FlowableOnSubscribe<DocumentSnapshot> { emitter ->
           val reference = firebaseFirestore.collection(Constant.USERS_NODE).document(uid)
           var registration = reference.addSnapshotListener { value, error ->
               error?.let {
                   emitter.onError(error)
               }
               value?.let {
                   emitter.onNext(value)
               }
           }
           emitter.setCancellable {
               registration.remove()
           }
       }, BackpressureStrategy.BUFFER)
    }*/

    fun getCurrentUserObject()  {

        var userObject :  UserProfile? = null
        if(firebaseFirestore == null){

        }
        firebaseFirestore
            .collection(Constant.USERS_NODE)
            .document(getCurrentUid()!!)
            .addSnapshotListener{ documentSnapshot, error ->
                error?.let {
                    Log.e("getCurrentUserObject"," error: $it")
                    return@addSnapshotListener
                }
                Log.i(TAG," flow:: 1")
                documentSnapshot?.let {
                    Log.i("getCurrentUserObject"," ${documentSnapshot.data} ")
                    UserProfile.currentUserProile = documentSnapshot.toObject(UserProfile::class.java)

                }
            }
    }

    //login
    fun login(firebaseToken: String?): Completable? {
        return Completable.create { emitter ->
            firebaseAuth.signInWithCustomToken(firebaseToken)
            //firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener { e ->
                    Log.e(TAG," firebaseSignIn::: Failed  ${e.printStackTrace()}")
                    emitter.onError(e) }
                .addOnSuccessListener {
                    Log.i(TAG," firebaseSignIn::: ${it.additionalUserInfo}")
                    emitter.onComplete()
                }
        }
    }

    //logout
    fun signOut() {
        firebaseAuth.signOut()
    }
}