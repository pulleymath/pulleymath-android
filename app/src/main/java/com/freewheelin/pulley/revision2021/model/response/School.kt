package com.freewheelin.pulley.revision2021.model.response

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.response.base.BaseResponsePageable

class School (
    val address: String,
    val apiSeq: Int,
    val category: String,
    val createDate: String,
    val id: Int,
    val isDeleted: Boolean,
    val name: String,
    val regionID: Int,
    val type: Type,
    val updateDate: String
) : BaseDiffItem {
    override fun equals(other: Any?): Boolean {
        return id == (other as School).id
    }

    override fun getId() = "$id"

    enum class Type {
        ELEMENTARY,
        MIDDLE,
        HIGH,
        UNIVERSITY
    }
    fun isElementary(): Boolean {
        return type == Type.ELEMENTARY
    }
    fun isMiddle(): Boolean {
        return type == Type.MIDDLE
    }
    fun isHigh(): Boolean {
        return type == Type.HIGH
    }
    fun isUniversity(): Boolean {
        return type == Type.UNIVERSITY
    }
}

class SchoolResponse : BaseResponsePageable<School>()