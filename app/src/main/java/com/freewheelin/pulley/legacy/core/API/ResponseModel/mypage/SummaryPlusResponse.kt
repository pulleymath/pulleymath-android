package com.freewheelin.pulley.legacy.core.API.ResponseModel.mypage

data class SummaryPlusResponse (
    var data : SummaryPlusItem?,
    var error: String?,
    var message: String?
)

data class SummaryPlusItem (
    var isActive: Boolean,
    var detail: SummaryPlusItemDetail
)

data class SummaryPlusItemDetail(
    var title: String,
    var startedAt: String?,
    var endAt: String?,
    var nextPaymentAt: String?
)