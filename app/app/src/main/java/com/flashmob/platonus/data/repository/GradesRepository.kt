package com.flashmob.platonus.data.repository

import com.flashmob.platonus.data.model.Grade
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.data.network.ApiClient
import com.flashmob.platonus.data.network.dto.GradeDto
import com.flashmob.platonus.data.network.dto.SubmitGradeRequest
import com.flashmob.platonus.data.network.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GradesRepository {
    private val api = ApiClient.service

    suspend fun getGradesForStudent(
        studentId: String,
        year: Int,
        period: Int,
        course: Int
    ) = withContext(Dispatchers.IO) {
        runCatching {
            api.grades(studentId, year, period, course).grades.map {
                it.toDomain()
            }
        }
    }

    suspend fun getGradeHistoryForSubject(
        studentId: String,
        subjectId: String,
        year: Int,
        period: Int
    ) = withContext(Dispatchers.IO) {
        runCatching {
            api.gradeHistory(studentId, subjectId, year, period).history.map {
                it.toDomain()
            }
        }
    }

    suspend fun submitOrUpdateGrade(
        teacherId: String,
        grade: Grade
    ) = withContext(Dispatchers.IO) {
        runCatching {
            api.submitGrade(
                SubmitGradeRequest(
                    teacherId,
                    grade.studentId,
                    grade.subjectId,
                    grade.score,
                    grade.date.time
                )
            ).success
        }
    }

    private fun GradeDto.toDomain() = Grade(
        id,
        studentId,
        subjectId,
        subjectName,
        teacherName,
        score,
        date,
        academicPeriodId,
        null,
        isFinal
    )

    private fun UserDto.toDomain() =
        User(id, name, email, role, course)
}