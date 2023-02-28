package com.meetsportal.meets.networking.exception

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.ErrorResponse

class ApiException(
    @SerializedName("errorResponse")
    var errorResponse: ErrorResponse?) : Exception(errorResponse?.message) {


}