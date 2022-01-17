package com.freewheelin.pulley.core.API.ResponseModel.sign

data class CountryCodeResponse(
    var data: List<CountryCode>,
    var error: String?,
    var message: String?
){
    data class CountryCode(
        var title: String,
        var code: String,
        var type: String
    )
}