package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("_id")
    var id: String,
    var fullName: String,
    var dob: String,
    var avatar: String,
)