package com.lyecdevelopers.form.data.repository


import com.lyecdevelopers.core.data.local.dao.EncounterDao
import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.FormEntity
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.repository.FormRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FormRepositoryImpl @Inject constructor(
    private val formDao: FormDao,
    private val formApi: FormApi,
    private val encounterDao: EncounterDao,
) : FormRepository {

    override fun loadForms(): Flow<Result<List<Form>>> = flow {

    }

    override fun filterForms(query: String, allForms: List<Form>): Flow<Result<List<Form>>> = flow {

    }

    override fun getFormByUuid(uuid: String, allForms: List<Form>): Flow<Result<Form>> = flow {

    }

    override fun getO3FormByUuid(uuid: String): Flow<Result<o3Form>> = flow {
        emit(Result.Loading)

        try {
            val response = formApi.getFormByUuid(uuid)

            if (response.isSuccessful) {
                val form = response.body()
                if (form != null) {
                    emit(Result.Success(form))
                } else {
                    emit(Result.Error("Empty form."))
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Failed to load form: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun saveForms(forms: List<FormEntity>) {
        formDao.insertForms(forms)
    }

    override suspend fun saveForm(form: FormEntity) {
        formDao.insertForm(form)
    }

    override suspend fun getAllForms(): Flow<Result<List<FormEntity>>> = flow {
        emit(Result.Loading)

        try {
            val forms = formDao.getAllForms()
            emit(Result.Success(forms))

        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load forms"))
            AppLogger.e(("" + e.message))
        }

    }.flowOn(Dispatchers.IO)

    override suspend fun getFormById(uuid: String): Flow<Result<FormEntity?>> = flow {
        emit(Result.Loading)

        try {
            val form = formDao.getFormById(uuid)
            emit(Result.Success(form))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load form"))
            AppLogger.e(e.message ?: "Failed to load form")
        }

    }.flowOn(Dispatchers.IO)


    override suspend fun clearAllForms() {
        formDao.deleteAllForms()
    }

    override suspend fun deleteForm(uuid: String) {
        formDao.deleteFormById(uuid)
    }

    override suspend fun saveEncounterLocally(encounter: EncounterEntity) {
        encounterDao.insert(encounter)
    }

}


