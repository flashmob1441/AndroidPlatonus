package com.flashmob.platonus.data.network

import com.flashmob.platonus.data.network.dto.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("schedule")
    suspend fun schedule(
        @Query("userId") userId: String,
        @Query("role") role: String,
        @Query("year") year: Int
    ): ScheduleResponse

    @GET("grades")
    suspend fun grades(
        @Query("studentId") studentId: String,
        @Query("year") year: Int,
        @Query("period") period: Int,
        @Query("course") course: Int
    ): GradesResponse

    @GET("grades/history")
    suspend fun gradeHistory(
        @Query("studentId") studentId: String,
        @Query("subjectId") subjectId: String,
        @Query("year") year: Int,
        @Query("period") period: Int
    ): GradeHistoryResponse

    @GET("teacher/students")
    suspend fun students(
        @Query("groupId") groupId: String
    ): StudentsResponse

    @GET("teacher/groups")
    suspend fun groups(): GroupsResponse

    @POST("grades")
    suspend fun submitGrade(@Body body: SubmitGradeRequest): SubmitGradeResponse
}