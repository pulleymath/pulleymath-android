package com.freewheelin.pulley.legacy.core.API.ResponseModel

class CommercialBookPageResponse(
        var bookName: String,
        var pieceID: Int,
        var commercialPageList: List<CommercialBookPage>


)



class CommercialBookPage(
        var id: Int,
        var page: Int,
        var title: String,
        var problemNumber: String,
        var unitCode: String,
        var problemLevel: Int
)