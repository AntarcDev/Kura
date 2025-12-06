package com.example.kemono.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CreatorDeserializer : JsonDeserializer<Creator> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Creator {
        val jsonObject = json.asJsonObject
        val id = jsonObject.get("id").asString
        val name = jsonObject.get("name").asString
        val service = jsonObject.get("service").asString
        
        val indexed = parseDate(jsonObject.get("indexed"))
        val updated = parseDate(jsonObject.get("updated"))
        
        val favorited = if (jsonObject.has("favorited") && !jsonObject.get("favorited").isJsonNull) {
            jsonObject.get("favorited").asInt
        } else {
            0
        }

        return Creator(id, name, service, indexed, updated, favorited)
    }

    private fun parseDate(element: JsonElement?): Long {
        if (element == null || element.isJsonNull) return 0L
        
        try {
            // Try as long (Unix timestamp)
            return element.asLong
        } catch (e: NumberFormatException) {
            // Try as String (ISO format)
            try {
                val dateStr = element.asString
                // Example: 2025-12-04T05:33:49.678601
                // We need to handle this. SimpleDateFormat might be tricky with microseconds.
                // Let's try standard ISO
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                // Handle optional fractional seconds
                val cleanDateStr = if (dateStr.length > 19) dateStr.substring(0, 19) else dateStr
                val date = format.parse(cleanDateStr)
                return (date?.time ?: 0L) / 1000 // Return seconds to match existing format if it expects seconds? 
                // Wait, existing "indexed": 1764826430 is seconds.
                // date.time is millis. So divide by 1000.
            } catch (ignored: Exception) {
                return 0L
            }
        }
    }
}
