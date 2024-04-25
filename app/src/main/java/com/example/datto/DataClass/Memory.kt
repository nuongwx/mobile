package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class MemoryResponse(
    var thumbnail: String,
    var info: String,
    var date: String,
    @SerializedName("_id")
    var id: String = "",
)

data class MemoryRequest(
    var thumbnail: String,
    var info: String,
    var date: String,
)
