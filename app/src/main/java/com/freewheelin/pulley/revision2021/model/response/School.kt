package com.freewheelin.pulley.revision2021.model.response

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.response.base.BaseResponsePageable
import com.freewheelin.pulley.revision2023.SchoolType

class School (
    val address: String,
    val apiSeq: Int,
    val category: String,
    val createDate: String,
    val id: Int,
    val isDeleted: Boolean,
    val name: String,
    val regionID: Int,
    val type: SchoolType,
    val updateDate: String
) : BaseDiffItem {
    override fun equals(other: Any?): Boolean {
        return id == (other as School).id
    }

    override fun getId() = "$id"

//    enum class Type {
//        ELEMENTARY,
//        MIDDLE,
//        HIGH,
//        UNIVERSITY
//    }
    fun isElementary(): Boolean {
        return type == SchoolType.ELEMENTARY
    }
    fun isMiddle(): Boolean {
        return type == SchoolType.MIDDLE
    }
    fun isHigh(): Boolean {
        return type == SchoolType.HIGH
    }
    fun isUniversity(): Boolean {
        return type == SchoolType.UNIVERSITY
    }
}

class SchoolResponse : BaseResponsePageable<School>()