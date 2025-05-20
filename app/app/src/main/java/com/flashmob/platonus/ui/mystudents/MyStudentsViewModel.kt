package com.flashmob.platonus.ui.mystudents

import androidx.lifecycle.ViewModel
import com.flashmob.platonus.data.model.Grade
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.data.repository.GradesRepository
import com.flashmob.platonus.data.repository.ScheduleRepository
import com.flashmob.platonus.data.repository.TeacherRepository
import java.util.Date

data class SubjectOption(val id: String, val name: String)

sealed class StudentsUiState {
    data class Success(val students: List<User>) : StudentsUiState()
    data class Error(val message: String) : StudentsUiState()
}

sealed class SubmitUiState {
    object Success : SubmitUiState()
    data class Error(val message: String) : SubmitUiState()
}

class MyStudentsViewModel(
    private val teacherRepo: TeacherRepository,
    private val gradesRepo: GradesRepository,
    private val scheduleRepo: ScheduleRepository
) : ViewModel() {

    suspend fun fetchGroups() = teacherRepo.getGroups()
        .getOrElse { emptyList() }

    suspend fun fetchStudents(groupId: String): StudentsUiState {
        val r = teacherRepo.getStudents(groupId)
        return if (r.isSuccess) StudentsUiState.Success(r.getOrNull()!!) else
            StudentsUiState.Error(r.exceptionOrNull()?.localizedMessage ?: "Ошибка")
    }

    suspend fun fetchTeacherSubjects(teacherId: String, year: Int): List<SubjectOption> {
        val r = scheduleRepo.getSchedule(teacherId, UserRole.TEACHER, year)
        return if (r.isSuccess)
            r.getOrNull().orEmpty().values.flatten()
                .distinctBy { it.id }
                .map { SubjectOption(it.id, it.name) }
        else emptyList()
    }

    suspend fun submitGrade(
        teacherId: String,
        studentId: String,
        subjectId: String,
        subjectName: String,
        score: Int
    ): SubmitUiState {
        val grade = Grade(
            id = "",
            studentId = studentId,
            subjectId = subjectId,
            subjectName = subjectName,
            teacherName = "",
            score = score,
            date = Date(),
            academicPeriodId = "temp",
            isFinal = false
        )
        val res = gradesRepo.submitOrUpdateGrade(teacherId, grade)
        return if (res.isSuccess) SubmitUiState.Success
        else SubmitUiState.Error(res.exceptionOrNull()?.localizedMessage ?: "Ошибка")
    }
}