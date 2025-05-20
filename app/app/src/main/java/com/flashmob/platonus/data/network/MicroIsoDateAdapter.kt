package com.flashmob.platonus.data.network

import com.squareup.moshi.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

class MicroIsoDateAdapter : JsonAdapter<Date>() {

    private val fmt: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .optionalStart()
        .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
        .optionalEnd()
        .optionalStart()
        .appendOffset("+HH:MM", "Z")
        .optionalEnd()
        .toFormatter()
        .withZone(ZoneOffset.UTC)

    @FromJson
    override fun fromJson(reader: JsonReader): Date? =
        reader.nextString()?.let {
            val instant = Instant.from(fmt.parse(it))
            Date.from(instant)
        }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) writer.nullValue()
        else writer.value(fmt.format(value.toInstant()))
    }
}