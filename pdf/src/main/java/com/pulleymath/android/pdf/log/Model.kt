package com.pulleymath.android.pdf.log

import com.pulleymath.android.pdf.memo.storage.PdfMemo
import java.io.Serializable

/** Pdf Log */
data class PdfReadLog (val pdf_id: Int, val cm_book_id: Int, val student_id: String)
data class PdfPageLog (val pdf_id: Int, val cm_book_id: Int, val student_id: String, val page_no: Int, val read_log_id: Int, val read_timestamp: String = "")

abstract class BaseResponse<T> : Serializable {
    var data: T? = null
    var error: Any? = null
    var message: Any? = null
}

data class PdfReadLogInsertResult(val id:Int, val pdf_id:Int, val cm_book_id: Int, val student_id: String)
class PdfReadLogInsertResponse : BaseResponse<PdfReadLogInsertResult>()
class PdfReadLogUpdateResponse : BaseResponse<Boolean>()
class PdfPageLogResponse : BaseResponse<PdfPageLog>()

/** Pdf Memo */

class PdfMemoResponse : BaseResponse<List<PdfMemo>>()

class PdfMemoPostResponse : BaseResponse<Any>()