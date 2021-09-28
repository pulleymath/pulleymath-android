package com.pulleymath.android.pdf.memo.storage

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_memo", indices = [Index(value=["updated_at"])])
data class PdfMemo (
    @PrimaryKey
    var id: String = "", // fileName이랑 같음
    var student_id: String = "",
    var pdf_id: Int = 0,
    var page_no: Int = 0,
    var file: String = "",
    var updated_at: Long = 0
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other?.hashCode()
    }
}