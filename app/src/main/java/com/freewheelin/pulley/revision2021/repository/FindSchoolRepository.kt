package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.local.SchoolDao
import com.freewheelin.pulley.revision2021.repository.remote.SchoolApi

class FindSchoolRepository  {
    private val schoolDao: SchoolDao? = null
    private val schoolService by lazy { SchoolApi.schoolService() }

    fun searchSchool(name:String, page:Int, size:Int=10) = schoolService.searchSchool(name, page, size)
}