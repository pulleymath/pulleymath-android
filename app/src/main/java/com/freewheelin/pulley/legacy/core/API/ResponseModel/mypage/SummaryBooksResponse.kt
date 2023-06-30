package com.freewheelin.pulley.legacy.core.API.ResponseModel.mypage

data class SummaryBooksResponse (
    var data : List<SummaryBooksItem>,
    var error: String?,
    var message: String?
)

data class SummaryBooksItem (
    var title: String,
    var publisher: String,
    var pdfID: Long,
    var createdAt: String?
)

