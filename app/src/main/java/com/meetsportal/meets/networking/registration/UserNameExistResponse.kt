package com.meetsportal.meets.networking.registration

import com.google.gson.annotations.SerializedName

data class UserNameExistResponse(
    @SerializedName("exists")
    val exists : Boolean?
)