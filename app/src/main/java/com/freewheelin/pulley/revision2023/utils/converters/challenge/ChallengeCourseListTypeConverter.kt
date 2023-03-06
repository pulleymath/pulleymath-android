package com.freewheelin.pulley.revision2023.utils.converters.challenge

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeFormat
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChallengeCourseListTypeConverter {
    @TypeConverter
    fun fromList(value: List<ChallengeCourse>?): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): List<ChallengeCourse>? {
        val listType = object: TypeToken<List<ChallengeCourse>>(){}.type
        return Gson().fromJson(value, listType)
    }
}
