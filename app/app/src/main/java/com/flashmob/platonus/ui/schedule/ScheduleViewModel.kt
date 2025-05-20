package com.flashmob.platonus.ui.schedule

import androidx.lifecycle.ViewModel
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.data.repository.ScheduleRepository
import java.util.Calendar

sealed class ScheduleUiState {
    data class Success(val items: List<ScheduleListItem>) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}

class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {

    private val mondayFirstOrder = listOf(1, 2, 3, 4, 5, 6, 7)

    suspend fun getSchedule(
        userId: String,
        role: UserRole,
        year: Int
    ): ScheduleUiState {
        val result = repository.getSchedule(userId, role, year)
        return if (result.isSuccess) {
            val map = result.getOrNull().orEmpty()
            val items = mutableListOf<ScheduleListItem>()
            mondayFirstOrder.forEach { day ->
                val lessons = map[day].orEmpty()
                if (lessons.isNotEmpty()) {
                    items.add(ScheduleListItem.DayHeader(day))
                    lessons.sortedBy { it.time }
                        .forEach { items.add(ScheduleListItem.Lesson(it)) }
                }
            }
            ScheduleUiState.Success(items)
        } else {
            ScheduleUiState.Error(result.exceptionOrNull()?.localizedMessage ?: "Ошибка загрузки")
        }
    }
}