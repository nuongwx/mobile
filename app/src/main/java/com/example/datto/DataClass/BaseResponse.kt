package com.example.datto.DataClass

data class BaseResponse<T>  (
    val success: Boolean,
    val message: String,
    val data: T
)