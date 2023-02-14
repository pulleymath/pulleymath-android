package com.freewheelin.pulley.revision2023.utils.converters.challenge

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PaidServiceListTypeConverter {
    @TypeConverter
    fun fromList(value: List<PaidServiceType>?): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<PaidServiceType>? {
        val type = object : TypeToken<List<PaidServiceType>>() {}.type
        return Gson().fromJson(value, type)
    }
}