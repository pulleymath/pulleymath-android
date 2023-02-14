package com.freewheelin.pulley.revision2023.utils.converters.challenge

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeFormat
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChallengeFormatTypeConverter {
    @TypeConverter
    fun fromValue(value: ChallengeFormat?): String? = Gson().toJson(value)

    @TypeConverter
    fun toValue(value: String): ChallengeFormat? {
        val listType = object: TypeToken<ChallengeFormat>(){}.type
        return Gson().fromJson(value, listType)
    }
}
