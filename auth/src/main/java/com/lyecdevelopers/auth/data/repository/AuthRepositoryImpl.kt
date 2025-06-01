package com.lyecdevelopers.auth.data.repository

import com.lyecdevelopers.auth.domain.model.Result
import com.lyecdevelopers.auth.domain.repository.AuthRepository
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.data.remote.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Credentials
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val schedulerProvider: SchedulerProvider
) : AuthRepository {

    override fun login(username: String, password: String): Flow<Result<Boolean>> = flow {
        emit(Result.Loading) // if Loading is object

        val credentials = Credentials.basic(username, password)
        val response = authApi.loginWithAuthHeader(credentials)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.authenticated) {
                emit(Result.Success(true))
            } else {
                emit(Result.Error("Invalid credentials"))
            }
        } else {
            emit(Result.Error("Login failed with code: ${response.code()}"))
        }
    }.catch { e ->
        emit(Result.Error("Login failed: ${e.localizedMessage ?: "Unknown error"}"))
    }.flowOn(schedulerProvider.io)
}


