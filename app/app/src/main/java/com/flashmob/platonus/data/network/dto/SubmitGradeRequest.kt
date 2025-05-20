package com.flashmob.platonus.data.network.dto

data class SubmitGradeRequest (
    val teacherId: String,
    val studentId: String,
    val subjectId: String,
    val score: Int,
    val date: Long
)