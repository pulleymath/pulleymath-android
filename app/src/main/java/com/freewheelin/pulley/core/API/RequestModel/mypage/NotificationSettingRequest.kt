package com.freewheelin.pulley.core.API.RequestModel.mypage

data class NotificationSettingRequest (
    var isAgreeAlimtalk: Boolean,
    var isAgreePush: Boolean,
    var isAgreeEmail: Boolean,
    var isAgreeMarketing: Boolean
)