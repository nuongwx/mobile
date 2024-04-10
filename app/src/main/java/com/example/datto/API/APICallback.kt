package com.example.datto.API

interface APICallback<T> {
    fun onSuccess(data: T)
    fun onError(error: Throwable)
}