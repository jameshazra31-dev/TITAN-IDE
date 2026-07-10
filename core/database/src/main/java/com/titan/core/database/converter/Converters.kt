package com.titan.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> = try {
        json.decodeFromString(value)
    } catch (e: Exception) {
        emptyMap()
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = try {
        json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }
}