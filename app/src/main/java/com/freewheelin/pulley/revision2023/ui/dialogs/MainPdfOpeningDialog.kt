package com.freewheelin.pulley.revision2023.ui.dialogs

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogMainPdfOpeningBinding
import com.freewheelin.pulley.databinding.DialogTeacherUtilityBinding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2023.viewmodel.TeacherUtilityViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.model.response.PdfLinkAnswerItem
import com.freewheelin.pulley.revision2021.utils.PdfDownloader
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.ui.fragment.MainFragment
import com.freewheelin.pulley.revision2023.viewmodel.MainPdfOpeningDialogViewModel
import com.pulleymath.android.pdf.PdfViewerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainPdfOpeningDialog(): DialogFragment() {

    private val viewModel: MainPdfOpeningDialogViewModel by viewModels()

    private val binding: DialogMainPdfOpeningBinding by lazy {
        DataBindingUtil.inflate(
            layoutInflater.cloneInContext(requireContext()),
            R.layout.dialog_main_pdf_opening,
            null,
            false
        )
    }

    var cb: (Intent) -> Unit = {}
    var failCb: () -> Unit = {}
    companion object {
        const val PDF_ID = "PDF_ID"
        fun newInstance(pdfId: Int, cb: (Intent) -> Unit, failCb: () -> Unit): MainPdfOpeningDialog {
            val args = Bundle().apply {
                putInt(PDF_ID, pdfId)
            }
            val instance = MainPdfOpeningDialog()
            instance.cb = cb
            instance.failCb = failCb
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.apply {
            viewModel.pdfId = getInt(PDF_ID)
        }
        return binding.root
    }

    var prevPdf: Pdf? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.errorAction.observe(viewLifecycleOwner) { type ->
                when (type) {
                    CoroutineExceptionType.HttpException401 -> {
                        dismiss()
                        failCb()
                    }
                    else -> {}
                }
            }


            viewModel.fetchPdfOnId(viewModel.pdfId)
            viewModel.pdf.observe(viewLifecycleOwner) {
                prevPdf?.let { PdfDownloader.cancelDownload(it, requireActivity().filesDir) }
                prevPdf = it

                it?.let { pdf ->
                    val fetchAnswerAndOpenPdf = {
                        viewModel.fetchPdfAnswer(pdf.cm_book_id) { answerLinks ->
                            val openPdf: (String) -> Unit = { answerPath ->
                                val links = answerLinks ?: listOf()
                                openPdf(pdf, answerPath, links)
                                pdf.opening.set(false)
                            }

                            if(pdf.answer != null) {
                                checkDownloaded(pdf.answer!!) {
                                    openPdf(it)
                                }
                            } else {
                                openPdf("")
                            }
                        }
                    }
//                    if (isPdfFileDownloaded(pdf)) {
//                        fetchAnswerAndOpenPdf()
//                    } else {
                    PdfDownloader.download(pdf, requireActivity().filesDir) {
                        viewModel.indicatorText.postValue("다운로드가 완료되었어요!")
                        fetchAnswerAndOpenPdf()
                    }
//                    }
                }
            }
            viewModel.onExitClickCallback = {
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        prevPdf?.let { PdfDownloader.cancelDownload(it, requireActivity().filesDir) }

    }

    fun checkDownloaded(pdf: Pdf, downloadCb: (String) -> Unit) {
        val answerPath = makeLocalPdfName(pdf)
        val file = File(answerPath)
        if(!file.exists()) {
            PdfDownloader.download(pdf, requireActivity().filesDir) {
                downloadCb(answerPath)
            }
//            throw Error("다운로드 안되어있음 / MainFragment ")
        } else {
            downloadCb(answerPath)
        }
    }
    private fun makeLocalPdfName(pdf: Pdf) = "${requireActivity().filesDir}/pdfs/${pdf.cm_book_id}/${pdf.id}.pdf"
    private fun openPdf(pdf:Pdf, answerPath:String, answerLinks:List<PdfLinkAnswerItem>) {
        val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(makeLocalPdfName(pdf)) // cmbookid , id

            putExtra(PdfViewerActivity.KEY_BOOK_ID, pdf.cm_book_id)
            putExtra(PdfViewerActivity.KEY_BOOK_TITLE, "${pdf.title} ${pdf.subject}")
            putExtra(PdfViewerActivity.KEY_PDF_ID, pdf.id)

            if(pdf.answer != null) putExtra(PdfViewerActivity.KEY_ANSWER_PDF_ID, pdf.answer!!.id) // 메인에서는 제외

            putExtra(PdfViewerActivity.KEY_INCLUDE_ANSWER, false)
            putExtra(PdfViewerActivity.KEY_ANSWER_PDF_PATH, answerPath)

            putExtra(PdfViewerActivity.KEY_STUDENT_ID, user!!.studentID)
            putExtra(PdfViewerActivity.KEY_TOKEN, user!!.token)

//                putExtra(PdfViewerActivity.KEY_TEST_API_FLAG, Preferences.onTestAPI.get())
            putExtra(PdfViewerActivity.KEY_API_FLAG, Preferences.onServerAPI.get().toString())

            var linkString = ""
            for(link in answerLinks) {
                val item = "${link.pdf_page_no}:${link.answer_page_no}"
                linkString += "/$item"
            }
            if (linkString.isNotEmpty() && linkString.length > 1) {
                putExtra(PdfViewerActivity.KEY_ANSWER_PAGE_LINK, linkString.substring(1)) // exclude first char "/"
            }
        }
        cb(intent)
        dismiss()
    }
    fun isPdfFileDownloaded(pdf: Pdf): Boolean {
        val answerPath = makeLocalPdfName(pdf)
        val file = File(answerPath)
        return file.exists()
    }
}