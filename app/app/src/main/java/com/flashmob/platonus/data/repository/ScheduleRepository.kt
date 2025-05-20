package com.flashmob.platonus.data.repository

import com.flashmob.platonus.data.model.Subject
import com.flashmob.platonus.data.model.UserRole
import com.flashmob.platonus.data.network.ApiClient
import com.flashmob.platonus.data.network.dto.SubjectDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository {
    private val api = ApiClient.service

    suspend fun getSchedule(
        userId: String,
        role: UserRole,
        year: Int
    ) = withContext(Dispatchers.IO) {
        runCatching {
            api.schedule(userId, role.name, year).days.mapValues {
                it.value.map {
                    dto -> dto.toDomain()
                }
            }
        }
    }

    private fun SubjectDto.toDomain() =
        Subject(id, name, teacherId, teacherName, time, room, lessonType, dayOfWeek, weekNumber)
}