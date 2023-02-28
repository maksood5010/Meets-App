package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class UserProfile(

    @get: PropertyName("id") @set: PropertyName("id") var id: String? = null,
    @get: PropertyName("firstName") @set: PropertyName("firstName") var firstName: String? = null,
    @get: PropertyName("lastName") @set: PropertyName("lastName") var lastName: String? = null,
    @get: PropertyName("displayName") @set: PropertyName("displayName") var displayName: String? = null,
    @get: PropertyName("photoUrl") @set: PropertyName("photoUrl") var photoUrl: String? = null,
    @get: PropertyName("phoneNumber") @set: PropertyName("phoneNumber") var phoneNumber: String? = null,
    @get: PropertyName("email") @set: PropertyName("email") var email: String? = null,
    @get: PropertyName("verified") @set: PropertyName("verified") var verified: Boolean? = null,
    @get: PropertyName("followers") @set: PropertyName("followers") var followers: Int? = null,
    @get: PropertyName("following") @set: PropertyName("following") var following: Int? = null,
    @get: PropertyName("followed") @set: PropertyName("followed") var followed: Boolean? = null,
    @get: PropertyName("loginType") @set: PropertyName("loginType") var loginType: String? = null,
    @get: PropertyName("bio") @set: PropertyName("bio") var bio: String? = null,
    @get: PropertyName("isNotificationOn") @set: PropertyName("isNotificationOn") var isNotificationOn: Boolean? = null,
    @get: PropertyName("language") @set: PropertyName("language") var language: String? = null,
    @get: PropertyName("highlights") @set: PropertyName("highlights") var highlights: Array<String>? = null,
    @get: PropertyName("postCount") @set: PropertyName("postCount") var postCount: Int? = null

) : Parcelable {
    companion object {
        var currentUserProile: UserProfile? = null
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass)
            return false

        other as UserProfile
        if (!id.equals(other.id))
            return false

        if (!firstName.equals(other.firstName))
            return false

        if (!lastName.equals(other.lastName))
            return false

        if (!displayName.equals(other.displayName))
            return false

        if (!photoUrl.equals(other.photoUrl))
            return false

        if (!phoneNumber.equals(other.phoneNumber))
            return false

        if (!email.equals(other.email))
            return false

        if (verified!! != other.verified!!)
            return false

        if (followers != other.followers)
            return false

        if (followers != other.followers)
            return false

        if (following != other.following)
            return false

        if (followed != other.followed)
            return false

        if (!loginType?.equals(other.loginType)!!)
            return false

        if (!bio?.equals(other.bio)!!)
            return false

        if (isNotificationOn != other.isNotificationOn)
            return false

        if (!language?.equals(other.language)!!)
            return false

        if (postCount != other.postCount)
            return false

        return true


    }
}



