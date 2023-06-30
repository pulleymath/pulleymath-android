package com.freewheelin.pulley.legacy.core.API.RequestModel.sign

data class ConfirmCodeRequest (
    var code: String,
    var channel: String
)
