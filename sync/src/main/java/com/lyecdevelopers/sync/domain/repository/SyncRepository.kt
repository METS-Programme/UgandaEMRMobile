package com.lyecdevelopers.sync.domain.repository

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.encounter.EncounterType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.model.order.OrderType

import kotlinx.coroutines.flow.Flow

interface SyncRepository {

    // forms
    fun loadForms(): Flow<Result<List<Form>>>
    fun loadFormByUuid(uuid: String): Flow<Result<o3Form>>
    fun filterForms(query: String): Flow<Result<List<Form>>>

    // local db
    fun saveFormsLocally(forms: List<o3Form>): Flow<Result<List<o3Form>>>

    // cohorts
    fun loadPatientsByCohort(): Flow<Result<List<Any>>>
    fun loadCohorts(): Flow<Result<List<Cohort>>>
    fun loadIndicators(): Flow<Result<List<Any>>>
    fun loadParameter(): Flow<Result<List<Any>>>

    // others
    // orders
    fun loadOrderTypes(): Flow<Result<List<OrderType>>>

    // encounters
    fun loadEncounterTypes(): Flow<Result<List<EncounterType>>>
}