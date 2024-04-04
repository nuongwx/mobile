package com.example.datto.API

import com.example.datto.GlobalVariable.GlobalVariable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    private val BASE_URL: String = GlobalVariable.BASE_URL
    fun get(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}