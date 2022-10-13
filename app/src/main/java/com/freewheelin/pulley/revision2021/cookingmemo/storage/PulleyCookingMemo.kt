package com.freewheelin.pulley.revision2021.cookingmemo.storage

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pulley_cooking_memo", indices = [Index(value=["updated_at"])])
data class PulleyCookingMemo (
    @PrimaryKey
    var id: String = "", // fileName이랑 같음
    var student_id: String = "",
    var type_id: Int = 0,
    var sub_id: Int = 0,
    var file: String = "",
    var updated_at: Long = 0
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other?.hashCode()
    }
}