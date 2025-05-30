package com.lyecdevelopers.core.data.remote.interceptor

import com.lyecdevelopers.core.model.Config
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
    private val config: Config
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Basic Authentication header
        val basicAuth = Credentials.basic(config.username, config.password)

        val request = original.newBuilder()
            .header("Authorization", basicAuth)
            .build()

        return chain.proceed(request)
    }
}
