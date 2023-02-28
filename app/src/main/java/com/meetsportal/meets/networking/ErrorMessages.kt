package com.meetsportal.meets.networking

object ErrorMessages {

    val UNKNOWN_HOST_EXCEPTION = "UNKNOWN_HOST_EXCEPTION"

    var ErrorMessagesMap = HashMap<String,String>().apply {
        this.put(UNKNOWN_HOST_EXCEPTION ,"Please check your Internet")
        this.put("errors.login.invalid" ,"Invalid Username or password")
    }

}