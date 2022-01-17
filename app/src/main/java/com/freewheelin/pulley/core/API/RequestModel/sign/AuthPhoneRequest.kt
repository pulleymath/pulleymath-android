package com.freewheelin.pulley.core.API.RequestModel.sign

data class AuthPhoneRequest(
    var type: String,
    var target: String,
    var countryCode: String,
    var countryType: String,
    var purposeType: String
)