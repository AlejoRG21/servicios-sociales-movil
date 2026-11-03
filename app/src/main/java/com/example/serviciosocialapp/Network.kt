package com.example.serviciosocialapp // <- Tu package

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class LoginRequest(val email: String, val password: String)
data class RegisterStudentRequest(val name: String, val email: String, val password: String, val targetHours: Int = 40)
data class LoginResponse(val token: String, val user: UserDto)
data class UserDto(@SerializedName("_id") val id: String, val name: String, val email: String, val targetHours: Int = 40)

data class ServiceRecord(
    @SerializedName("_id") val id: String? = null,
    val studentName: String? = null,
    val organization: String,
    val category: String = "Asistencia comunitaria",
    val hours: Double,
    val date: String,
    val description: String,
    val coordinator: String = "Maya Thompson",
    val status: String = "PENDING",
    val approvedBy: String? = "Prof. Carter",
    val approvedDate: String? = "26 oct 2024, 10:32 a. m."
)

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun registerStudent(@Body req: RegisterStudentRequest): Response<LoginResponse>

    @GET("api/services/pending")
    suspend fun getMyRecords(): Response<List<ServiceRecord>>

    @POST("api/services")
    suspend fun createServiceRecord(@Body record: ServiceRecord): Response<ServiceRecord>
}

object RetrofitClient {
    private const val BASE_URL = "https://servicios-sociales-backend.onrender.com"
    val api: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}