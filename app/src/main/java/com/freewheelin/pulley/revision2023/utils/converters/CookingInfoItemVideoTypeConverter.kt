package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CookingInfoItemVideoTypeConverter {
    @TypeConverter
    fun videoToJson(value: CookingInfo.Video?): String? = Gson().toJson(value)

    @TypeConverter
    fun toVideo(value: String): CookingInfo.Video? {
        val listType = object: TypeToken<CookingInfo.Video>(){}.type
        return Gson().fromJson(value, listType)
    }
}