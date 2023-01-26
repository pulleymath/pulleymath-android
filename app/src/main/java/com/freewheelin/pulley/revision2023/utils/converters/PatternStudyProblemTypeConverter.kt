package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.BookPage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PatternStudyProblemTypeConverter {
    @TypeConverter
    fun fromList(value: List<Problem>): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<Problem> {
        val listType = object: TypeToken<List<Problem>>(){}.type
        return Gson().fromJson(value, listType)
    }
}