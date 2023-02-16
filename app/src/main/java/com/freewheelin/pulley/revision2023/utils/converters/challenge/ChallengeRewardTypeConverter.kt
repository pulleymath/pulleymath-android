package com.freewheelin.pulley.revision2023.utils.converters.challenge

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeReward
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChallengeRewardTypeConverter {
    @TypeConverter
    fun fromValue(value: ChallengeReward?): String? = Gson().toJson(value)

    @TypeConverter
    fun toValue(value: String): ChallengeReward? {
        val listType = object: TypeToken<ChallengeReward>(){}.type
        return Gson().fromJson(value, listType)
    }
}