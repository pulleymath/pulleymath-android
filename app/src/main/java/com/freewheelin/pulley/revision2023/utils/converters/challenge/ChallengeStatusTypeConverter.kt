package com.freewheelin.pulley.revision2023.utils.converters.challenge

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChallengeStatusTypeConverter {
    @TypeConverter
    fun fromValue(value: ChallengeStatus?): String? = Gson().toJson(value)

    @TypeConverter
    fun toValue(value: String): ChallengeStatus? {
        val listType = object: TypeToken<ChallengeStatus>(){}.type
        return Gson().fromJson(value, listType)
    }
}

class TypeConverterTest<T: Any> {
    @TypeConverter
    fun fromValue(value: T?): String? = Gson().toJson(value)

    @TypeConverter
    fun toValue(value: String): T? {
        val listType = object: TypeToken<T>(){}.type
        return Gson().fromJson(value, listType)
    }
}
class ListTypeConverterTest<T: Any> {
    @TypeConverter
    fun fromList(value: List<T>?): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<T>? {
        val listType = object: TypeToken<List<T>>(){}.type
        return Gson().fromJson(value, listType)
    }
}