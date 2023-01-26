package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.contents.BookPage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;
import java.util.HashSet

class PatternStudyPieceCategoryTypeConverter {
    @TypeConverter
    fun fromList(value: HashSet<String>): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): HashSet<String> {
        val listType = object: TypeToken<HashSet<String>>(){}.type
        return Gson().fromJson(value, listType)
    }
}