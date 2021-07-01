package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.CityApi

class FindCityRepository  {
//    private val schoolDao: SchoolDao? = null
    private val cityService by lazy { CityApi.cityService() }

    fun getCities() = cityService.getCities()
}