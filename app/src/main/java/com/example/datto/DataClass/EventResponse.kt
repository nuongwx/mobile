package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("_id")
    var id: String,
    var name: String,
    var description: String,
)
