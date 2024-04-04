package com.example.datto.API

import com.example.datto.DataClass.BaseResponse
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GetInterface {
    @GET("{endpoint}")
    fun get(@Path("endpoint", encoded = true) endpoint: String): Call<BaseResponse<JsonElement>>
}