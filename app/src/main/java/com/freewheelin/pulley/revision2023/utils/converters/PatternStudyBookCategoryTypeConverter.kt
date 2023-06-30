package com.freewheelin.pulley.revision2023.utils.converters

import androidx.room.TypeConverter
import com.freewheelin.pulley.legacy.model.contents.BookCategoryList
import com.freewheelin.pulley.legacy.model.contents.BookPage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken;

class PatternStudyBookCategoryTypeConverter {
    @TypeConverter
    fun fromList(value: BookCategoryList): String? = Gson().toJson(value)

    @TypeConverter
    fun toList(value: String): BookCategoryList {
        val listType = object: TypeToken<BookCategoryList>(){}.type
        return Gson().fromJson(value, listType)
    }
}