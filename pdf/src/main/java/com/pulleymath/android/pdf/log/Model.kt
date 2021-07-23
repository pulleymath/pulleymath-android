package com.pulleymath.android.pdf.log

import java.io.Serializable

data class PdfReadLog (val pdf_id: Int, val cm_book_id: Int, val student_id: String)
data class PdfPageLog (val pdf_id: Int, val cm_book_id: Int, val student_id: String, val page_no: Int, val read_log_id: Int, val read_timestamp: String = "")

abstract class BaseSingleResponseNode<T> : Serializable {
    var data: T? = null
    var error: Any? = null
    var message: Any? = null
}

data class PdfReadLogInsertResult(val id:Int, val pdf_id:Int, val cm_book_id: Int, val student_id: String)
class PdfReadLogInsertResponse : BaseSingleResponseNode<PdfReadLogInsertResult>()
class PdfReadLogUpdateResponse : BaseSingleResponseNode<Boolean>()
class PdfPageLogResponse : BaseSingleResponseNode<PdfPageLog>()