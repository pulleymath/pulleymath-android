package com.freewheelin.pulley.legacy.core.API.ResponseModel

import com.freewheelin.pulley.legacy.model.contents.Book

class CommercialBook {
    var bookName: String? = null
    var bookTag: String? = null
    var publisher: String? = null
    var subject: String? = null
    var pieceID: Int
    var tag: Tag = Tag.None


    enum class Tag {
        None, New, Best
    }


    constructor(book: Book) {
        this.pieceID = book.pieceID
        this.bookName = book.bookName
    }
}