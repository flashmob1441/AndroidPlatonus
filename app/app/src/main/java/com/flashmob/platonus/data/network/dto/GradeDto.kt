package com.flashmob.platonus.data.network.dto

import java.util.Date

data class GradeDto (
    val id: String,
    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val teacherName: String,
    val score: Int,
    val date: Date,
    val academicPeriodId: String,
    val isFinal: Boolean
)
