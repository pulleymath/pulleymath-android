package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.BookPage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PatternStudyTempSimilarProblemTypeConverter {
    @TypeConverter
    fun fromList(value: ArrayList<Problem>): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): ArrayList<Problem> {
        val listType = object: TypeToken<ArrayList<Problem>>(){}.type
        return Gson().fromJson(value, listType)
    }
}