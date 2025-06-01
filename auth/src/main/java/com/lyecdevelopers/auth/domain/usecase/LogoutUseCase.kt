package com.lyecdevelopers.auth.domain.usecase

import com.lyecdevelopers.auth.domain.model.Result
import com.lyecdevelopers.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(username: String, password: String): Flow<Result<Boolean>> {
        return repository.logout(username, password)
    }
}