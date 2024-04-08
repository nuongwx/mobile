package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class BucketResponse (
    @SerializedName("_id")
    var id: String,
)