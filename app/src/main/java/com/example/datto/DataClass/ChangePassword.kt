package com.example.datto.DataClass

import java.lang.reflect.Constructor

data class ChangePasswordRequest (
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)