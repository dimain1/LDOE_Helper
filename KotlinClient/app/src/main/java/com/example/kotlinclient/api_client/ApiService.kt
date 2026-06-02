package com.example.kotlinclient.api_client

import com.example.kotlinclient.api_client.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {


    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Long

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest)

    /** Обновление токенов по refresh token. */
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse


    @GET("users/me")
    suspend fun getMe(): UserDto

    @PUT("users/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): UserDto

    @PUT("users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    @GET("events")
    suspend fun getEvents(): List<EventDto>

    @POST("events")
    suspend fun createEvent(@Body request: EventCreateRequest): EventDto

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") id: Long, @Body request: EventUpdateRequest): EventDto

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Long)


    @GET("templates")
    suspend fun getTemplates(): List<EventTemplateDto>

    /**
     * Создать шаблон с опциональным изображением.
     * "request" — JSON-часть (application/json), "image" — файл.
     */
    @Multipart
    @POST("templates")
    suspend fun createTemplate(
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): EventTemplateDto

    @Multipart
    @PUT("templates/{id}")
    suspend fun updateTemplate(
        @Path("id") id: Long,
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): EventTemplateDto

    @DELETE("templates/{id}")
    suspend fun deleteTemplate(@Path("id") id: Long)


    @GET("content")
    suspend fun getContent(): List<GameContentDto>

    @POST("content/{id}/pin")
    suspend fun pinContent(@Path("id") id: Long)

    @DELETE("content/{id}/pin")
    suspend fun unpinContent(@Path("id") id: Long)
}
