package com.freewheelin.pulley.revision2021.utils.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PriorConceptTagTypeConverter {
    @TypeConverter
    fun fromList(value: List<String>): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object: TypeToken<List<String>>(){}.type
        return Gson().fromJson(value, listType)
    }
}