package com.freewheelin.pulley.revision2021.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityPdfListBinding
import com.freewheelin.pulley.databinding.HeaderPdfListBinding
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
            val manager = GridLayoutManager(baseContext, 5)
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when(position) {
                        0 -> 5
                        else -> 1
                    }
                }
            }
            recyclerPdf.layoutManager = manager
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
        with(binding) {
            subjectSpinnerAdapter = ArrayAdapter<String>(this@PdfListActivity, R.layout.item_spinner_textview, viewModel.subjectItems)
            categorySpinnerAdapter = ArrayAdapter<String>(this@PdfListActivity, R.layout.item_spinner_textview, viewModel.categoryItems)
        }

        with(viewModel) {
            subjectSelectedPosition.observe(this@PdfListActivity,
                object : Observer<Int> {
                    override fun onChanged(position: Int?) {
                        position?.let {
                            subjectFilter = if(position > 0) PdfListFilter.subject.keys.toList().get(it) else ""
                            filter()
                            ySum = 0
                        }
                    }
                })
            categorySelectedPosition.observe(this@PdfListActivity,
                object : Observer<Int> {
                    override fun onChanged(position: Int?) {
                        position?.let {
                            categoryFilter = if (position > 0) PdfListFilter.category.keys.toList().get(it) else ""
                            filter()
                            ySum = 0
                        }
                    }
                })
            searchText.observe(this@PdfListActivity,
                object : Observer<String> {
                    override fun onChanged(query: String?) {
                        query?.let {
                            with(binding) {
                                val currentQuery = searchName.query
                                if (query != currentQuery) {
                                    searchName.setQuery(it, false)
                                }
                                if (query != "") searchName.isIconified = false
                            }
                        }
                    }
                })
            isSearchViewIconified.observe(this@PdfListActivity,
                object : Observer<Boolean> {
                    override fun onChanged(isIconified: Boolean) {
                        with(binding) {
                            searchName.let { searchView ->
                                if (searchView.isIconified == isIconified) {
                                    searchView.setBackgroundResource(if (isIconified) R.drawable.bg_grey_f2f2f2_round_5 else R.drawable.bg_white_ffffff_round_5)
                                }
                            }
                        }
                    }

                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onBackPressed() {
        with(viewModel) {
            if (currSearchText.value != "") {
                searchText.value = ""
                binding.searchName.let {
                    it.isIconified = true
                }

                (binding.recyclerPdf.adapter as PdfAdapter).headerBinding.searchNameInHeader.let {
                    it.isIconified = true
                }

            } else {
                super.onBackPressed()
            }
        }
    }

    inner class PdfAdapter(private val viewModel: PdfViewModel): ListAdapter<Pdf, RecyclerView.ViewHolder>(DiffCallback<Pdf>()) {
        private val typeHeader = 0
        private val typeItem = 1
        lateinit var headerBinding: HeaderPdfListBinding
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeHeader -> {
                    headerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.header_pdf_list, parent, false)
                    HeaderViewHolder(headerBinding)
                }
                typeItem -> PdfHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_pdf, parent, false))
                else -> PdfHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_pdf, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (position) {
                0 -> (holder as HeaderViewHolder).bind(getItem(position))
                else -> (holder as PdfHolder).bind(getItem(position))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when(position) {
                0 -> typeHeader
                else -> typeItem
            }
        }
    }

    inner class HeaderViewHolder(private val binding: HeaderPdfListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            with(binding) {
                subjectSpinnerAdapter = ArrayAdapter<String>(this@PdfListActivity, R.layout.item_spinner_textview, viewModel.subjectItems)
                categorySpinnerAdapter = ArrayAdapter<String>(this@PdfListActivity, R.layout.item_spinner_textview, viewModel.categoryItems)

                viewModel.run {
                    subjectSelectedPosition.observe(this@PdfListActivity,
                        object : Observer<Int> {
                            override fun onChanged(position: Int?) {
                                position?.let {
                                    spinnerSubject.setSelection(it)
                                }
                            }
                        })
                    categorySelectedPosition.observe(this@PdfListActivity,
                        object : Observer<Int> {
                            override fun onChanged(position: Int?) {
                                position?.let {
                                    spinnerCategory.setSelection(it)
                                }
                            }
                        })
                    pdfListLength.observe(this@PdfListActivity,
                        object : Observer<String> {
                            override fun onChanged(str: String?) {
                                str?.let {
                                    pdfListCountTv.text = it
                                }
                            }
                        })
                    searchText.observe(this@PdfListActivity,
                        object : Observer<String> {
                            override fun onChanged(query: String?) {
                                query?.let {
                                    with(binding) {
                                        val currentQuery = searchNameInHeader.query
                                        if (query != currentQuery) {
                                            searchNameInHeader.setQuery(it, false)
                                        }
                                        if (query != "") searchNameInHeader.isIconified = false
                                    }
                                }
                            }
                        })
                    isHeaderSearchViewIconified.observe(this@PdfListActivity,
                        { isIconified ->
                            with(binding) {
                                this.searchNameInHeader.let { searchView ->
                                    if (searchView.isIconified == isIconified) {
                                        searchView.setBackgroundResource(if (isIconified) R.drawable.bg_grey_f2f2f2_round_5 else R.drawable.bg_white_ffffff_round_5)
                                    }
                                }
                            }
                        })
                }
            }
        }

        fun bind(item: Pdf) {
            with(binding) {
                vm = viewModel
            }
        }
    }

    inner class PdfHolder(private val binding: ItemPdfBinding): RecyclerView.ViewHolder(binding.root), PdfItemClickListener {
        private val downloadThreads = arrayListOf<Thread>()
        private val downloadConnections = arrayListOf<HttpURLConnection>()
        fun bind(item: Pdf) {
            /** 다운로드 체크 */
            item.downloaded.set(File(makeLocalPdfName(item)).exists())
            item.subject = PdfListFilter.subject.get(item.subject_code)?:""

            binding.listener = this
            binding.item = item
            binding.vm = viewModel
        }

        override fun onItemClick(pdf: Pdf) {
            if (pdf.opening.get()) return // pdf 여는중일때 클릭방지

            if(!pdf.downloading.get()) { // 다운로드 중이면 disabled
                if (pdf.downloaded.get()) { open(pdf) } else { download(pdf) }
            }
            else {
                showDownloadCancelMsg()
                downloadThreads.forEach {
                    it.interrupt()
                    cancelDownloadingFile(pdf)
                    pdf.downloadProgress.set(0)
                }
                downloadThreads.clear()
                downloadConnections.forEach {
                    it.disconnect()
                }
                downloadConnections.clear()
            }
        }

        private fun open(pdf: Pdf) {
            pdf.opening.set(true)
            binding.root.context.let { context ->
                viewModel.answer(pdf.cm_book_id) { answerLinks ->
                    thread(start=true) {
                        val answerPath = if(pdf.answer != null) checkDownloaded(pdf.answer!!) else ""
                        openPdf(context, pdf, answerPath, answerLinks)
                        pdf.opening.set(false)
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
            val th = thread(start=true) {
                try {
                    downloadAndSave(pdf) // 본문
                    pdf.answer?.let { downloadAndSave(it) } // 정답
                    pdf.downloaded.set(true)
                } catch (e: Exception) {
                    DialogUtils.showServerErr(baseContext)
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

        private fun showDownloadingMsg() {
            runOnUiThread {
                DaebakToast.show(this@PdfListActivity, "파일을 다운로드 중입니다.")
            }
        }

        private fun showDownloadCancelMsg() {
            runOnUiThread {
                DaebakToast.show(this@PdfListActivity, "파일 다운로드가 취소되었습니다.")
            }
        }

        private fun downloadAndSave(pdf:Pdf) {
            val uri = Uri.parse("$PDF_URL_PREFIX/${pdf.id}")
            val url = URL(uri.toString())
            (url.openConnection() as HttpURLConnection).run {
                downloadConnections.add(this)
                setRequestProperty ("Authorization", "bearer ${user?.token}")
                requestMethod = "GET"
                val buffer = receiveFileByteArray(this, pdf)
                val filepath = makeLocalPdfName(pdf)
                saveFile(filepath, buffer).let {
                    if (downloadConnections.contains(this)) {
                        downloadConnections.remove(this)
                    }
                }
            }
        }

        private fun makeLocalPdfName(pdf: Pdf) = "${PDF_DIR}/${pdf.cm_book_id}/${pdf.id}.pdf"

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

        private fun cancelDownloadingFile(pdf: Pdf) {
            pdfDelete(pdf)
            pdf.answer?.let { pdfDelete(it) }
        }
        private fun pdfDelete(pdf: Pdf) {
            val filepath = makeLocalPdfName(pdf)
            val file = File(filepath)
            if (file.exists()) { file.delete() }
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


@BindingAdapter("stickyAppBarAnim")
fun stickAppBarAnimation(view: View, stickyAppBarShow: Boolean) {
    Log.d("stickyAppBarAnim", "bool=$stickyAppBarShow")
    if (stickyAppBarShow) {
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
        animator.duration = 200
        animator.start()
    }
}