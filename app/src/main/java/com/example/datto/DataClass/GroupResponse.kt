package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class GroupResponse(
    @SerializedName("_id")
    var id: String,
    var members: List<String>,
    var events: List<String>,
    var memories: List<MemoryResponse>,
    var name: String,
    var inviteCode: String,
    val thumbnail: String,
)
