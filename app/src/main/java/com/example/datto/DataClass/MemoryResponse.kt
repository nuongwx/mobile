package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class MemoryResponse(
    @SerializedName("_id")
    var id: String,
    var event: EventResponse,
    var thumbnail: String,
    var info: String
)
