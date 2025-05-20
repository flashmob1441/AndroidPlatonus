package com.flashmob.platonus.ui.grades

import androidx.lifecycle.ViewModel
import com.flashmob.platonus.data.model.Grade
import com.flashmob.platonus.data.repository.GradesRepository

sealed class GradesUiState {
    data class Success(val grades: List<Grade>) : GradesUiState()
    data class Error(val message: String) : GradesUiState()
}

class GradesViewModel(private val repository: GradesRepository) : ViewModel() {

    suspend fun fetchGrades(
        studentId: String,
        year: Int,
        period: Int,
        course: Int
    ): GradesUiState {
        val r = repository.getGradesForStudent(studentId, year, period, course)
        return if (r.isSuccess) GradesUiState.Success(r.getOrNull()!!)
        else GradesUiState.Error(r.exceptionOrNull()?.localizedMessage ?: "Ошибка")
    }

    suspend fun fetchHistory(
        studentId: String,
        subjectId: String,
        year: Int,
        period: Int
    ): GradesUiState {
        val r = repository.getGradeHistoryForSubject(studentId, subjectId, year, period)
        return if (r.isSuccess) GradesUiState.Success(r.getOrNull()!!)
        else GradesUiState.Error(r.exceptionOrNull()?.localizedMessage ?: "Ошибка")
    }
}