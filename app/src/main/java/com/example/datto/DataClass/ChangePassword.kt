package com.example.datto.DataClass

import java.lang.reflect.Constructor

data class ChangePasswordRequest (
    var currentPassword: String,
    var newPassword: String,
    var confirmPassword: String
)