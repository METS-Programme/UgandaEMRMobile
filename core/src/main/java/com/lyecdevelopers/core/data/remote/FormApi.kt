package com.lyecdevelopers.core.data.remote

import com.lyecdevelopers.core.model.FormsListResponse
import com.lyecdevelopers.core.model.IdentifierListResponse
import com.lyecdevelopers.core.model.PersonAttributeTypeListResponse
import com.lyecdevelopers.core.model.cohort.CohortListResponse
import com.lyecdevelopers.core.model.cohort.DataDefinition
import com.lyecdevelopers.core.model.encounter.EncounterTypeListResponse
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.model.order.OrderTypeListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface FormApi {
    // form
    @GET("form")
    suspend fun getForms(
        @Query("v") view: String = "full",
    ): Response<FormsListResponse>

    @GET("o3/forms/{formId}")
    suspend fun loadFormByUuid(
        @Path("formId") formId: String,
        @Query("v") view: String = "full",
    ): Response<o3Form>

    @GET("form")
    suspend fun filterForms(
        @Query("q") query: String,
        @Query("v") view: String = "full",
    ): Response<FormsListResponse>

    @GET("o3/forms/{formId}")
    suspend fun getFormByUuid(@Path("formId") formId: String): Response<o3Form>

    // cohort
    @GET("cohort")
    suspend fun getCohorts(@Query("v") view: String = "full"): Response<CohortListResponse>

    // orders
    @GET("ordertype")
    suspend fun getOrderTypes(): Response<OrderTypeListResponse>

    // encounters
    @GET("encountertype")
    suspend fun getEncounterTypes(): Response<EncounterTypeListResponse>

    // patientIdentifiers
    @GET("patientidentifiertype")
    suspend fun getPatientIdentifiers(): Response<IdentifierListResponse>

    // personattributetype
    @GET("personattributetype")
    suspend fun getPersonAttributeTypes(): Response<PersonAttributeTypeListResponse>

    // conditions
    @GET("ugandaemrreports/concepts/conditions")
    suspend fun getConditions(): Response<Any>

    // data definition
    @POST("ugandaemrreports/dataDefinition")
    suspend fun generateDataDefinition(payload: DataDefinition): Response<Any>

}
