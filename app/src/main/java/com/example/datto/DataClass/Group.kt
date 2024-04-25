package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class GroupEditRequest(
    var name: String,
    var thumbnail: String
)

data class GroupResponse(
    @SerializedName("_id")
    var id: String,
    var members: List<String>,
    var events: List<String>,
    var name: String,
    var inviteCode: String,
    val thumbnail: String,
)
