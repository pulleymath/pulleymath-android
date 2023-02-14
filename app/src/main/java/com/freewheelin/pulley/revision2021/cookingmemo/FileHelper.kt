package com.freewheelin.pulley.revision2021.cookingmemo

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.freewheelin.pulley.revision2021.cookingmemo.storage.DatabaseHelper
import com.freewheelin.pulley.revision2021.cookingmemo.storage.PulleyCookingMemo
import com.pulleymath.android.pdf.utils.getImageToByteArray
import com.pulleymath.android.pdf.utils.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import java.io.*

/**
 * Created by Riccardo on 23/05/2017.
 */

public class FileHelper {

    companion object {
        fun saveStateIntoFile(
            context: Context?,
            state: FreeDrawSerializableState?,
            fileName: String,
            listener: StateSaveInterface?) {

            if (context != null && state != null) {
                StateSaveRunnable(context, listener, state, fileName).run()
            } else {
                listener?.onStateSaveError()
            }
        }

        fun getSavedStoreFromFile(
                context: Context?,
                fileName: String,
                listener: StateExtractorInterface?) {

            if (context != null) {
                StateExtractorRunnable(context, listener, fileName).run()
            } else {

                listener?.onStateExtractionError()
            }
        }

        fun deleteSavedStateFile(context: Context?, fileName: String) {

            if (context != null) {

                var fos: FileOutputStream? = null
                try {
                    fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
                    val os = ObjectOutputStream(fos)
                    os.close()
                    fos!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()

                    if (fos != null) {

                        try {
                            fos.close()
                        } catch (e1: Exception) {
                            e1.printStackTrace()
                        }

                    }
                }

            }
        }
        fun eraseMemo(context: Context, fileName: String) {
            Log.d(javaClass.simpleName, "eraseMemo() fileName=$fileName")
            if (fileName.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                val db = DatabaseHelper.get(context)
                val valueArray = fileName.split("&&")

                val studentId = valueArray[1]
                val typeId = valueArray[2].toInt() // patternId or chapterId ...
                val subId = valueArray[3].toInt() // patternQuizId or cookingId
                val updatedAt = System.currentTimeMillis()
                val fileData = ""
                var memo: PulleyCookingMemo? = PulleyCookingMemo(
                    id = fileName,
                    student_id = studentId,
                    type_id = typeId,
                    sub_id = subId,
                    file = fileData,
                    updated_at = updatedAt
                )

//                var fileByteArray: ByteArray? = ByteArray(0)
//                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), fileByteArray)
//                val body = MultipartBody.Part.createFormData("image", "memo", requestFile)
//                val id = RequestBody.create(MediaType.parse("multipart/form-data"), fileName)
//                val student_id = RequestBody.create(MediaType.parse("multipart/form-data"), studentId)
//                val page_no = RequestBody.create(MediaType.parse("multipart/form-data"), pageNo.toString())
//                val pdf_id = RequestBody.create(MediaType.parse("multipart/form-data"), pdfId.toString())
//                val updated_at = RequestBody.create(MediaType.parse("multipart/form-data"), updatedAt.toString())

                db.pulleyCookingWritingDao().delete(memo!!)
//                Network.uploadTestMemo(body, id, student_id, pdf_id, updated_at, page_no) {}
            }
        }

        fun loadMemo(context: Context,
                     fileName: String,
                     completionBitmap:(Bitmap)->Unit,
                     errorCompletion:((String)->Unit)?=null) {

            CoroutineScope(Dispatchers.IO).launch {
                DatabaseHelper.get(context).let { db ->
                    try {
                        val memo = db.pulleyCookingWritingDao().get(fileName)

                        if (memo != null) {
                            Base64.decode(memo.file, Base64.DEFAULT).toBitmap().run(completionBitmap)
                        } else {
                            errorCompletion?.let { it("error") }
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e(javaClass.simpleName, "Exception ${e.localizedMessage}")
                    }
                }
            }
        }
        fun saveImagedMemo(context: Context, fileName: String, view: View) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    var fileByteArray = withContext(Dispatchers.Main) {
                        view.getImageToByteArray()
                    }

                    val db = DatabaseHelper.get(context)
                    val valueArray = fileName.split("&&")
                    val studentId = valueArray[1]
                    val typeId = valueArray[2].toInt()
                    val subId = valueArray[3].toInt()
                    val updatedAt = System.currentTimeMillis()

                    var fileB64: String? = Base64.encodeToString(fileByteArray, Base64.DEFAULT)
                    var memo: PulleyCookingMemo? = PulleyCookingMemo(
                        id = fileName,
                        student_id = studentId,
                        type_id = typeId,
                        sub_id = subId,
                        file = fileB64!!,
                        updated_at = updatedAt
                    )

                    // TODO 추후 서버에 메모저장할때 사용함
//                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), fileByteArray)
//                    val body = MultipartBody.Part.createFormData("image", "memo", requestFile)
//                    val id = RequestBody.create(MediaType.parse("multipart/form-data"), fileName)
//                    val student_id = RequestBody.create(MediaType.parse("multipart/form-data"), studentId)
//                    val page_no = RequestBody.create(MediaType.parse("multipart/form-data"), pageNo.toString())
//                    val pdf_id = RequestBody.create(MediaType.parse("multipart/form-data"), pdfId.toString())
//                    val updated_at = RequestBody.create(MediaType.parse("multipart/form-data"), updatedAt.toString())

                    db.pulleyCookingWritingDao().upsert(listOf(memo!!))

//                Network.uploadTestMemo(body, id, student_id, pdf_id, updated_at, page_no) {
//                    fileByteArray = null
//                    fileB64 = null
//                }

                } catch (e:OutOfMemoryError) {
                    showAlert(context, "메모리가 부족해서 필기한 내용을 저장할 수 없습니다. 메모리를 정리하세요.")
                    e.printStackTrace()
                    println("OutOfMemoryError e:${e.localizedMessage}")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    println("Exception e:${e.localizedMessage}")
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


    // Runnable that extracts the FreeDrawSerializableState from a file
    private class StateExtractorRunnable(
        private val mContext: Context,
        private val mListener: StateExtractorInterface?,
        val fileName: String) : Runnable {

        override fun run() {
            try {
                var fis: FileInputStream? = null

                val file = mContext.getFileStreamPath(fileName)
                if (file.exists() == false) {
                    mListener?.onStateExtractionError()
                    return
                }

                fis = mContext.openFileInput(fileName)
                val `is` = ObjectInputStream(fis)

                val state = `is`.readObject() as FreeDrawSerializableState

                fis!!.close()
                `is`.close()

                mListener?.onStateExtracted(state)
            } catch (e:Exception) {
                Log.e(javaClass.simpleName, "state Extractor error:${e.localizedMessage}")
            }
        }
    }


    // Runnable that save a FreeDrawSerializableState inside a file
    private class StateSaveRunnable(
        private val mContext: Context,
        private val mListener: StateSaveInterface?,
        private val mState: FreeDrawSerializableState,
        private val fileName: String) : Runnable {

        override fun run() {
            var fos: FileOutputStream? = null
            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(mState)
            os.flush()
            fos!!.flush()
            os.close()
            fos.close()

            mListener?.onStateSaved()
        }
    }


    // Listener for file creation
    interface StateSaveInterface {
        fun onStateSaved()

        fun onStateSaveError()
    }

    // Listener for file data extraction
    interface StateExtractorInterface {
        fun onStateExtracted(state: FreeDrawSerializableState)

        fun onStateExtractionError()
    }


    // Shortcut method to run on uiThread a runnable
    private fun runOnUiThread(runnable: Runnable) {

        Handler(Looper.getMainLooper()).post(runnable)
    }



}
