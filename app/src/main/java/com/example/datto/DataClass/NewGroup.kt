package com.example.datto.DataClass

data class NewGroupRequest (
    var accountId: String,
    var name: String,
    var thumbnail: String
)

data class NewGroupResponse (
    var inviteCode: String
)