package com.example.datto.API

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.datto.Credential.CredentialService
import com.example.datto.GlobalVariable.GlobalVariable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {
    private val BASE_URL: String = GlobalVariable.BASE_URL
    private val cacheSize = (50 * 1024 * 1024).toLong() // 50 MB

    private fun hasNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun getCacheInstance(context: Context): OkHttpClient {
        val myCache = okhttp3.Cache(context.cacheDir, cacheSize)
        val maxAge = 60 * 60 * 24 * 7 // 1 week
        val timeout = 10L // 10 seconds

        val token = CredentialService().getJWTToken()

        val okHttpClient = OkHttpClient.Builder()
            // Specify the cache we created earlier.
            .cache(myCache)
            // Set the timeout for the OkHttpClient.
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            // Add an Interceptor to the OkHttpClient.
            .addInterceptor { chain ->

                // Get the request from the chain.
                var request = chain.request()

                // Add the JWT token to the "x-access-token" header.
                request = request.newBuilder()
                    .addHeader("x-access-token", token)
                    .build()

                request = if (hasNetwork(context))
                    request.newBuilder().header("Cache-Control", "public, max-age=5").build()
                else
                    request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=$maxAge").build()

                request = if (token != "" && CredentialService().isExpired())
                    request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=$maxAge").build()
                else
                    request

                try {
                    // Try to proceed with the request.
                    chain.proceed(request)
                } catch (e: Exception) {
                    // If the request times out, proceed with the cached response.
                    request = request.newBuilder().header("Cache-Control", "public, only-if-cached").build()
                    chain.proceed(request)
                }
            }
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .removeHeader("Pragma")
                    .build()
            }
            .build()

        return okHttpClient
    }

    fun get(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getCacheInstance(context))
            .build()
    }
}