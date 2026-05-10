package com.lyecdevelopers.core.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class RetryInterceptor @Inject constructor(
    private val maxRetry: Int = 2,
    private val retryDelayMs: Long = 1000
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var tryCount = 0

        while (tryCount < maxRetry) {
            try {
                response = chain.proceed(request)
                break
            } catch (e: Exception) {
                tryCount++
                if (tryCount >= maxRetry) throw e
                // Small delay before retry
                try {
                    Thread.sleep(retryDelayMs)
                } catch (interrupted: InterruptedException) {
                    throw IOException("Retry interrupted", interrupted)
                }
            }
        }

        return response!!
    }
}