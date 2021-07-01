package com.freewheelin.pulley.revision2021.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import java.io.File

class PdfDownloader {

    private var mDownloadManager: DownloadManager? = null
    private var mDownloadQueueId: Long = 0

    private fun downloadAndSave(url: Uri, savePath:String, context: Context) {

        if (mDownloadManager == null) {
            mDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }

        File(savePath)?.let { outputFile ->
            if (!outputFile.parentFile.exists()) {
                outputFile.parentFile.mkdirs()
            }

            val downloadUri = url
            val request = DownloadManager.Request(downloadUri)
            val pathSegmentList = downloadUri.pathSegments
            request.setTitle("다운로드 항목")
            request.setDestinationUri(Uri.fromFile(outputFile))
            request.setAllowedOverMetered(true)

            mDownloadQueueId = mDownloadManager?.enqueue(request) ?: 0
        }
    }
}