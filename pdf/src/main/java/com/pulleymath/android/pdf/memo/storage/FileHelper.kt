package com.pulleymath.android.pdf.memo.storage

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.pulleymath.android.pdf.PdfViewerActivity
import com.pulleymath.android.pdf.log.Network

import com.pulleymath.android.pdf.memo.FreeDrawSerializableState
import java.io.*
import java.lang.Exception
import kotlin.concurrent.thread

/**
 * Created by Riccardo on 23/05/2017.
 */

object FileHelper {

    fun eraseMemo(context: Context, fileName: String) {
        Log.d(javaClass.simpleName, "eraseMemo() fileName=$fileName")

        thread(start = true) {
            val db = DatabaseHelper.get(context)
            val valueArray = fileName.split("_")
            val studentId = valueArray[1]
            val pdfId = valueArray[2].toInt()
            val pageNo = valueArray[3].toInt()
            val updatedAt = System.currentTimeMillis()
            val fileData = ""
            val memo = PdfMemo(
                id = fileName,
                student_id = studentId,
                pdf_id = pdfId,
                page_no = pageNo,
                file = fileData,
                updated_at = updatedAt
            )

            db.pdfWritingDao().delete(memo)
            Network.uploadMemo(listOf(memo))
//            PdfViewerActivity.memos.set(memo.id, memo)
        }
    }

    fun saveMemo(context: Context,state: FreeDrawSerializableState?, fileName: String) {
        Log.d(javaClass.simpleName, "saveMemo() fileName=$fileName")
        state?.let { memoObject ->
            thread(start = true) {
                try {
                    val db = DatabaseHelper.get(context)
                    val valueArray = fileName.split("_")
                    val studentId = valueArray[1]
                    val pdfId = valueArray[2].toInt()
                    val pageNo = valueArray[3].toInt()
                    val updatedAt = System.currentTimeMillis()
                    val fileData = Gson().toJson(memoObject) // 숫자 값에 NaN 넘어오는 경우 있음
                    val memo = PdfMemo(
                        id = fileName,
                        student_id = studentId,
                        pdf_id = pdfId,
                        page_no = pageNo,
                        file = fileData,
                        updated_at = updatedAt
                    )
                    db.pdfWritingDao().upsert(listOf(memo))
                    Network.uploadMemo(listOf(memo))
//                    PdfViewerActivity.memos.set(memo.id, memo)
                } catch (e:OutOfMemoryError) {
                    showAlert(context, "메모리가 부족해서 필기한 내용을 저장할 수 없습니다. 메모리를 정리하세요.")
                } catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadMemo(context: Context,
            fileName: String,
            completion:(FreeDrawSerializableState)->Unit,
            errorCompletion:((String)->Unit)?=null) {

        thread(start = true) {
            DatabaseHelper.get(context)?.let { db ->
                try {
                    val pdfMemo = db.pdfWritingDao().get(fileName)
                    if (pdfMemo != null) {
                        val state = Gson().fromJson(pdfMemo.file, FreeDrawSerializableState::class.java)
                        if (state != null) {
                            completion(state)
                        }
                    } else {
                        errorCompletion?.let { it("error") }
                    }
                }catch (e:Exception) {
                    Log.e(javaClass.simpleName, "${e.localizedMessage}")
                }
            }
        }
    }

    fun showAlert(context: Context, msg: String, title: String="알림") {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(msg)
            setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
//            setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
//                /* 취소 후 처리 */
//            })
            show()
        }
    }
}
