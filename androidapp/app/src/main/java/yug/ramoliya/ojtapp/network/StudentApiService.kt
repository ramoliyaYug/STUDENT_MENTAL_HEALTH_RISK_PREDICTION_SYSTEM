package yug.ramoliya.ojtapp.network

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import yug.ramoliya.ojtapp.data.model.HealthResponse
import yug.ramoliya.ojtapp.data.model.RegisterRequest
import yug.ramoliya.ojtapp.data.model.StudentIndicatorRequest
import yug.ramoliya.ojtapp.data.model.StudentIndicatorResponse
import yug.ramoliya.ojtapp.data.model.TokenResponse
import yug.ramoliya.ojtapp.data.model.UserResponse

/**
 * Student-facing API (see backend [apitesting.md]).
 */
interface StudentApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): TokenResponse

    @GET("auth/me")
    suspend fun getMe(): UserResponse

    @GET("health")
    suspend fun health(): HealthResponse

    /** Submit indicators and persist them (saves to DB). */
    @POST("student/indicators")
    suspend fun submitIndicators(@Body body: StudentIndicatorRequest): StudentIndicatorResponse

    /** Run direct ML prediction (does NOT persist). */
    @POST("ml/predict")
    suspend fun predict(@Body body: StudentIndicatorRequest): StudentIndicatorResponse

    @GET("student/history")
    suspend fun getHistory(@Query("range") range: String = "weekly"): List<StudentIndicatorResponse>
}
