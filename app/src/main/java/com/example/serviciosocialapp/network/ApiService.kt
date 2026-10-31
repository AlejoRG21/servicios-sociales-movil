package com.example.serviciosocialapp.network

import com.example.serviciosocialapp.model.Institucion
import com.example.serviciosocialapp.model.RegistroHora
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/instituciones")
    suspend fun obtenerInstituciones(): Response<List<Institucion>>

    @POST("api/instituciones")
    suspend fun crearInstitucion(@Body institucion: Institucion): Response<Institucion>

    @POST("api/registros-horas")
    suspend fun crearRegistroHora(@Body registro: RegistroHora): Response<RegistroHora>

    companion object {
        const val BASE_URL = "http://10.0.2.2:3000/"
    }
}