package com.flashmob.platonus.data.network.dto

data class ScheduleResponse (
    val days: Map<Int, List<SubjectDto>>
)