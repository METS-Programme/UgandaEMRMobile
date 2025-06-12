package com.lyecdevelopers.sync.domain.usecase

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.sync.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val repository: SyncRepository,
) {
    fun loadForms(): Flow<Result<List<Form>>> {
        return repository.loadForms()
    }

    fun filterForms(query: String): Flow<Result<List<Form>>> {
        return repository.filterForms(query)
    }

    fun loadFormByUuid(uuid: String): Flow<Result<o3Form>> {
        return repository.loadFormByUuid(uuid)
    }

    fun saveFormsLocally(forms: List<o3Form>): Flow<Result<List<o3Form>>> {
        return repository.saveFormsLocally(forms)
    }

}