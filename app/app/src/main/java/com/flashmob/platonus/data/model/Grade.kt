package com.flashmob.platonus.data.model

import java.util.Date

data class Grade (
    val id: String,
    val studentId: String,
    val subjectId: String,
    val subjectName: String,
    val teacherName: String,
    val score: Int,
    val date: Date,
    val academicPeriodId: String,
    val comment: String? = null,
    val isFinal: Boolean = false
)