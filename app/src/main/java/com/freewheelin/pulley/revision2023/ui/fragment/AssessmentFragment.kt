package com.freewheelin.pulley.revision2023.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.assessment.component.CommunityJavascriptInterface
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.databinding.FragmentAssessmentBinding
import com.freewheelin.pulley.databinding.ItemAssessmentCardBinding
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.assessment.AssessmentReportDialog
import com.freewheelin.pulley.revision2021.activity.AssessmentSolveActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.AssessmentCard
import com.freewheelin.pulley.revision2021.model.response.AssessmentWorkbook
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentFViewModel
import com.freewheelin.pulley.revision2023.ui.activity.UnivAdditionalLearningActivity
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.model.AssessmentDesignSkin
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class AssessmentFragment: MainTabFragment() {
    companion object {
//        const val SHOW_REPORT = "SHOW_REPORT"
        const val SHOW_REPORT_INT = 1000
        const val SHOW_ADDITIONAL_LEARNING = "SHOW_ADDITIONAL_LEARNING"
        fun newInstance(): AssessmentFragment {
            return AssessmentFragment()
        }
    }

    private val viewModel: AssessmentFViewModel by viewModels()
    lateinit var additionalLearningReceiver: BroadcastReceiver

    override var type = MainTab.대학

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var binding: FragmentAssessmentBinding
    private lateinit var solveResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_assessment, container, false)

        solveResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == SHOW_REPORT_INT) {
                val workbook = it.data?.getSerializableExtra(AssessmentSolveActivity.SELECTED_WORKBOOK) as? AssessmentWorkbook ?: return@registerForActivityResult
                showReportDialog(workbook)
                fetchUnivTestGroup()
            }
        }

        additionalLearningReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                val actIntent = UnivAdditionalLearningActivity.getIntent(p0, intent?.getStringExtra("subject"))
                startActivity(actIntent)
            }
        }

        return binding.root
    }

    var isShowMainTab = true
    private fun bindingUI () {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(additionalLearningReceiver, IntentFilter(
            SHOW_ADDITIONAL_LEARNING
        ))
        binding.apply {
            vm = viewModel
            skin = viewModel.getDesignSkin()
            lifecycleOwner = viewLifecycleOwner
            val adapter = UnivTestAdapter(viewModel)
            cardListRv.adapter = adapter
            uuiTv.movementMethod = ScrollingMovementMethod()

//            val skin = viewModel.getDesignSkin()
//            testCompletedIv.setImageResource(skin.completedSrc)
            rightSv.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                if (i2 > 180 && isShowMainTab) {
                    isShowMainTab = false
                    (activity as? MainActivity)?.showTabHeader(false)
                } else if (i2 < 10 && !isShowMainTab) {
                    isShowMainTab = true
                    (activity as? MainActivity)?.showTabHeader(true)
                }
            }
            reportBtn.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "테스트", "보고서-보기")
                val workbookId = viewModel.selectedUnivTestCard.value?.selectedWorkbook?.id ?: return@setOnClickListener
                val version = viewModel.selectedUnivTestCard.value?.selectedWorkbook?.version ?: return@setOnClickListener
                val card = viewModel.selectedUnivTestCard.value ?: return@setOnClickListener
                val skin = viewModel.getDesignSkin()
                AssessmentReportDialog(requireContext(), workbookId, version, skin, card.selectedWorkbook).show()
            }

            webLinkTv.setOnClickListener {
                viewModel.getTempToken { shortToken ->
                    val relativeUrl = "/exam/list"
                    val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                    IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                }
            }
            webLinkTv.text = webLinkTv.text
                .partialUnderline("웹으로 시험 응시하기") {
                    // 동작 안해서 걍 setOnClickListener 달아놓음
                    // 이거 왜 동작을 안하지?
                }
                .partialFontAndColored(Theme.bold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_300), "웹으로 시험 응시하기")

            testStartBtn.setOnClickListener { view ->
                viewModel.selectedUnivTestCard.value?.let {
                    if (it.isTestEnable(viewModel.currentTimeString.value)) {
                        val selectedWorkbook = it.selectedWorkbook
                        val testStartedAt = it.selectedWorkbook?.started_at
                        if (selectedWorkbook?.seq == 1 || !testStartedAt.isNullOrEmpty()) {
                            val intent = AssessmentSolveActivity.getIntent(requireContext(), it)
                            solveResultLauncher.launch(intent)
                        } else {
                            DialogUtils.v2AssessmentStartWarningDialog(requireContext()) {
                                val intent = AssessmentSolveActivity.getIntent(requireContext(), it.selectedWorkbook )
                                solveResultLauncher.launch(intent)
                            }
                        }

                    } else {
                        DaebakToast.show(requireContext(), "시험시작 30분 전부터 입장할 수 있습니다.")
                    }
                }
            }

            initWebView()
//            initAdditionalLearningWebView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(additionalLearningReceiver)

    }

    fun initWebView() {

        binding.apply {
            val token = user?.token ?: "-1"

            val API_COMMUNITY_DOMAIN = when (Preferences.onServerAPI.get()) {
                Network.Server.live.toString() -> "https://pulleymath.com/community?is_mobile=true&token=${token}"
                Network.Server.staging.toString() -> "https://dev.pulleymath.com/community?is_mobile=true&token=${token}"
                Network.Server.dev.toString() -> "https://dev.pulleymath.com/community?is_mobile=true&token=${token}"
                else -> "https://pulleymath.com"
            }

            webView.webViewClient = object: WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    webView.clearHistory()
                    webView.clearCache(true)
                    clearCache(requireContext())
                    super.onPageFinished(view, url)
                }
                fun clearCache(context: Context, file: File? = null) {
                    var dir: File? = file ?: requireActivity().cacheDir ?: return
                    val children = dir?.listFiles()
                    try {
                        children?.forEach {
                            if (it.isDirectory) { clearCache(context, it) }
                            else { it.delete() }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
            webView.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                if (i2 > 180 && isShowMainTab) {
                    isShowMainTab = false
                    (activity as? MainActivity)?.showTabHeader(false)
                } else if (i2 < 10 && !isShowMainTab) {
                    isShowMainTab = true
                    (activity as? MainActivity)?.showTabHeader(true)
                }
            }

            webView.webViewClient = object: WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url);
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
//                    Toast.makeText(requireContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();

                }
            }
            webView.webChromeClient = UnivCommunityClient()
            webView.settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
                allowFileAccess = true
                cacheMode = WebSettings.LOAD_NO_CACHE
            }

            webView.loadUrl(API_COMMUNITY_DOMAIN)
            webView.addJavascriptInterface(CommunityJavascriptInterface(requireContext()), "AndroidFunction");
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        var results: Array<Uri?>? = null

        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            if (callbackArray == null) return

            when (intent) {
                null -> photoPath?.let { results = arrayOf(Uri.parse(it))}
                else -> {
                    intent.dataString?.let {
                        results = arrayOf(Uri.parse(it))
                    }
                }
            }
        }
        callbackArray!!.onReceiveValue(results)
        callbackArray = null
    }

    var callbackArray: ValueCallback<Array<Uri?>?>? = null
    private var photoPath: String? = null
    private val FILECHOOSER_RESULTCODE = 1

    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindingUI()
        viewModel.apply {
            assessmentMetadata.observe(viewLifecycleOwner) {
                binding.run {
                    val _skin = AssessmentDesignSkin.convertGroupCodeToSkin(it?.group_code)
                    skin = _skin
                    testCompletedIv.setImageResource(_skin.completedSrc)
                }
            }
            selectedUnivTestCard.observe(viewLifecycleOwner) {
                val title = it.selectedWorkbook?.title ?: "-"
                val subject = "[${it.selectedWorkbook?.subject ?: "-"}]"
                selectedCardTitle.postValue("${title}${subject}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        remainingTimerListInStartTime.forEach { it.cancel() }
        remainingTimerListInFinishedTime.forEach { it.cancel() }

    }

    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA) }
    var remainingTimerListInStartTime: MutableList<CountDownTimer> = mutableListOf()
    var remainingTimerListInFinishedTime: MutableList<CountDownTimer> = mutableListOf()

    override fun onResume() {
        super.onResume()
        remainingTimerListInStartTime.clear()
        remainingTimerListInFinishedTime.clear()
        fetchUnivTestGroup()
    }

    private fun fetchUnivTestGroup () {
        viewModel.fetchUnivTestGroup {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.assessmentCardList.value?.forEach {
                    when {
                        it.thirdWorkbook?.isFinished() == true -> {
                            it.remainingTimeText.set("")
                            it.isCompleted.set(true)
                        }
                        it.secondWorkbook?.isFinished() == true -> {
                            when {
                                it.thirdWorkbook?.isTestNotStartedYet() == true -> {
                                    it.remainingTimeText.set("${viewModel.selectedCardSecondWorkbookTitle()} 완료")
                                }
                                it.getTestSize() == 2 -> {
                                    it.remainingTimeText.set("${viewModel.selectedCardSecondWorkbookTitle()} 완료")
                                    it.isCompleted.set(true)
                                }
                                else -> {
                                    it.remainingTimeText.set("${viewModel.selectedCardThirdWorkbookTitle()} 진행중")
                                }
                            }
                        }
                        it.firstWorkbook?.isFinished() == true -> {
                            when {
                                it.secondWorkbook?.isTestNotStartedYet() == true -> {
                                    it.remainingTimeText.set("${viewModel.selectedCardSecondWorkbookTitle()} 진행 가능")
                                }
                                else -> {
                                    it.remainingTimeText.set("${viewModel.selectedCardSecondWorkbookTitle()} 진행중")
                                }
                            }
                        }
                        else -> {
//                    val testStartedAt = "2022-02-05 19:50:26"
                            val testStartedAt = it.firstWorkbook?.test_started_at ?: return@postDelayed
                            setTestStartCountDownTimer(it, testStartedAt)

//                    val testFinishedAt = "2022-02-05 19:51:26"
                            val testFinishedAt = it.firstWorkbook?.test_finished_at ?: return@postDelayed
                            setTestFinishCountDownTimer(it, testFinishedAt)
                        }
                    }
                }
            }, 100)
        }
    }

    private fun setTestStartCountDownTimer(card: AssessmentCard, timeStr: String) {
        val timeDiffMilli = getTimeDiffMilli(timeStr) ?: return

        val remainingTimerInStartTime = object : CountDownTimer(timeDiffMilli, 1000) {
            val univFirstTestName = viewModel.selectedUnivTestCard.value?.firstWorkbook?.title ?: ""

            override fun onTick(diff: Long) {
                val day = diff / 1000 / 3600 / 24
                val hour = (diff / 1000 / 3600) - (day * 24)
                val min = (diff / 1000 / 60) - (hour * 60) - (day * 24 * 60)

                val hourStr = if (hour.toString().length < 2) "0$hour" else hour.toString()
                val minStr = if (min.toString().length < 2) "0$min" else min.toString()

                card.remainingTimeText.set("$univFirstTestName 시작까지 ${day}일 ${hourStr}시간 ${minStr}분 남음")
            }
            override fun onFinish() {
                card.remainingTimeText.set("$univFirstTestName 진행 중")
            }
        }.start()
        remainingTimerListInStartTime.add(remainingTimerInStartTime)
    }
    private fun setTestFinishCountDownTimer(card: AssessmentCard, timeStr: String) {
        val univFirstTestName = viewModel.selectedUnivTestCard.value?.firstWorkbook?.title ?: ""

        val finishedTimeDiffMilli = getTimeDiffMilli(timeStr) ?: return

        val remainingTimerInFinishedTime = object : CountDownTimer(finishedTimeDiffMilli, 1000) {
            override fun onTick(diff: Long) {}
            override fun onFinish() {
                card.remainingTimeText.set("${univFirstTestName}가 종료되었습니다.")
            }
        }.start()
        remainingTimerListInFinishedTime.add(remainingTimerInFinishedTime)
    }

    private fun getTimeDiffMilli (timeStr: String): Long? {
        val paredDate = sdf.parse(timeStr) ?: return null
        viewModel.currentTimeString.value?.let { serverTime ->
            val parsedCurrentServerDate = sdf.parse(serverTime) ?: return null
            return paredDate.time - parsedCurrentServerDate.time
        }
        val nowDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        return paredDate.time - nowDate.time.time
    }

    fun getCardOnGroupId(groupId: Int): AssessmentCard? {
        return viewModel.assessmentCardList.value?.filter { it.groupId == groupId}?.get(0)
    }
    fun showReportDialog(workbook: AssessmentWorkbook) {
        val workbookId = workbook.id
        val version = workbook.version
        val skin = viewModel.getDesignSkin()
        AssessmentReportDialog(requireContext(), workbookId, version, skin, workbook).show()
    }

    inner class UnivTestAdapter(private val viewModel: AssessmentFViewModel) :
        ListAdapter<AssessmentCard, AssessmentCardHolder>(
            DiffCallback<AssessmentCard>()
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentCardHolder {
            return AssessmentCardHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_assessment_card, parent, false))
        }

        override fun onBindViewHolder(holder: AssessmentCardHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    interface AssessmentCardClickListener {
        fun onItemClick(ut: AssessmentCard)
    }

    inner class AssessmentCardHolder(val binding: ItemAssessmentCardBinding):
        RecyclerView.ViewHolder(binding.root),
        AssessmentCardClickListener {

        fun bind(item: AssessmentCard) {
            binding.listener = this
            binding.item = item
            binding.vm = viewModel
            binding.skin = viewModel.getDesignSkin()
        }
        override fun onItemClick(ut: AssessmentCard) {
            viewModel.selectCard(ut)
        }
    }
    interface TestStartClickListener {
        fun onItemClick()
    }

    inner class UnivCommunityClient: WebChromeClient() {
        // https://stackoverflow.com/questions/29045637/html-input-type-file-is-not-working-on-webview-in-android-is-there-any-way-to
        override fun onShowFileChooser(
            webView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            callbackArray?.onReceiveValue(null)
            callbackArray = filePathCallback

            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(requireContext().packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", photoPath)
                } catch (ex: IOException) {
                    println("onShowFile Image file creation failed ${ex}")
                }
                if (photoFile != null) {
                    photoPath = "file:" + photoFile.absolutePath
                    val photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        getString(R.string.provider_id),
                        photoFile
                    )
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        photoURI
                    )
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            val intentArray: Array<Intent?> = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
            val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
                putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            }

            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
            return true
        }
    }
}
@BindingAdapter("bind_assessment_card_list")
fun bindItem(recyclerView: RecyclerView, item: List<AssessmentCard>?) {
    item?.let { workbookList ->
        val adapter = recyclerView.adapter as AssessmentFragment.UnivTestAdapter
        adapter.submitList(workbookList.toMutableList())
    }
}

@BindingAdapter("android:src")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}