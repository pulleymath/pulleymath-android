package com.freewheelin.pulley.core.API.RequestModel.sign

data class ConfirmCodeRequest (
    var code: String,
    var channel: String
)
