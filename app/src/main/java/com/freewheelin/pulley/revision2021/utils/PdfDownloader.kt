package com.freewheelin.pulley.revision2021.utils

import android.net.Uri
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.repository.remote.Network
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class PdfDownloader {

    companion object {
        private val PDF_URL_PREFIX = "${Network.baseNodeUrl}/v1/pdf"
        private val downloadThreads = arrayListOf<Thread>()
        private val downloadConnections = arrayListOf<HttpURLConnection>()

        fun download(pdf: Pdf, filesDir: File, cb: () -> Unit) {
            pdf.downloaded.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if ((sender as ObservableBoolean).get()) {
                        pdf.downloaded.removeOnPropertyChangedCallback(this)
                        cb()
                    }
                }
            })
            pdf.downloading.set(true)
            val th = thread(start=true) {
                try {
                    downloadAndSave(pdf, filesDir) // 본문
                    pdf.answer?.let { downloadAndSave(it, filesDir) } // 정답
                    pdf.downloaded.set(true)
                } catch (e: Exception) {
//                    DialogUtils.showServerErr(baseContext)
                    pdf.downloadProgress.set(0)
                    pdf.downloaded.set(false)
                    e.printStackTrace()
                }
                pdf.downloading.set(false)

            }
            downloadThreads.add(th)
            pdf.downloading.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (!(sender as ObservableBoolean).get()) {
                        pdf.downloading.removeOnPropertyChangedCallback(this)
                        if (downloadThreads.contains(th)) downloadThreads.remove(th)
                    }
                }
            })
        }

        private fun downloadAndSave(pdf: Pdf, filesDir: File) {
            val uri = Uri.parse("$PDF_URL_PREFIX/${pdf.id}")
            val PDF_DIR = "$filesDir/pdfs"
            val url = URL(uri.toString())
            (url.openConnection() as HttpURLConnection).run {
                downloadConnections.add(this)
                println("pdf token=${user?.token}")
                setRequestProperty ("Authorization", "Bearer ${user?.token}")
                requestMethod = "GET"
                println("response=$responseCode, $responseMessage")
                val buffer = receiveFileByteArray(this, pdf)
                val filepath = "${PDF_DIR}/${pdf.cm_book_id}/${pdf.id}.pdf"

                saveFile(filepath, buffer).let {
                    if (downloadConnections.contains(this)) {
                        downloadConnections.remove(this)
                    }
                }
            }
        }

        private fun receiveFileByteArray(urlConnection: HttpURLConnection, pdf: Pdf) : ByteArray {
            val isr = urlConnection.inputStream
            var len: Int
            val totalSize = urlConnection.contentLength

            val buffer = ByteArrayOutputStream().use { stream ->
                val data = ByteArray(16384)
                var currSize = 0
                while (isr!!.read(data, 0, data.size).also { len = it } != -1) {
                    stream.write(data, 0, len)
                    currSize += len

                    val percentage = currSize.toFloat() / totalSize.toFloat() * 100
                    pdf.downloadProgress.set(percentage.toInt())
                }
                stream.flush()
                stream.toByteArray()
            }
            isr.close()
            return buffer
        }
        private fun saveFile(fullPath: String, buffer:ByteArray) {
            val file = File(fullPath)

            // pdf root directory 없으면 생성
            if(file.parentFile?.exists() == false) {
                file.parentFile?.mkdirs()
            }

            FileOutputStream(file, true).use { stream ->
                stream.write(buffer)
                stream.close()
            }
        }
        fun pdfDelete(pdf: Pdf, filesDir: File) {
            val PDF_DIR = "$filesDir/pdfs"
            val filepath = "${PDF_DIR}/${pdf.cm_book_id}/${pdf.id}.pdf"
            val file = File(filepath)
            if (file.exists()) { file.delete() }

        }
        fun cancelDownload(pdf: Pdf, filesDir: File) {
            downloadThreads.forEach {
                it.interrupt()
                cancelDownloadingFile(pdf, filesDir)
                pdf.downloadProgress.set(0)
            }
            downloadThreads.clear()
            downloadConnections.forEach {
                thread(start=true) {
                    it.disconnect()
                }
            }
            downloadConnections.clear()
        }
        private fun cancelDownloadingFile(pdf: Pdf, filesDir: File) {
            pdfDelete(pdf, filesDir)
            pdf.answer?.let { pdfDelete(it, filesDir) }
        }
    }

}