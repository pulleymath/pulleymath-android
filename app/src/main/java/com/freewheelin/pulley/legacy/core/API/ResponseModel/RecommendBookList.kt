package com.freewheelin.pulley.legacy.core.API.ResponseModel

import com.freewheelin.pulley.legacy.model.contents.Book

data class RecommendBookList (
        var title: String,
        var targetBookPlanList: List<Book>
)