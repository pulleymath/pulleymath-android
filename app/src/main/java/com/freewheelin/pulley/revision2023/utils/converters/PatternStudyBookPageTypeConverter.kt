package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.model.contents.BookPage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PatternStudyBookPageTypeConverter {
    @TypeConverter
    fun fromList(value: List<BookPage>): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<BookPage> {
        val listType = object: TypeToken<List<BookPage>>(){}.type
        return Gson().fromJson(value, listType)
    }
}