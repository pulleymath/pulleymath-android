package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.LCPatternConcept
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PatternQuizConceptTypeConverter {

    @TypeConverter
    fun fromList(value: List<LCPatternConcept>): String = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<LCPatternConcept> {
        val listType = object: TypeToken<List<LCPatternConcept>>(){}.type
        return Gson().fromJson(value, listType)
    }
}