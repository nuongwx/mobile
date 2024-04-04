package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("_id")
    var id: String,
    var username: String,
    var email: String,
    var profile: ProfileResponse,
)