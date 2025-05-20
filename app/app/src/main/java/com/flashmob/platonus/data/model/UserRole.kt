package com.flashmob.platonus.data.model

import com.squareup.moshi.Json

enum class UserRole {
    @Json(name = "student") STUDENT,
    @Json(name = "teacher") TEACHER
}