package com.example.datto.DataClass

data class ProfileEditRequest (
    var username: String,
    var fullName: String,
    var dob: String,
    var avatar: String,
)