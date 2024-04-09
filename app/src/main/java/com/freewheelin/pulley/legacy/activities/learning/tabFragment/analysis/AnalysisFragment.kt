package com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ScrollView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.MockReportActivity
import com.freewheelin.pulley.legacy.activities.OMRActivity
import com.freewheelin.pulley.legacy.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.legacy.activities.WrongTestReportActivity
import com.freewheelin.pulley.legacy.activities.analysis.AnalysisTabActivity


import com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis.component.*
import com.freewheelin.pulley.legacy.activities.solve.CustomBarChartRender
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.*
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.core.manage.ContentManager
import com.freewheelin.pulley.legacy.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.FragmentAnalysisBinding
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.legacy.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.legacy.model.contents.*
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.viewmodel.AnalysisFViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.textViews.UpDownTextView.Change.*
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity
import com.freewheelin.pulley.revision2023.ui.fragment.MainTabFragment
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Math.abs
import java.util.*


class AnalysisFragment : MainTabFragment(),
//        ShareAnalysisDialogListener,
        AnalysisTodayStudyListViewListener,
        AnalysisStudyRateViewListener,
        AnalysisRecommendStudyViewListener,
        MockExamGuideDialogListener {

    companion object {
        const val IS_SAMPLE = "IS_SAMPLE"
        const val TAB_SCROLL_EVENT = "TAB_SCROLL_EVENT"
        const val TAB_SCROLL_EVENT_TARGET = "TAB_SCROLL_EVENT_TARGET"

        fun newInstance(): AnalysisFragment {
            return AnalysisFragment()
        }
    }
    lateinit var binding: FragmentAnalysisBinding
    val viewModel: AnalysisFViewModel by viewModels()
    lateinit var tabScrollReceiver: BroadcastReceiver

    override var type = MainTab.분석

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analysis, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        initReceiver()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(tabScrollReceiver, IntentFilter(TAB_SCROLL_EVENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.apply {
            userInRepo.observe(viewLifecycleOwner) { user ->
                user?.let {
                    // guest과 BasicC만 표시
                    val isNoneUser = it.serviceType.isNoneUser
                    val isBasicC = it.serviceType.isBasicC
                    binding.totalAnalysisBtn.showStartIcon(isNoneUser || isBasicC)

                    val showLockIv = user.serviceType.isUnderBasicP()
                    binding.recommendStudyView.actionBtn.showStartIcon(showLockIv)
                    binding.studyRateView.actionBtn.showStartIcon(showLockIv)
                }
            }
            schoolType.observe(viewLifecycleOwner) {
                binding.percentCompareTv.binding.topLabel.text = if(it.isHigh) {
                    "오늘의 백분위"
                } else {
                    "오늘의 정답률"
                }

                user?.let { user ->
                    getDailyStudy(user.studentID) { setUpStudyUI(it) }
                    getDailyRecommend(user.studentID, callback = {
                        setUpRecommendUI(it)
                    }, failCB = {
                        setUpRecommendUI(null)
                    })
                    getDailyPiece(user.studentID) { setUpPieceUI(it) }
                }
            }
        }
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            todayStudyView.listener = this@AnalysisFragment
            studyRateView.listener = this@AnalysisFragment
            recommendStudyView.listener = this@AnalysisFragment

            initChart(timeCountChart)

            totalAnalysisBtn.setOnBasicPOrHigherClickListener(cb = {
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "전체분석보기")
                val intent = Intent(requireContext(), AnalysisTabActivity::class.java)
                startActivity(intent)
            }, deniedCb = {
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "분석", "전체분석보러가기")
                if (user?.serviceType?.isGuestUser == true) {
                    LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "분석", "가입유도", "전체분석보러가기")
                    val dialog = JoinInduceForGuestDialog().apply {
                        updateDismissCallback {
                            viewModel.errorStatusReset()
                        }
                    }
                    childFragmentManager.let { dialog.show(it, "joinInduceDialog") }
                } else {
                    LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "분석", "결제유도", "전체분석보러가기")
                    val dialog = PurchaseGuideDialog.newInstance()
                    childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
//                DialogUtils.confirmDialog(requireContext(), "[테스트]구독중이 아닙니다.", "하하")
                }
            })
        }
    }

    private fun getDailyStudy(studentId: String, callback: (DailyStudy)->Unit) {
        API_V3.getDailyStudy(studentId).enqueue(object : Callback<ResponseBody<DailyStudy>> {
            override fun onResponse(call: Call<ResponseBody<DailyStudy>>, response: Response<ResponseBody<DailyStudy>>) {
                val res = response.body() ?: return responseError(requireContext(), response)
                res.data?.let { callback(it) }
            }

            override fun onFailure(call: Call<ResponseBody<DailyStudy>>, t: Throwable) {
                responseFailed(requireContext(), t)
            }
        })
    }
    fun initReceiver () {
        tabScrollReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                intent?.let {
                    val target = it.getStringExtra(TAB_SCROLL_EVENT_TARGET)
                    when (target) {
                        "todayStudyView" -> {
                            val outArr = arrayOf(0, 0).toIntArray()
                            binding.todayStudyView.getLocationOnScreen(outArr)
                            val yValueOnView = outArr[1] - 100.toPx()
                            binding.scrollContainer.smoothScrollTo(0, yValueOnView)
                        }
                        else -> {}
                    }
                }
            }

        }
    }
    override fun onResume() {
        super.onResume()
        user?.let {
            getDailyStudy(it.studentID) { setUpStudyUI(it) }
            getDailyRecommend(it.studentID, callback = {
                setUpRecommendUI(it)
            }, failCB = {
                setUpRecommendUI(null)
            })
            getDailyPiece(it.studentID) {
                setUpPieceUI(it)
            }

        }

        initChart(binding.timeCountChart)
    }

    private fun setUpStudyUI(study: DailyStudy) {
        try {
            binding.apply {
                mainCurationTv.text = study.mainCuration
                percentCompareTv.valueText = "${study.todayPercentage}%"
                cntCompareTv.valueText = "${study.todayProblemCount}"

                percentCompareTv.diffText =
                    if (study.todayPercentageDiff == 0) "-" else "${abs(study.todayPercentageDiff)}%"
                cntCompareTv.diffText =
                    if (study.todayProblemCountDiff == 0) "-" else "${abs(study.todayProblemCountDiff)}"

                percentCompareTv.change = when {
                    study.todayPercentageDiff == 0 -> noChange
                    study.todayPercentageDiff > 0 -> increase
                    else -> decrease
                }

                cntCompareTv.change = when {
                    study.todayProblemCountDiff == 0 -> noChange
                    study.todayProblemCountDiff > 0 -> increase
                    else -> decrease
                }

                timeCompareTv.change = noChange
                timeCompareTv.valueText = DateTimeUtils.getHourMinSpentTimeStr(study.totalStudyTime)
                timeCompareTv.diffText = DateTimeUtils.getHourMinSpentTimeStr(study.onlyStudyTime)
                if (study.onlyStudyTime < 60) {
                    timeCompareTv.setDiffTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.gray_600
                        )
                    )
                } else {
                    timeCompareTv.setDiffTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_500
                        )
                    )
                }

                setChartData(study.weekStudyData)
                sampleWrapperCl.setOnClickListener {
                    val intent = Intent(requireContext(), AnalysisTabActivity::class.java)
                    intent.putExtra(IS_SAMPLE, true)
                    startActivity(intent)
                }
            }
        } catch(e:Exception) {
            Log.e("화면크래쉬", "error==>${e.localizedMessage}")
        }
    }

    private fun setUpPieceUI(pieces: List<Content>) {
        try {
            binding.todayStudyView.setUpUI(pieces)
        }catch(e:Exception) {
            Log.e("화면크래쉬", "error==>${e.localizedMessage}")
        }
    }

    private fun setUpRecommendUI(recommend: DailyRecommend?) {
        if(recommend != null) {
            try {
                binding.apply {
                    recommendStudyView.setUpUI(recommend.weakChapter, recommend.curation)
                    studyRateView.setUpUI(recommend.compareNormalAndNote, recommend.curation)
                    recommendStudyView.showIfNeed()
                    studyRateView.showIfNeed()
                }
            } catch(e:Exception) {
                Log.e("화면크래쉬", "error==>${e.localizedMessage}")
            }
        } else {
            binding.recommendStudyView.visibleIf(false)
            binding.studyRateView.visibleIf(false)
        }
    }

    private fun setUpUI(summary: DailySummary) {
        binding.apply {
            if (summary.isNeedToStudyUI) {
                recommendStudyView.visibility = View.GONE
                studyRateView.visibility = View.GONE
                setChartData(summary.weekStudyData)

            } else {
                recommendStudyView.visibility = View.VISIBLE
                studyRateView.visibility = View.VISIBLE

                recommendStudyView.setUpUI(summary.weakChapter, summary.curation)
                studyRateView.setUpUI(summary.compareNormalAndNote, summary.curation)
                setChartData(summary.weekStudyData)
            }
        }
    }

    private fun initChart(chart: BarChart) {
        chart.isScaleXEnabled = false
        chart.isScaleYEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawAxisLine(false)
        chart.axisRight.setDrawGridLines(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisLeft.setDrawLabels(true)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.labelCount = 3
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.gray_300)
        chart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.gray_500)
        chart.axisLeft.textSize = 14f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.axisLineColor = Color.TRANSPARENT
        chart.xAxis.textSize = resources.getDimension(R.dimen.sp14).pxToSp()
        chart.xAxis.typeface = Theme.bold(requireContext())
        chart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.gray_600)
        chart.xAxis.setDrawAxisLine(true)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setValueFormatter { value, axis ->
            val index = value.toInt()
            when(index) {
                0 -> "월"
                1 -> "화"
                2 -> "수"
                3 -> "목"
                4 -> "금"
                5 -> "토"
                else -> "일"
            }
        }

        chart.xAxis.labelCount = 5
        chart.extraBottomOffset = resources.getDimension(R.dimen.dp32)

        val entry = arrayListOf<BarEntry>()

        for(x in 0 until 7) {
            entry.add(BarEntry(x.toFloat(), 0f))
        }
        val barDataSet = BarDataSet(entry, "개수")

        chart.data = BarData(barDataSet).apply {
            barWidth = 0.7f
            isHighlightEnabled = false
            setDrawValues(false)
            setValueTextSize(resources.getDimension(R.dimen.sp14).pxToSp())
            setValueTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            setValueTypeface(Theme.bold(requireContext()))
        }
        binding.apply {
            val renderer = CustomBarChartRender(
                timeCountChart,
                timeCountChart.animator,
                timeCountChart.viewPortHandler
            )
            renderer.setRadius(16f.toPx())
            chart.renderer = renderer
            timeCountChart.notifyDataSetChanged()
            timeCountChart.invalidate()
        }

    }

    private fun setChartData(weekStudyData: List<WeekStudyData>) {
        val entry = arrayListOf<BarEntry>()

        for(x in 0 until weekStudyData.size) {
            val value = weekStudyData[x].solvedCount.toFloat()
            entry.add(BarEntry(x.toFloat(), value))
        }


        val barDataSet = BarDataSet(entry, "개수")
        binding.timeCountChart.axisLeft.axisMinimum = 0f
        binding.timeCountChart.axisLeft.axisMaximum = maxOf(100f, barDataSet.yMax)
        val colors = mutableListOf(
                ContextCompat.getColor(requireContext(), R.color.gray_400),
                ContextCompat.getColor(requireContext(), R.color.gray_400),
                ContextCompat.getColor(requireContext(), R.color.gray_400),
                ContextCompat.getColor(requireContext(), R.color.gray_400),
                ContextCompat.getColor(requireContext(), R.color.gray_400),
                ContextCompat.getColor(requireContext(), R.color.gray_400),
                ContextCompat.getColor(requireContext(), R.color.gray_400)
        )
        when(Date().dayOfWeek()) {
            Calendar.MONDAY -> colors[0] = ContextCompat.getColor(requireContext(), R.color.blue_200)
            Calendar.TUESDAY -> colors[1] = ContextCompat.getColor(requireContext(), R.color.blue_200)
            Calendar.WEDNESDAY -> colors[2] = ContextCompat.getColor(requireContext(), R.color.blue_200)
            Calendar.THURSDAY -> colors[3] = ContextCompat.getColor(requireContext(), R.color.blue_200)
            Calendar.FRIDAY -> colors[4] = ContextCompat.getColor(requireContext(), R.color.blue_200)
            Calendar.SATURDAY -> colors[5] = ContextCompat.getColor(requireContext(), R.color.blue_200)
            Calendar.SUNDAY -> colors[6] = ContextCompat.getColor(requireContext(), R.color.blue_200)
        }

        barDataSet.colors = colors

        binding.timeCountChart.data = BarData(barDataSet).apply {
            barWidth = 0.7f
            isHighlightEnabled = false
            setDrawValues(false)
            setValueTextSize(resources.getDimension(R.dimen.sp14).pxToSp())
            setValueTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            setValueTypeface(Theme.bold(requireContext()))
        }

        binding.timeCountChart.notifyDataSetChanged()
        binding.timeCountChart.invalidate()
    }


//    override fun onDownloadClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap) {
//            val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            if(permission == PackageManager.PERMISSION_GRANTED) {
//                saveImage(bitmap)
//                DaebakToast.show(requireContext(), "저장되었습니다.", bottomOffset = 64.toPx(), overDialog = true)
//            } else {
//                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
//            }
//    }
//
//    override fun onShareBtnClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap) {
//        val uri = saveImageAsCache(bitmap)
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, uri)
//            type = "image/*"
//        }
//
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        startActivity(shareIntent)
//    }

    override fun onStudyHistoryBtnClicked(view: AnalysisTodayStudyListView) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역보기")
        val intent = StudyHistoryActivity.getIntent(requireContext())
        startActivity(intent)
    }
    override fun onSolveBtnClicked(view: AnalysisTodayStudyListView, content: Content) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역풀기")
        when(content.pieceCategoryTag) {
            BookType.MO -> {
                if (content.isCompleted()) {
                    getMockWithOptionalSubjects(content) { mock ->
                        val intent = SolveActivity.getReviewIntent(requireContext(), mock)
                        startActivity(intent)
                    }
                } else {
                    val exam = MockExam(content)
                    MockExamGuideDialog(requireContext(), exam, true, this).show()
                }
            }
            BookType.CUSTOM_BOOK -> {
                val intent = if (content.isCompleted())
                    SolveActivity.getIntent(requireContext(), Book(content))
                else
                    SolveActivity.getIntent(requireContext(), Book(content))
                startActivity(intent)
            }
            BookType.BOOK, BookType.PRACTICE, BookType.TEACHER -> {
                val intent = SolveActivity.getIntent(requireContext(), Book(content))
                startActivity(intent)
            }

            BookType.NOTE, BookType.RECOMMEND -> {
                val intent = if (content.isCompleted()) {
                    SolveActivity.getReviewIntent(requireContext(), Piece(content))
                } else {
                    SolveActivity.getIntent(requireContext(), Piece(content))
                }
                startActivity(intent)
            }

            BookType.TEST -> {
                val intent = if (content.isCompleted())
                    SolveActivity.getReviewIntent(requireContext(), Test(content))
                else
                    SolveActivity.getIntent(requireContext(), Test(content))
                startActivity(intent)
            }
            else -> {}
        }
    }

    override fun onReportBtnClicked(view: AnalysisTodayStudyListView, content: Content) {
        println("asoaso onReportBtnClicked 2")
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역보고서")
        when(content.pieceCategoryTag) {
            BookType.MO -> {
                getMockWithOptionalSubjects(content) { mock ->
                    val intent = MockReportActivity.getIntent(requireContext(), mock)
                    startActivity(intent)
                }
            }
            BookType.TEST -> {
                val test = Test(content)
                when (test.getTestType()) {
                    Test.TestType.weekly -> {
                        val intent = WeeklyTestReportActivity.getIntent(requireContext(), test)
                        startActivity(intent)
                    }
                    Test.TestType.wrong -> {
                        val intent = WrongTestReportActivity.getIntent(requireContext(), test)
                        startActivity(intent)
                    }
                    else -> {
                        LogUtils.assert(false, "예상치 못한 테스트 타입 ${test.getTestType()}")
                    }
                }
            }
            else -> {
                LogUtils.assert(false, "예상치 못한 카테고리")
            }
        }
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(requireContext(), mockExam, makeNew)
        startActivityForResult(intent, MockListActivity.RESULT_MOCK_FINISH)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(requireContext(), mockExam, makeNew)
        startActivityForResult(intent, MockListActivity.RESULT_MOCK_FINISH)
    }

    fun saveImage(bitmap: Bitmap) {
        val dateStr = DateTimeUtils.yyyy_MM_dd.format(Date())
        val title = "${dateStr} 데일리 서머리"
        MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, title, "")
    }

    private fun saveImageAsCache(image: Bitmap): Uri? {
        val imagesFolder = File(requireContext().cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            val provider = if(BuildConfig.FLAVOR == "beta") "com.freewheelin.beta.fileprovider" else "com.freewheelin.fileprovider"
            uri = FileProvider.getUriForFile(requireContext(), provider, file)
        } catch (e: IOException) {
            LogUtils.assert(false, "saveImageAsCache 실패")
        }
        return uri
    }

    override fun onWrongStudyBtnClicked(view: AnalysisStudyRateView) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "추천문제버튼")
        ContentManager.makeWrongPiece(requireContext(), user!!) {
            val intent = SolveActivity.getIntent(requireContext(), it)
            startActivity(intent)
        }
    }

    override fun onRecommendBtnClicked(view: AnalysisRecommendStudyView) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "추천문제집버튼")
        ContentManager.makeRecommendPiece(requireContext(), user!!, successCB = {
            val intent = SolveActivity.getIntent(requireContext(), it)
            startActivity(intent)
        }, failedCb = {
            val message = it ?: "추천 문제를 찾을 수 없습니다."
            DaebakToast.show(requireContext(), message)
        })
    }

//    override fun onDeniedCallback() {   val dialog = PurchaseGuideDialog()
//        childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
//
//    }

    override fun onStudyBtnClicked(view: AnalysisTodayStudyListView) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "공부하기버튼")
        (activity as? MainActivity)?.tabMove(2)

    }

    fun setTodayStudyNewOne() {
        Log.d("테스트", "AnalysisFragment => setTodayStudyNewOne.setList(true)")
        user?.let {
            getDailyStudy(it.studentID) { setUpStudyUI(it) }
            getDailyRecommend(it.studentID, callback = {
                setUpRecommendUI(it)
            }, failCB = {
                setUpRecommendUI(null)
            })
            getDailyPiece(it.studentID) {
                setUpPieceUI(it)
                scrollToView(binding.scrollContainer, binding.todayStudyView)
                binding.todayStudyView.newOne = true
            }

        }

        initChart(binding.timeCountChart)
    }

    private fun scrollToView(scrollViewParent: ScrollView, view: View) {
        val childOffset = Point()
        getDeepChildOffset(scrollViewParent, view.parent, view, childOffset)
        scrollViewParent.smoothScrollTo(0, childOffset.y)
    }

    private fun getDeepChildOffset(mainParent: ViewGroup, parent: ViewParent, child: View, accumulatedOffset: Point) {
        val parentGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup == mainParent) {
            return
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
    }
    private fun getMockWithOptionalSubjects(content: Content, cb: (summary: MockExam) -> Unit) {
        val mock = MockExam(content)
        viewModel.fetchMockSummary(content.mockID, content.assignID) { mockExamSummary ->
            val optionResult = mutableListOf<CommercialSubject>()
            mockExamSummary?.let {
                val optionalSubjects = mockExamSummary.optionalSubjectSummary

                for(subject in optionalSubjects) {
                    if (subject.isSelected) {
                        optionResult.add(CommercialSubject.valueOf(subject.subjectCodeType))
                    }
                }
                mock.selectOptional = optionResult
                mock.examType = mockExamSummary.examType.let {
                    MockExam.ExamType.valueOnString(it)
                }
                mock.grade = mockExamSummary.grade
            }

            cb(mock)
        }
    }

    private fun getDailyPiece(studentId: String, callback: (List<Content>)->Unit) {
        API_V3.getDailyPiece(studentId).enqueue(object : Callback<ResponseListBody<Content>> {
            override fun onResponse(call: Call<ResponseListBody<Content>>, response: Response<ResponseListBody<Content>>) {
                val data = response.body()?.data ?: return responseError(requireContext(), response)
                callback(data)
            }

            override fun onFailure(call: Call<ResponseListBody<Content>>, t: Throwable) {
                responseFailed(requireContext(), t)
            }
        })
    }

    private fun getDailyRecommend(studentId: String, callback: (DailyRecommend)->Unit, failCB: () -> Unit) {
        API_V3.getDailyRecommend(studentId).enqueue(object : Callback<ResponseBody<DailyRecommend>> {
            override fun onResponse(call: Call<ResponseBody<DailyRecommend>>, response: Response<ResponseBody<DailyRecommend>>) {
                val res = response.body() ?: return responseError(requireContext(), response)
                if (res.data != null) {
                    callback(res.data)
                } else {
                    failCB()
                }
            }

            override fun onFailure(call: Call<ResponseBody<DailyRecommend>>, t: Throwable) {
                failCB()
            }
        })
    }
}