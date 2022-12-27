package com.freewheelin.pulley.revision2021.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2023.model.LCPatternMapProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PatternMapProgressTypeConverter {
    @TypeConverter
    fun fromList(value: List<LCPatternMapProgress>): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<LCPatternMapProgress> {
        val listType = object: TypeToken<List<LCPatternMapProgress>>(){}.type
        return Gson().fromJson(value, listType)
    }
}