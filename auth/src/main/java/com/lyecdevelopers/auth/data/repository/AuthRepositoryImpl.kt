package com.lyecdevelopers.auth.data.repository

import com.lyecdevelopers.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

 class AuthRepositoryImpl @Inject constructor() : AuthRepository {
     override fun login(username: String, password: String): Flow<Result<Boolean>> {
         return TODO("Provide the return value")
     }
 }
