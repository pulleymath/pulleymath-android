package com.freewheelin.pulley.legacy.core.API.ResponseModel.mypage

data class NotificationResponse (
    var data: Data,
    var error: String?,
    var message: String?
) {
    data class Data (
        var isAgreeAlimtalk: Boolean,
        var isAgreePush: Boolean,
        var isAgreeEmail: Boolean,
        var isAgreeMarketing: Boolean
    )
}



