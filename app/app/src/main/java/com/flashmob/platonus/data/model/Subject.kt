package com.flashmob.platonus.data.model

data class Subject (
    val id: String,
    val name: String,
    val teacherId: String,
    val teacherName: String,
    val time: String,
    val room: String,
    val lessonType: String,
    val dayOfWeek: Int,
    val weekNumber: Int? = null
)