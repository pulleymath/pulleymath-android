package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.CookingInfoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CookingInfoItemTypeConverter {
    @TypeConverter
    fun typeToJson(value: CookingInfoItem.ItemType): String? = Gson().toJson(value)

    @TypeConverter
    fun toItemType(value: String): CookingInfoItem.ItemType {
        val listType = object: TypeToken<CookingInfoItem.ItemType>(){}.type
        return Gson().fromJson(value, listType)
    }
}