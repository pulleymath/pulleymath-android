package com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.MockReportActivity
import com.freewheelin.pulley.activities.OMRActivity
import com.freewheelin.pulley.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.activities.WrongTestReportActivity
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.ShareAnalysisDialog
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.ShareAnalysisDialogListener
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.component.*
import com.freewheelin.pulley.activities.solve.CustomBarChartRender
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.core.API.ResponseModel.DailyRecommend
import com.freewheelin.pulley.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.core.API.ResponseModel.DailySummary
import com.freewheelin.pulley.core.API.ResponseModel.WeekStudyData
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.dialogs.MockExamGuideDialog
import com.freewheelin.pulley.dialogs.MockExamGuideDialogListener
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.textViews.UpDownTextView.Change.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.android.synthetic.main.fragment_analysis.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Math.abs
import java.util.*


class UserAnalysisActivity : AppCompatActivity(),
    ShareAnalysisDialogListener,
        AnalysisTodayStudyListViewListener,
        AnalysisStudyRateViewListener,
        AnalysisRecommendStudyViewListener,
        MockExamGuideDialogListener {

    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_analysis)
        initUI()
    }

    override fun onResume() {
        super.onResume()

        val studentID = intent.getStringExtra(KEY_STUDENT_ID)?:"none"
        val name = intent.getStringExtra(KEY_STUDENT_NAME)?:"none"

        // 액티비티 내에서 api 호출할때 쓰는 값은 studentID, fullName
        user = User()
        user.studentID = studentID
        user.fullName = name

        user.getDailyStudy(this) { setUpStudyUI(it) }
        user.getDailyRecommend(this, callback = { setUpRecommendUI(it) }, failCB = { setUpRecommendUI(null) })
        user.getDailyPiece(this) { setUpPieceUI(it) }
    }

    private fun setUpStudyUI(study: DailyStudy) {
        try {
            mainCurationTv.text = study.mainCuration
            percentCompareTv.valueText = "${study.todayPercentage}%"
            cntCompareTv.valueText = "${study.todayProblemCount}"

            percentCompareTv.diffText = if (study.todayPercentageDiff == 0) "-" else "${abs(study.todayPercentageDiff)}%"
            cntCompareTv.diffText = if (study.todayProblemCountDiff == 0) "-" else "${abs(study.todayProblemCountDiff)}"

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
                timeCompareTv.setDiffTextColor(ContextCompat.getColor(this, R.color.grey_9f9f9f))
            } else {
                timeCompareTv.setDiffTextColor(ContextCompat.getColor(this, R.color.blue_2287ef))
            }

            shareBtn.setOnClickListener {
                LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "공유하기")
                val dialog = ShareAnalysisDialog(this, study)
                dialog.listener = this
                dialog.show()
            }

            setChartData(study.weekStudyData)

        } catch(e:Exception) {
            Log.e("화면크래쉬", "error==>${e.localizedMessage}")
        }
    }

    private fun setUpPieceUI(pieces: List<Content>) {
        try {
            todayStudyView.setUpUIByUserAnalysis()
            todayStudyView.setUpUI(pieces)
            todayStudyView.findViewById<View>(R.id.studyBtn).visibility = View.INVISIBLE
        }catch(e:Exception) {
            Log.e("화면크래쉬", "error==>${e.localizedMessage}")
        }
    }

    private fun setUpRecommendUI(recommend: DailyRecommend?) {
        if(recommend != null) {
            try {
                recommendStudyView.setUpUI(recommend.weakChapter, recommend.curation)
                studyRateView.setUpUI(recommend.compareNormalAndNote, recommend.curation)
                recommendStudyView.showIfNeed()
                studyRateView.showIfNeed()
            }catch(e:Exception) {
                Log.e("화면크래쉬", "error==>${e.localizedMessage}")
            }
        }
    }

    fun initUI() {
        try {
            todayStudyView.listener = this
            studyRateView.listener = this
            recommendStudyView.listener = this
            initChart(timeCountChart)
            mainAnalysisBtn.setOnClickListener {
                LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "전체분석보기")
                val intent = Intent(this, UserAnalysisAllActivity::class.java)
                intent.putExtra(KEY_STUDENT_ID, user.studentID)
                startActivity(intent)
            }
            onFragmentSelected()
        }catch(e:Exception) {
            Log.e("화면크래쉬", "error==>${e.localizedMessage}")
        }
    }

    fun onFragmentSelected() {
        user!!.getDailyStudy(this) { setUpStudyUI(it) }
        user!!.getDailyRecommend(this, callback = {
            setUpRecommendUI(it)
        }, failCB = {
            setUpRecommendUI(null)
        })
        user!!.getDailyPiece(this) { setUpPieceUI(it) }
    }

    private fun setUpUI(summary: DailySummary) {
        if(summary.isNeedToStudyUI) {
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
        chart.axisLeft.gridColor = ContextCompat.getColor(this, R.color.grey_e8e8e8)
        chart.axisLeft.textColor = ContextCompat.getColor(this, R.color.grey_c0c0c0)
        chart.axisLeft.textSize = 14f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.axisLineColor = Color.TRANSPARENT
        chart.xAxis.textSize = resources.getDimension(R.dimen.sp14).pxToSp()
        chart.xAxis.typeface = Theme.bold(this)
        chart.xAxis.textColor = ContextCompat.getColor(this, R.color.grey_9f9f9f)
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
            setValueTextColor(ContextCompat.getColor(baseContext, R.color.purple_ACACFF))
            setValueTypeface(Theme.bold(baseContext))
        }
        val renderer = CustomBarChartRender(timeCountChart, timeCountChart.animator, timeCountChart.viewPortHandler)
        renderer.setRadius(16f.toPx())
        chart.renderer = renderer
        timeCountChart.notifyDataSetChanged()
        timeCountChart.invalidate()

    }

    private fun setChartData(weekStudyData: List<WeekStudyData>) {
        val entry = arrayListOf<BarEntry>()

        for(x in 0 until weekStudyData.size) {
            val value = weekStudyData[x].solvedCount.toFloat()
            entry.add(BarEntry(x.toFloat(), value))
        }


        val barDataSet = BarDataSet(entry, "개수")
        timeCountChart.axisLeft.axisMinimum = 0f
        timeCountChart.axisLeft.axisMaximum = maxOf(100f, barDataSet.yMax)
        val colors = mutableListOf(
                ContextCompat.getColor(this, R.color.grey_e0e0e0),
                ContextCompat.getColor(this, R.color.grey_e0e0e0),
                ContextCompat.getColor(this, R.color.grey_e0e0e0),
                ContextCompat.getColor(this, R.color.grey_e0e0e0),
                ContextCompat.getColor(this, R.color.grey_e0e0e0),
                ContextCompat.getColor(this, R.color.grey_e0e0e0),
                ContextCompat.getColor(this, R.color.grey_e0e0e0)
        )
        when(Date().dayOfWeek()) {
            Calendar.MONDAY -> colors[0] = ContextCompat.getColor(this, R.color.blue_b9defe)
            Calendar.TUESDAY -> colors[1] = ContextCompat.getColor(this, R.color.blue_b9defe)
            Calendar.WEDNESDAY -> colors[2] = ContextCompat.getColor(this, R.color.blue_b9defe)
            Calendar.THURSDAY -> colors[3] = ContextCompat.getColor(this, R.color.blue_b9defe)
            Calendar.FRIDAY -> colors[4] = ContextCompat.getColor(this, R.color.blue_b9defe)
            Calendar.SATURDAY -> colors[5] = ContextCompat.getColor(this, R.color.blue_b9defe)
            Calendar.SUNDAY -> colors[6] = ContextCompat.getColor(this, R.color.blue_b9defe)
        }

        barDataSet.colors = colors

        timeCountChart.data = BarData(barDataSet).apply {
            barWidth = 0.7f
            isHighlightEnabled = false
            setDrawValues(false)
            setValueTextSize(resources.getDimension(R.dimen.sp14).pxToSp())
            setValueTextColor(ContextCompat.getColor(baseContext, R.color.purple_ACACFF))
            setValueTypeface(Theme.bold(baseContext))
        }

        timeCountChart.notifyDataSetChanged()
        timeCountChart.invalidate()
    }


    override fun onDownloadClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap) {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(permission == PackageManager.PERMISSION_GRANTED) {
                saveImage(bitmap)
                DaebakToast.show(this, "저장되었습니다.", bottomOffset = 64.toPx(), overDialog = true)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
    }

    override fun onShareBtnClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap) {
        val uri = saveImageAsCache(bitmap)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onStudyHistoryBtnClicked(view: AnalysisTodayStudyListView) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역보기")
        val intent = Intent(this, UserHistoryActivity::class.java)
        intent.putExtra(KEY_STUDENT_ID, user.studentID)
        intent.putExtra(KEY_STUDENT_NAME, user.fullName)
        startActivity(intent)
    }

    override fun onSolveBtnClicked(view: AnalysisTodayStudyListView, content: Content) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역풀기")
        when(content.pieceCategoryTag) {
            BookType.MO -> {
                if (content.isCompleted()) {
                    val intent = SolveActivity.getReviewIntent(this, MockExam(content))
                    startActivity(intent)
                } else {
                    val exam = MockExam(content)
                    MockExamGuideDialog(this, exam, true, this).show()
                }
            }
            BookType.BOOK, BookType.CUSTOM_BOOK -> {
                val intent = if (content.isCompleted())
                    SolveActivity.getReviewIntent(this, Book(content))
                else
                    SolveActivity.getIntent(this, Book(content))
                startActivity(intent)
            }

            BookType.NOTE, BookType.RECOMMEND -> {
                val intent = if (content.isCompleted()) {
                    SolveActivity.getReviewIntent(this, Piece(content))
                } else {
                    SolveActivity.getIntent(this, Piece(content))
                }
                startActivity(intent)
            }

            BookType.TEST -> {
                val intent = if (content.isCompleted())
                    SolveActivity.getReviewIntent(this, Test(content))
                else
                    SolveActivity.getIntent(this, Test(content))
                startActivity(intent)
            }
        }
    }

    override fun onReportBtnClicked(view: AnalysisTodayStudyListView, content: Content) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "학습내역보고서")
        when(content.pieceCategoryTag) {
            BookType.MO -> {
                val intent = MockReportActivity.getIntent(this, MockExam(content))
                startActivity(intent)
            }
            BookType.TEST -> {
                val test = Test(content)
                when (test.getTestType()) {
                    Test.TestType.weekly -> {
                        val intent = WeeklyTestReportActivity.getIntent(this, test)
                        startActivity(intent)
                    }
                    Test.TestType.wrong -> {
                        val intent = WrongTestReportActivity.getIntent(this, test)
                        startActivity(intent)
                    }
                    else -> {
                        LogUtils.assert(false, "예상치 못한 테스트 타입 ${test.getTestType()}")
                    }
                }
            }
            else -> {
                LogUtils.assert(false, "예상치 못한 카테고리 ${content.category}")
            }
        }
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(this, mockExam, makeNew)
        startActivityForResult(intent, MockExamFragment.REQUEST_MOCK_TEST)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(this, mockExam, makeNew)
        startActivityForResult(intent, MockExamFragment.REQUEST_MOCK_TEST)
    }

    fun saveImage(bitmap: Bitmap) {
        val dateStr = DateTimeUtils.yyyy_MM_dd.format(Date())
        val title = "${dateStr} 데일리 서머리"
        MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, title, "")
    }

    private fun saveImageAsCache(image: Bitmap): Uri? {
        val imagesFolder = File(this.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            val provider = if(BuildConfig.FLAVOR == "beta") "com.freewheelin.beta.fileprovider" else "com.freewheelin.fileprovider"
            uri = FileProvider.getUriForFile(this, provider, file)
        } catch (e: IOException) {
            LogUtils.assert(false, "saveImageAsCache 실패")
        }
        return uri
    }

    override fun onWrongStudyBtnClicked(view: AnalysisStudyRateView) {
//        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "추천문제버튼")
//        ContentManager.makeWrongPiece(this, user!!) {
//            val intent = SolveActivity.getIntent(this, it)
//            startActivity(intent)
//        }
    }

    override fun onRecommendBtnClicked(view: AnalysisRecommendStudyView) {
//        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "추천플랜버튼")
//        ContentManager.makeRecommendPiece(this, user!!) {
//            val intent = SolveActivity.getIntent(this, it)
//            startActivity(intent)
//        }
    }

    // 공부하기 버튼 삭제 필요
    override fun onStudyBtnClicked(view: AnalysisTodayStudyListView) {
//        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "공부하기버튼")
//        (activity as? LearningTabActivity)?.setSelectedTab(3)
    }

    fun setTodayStudyNewOne() {
        Log.d("테스트", "AnalysisFragment => setTodayStudyNewOne.setList(true)")
        scrollToView(scrollContainer, todayStudyView)
        todayStudyView.newOne = true
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

    companion object {
        const val KEY_STUDENT_ID = "student_id"
        const val KEY_STUDENT_NAME = "student_name"
    }
}