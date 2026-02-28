package com.CuriosityLabs.digibalance.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class Converters {
    
    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String? {
        return value?.let { Json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        return value?.let { Json.decodeFromString(it) }
    }
}
