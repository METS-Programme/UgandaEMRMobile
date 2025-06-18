package com.lyecdevelopers.core.data.remote.interceptor

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
) : Interceptor {

    // In-memory cached username & password
    @Volatile
    private var username: String? = null

    @Volatile
    private var password: String? = null

    fun updateCredentials(username: String, password: String) {
        this.username = username
        this.password = password
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val user = username
        val pass = password

        val requestBuilder = original.newBuilder()

        if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
            val basicAuth = Credentials.basic(user, pass)
            requestBuilder.header("Authorization", basicAuth)
        }

        val request = requestBuilder.build()

        return chain.proceed(request)
    }
}

