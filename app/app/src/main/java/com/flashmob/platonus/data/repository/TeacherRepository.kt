package com.flashmob.platonus.data.repository

import com.flashmob.platonus.data.model.Group
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.data.network.ApiClient
import com.flashmob.platonus.data.network.dto.GroupDto
import com.flashmob.platonus.data.network.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherRepository {
    private val api = ApiClient.service

    suspend fun getGroups() = withContext(Dispatchers.IO) {
        runCatching {
            api.groups().groups.map {
                it.toDomain()
            }
        }
    }

    suspend fun getStudents(groupId: String) = withContext(Dispatchers.IO) {
        runCatching {
            api.students(groupId).students.map {
                it.toDomain()
            }
        }
    }

    private fun GroupDto.toDomain() = Group(id, name)

    private fun UserDto.toDomain() = User(id, name, email, role, course)
}