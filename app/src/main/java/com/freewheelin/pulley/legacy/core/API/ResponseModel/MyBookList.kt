package com.freewheelin.pulley.legacy.core.API.ResponseModel

import com.freewheelin.pulley.legacy.model.contents.Book

data class MyBookList(
        var pinBookPlanCount: Int,
        var totalPlanList: Int,
        val myPieceStorageList: MutableList<Book>
) {
    private fun sync() {
        pinBookPlanCount = myPieceStorageList.filter { it.isPinned }.size
        totalPlanList = myPieceStorageList.size

    }

    fun publicSync() {
        pinBookPlanCount = myPieceStorageList.filter { it.isPinned }.size
        totalPlanList = myPieceStorageList.size
    }
    fun removeBook(book: Book, cb: ((index: Int) -> Unit)? = null) {
        val index = myPieceStorageList.indexOf(book)
        if(index >= 0) {
            myPieceStorageList.removeAt(index)
            sync()
            if(cb != null)cb(index)
        }
    }
}