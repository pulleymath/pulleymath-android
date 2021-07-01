package com.freewheelin.pulley.revision2021.model.response

import com.freewheelin.pulley.revision2021.model.response.base.BaseResponse

class City (
    val id: Int,
    val name: String
)

class CityResponse : BaseResponse<City>()