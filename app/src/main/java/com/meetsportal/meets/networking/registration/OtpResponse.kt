package com.meetsportal.meets.networking.registration

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OtpResponse (
    @SerializedName("auth")
    var auth :Auth? ,
    @SerializedName("user")
    var user: User?,

): Parcelable
@Parcelize
data class Auth(
    @SerializedName("access_token")
    var access_token : String?,
    @SerializedName("refresh_token")
    var refresh_token : String?,
    @SerializedName("firebase")
    var firebase : FirebaseModel?,
    @SerializedName("first_time_login")
    var first_time_login : Boolean?
): Parcelable
@Parcelize
data class User(
    @SerializedName("sid")
    var sid : String?,
    @SerializedName("phone_number")
    var phone_number : String?,
    @SerializedName("email")
    var email : String?,
    @SerializedName("mid")
    var mid: String?,
    @SerializedName("username")
    var username : String?,
): Parcelable
@Parcelize
data class FirebaseModel(
//    var password : String?,
//    var email : String?,
    @SerializedName("custom_token")
    var custom_token : String?
): Parcelable
