package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class OtpResponse(
    @SerializedName("_id")
    var id: String,
)

data class NewOtpRequest(
    var key: String,
    var email: String,
)

data class VerifyOtpRequest(
    var otpId: String,
    var code: Number,
)