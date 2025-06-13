package com.lyecdevelopers.sync.data.repository

import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.encounter.EncounterType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.model.order.OrderType
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.mapper.toEntity
import com.lyecdevelopers.sync.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class SyncRepositoryImpl @Inject constructor(
    private val formApi: FormApi,
    private val formDao: FormDao,
) : SyncRepository {

    override fun loadForms(): Flow<Result<List<Form>>> = flow {
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

}
