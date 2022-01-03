package com.freewheelin.pulley.revision2021.model.response

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.response.base.BaseResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseResponseNode
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import java.io.Serializable

/** Pdf */
class Pdf : BaseDiffItem, Serializable {
    var id: Int = 0
    lateinit var cover_url: String
    var cm_book_id: Int = 0
    lateinit var title: String
    lateinit var subject: String
    lateinit var subject_code: String
    lateinit var type: String
    lateinit var category: String
    var publisher_id: Int = 0
    lateinit var publisher: Publisher
    lateinit var publish_date: String
    var is_contain_answer: Boolean = false
    var is_new_mark: Boolean = false
    val edition: Int = 0
    var answer: Pdf? = null

    var landscape_cover_url: String = ""
    var is_purchased: Boolean = true
    var shop_id: Long = 0

    lateinit var created_at: String
    lateinit var updated_at: String
    // use in local
    var filepath: String = ""

    val opening: ObservableBoolean = ObservableBoolean(false)
    val downloading: ObservableBoolean = ObservableBoolean(false)
    val downloadProgress: ObservableInt = ObservableInt(0)
    val downloaded: ObservableBoolean = ObservableBoolean(false)

    override fun equals(other: Any?): Boolean {
        return id == (other as Pdf).id
    }
    override fun getId() = "$id"
}

class PdfListResponse : BaseResponseNode<Pdf>()

/** Pdf 정답 */
data class PdfLinkAnswerItem(val id: Int, val cm_book_id: Int, val pdf_page_no: Int, val answer_page_no: Int) : Serializable
class PdfAnswerResponse : BaseSingleResponseNode<List<PdfLinkAnswerItem>>()

/** Pdf read Log */
data class PdfReadLogInsertResult(val id:Int, val pdf_id:Int, val cm_book_id: Int, val student_id: Int)
class PdfReadLogInsertResponse : BaseSingleResponseNode<PdfReadLogInsertResult>()
class PdfReadLogUpdateResponse : BaseSingleResponseNode<Boolean>()