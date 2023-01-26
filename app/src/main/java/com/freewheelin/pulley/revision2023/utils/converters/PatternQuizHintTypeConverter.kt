package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.LCPatternQuizHint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PatternQuizHintTypeConverter {

    @TypeConverter
    fun fromList(value: List<LCPatternQuizHint>): String = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<LCPatternQuizHint> {
        val listType = object: TypeToken<List<LCPatternQuizHint>>(){}.type
        return Gson().fromJson(value, listType)
    }
}