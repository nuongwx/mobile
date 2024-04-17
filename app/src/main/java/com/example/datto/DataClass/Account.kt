package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("_id")
    var id: String,
    var username: String,
    var email: String,
    var profile: ProfileResponse,
)

data class NewAccountRequest(
    var username: String,
    var email: String,
    var password: String,
)

data class NewAccountResponse(
    @SerializedName("_id")
    var id: String,
)

data class AccountRequest(
    var username: String,
    var password: String,
)

data class NewPasswordRequest(
    var email: String,
    var password: String,
)
