package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.model.request.PdfReadPost
import com.freewheelin.pulley.revision2021.repository.local.PdfDao
import com.freewheelin.pulley.revision2021.repository.remote.PdfApi
import com.freewheelin.pulley.revision2021.repository.remote.PdfService

class PdfRepository {
    private val pdfDao: PdfDao? = null
    private val pdfService : PdfService by lazy { PdfApi.pdfService() }

    fun pdfList(title:String="", page:Int=0, size:Int=20, subjectCode:String="", category:String="") = pdfService.list(title, page, size, subjectCode, category)
    fun answer(cmBookId:Int) = pdfService.answer(cmBookId)

//    fun postReadLog(request:PdfReadPost) = pdfService.postReadLog()
//    fun patchReadLog(logId:Int) = pdfService.patchReadLog(logId)
}