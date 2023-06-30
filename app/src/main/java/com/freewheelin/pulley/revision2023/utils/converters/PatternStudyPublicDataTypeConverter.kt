package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.contents.BookPage
import com.freewheelin.pulley.legacy.model.contents.PublicData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PatternStudyPublicDataTypeConverter {
    @TypeConverter
    fun fromList(value: PublicData): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): PublicData {
        val listType = object: TypeToken<PublicData>(){}.type
        return Gson().fromJson(value, listType)
    }
}