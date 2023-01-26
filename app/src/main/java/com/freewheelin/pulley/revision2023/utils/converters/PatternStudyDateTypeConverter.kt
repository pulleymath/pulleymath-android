package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.BookPage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;
import java.util.*

class PatternStudyDateTypeConverter {
    @TypeConverter
    fun fromDate(value: Date): String? = Gson().toJson(value)

    @TypeConverter
    fun toDate(value: String): Date {
        val listType = object: TypeToken<Date>(){}.type
        return Gson().fromJson(value, listType)
    }
}