package com.pulleymath.android.pdf.memo.storage

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.pulleymath.android.pdf.log.Network

import com.pulleymath.android.pdf.memo.FreeDrawSerializableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import com.pulleymath.android.pdf.utils.getImageToByteArray
import com.pulleymath.android.pdf.utils.toBitmap
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Created by Riccardo on 23/05/2017.
 */

object FileHelper {

    fun eraseMemo(context: Context, fileName: String) {
        Log.d(javaClass.simpleName, "eraseMemo() fileName=$fileName")

        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseHelper.get(context)
            val valueArray = fileName.split("_")
            val studentId = valueArray[1]
            val pdfId = valueArray[2].toInt()
            val pageNo = valueArray[3].toInt()
            val updatedAt = System.currentTimeMillis()
            val fileData = ""
            var memo:PdfMemo? = PdfMemo(
                id = fileName,
                student_id = studentId,
                pdf_id = pdfId,
                page_no = pageNo,
                file = fileData,
                updated_at = updatedAt
            )

            var fileByteArray: ByteArray? = ByteArray(0)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), fileByteArray)
            val body = MultipartBody.Part.createFormData("image", "memo", requestFile)
            val id = RequestBody.create(MediaType.parse("multipart/form-data"), fileName)
            val student_id = RequestBody.create(MediaType.parse("multipart/form-data"), studentId)
            val page_no = RequestBody.create(MediaType.parse("multipart/form-data"), pageNo.toString())
            val pdf_id = RequestBody.create(MediaType.parse("multipart/form-data"), pdfId.toString())
            val updated_at = RequestBody.create(MediaType.parse("multipart/form-data"), updatedAt.toString())

            db.pdfWritingDao().delete(memo!!)
            Network.uploadTestMemo(body, id, student_id, pdf_id, updated_at, page_no) {}
        }
    }

    // deprecated
    fun saveMemo(context: Context,state: FreeDrawSerializableState?, fileName: String) {
        Log.d(javaClass.simpleName, "saveMemo() fileName=$fileName")
        state?.let { memoObject ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = DatabaseHelper.get(context)
                    val valueArray = fileName.split("_")
                    val studentId = valueArray[1]
                    val pdfId = valueArray[2].toInt()
                    val pageNo = valueArray[3].toInt()
                    val updatedAt = System.currentTimeMillis()
                    var fileData:String? = Gson().toJson(memoObject) // 숫자 값에 NaN 넘어오는 경우 있음
                    var memo:PdfMemo? = PdfMemo(
                        id = fileName,
                        student_id = studentId,
                        pdf_id = pdfId,
                        page_no = pageNo,
                        file = fileData!!,
                        updated_at = updatedAt
                    )
                    db.pdfWritingDao().upsert(listOf(memo!!))

                    Network.uploadMemo(listOf(memo)) {
                        fileData = null
                        memo = null
                    }

//                    PdfViewerActivity.memos.set(memo.id, memo)
                } catch (e:OutOfMemoryError) {
                    showAlert(context, "메모리가 부족해서 필기한 내용을 저장할 수 없습니다. 메모리를 정리하세요.")
                } catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveImagedMemo(context: Context, fileName: String, view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var fileByteArray: ByteArray? = view.getImageToByteArray()

                val db = DatabaseHelper.get(context)
                val valueArray = fileName.split("_")
                val studentId = valueArray[1]
                val pdfId = valueArray[2].toInt()
                val pageNo = valueArray[3].toInt()
                val updatedAt = System.currentTimeMillis()

                var fileB64: String? = Base64.encodeToString(fileByteArray, Base64.DEFAULT)
                var memo:PdfMemo? = PdfMemo(
                    id = fileName,
                    student_id = studentId,
                    pdf_id = pdfId,
                    page_no = pageNo,
                    file = fileB64!!,
                    updated_at = updatedAt
                )

                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), fileByteArray)
                val body = MultipartBody.Part.createFormData("image", "memo", requestFile)
                val id = RequestBody.create(MediaType.parse("multipart/form-data"), fileName)
                val student_id = RequestBody.create(MediaType.parse("multipart/form-data"), studentId)
                val page_no = RequestBody.create(MediaType.parse("multipart/form-data"), pageNo.toString())
                val pdf_id = RequestBody.create(MediaType.parse("multipart/form-data"), pdfId.toString())
                val updated_at = RequestBody.create(MediaType.parse("multipart/form-data"), updatedAt.toString())

                db.pdfWritingDao().upsert(listOf(memo!!))
                Network.uploadTestMemo(body, id, student_id, pdf_id, updated_at, page_no) {
                    fileByteArray = null
                    fileB64 = null
                }

            } catch (e:OutOfMemoryError) {
                showAlert(context, "메모리가 부족해서 필기한 내용을 저장할 수 없습니다. 메모리를 정리하세요.")
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMemo(context: Context,
            fileName: String,
            completion:(FreeDrawSerializableState)->Unit,
            completionBitmap:(Bitmap)->Unit,
            errorCompletion:((String)->Unit)?=null) {

        CoroutineScope(Dispatchers.IO).launch {
            DatabaseHelper.get(context).let { db ->
                try {
                    val pdfMemo = db.pdfWritingDao().get(fileName)

                    if (pdfMemo != null) {
                        if (pdfMemo.file.startsWith("{\"mCanceledPaths")) {
                            val state = Gson().fromJson(pdfMemo.file, FreeDrawSerializableState::class.java)
                            state?.let(completion)
                        } else {
                            Base64.decode(pdfMemo.file, Base64.DEFAULT).toBitmap().run(completionBitmap)
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
