package com.lyecdevelopers.sync.domain.repository

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.o3.o3Form

import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun loadForms(): Flow<Result<List<Form>>>
    fun loadFormByUuid(uuid: String): Flow<Result<o3Form>>
    fun filterForms(query: String): Flow<Result<List<Form>>>
    fun loadPatientsByCohort(): Flow<Result<List<Any>>>
    fun saveFormsLocally(forms: List<o3Form>): Flow<Result<List<o3Form>>>
}