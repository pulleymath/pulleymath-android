package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.CookingQuiz
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CookingInfoItemQuizTypeConverter {
    @TypeConverter
    fun quizToJson(value: CookingQuiz?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToQuiz(value: String): CookingQuiz? {
        val listType = object: TypeToken<CookingQuiz>(){}.type
        return Gson().fromJson(value, listType)
    }
}