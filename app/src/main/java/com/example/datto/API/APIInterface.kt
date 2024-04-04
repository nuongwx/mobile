package com.example.datto.API

import android.view.PixelCopy.Request
import com.example.datto.DataClass.BaseResponse
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface APIInterface {
    @GET("{endpoint}")
    fun get(@Path("endpoint", encoded = true) endpoint: String): Call<BaseResponse<JsonElement>>

    @PATCH("{endpoint}")
    fun patch(@Path("endpoint", encoded = true) endpoint: String, @Body body: Any): Call<BaseResponse<JsonElement>>

    @Multipart
    @PUT("{endpoint}")
    fun putMultipart(@Path("endpoint", encoded = true) endpoint: String, @Body body: MultipartBody.Part): Call<BaseResponse<JsonElement>>
}
