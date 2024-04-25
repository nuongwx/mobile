package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class Time(
    var start: String,
    var end: String,
)

data class EventResponse(
    @SerializedName("_id")
    var id: String,
    var name: String,
    var description: String,
    var time: Time,
    var memory: String,
)
