package com.lyecdevelopers.form.domain.usecase

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.form.domain.repository.FormRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FormsUseCase @Inject constructor(
    private val repository: FormRepository,
) {
    operator fun invoke(): Flow<Result<List<Form>>> {
        return repository.loadForms()
    }

    fun filterForms(query: String, allForms: List<Form>): Flow<Result<List<Form>>> {
        return repository.filterForms(query, allForms)
    }

    fun getFormByUuid(uuid: String, allForms: List<Form>): Flow<Result<Form>> {
        return repository.getFormByUuid(uuid, allForms)
    }
}

