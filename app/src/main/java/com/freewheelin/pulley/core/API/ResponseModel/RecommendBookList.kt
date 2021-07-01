package com.freewheelin.pulley.core.API.ResponseModel

import com.freewheelin.pulley.model.contents.Book

data class RecommendBookList (
        var title: String,
        var targetBookPlanList: List<Book>
)