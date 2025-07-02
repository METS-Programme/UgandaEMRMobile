package com.lyecdevelopers.sync.data.repository

import com.lyecdevelopers.core.data.local.dao.EncounterDao
import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.local.dao.PatientDao
import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Identifier
import com.lyecdevelopers.core.model.PersonAttributeType
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.DataDefinition
import com.lyecdevelopers.core.model.encounter.EncounterType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.model.order.OrderType
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.mapper.toEntity
import com.lyecdevelopers.sync.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject


class SyncRepositoryImpl @Inject constructor(
    private val formApi: FormApi,
    private val formDao: FormDao,
    private val encounterDao: EncounterDao,
    private val patientDao: PatientDao,
) : SyncRepository {

    override fun loadForms(): Flow<Result<List<Form>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.getForms()
            if (response.isSuccessful) {
                val forms = response.body()?.results?.filter { it.published } ?: emptyList()
                emit(Result.Success(forms))
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d("Error Code" + response.code(), "Error Message" + response.message())
            }
        } catch (e: Exception) {
            emit(Result.Error("Error ${e.localizedMessage}"))
            AppLogger.e("Error" + e.localizedMessage)
        }
    }.flowOn(Dispatchers.IO)

    override fun loadFormByUuid(uuid: String): Flow<Result<o3Form>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.loadFormByUuid(uuid)
            if (response.isSuccessful) {
                val form = response.body()
                if (form != null) {
                    emit(Result.Success(form))
                } else {
                    emit(Result.Error("Form with uuid $uuid not found"))
                    AppLogger.d("Form with uuid $uuid not found")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d("Error Code" + response.code(), "Error Message" + response.message())
            }
        } catch (e: Exception) {
            emit(Result.Error("Error ${e.localizedMessage}"))
            AppLogger.e("Error" + e.localizedMessage)
        }
    }.flowOn(Dispatchers.IO)

    override fun filterForms(query: String): Flow<Result<List<Form>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.filterForms(query)
            if (response.isSuccessful) {
                val forms = response.body()?.results
                if (forms != null) {
                    emit(Result.Success(forms))
                } else {
                    emit(Result.Error("Empty form list received."))
                    AppLogger.d("Empty form list received.")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d("Error Code" + response.code(), "Error Message" + response.message())
            }
        } catch (e: Exception) {
            emit(Result.Error("Error ${e.localizedMessage}"))
            AppLogger.e("Error" + e.localizedMessage)
        }
    }.flowOn(Dispatchers.IO)


    override fun saveFormsLocally(forms: List<o3Form>): Flow<Result<List<o3Form>>> = flow {
        emit(Result.Loading)
        try {
            val entities = forms.map { it.toEntity() }
            if (entities.isNotEmpty()) {
                formDao.insertForms(entities)
                emit(Result.Success(forms))
            } else {
                emit(Result.Error("Empty form list received."))
                AppLogger.d("An Error Occurred while saving the ")
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to save forms locally"))
            AppLogger.e(e.message ?: "Failed to save forms locally")

        }
    }.flowOn(Dispatchers.IO)


    override fun getFormCount(): Flow<Result<Int>> = flow {
        try {
            val count = formDao.getFormCount()
            emit(Result.Success(count))
        } catch (e: IOException) {
            val errorMsg =
                "Network error while fetching form count: ${e.localizedMessage ?: "Unknown network error"}"
            AppLogger.e("FormCountIOException", errorMsg, e)
            emit(Result.Error(errorMsg))
        } catch (e: SQLException) {
            val errorMsg =
                "Database error while fetching form count: ${e.localizedMessage ?: "Unknown DB error"}"
            AppLogger.e("FormCountSQLException", errorMsg, e)
            emit(Result.Error(errorMsg))
        } catch (e: Exception) {
            val errorMsg =
                "Unexpected error while fetching form count: ${e.localizedMessage ?: "Unknown error"}"
            AppLogger.e("FormCountUnknownException", errorMsg, e)
            emit(Result.Error(errorMsg))
            throw e // rethrow to allow .catch to process if needed
        }
    }.catch { e ->
        val errorMsg =
            "Unhandled exception in getFormCount(): ${e.localizedMessage ?: "Unknown error"}"
        AppLogger.e("FormCountFlowCatch", errorMsg, e)
        emit(Result.Error(errorMsg))
    }.flowOn(Dispatchers.IO)

    override fun loadPatientsByCohort(): Flow<Result<List<Any>>> {
        TODO("Not yet implemented")
    }

    override fun loadCohorts(): Flow<Result<List<Cohort>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.getCohorts()
            if (response.isSuccessful) {
                val cohorts = response.body()?.results

                if (cohorts != null) {
                    emit(Result.Success(cohorts))
                } else {
                    emit(Result.Error(message = "No cohorts available"))
                    AppLogger.d(message = "No cohorts available")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d("Error Code" + response.code(), "Error Message" + response.message())
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load cohorts"))
            AppLogger.e("Error" + e.localizedMessage)

        }
    }.flowOn(Dispatchers.IO)

    override fun loadIndicators(): Flow<Result<List<Any>>> {
        TODO("Not yet implemented")
    }

    override fun loadParameter(): Flow<Result<List<Any>>> {
        TODO("Not yet implemented")
    }

    override fun loadOrderTypes(): Flow<Result<List<OrderType>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.getOrderTypes()

            if (response.isSuccessful) {
                val ordertypes = response.body()?.results
                if (ordertypes != null) {
                    emit(Result.Success(ordertypes))
                } else {
                    emit(Result.Error(message = "No ordertypes available"))
                    AppLogger.d(message = "No ordertypes available")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d("Error Code" + response.code(), "Error Message" + response.message())
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load orderTypes"))
            AppLogger.e(e.message ?: "Failed to load orderTypes")
        }

    }.flowOn(Dispatchers.IO)

    override fun loadEncounterTypes(): Flow<Result<List<EncounterType>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.getEncounterTypes()
            if (response.isSuccessful) {
                val encountertypes = response.body()?.results
                if (encountertypes != null) {
                    emit(Result.Success(encountertypes))
                } else {
                    emit(Result.Error(message = "No encountertypes available"))
                    AppLogger.d(message = "No encountertypes available")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d(
                    "Error Code" + response.code(), "Error Message" + response.message()
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load encounterTypes"))
            AppLogger.e(e.message ?: "Failed to load encounterTypes")
        }
    }.flowOn(Dispatchers.IO)

    override fun loadPatientIndentifiers(): Flow<Result<List<Identifier>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.getPatientIdentifiers()
            if (response.isSuccessful) {
                val identifiers = response.body()?.results
                if (identifiers != null) {
                    emit(Result.Success(identifiers))
                } else {
                    emit(Result.Error(message = "No encountertypes available"))
                    AppLogger.d(message = "No encountertypes available")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d(
                    "Error Code" + response.code(), "Error Message" + response.message()
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load Indentifier types"))
            AppLogger.e(e.message ?: "Failed to load Indentifier types")
        }

    }.flowOn(Dispatchers.IO)

    override fun loadPersonAttributeTypes(): Flow<Result<List<PersonAttributeType>>> = flow {
        emit(Result.Loading)
        try {
            val response = formApi.getPersonAttributeTypes()
            if (response.isSuccessful) {
                val personAttributeTypes = response.body()?.results
                if (personAttributeTypes != null) {
                    emit(Result.Success(personAttributeTypes))
                } else {
                    emit(Result.Error(message = "No person attributes available"))
                    AppLogger.d(message = "No person attributes available")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d(
                    "Error Code" + response.code(), "Error Message" + response.message()
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load person attribute types"))
            AppLogger.e(e.message ?: "Failed to load attribute types")
        }
    }.flowOn(Dispatchers.IO)

    override fun loadConditions(): Flow<Result<List<Any>>> {
        TODO("Not yet implemented")
    }

    override fun createDataDefinition(payload: DataDefinition): Flow<Result<Any>> = flow {
        emit(Result.Loading)
        try {

            AppLogger.d("payload--->" + payload)

            val response = formApi.generateDataDefinition(payload)
            if (response.isSuccessful) {
                val definitions = response.body()
                if (definitions != null) {
                    emit(Result.Success(definitions))
                } else {
                    emit(Result.Error(message = "No data definitions created "))
                    AppLogger.d(message = "No data definitions created")
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
                AppLogger.d(
                    "Error Code" + response.code(), "Error Message" + response.message()
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to create  data definition"))
            AppLogger.e(e.message ?: "Failed to create  data definition")
        }
    }.flowOn(Dispatchers.IO)

    override fun getUnsynced(): Flow<List<EncounterEntity>> = flow {
        try {
            val unsynced = encounterDao.getUnsynced()
            emit(unsynced)
        } catch (e: Exception) {
            AppLogger.e("DB error when fetching unsynced encounters: ${e.message}")
            throw e // ⚡ Let the caller handle it!
        }
    }

    override fun markSynced(encounter: EncounterEntity): Flow<Unit> = flow {
        try {
            val rows = encounterDao.update(encounter.copy(synced = true))
            if (rows > 0) {
                emit(Unit)
            } else {
                throw IllegalStateException("No rows updated — does encounter exist?")
            }
        } catch (e: Exception) {
            AppLogger.e("DB error marking encounter synced: ${e.message}")
            throw e // ⚡ Let the caller handle it!
        }
    }

    override fun getUnsyncedPatients(): Flow<List<PatientEntity>> = flow {
        try {
            val unsynced = patientDao.getUnsyncedPatients()
            emit(unsynced)
        } catch (e: Exception) {
            AppLogger.e("DB error when fetching unsynced patients: ${e.message}")
            throw e // ⚡ Let the caller handle it!
        }
    }


    override fun markSyncedPatient(patient: PatientEntity): Flow<Unit> = flow {
        try {
            val rows = patientDao.updatePatient(patient.copy(synced = true))
            if (rows > 0) {
                emit(Unit)
            } else {
                throw IllegalStateException("No rows updated — does patient exist?")
            }
        } catch (e: Exception) {
            AppLogger.e("DB error marking patient synced: ${e.message}")
            throw e // ⚡ Let the caller handle it!
        }
    }

}






