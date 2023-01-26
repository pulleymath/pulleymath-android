package com.freewheelin.pulley.revision2021.utils.listener

import com.freewheelin.pulley.revision2021.model.response.School

fun interface UpdateGradeSchoolSelectListener {
    fun onClick(item: School)
}