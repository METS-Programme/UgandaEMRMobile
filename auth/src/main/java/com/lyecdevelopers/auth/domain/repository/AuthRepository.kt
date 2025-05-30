package com.lyecdevelopers.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(username: String, password: String): Flow<Result<Boolean>>
}
