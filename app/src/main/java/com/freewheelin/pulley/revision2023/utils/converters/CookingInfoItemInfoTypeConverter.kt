package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CookingInfoItemInfoTypeConverter {
    @TypeConverter
    fun infoToJson(value: CookingInfo?): String? = Gson().toJson(value)

    @TypeConverter
    fun toInfo(value: String): CookingInfo? {
        val listType = object: TypeToken<CookingInfo>(){}.type
        return Gson().fromJson(value, listType)
    }
}