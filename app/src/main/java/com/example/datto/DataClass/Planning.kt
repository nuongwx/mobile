package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Planning(
    val name: String,
    val description: String,
    val start: String,
    val end: String,
    @SerializedName("_id")
    var id: String = ""
)