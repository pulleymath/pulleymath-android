package com.freewheelin.pulley.legacy.core.API.RequestModel.sign

data class AuthPhoneRequest(
    var type: String,
    var target: String,
    var countryCode: String?,
    var countryType: String?,
    var purposeType: String
)