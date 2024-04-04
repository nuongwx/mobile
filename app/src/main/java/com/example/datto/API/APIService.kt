package com.example.datto.API

import android.util.Log
import com.example.datto.DataClass.Account
import com.example.datto.DataClass.BaseResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Response

class APIService {
    val retrofitClient = RetrofitInstance().get()

    inline fun <reified T> doGet(endpoint: String, callback: APICallback<Any>) {
        val service = retrofitClient.create(GetInterface::class.java).get(endpoint)

        // Enqueue the request
        service.enqueue(object : retrofit2.Callback<BaseResponse<JsonElement>> {
            override fun onResponse(
                call: Call<BaseResponse<JsonElement>>,
                response: Response<BaseResponse<JsonElement>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    val gson = Gson()
                    val type = object : TypeToken<T>() {}.type
                    val result = gson.fromJson(data, type) as Any

                    // Custom callback to process data
                    callback.onSuccess(result)
                } else {
                    callback.onError(Throwable(response.message()))
                }
            }

            override fun onFailure(call: Call<BaseResponse<JsonElement>>, t: Throwable) {
                // Custom callback to process data
                callback.onError(t)
            }
        })
    }
}