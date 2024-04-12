package com.example.datto.DataClass

data class ChangePasswordRequest (
    var currentPassword: String,
    var newPassword: String,
    var confirmPassword: String
)