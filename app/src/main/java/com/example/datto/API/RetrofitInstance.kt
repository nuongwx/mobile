package com.example.datto.API

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.datto.GlobalVariable.GlobalVariable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    private val BASE_URL: String = GlobalVariable.BASE_URL
    private val cacheSize = (50 * 1024 * 1024).toLong() // 50 MB

    private fun hasNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun getCacheInstance(context: Context): OkHttpClient {
        val myCache = okhttp3.Cache(context.cacheDir, cacheSize)
        val maxAge = 60 * 60 * 24 * 7 // 1 week

        val okHttpClient = OkHttpClient.Builder()
            // Specify the cache we created earlier.
            .cache(myCache)
            // Add an Interceptor to the OkHttpClient.
            .addInterceptor { chain ->

                // Get the request from the chain.
                var request = chain.request()

                /*
                *  Leveraging the advantage of using Kotlin,
                *  we initialize the request and change its header depending on whether
                *  the device is connected to Internet or not.
                */
                request = if (hasNetwork(context))
                /*
                *  If there is Internet, get the cache that was stored 5 seconds ago.
                *  If the cache is older than 5 seconds, then discard it,
                *  and indicate an error in fetching the response.
                *  The 'max-age' attribute is responsible for this behavior.
                */
                    request.newBuilder().header("Cache-Control", "public, max-age=5").build()
                else
                /*
                *  If there is no Internet, get the cache that was stored 7 days ago.
                *  If the cache is older than 7 days, then discard it,
                *  and indicate an error in fetching the response.
                *  The 'max-stale' attribute is responsible for this behavior.
                *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
                */
                    request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=$maxAge").build()
                // End of if-else statement

                // Add the modified request to the chain.
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                // Add an Interceptor to the OkHttpClient.
                // Always serve the response from the cache first
                // Update the cache with the response from the server later
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