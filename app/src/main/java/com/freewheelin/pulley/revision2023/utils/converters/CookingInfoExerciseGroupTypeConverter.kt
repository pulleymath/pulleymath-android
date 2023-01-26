package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CookingInfoExerciseGroupTypeConverter {
    @TypeConverter
    fun fromList(value: List<CookingExercise>?): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<CookingExercise>? {
        val listType = object: TypeToken<List<CookingExercise>>(){}.type
        return Gson().fromJson(value, listType)
    }
}