package com.lyecdevelopers.form.domain.repository

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import kotlinx.coroutines.flow.Flow


interface FormRepository {
    fun loadForms(): Flow<Result<List<Form>>>
    fun filterForms(query: String, allForms: List<Form>): Flow<Result<List<Form>>>
    fun getFormByUuid(uuid: String, allForms: List<Form>): Flow<Result<Form>>
}
