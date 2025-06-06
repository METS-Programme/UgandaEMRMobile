package com.lyecdevelopers.core.data.remote

import com.lyecdevelopers.core.model.Form
import retrofit2.Response
import retrofit2.http.GET


interface FormApi {
    @GET("form?v=full")
    suspend fun getForms(): Response<List<Form>>
}