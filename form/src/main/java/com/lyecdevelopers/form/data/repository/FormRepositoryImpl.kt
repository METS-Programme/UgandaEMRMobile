package com.lyecdevelopers.form.data.repository


import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.form.domain.repository.FormRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FormRepositoryImpl @Inject constructor(
    private val formApi: FormApi,
) : FormRepository {

    override fun loadForms(): Flow<Result<List<Form>>> = flow {
        emit(Result.Loading)

        try {
            val response = formApi.getForms()

            if (response.isSuccessful) {
                val forms = response.body()
                if (forms != null) {
                    emit(Result.Success(forms))
                } else {
                    emit(Result.Error("Empty form list received."))
                }
            } else {
                emit(Result.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Error ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun filterForms(query: String, allForms: List<Form>): Flow<Result<List<Form>>> = flow {
        try {
            val filtered = allForms.filter { form ->
                form.name?.contains(query, ignoreCase = true) == true || form.description?.contains(
                    query,
                    ignoreCase = true
                ) == true
            }
            emit(Result.Success(filtered))
        } catch (e: Exception) {
            emit(Result.Error("Error ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getFormByUuid(uuid: String, allForms: List<Form>): Flow<Result<Form>> = flow {
        try {
            val form = allForms.find { it.uuid == uuid }
                ?: throw NoSuchElementException("Form with uuid $uuid not found")
            emit(Result.Success(form))
        } catch (e: Exception) {
            emit(Result.Error("Error ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
}


