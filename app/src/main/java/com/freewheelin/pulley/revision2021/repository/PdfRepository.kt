package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.revision2021.model.request.PdfReadPost
import com.freewheelin.pulley.revision2021.model.response.EventBook
import com.freewheelin.pulley.revision2021.repository.local.PdfDao
import com.freewheelin.pulley.revision2021.repository.remote.PdfApi
import com.freewheelin.pulley.revision2021.repository.remote.PdfService
import com.freewheelin.pulley.revision2021.repository.remote.SpringApi
import com.freewheelin.pulley.revision2021.repository.remote.SpringService
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PdfRepository {
    private val pdfDao: PdfDao? = null
    private val pdfService : PdfService by lazy { PdfApi.pdfService() }
    private val springService : SpringService by lazy { SpringApi.springService() }

    suspend fun fetchPdf(pdfId: Int) = pdfService.fetchPdf(pdfId).data
    fun pdfList(title:String="", page:Int=0, size:Int=20, subjectCode: String="", category:String="") = pdfService.listV2(title, page, size, subjectCode, category)
    fun answer(cmBookId:Int) = pdfService.answer(cmBookId)
    fun eventBookCheck(param: EventBook) = springService.eventBookCheck(param)

    suspend fun uploadMemoImage(file: MultipartBody.Part,
                        id: RequestBody, student_id: RequestBody,
                        pdf_id: RequestBody, updated_at: RequestBody, page_no: RequestBody) = pdfService.uploadMemoByteArray2(file, id, student_id, pdf_id, updated_at, page_no)

    suspend fun downloadMemoImages(
        studentId: String, assignId: Int, latest: Long?
    ) = pdfService.downloadMemo(studentId, assignId, null, latest).data
    suspend fun asd() = pdfService.fetchPdf(81)
//    println("aspasp uploadMemoImage")
//        pdfService.uploadMemoByteArray(file, id, student_id, pdf_id, updated_at, page_no)
//        println("aspasp uploadMemoImage")
//
//    }

//    fun postReadLog(request:PdfReadPost) = pdfService.postReadLog()
//    fun patchReadLog(logId:Int) = pdfService.patchReadLog(logId)
}