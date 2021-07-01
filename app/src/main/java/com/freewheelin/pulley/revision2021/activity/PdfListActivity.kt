package com.freewheelin.pulley.revision2021.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityPdfListBinding
import com.freewheelin.pulley.databinding.ItemPdfBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.model.response.PdfLinkAnswerItem
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2021.viewmodel.PdfListFilter
import com.freewheelin.pulley.revision2021.viewmodel.PdfViewModel
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.views.DaebakToast
import com.pulleymath.android.pdf.PdfViewerActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class PdfListActivity : AppCompatActivity() {

    private val PDF_DIR by lazy {"$filesDir/pdfs"}
    private val PDF_URL_PREFIX = "${Network.baseNodeUrl}/v1/pdf"

    private val binding: ActivityPdfListBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_pdf_list, null, false)
    }

    private val viewModel:PdfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        createCacheDir()

        binding.apply {
            lifecycleOwner = this@PdfListActivity
            vm = viewModel
            val adapter = PdfAdapter(viewModel)
            recyclerPdf.adapter = adapter
            recyclerPdf.layoutManager = GridLayoutManager(baseContext, 5)
            btnBack.setOnClickListener { finish() }

            /** 아이템 변경시 스크롤이 top으로 안가는 문제 해결용 */
            adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    recyclerPdf.smoothScrollToPosition(0)
                }
            })
        }

        initUI()
    }

    private fun createCacheDir() {
        File(PDF_DIR)?.let { outputDir ->
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
        }
    }

    private fun initUI() {
        val subjectAdapter = ArrayAdapter<String>(this, R.layout.item_spinner_textview, PdfListFilter.subjectList)
        val categoryAdapter = ArrayAdapter<String>(this, R.layout.item_spinner_textview, PdfListFilter.categoryList)
        with(binding) {
            spinnerSubject.adapter = subjectAdapter
            spinnerCategory.adapter = categoryAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.run {
            clearCompositeDisposable()
        }
    }

    inner class PdfAdapter(private val viewModel: PdfViewModel): ListAdapter<Pdf, PdfHolder>(DiffCallback<Pdf>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfHolder {
            return PdfHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_pdf, parent, false))
        }

        override fun onBindViewHolder(holder: PdfHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    inner class PdfHolder(private val binding: ItemPdfBinding): RecyclerView.ViewHolder(binding.root), PdfItemClickListener {
        fun bind(item: Pdf) {
            /** 다운로드 체크 */
            item.downloaded.set(File(makeLocalPdfName(item)).exists())
            item.subject = PdfListFilter.subject.get(item.subject_code)?:""

            binding.listener = this
            binding.item = item
            binding.vm = viewModel
        }

        override fun onItemClick(pdf: Pdf) {
            if(!pdf.downloading.get()) { // 다운로드 중이면 disabled
                if (pdf.downloaded.get()) { open(pdf) } else { download(pdf) }
            }
        }

        private fun open(pdf: Pdf) {
            pdf.downloading.set(true)
            binding.root.context.let { context ->
                viewModel.answer(pdf.cm_book_id) { answerLinks ->
                    thread(start=true) {
                        val answerPath = if(pdf.answer != null) checkDownloaded(pdf.answer!!) else ""
                        openPdf(context, pdf, answerPath, answerLinks)
                        pdf.downloading.set(false)
                    }
                }
            }
        }

        private fun checkDownloaded(pdf: Pdf) : String{
            /** 해설파일 체크하고 없으면 다운로드 */
            val answerPath = makeLocalPdfName(pdf)
            val file = File(answerPath)
            if(!file.exists()) {
                download(pdf)
            }
            return answerPath
        }

        /** open viewer */
        private fun openPdf(context: Context, pdf:Pdf, answerPath:String, answerLinks:List<PdfLinkAnswerItem>) {
            Intent(context, PdfViewerActivity::class.java).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) addFlags(
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                ) else addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)

                action = Intent.ACTION_VIEW
                data = Uri.parse(makeLocalPdfName(pdf))

                putExtra(PdfViewerActivity.KEY_BOOK_ID, pdf.cm_book_id)
                putExtra(PdfViewerActivity.KEY_BOOK_TITLE, "${pdf.title} ${pdf.subject}")
                putExtra(PdfViewerActivity.KEY_PDF_ID, pdf.id)

                if(pdf.answer != null) putExtra(PdfViewerActivity.KEY_ANSWER_PDF_ID, pdf.answer!!.id)

                putExtra(PdfViewerActivity.KEY_INCLUDE_ANSWER, false)
                putExtra(PdfViewerActivity.KEY_ANSWER_PDF_PATH, answerPath)

                putExtra(PdfViewerActivity.KEY_STUDENT_ID, user!!.studentID)
                putExtra(PdfViewerActivity.KEY_TOKEN, user!!.token)

                var linkString = ""
                for(link in answerLinks) {
                    val item = "${link.pdf_page_no}:${link.answer_page_no}"
                    linkString += "/$item"
                }
                putExtra(PdfViewerActivity.KEY_ANSWER_PAGE_LINK, linkString.substring(1)) // exclude first char "/"

                runOnUiThread {
                    context.startActivity(this)
                }
            }
        }

        private fun download(pdf: Pdf) {
            pdf.downloading.set(true)
            showDownloadingMsg()
            thread(start=true) {
                try {
                    downloadAndSave(pdf) // 본문
                    pdf.answer?.let { downloadAndSave(it) } // 정답
                    pdf.downloaded.set(true)
                } catch (e: Exception) {
                    DialogUtils.showServerErr(baseContext)
                    pdf.downloaded.set(false)
                    e.printStackTrace()
                }
                pdf.downloading.set(false)
            }
        }

        private fun showDownloadingMsg() {
            runOnUiThread {
                DaebakToast.show(this@PdfListActivity, "파일을 다운로드 중입니다.")
            }
        }

        private fun downloadAndSave(pdf:Pdf) {
            val uri = Uri.parse("$PDF_URL_PREFIX/${pdf.id}")
            val url = URL(uri.toString())
            (url.openConnection() as HttpURLConnection).run {
                setRequestProperty ("Authorization", "bearer ${user?.token}")
                requestMethod = "GET"
                val buffer = receiveFileByteArray(this)
                val filepath = makeLocalPdfName(pdf)
                saveFile(filepath, buffer)
            }
        }

        private fun makeLocalPdfName(pdf: Pdf) = "${PDF_DIR}/${pdf.cm_book_id}/${pdf.id}.pdf"

        private fun receiveFileByteArray(urlConnection: HttpURLConnection) : ByteArray {
            val isr = urlConnection.inputStream
            var len: Int
            val buffer = ByteArrayOutputStream().use { stream ->
                val data = ByteArray(16384)
                while (isr!!.read(data, 0, data.size).also { len = it } != -1) {
                    stream.write(data, 0, len)
                }
                stream.flush()
                stream.toByteArray()
            }
            isr.close()
            return buffer
        }

        private fun saveFile(fullpath: String, buffer:ByteArray) {
            val file = File(fullpath)

            // pdf root directory 없으면 생성
            if(!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }

            FileOutputStream(file, true).use { stream ->
                stream.write(buffer)
                stream.close()
            }
        }
    }

    interface PdfItemClickListener {
        fun onItemClick(pdf: Pdf)
    }
}

@BindingAdapter("bind_pdf_response")
fun bindPdfRecyclerView(recyclerView: RecyclerView, item: List<Pdf>?){
    Log.d("bind_pdf_response", "list=$item")
    item?.let { pdfList ->
        val adapter = recyclerView.adapter as PdfListActivity.PdfAdapter
        adapter.submitList(pdfList)
    }
}

