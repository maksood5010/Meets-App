package com.meetsportal.meets.database.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.*
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.SelectedContactPeople
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "contact")
@Parcelize
data class Contact(

    @SerializedName("id")
    @PrimaryKey val id:String,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("number")
    var number: String? = null,

    @SerializedName("image")
    @Ignore var image: Bitmap? = null,

    @SerializedName("selected")
    @Ignore var selected : Boolean = false,

    @SerializedName("isItFirst")
    @Ignore var isItFirst : Boolean? = false,

    @SerializedName("selectTimestamp")
    @Ignore var selectTimestamp : Long? = null
): Parcelable {
    constructor( id:String, name: String? = null, number: String? = null ): this(
        id = id,
        name = name,
        number = number,
        null,
        false,
        false
    )

    override fun equals(other: Any?): Boolean {
        if(other is Contact){
            return id.equals(other.id)
        }
        return false
    }

    fun toSelectedPerson() : SelectedContactPeople.SelectedContact{
        return SelectedContactPeople.SelectedContact(id,name,number,image,selected,selectTimestamp)
    }
}


@Entity(tableName = "contact_fts") 
@Fts4(contentEntity = Contact::class)
data class ContactFTS(
    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String? = null,

    @SerializedName("number")
    @ColumnInfo(name = "number")
    var number: String? = null,
)