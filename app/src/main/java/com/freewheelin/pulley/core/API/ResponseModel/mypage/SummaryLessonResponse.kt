package com.freewheelin.pulley.core.API.ResponseModel.mypage

data class SummaryLessonResponse (
    var data : List<SummaryLessonItem>,
    var error: String?,
    var message: String?
)

data class SummaryLessonItem (
    var userLessonID: Long,
    var title: String,
    var isWait: Boolean,
    var detail: SummaryLessonItemDetail
)

data class SummaryLessonItemDetail (
    var startedAt: String?,
    var endAt: String?,
    var nextPaymentAt: String?
)