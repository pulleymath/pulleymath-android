package com.freewheelin.pulley.activities.analysis

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.analysis.tabFragment.AnalysisByLevelFragment
import com.freewheelin.pulley.activities.analysis.tabFragment.AnalysisStudyAmountFragment
import com.freewheelin.pulley.activities.analysis.tabFragment.AnalysisUnitFragment
import com.freewheelin.pulley.bases.BaseNavActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.ActivityAnalysisTabBinding
import com.freewheelin.pulley.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.dialogs.DateRangePickerDialogListener
import com.freewheelin.pulley.model.Analysis
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.extensionTouchArea
import com.freewheelin.pulley.utils.showBalloon
import com.freewheelin.pulley.utils.toPx
import com.google.android.material.tabs.TabLayout
import org.joda.time.LocalDate
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow

interface AnalysisTabDelegate {
    val tabTitle: String
    var scrollPosition: Int
    fun getFragment(): Fragment
    var from: LocalDate
    var to: LocalDate

    val analysis: Analysis?


    fun onPeriodSelected(from: LocalDate, to: LocalDate, period: Int) {
        this.from = from
        this.to = to
    }
}

interface AnanlysisTabActivityInterface {
    var analysis:Analysis?
    var notExistDataText:String
}

class AnalysisTabActivity : BaseNavActivity(),
        TabLayout.OnTabSelectedListener,
        View.OnScrollChangeListener,
        DateRangePickerDialogListener,
        AnanlysisTabActivityInterface {

    override var analysis: Analysis? = null
    override var notExistDataText = "분석을 위한 학습 내역이 부족해요."

    val dialog: DateRangePickerDialog by lazy {
        val to = LocalDate.now()
        val from = LocalDate.now().minusDays(6)
        val pickerDialog = DateRangePickerDialog(this, from, to, LocalDate(user!!.firstDate))
        pickerDialog.listener = this
        pickerDialog
    }

    val analysisTab: List<AnalysisTabDelegate> = listOf(
            AnalysisUnitFragment.newInstance(),
            AnalysisByLevelFragment.newInstance(),
            AnalysisStudyAmountFragment.newInstance()
    )
    private val binding: ActivityAnalysisTabBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_analysis_tab, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        configTab(0)
        configTab(1)
        configTab(2)
        replaceFragment(analysisTab[0])
        binding.apply {
            scrollView.setOnScrollChangeListener(this@AnalysisTabActivity)

            val to = LocalDate.now()
            val from = LocalDate.now().minusDays(6)
            configUI(from, to)

            tabLayout.addOnTabSelectedListener(this@AnalysisTabActivity)

            monthContainerCl.setOnClickListener {
                dialog.show()
            }

            prevBtn.setOnClickListener {
                val from = dialog.from
                val to = dialog.to
                val duration = dialog.period

                dialog.from = from.minusDays(duration)
                dialog.to = to.minusDays(duration)
                dialog.binding.selectRangeBtn.performClick()

                configUI(dialog.from, dialog.to)
            }

            nextBtn.setOnClickListener {
                val from = dialog.from
                val to = dialog.to
                val duration = dialog.period

                dialog.from = from.plusDays(duration)
                dialog.to = to.plusDays(duration)
                dialog.binding.selectRangeBtn.performClick()

                configUI(dialog.from, dialog.to)
            }


            backBtn.setOnClickListener {
                finish()
            }

            correctRateQuestionIv.extensionTouchArea(4.toPx())
            correctRateQuestionIv.setOnClickListener {
                it.showBalloon("문항의 난이도와 회원의 수준별 정답률을\n고려한 성적 지표입니다.")
            }
            ratingQuestionIV.extensionTouchArea(4.toPx())
            ratingQuestionIV.setOnClickListener {
                it.showBalloon("내부 백분위 점수 바탕으로\n추출된 성적 지표입니다.")
            }
            myAnalysisGuideBtn.setOnClickListener {
                showMyAnalysisGuide()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (Tutor.TooltipType.analysisMain.isNeedToShow())
                    showMyAnalysisGuide()
            }, 1000)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabSelected(tab: TabLayout.Tab) {
        val delegate = analysisTab[tab.position]
        replaceFragment(delegate)
    }

    override fun onScrollChange(view: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        analysisTab[binding.tabLayout.selectedTabPosition].scrollPosition = scrollY
    }

    override fun onUpdateClicked(picker: DateRangePickerDialog, from: LocalDate, to: LocalDate, type:DateRangePickerDialog.Type) {
        analysisTab[binding.tabLayout.selectedTabPosition].onPeriodSelected(dialog.from, dialog.to, dialog.period)
        configUI(from, to)
    }

    private fun configTab(position: Int) {
        binding.tabLayout.addTab(binding.tabLayout.newTab())
        val view = AnalysisTabView(this)
        val startBorder = view.startBorder
        val endBorder = view.endBorder
        view.tabTitleTv.text = analysisTab[position].tabTitle
        when(position) {
            0 -> {
                startBorder.visibility = View.GONE
                endBorder.visibility = View.GONE
            }
            1 -> {
                startBorder.visibility = View.VISIBLE
                endBorder.visibility = View.VISIBLE
            }
            2 -> {
                startBorder.visibility = View.GONE
                endBorder.visibility = View.GONE
            }
            else -> {
                startBorder.visibility = View.GONE
                endBorder.visibility = View.GONE
            }
        }

        val tab = (binding.tabLayout.getChildAt(0) as LinearLayout).getChildAt(position)
        val layoutParam = tab.layoutParams

        layoutParam.width = resources.getDimension(R.dimen.Report_tab_width).toInt()
        tab.setPadding(0,0,0,0)
        binding.tabLayout.getTabAt(position)?.customView = view
    }

    private fun replaceFragment(delegate: AnalysisTabDelegate) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        delegate.from = dialog.from
        delegate.to = dialog.to
        transaction.replace(R.id.fragContainerLl, delegate.getFragment())

        transaction.commit()
    }

    private fun setRangeText(from: LocalDate, to: LocalDate) {
        binding.calendarRangeTv.text = "${DateTimeUtils.yyyyMMddFormat.format(from.toDate())}" +
                " - " +
                "${DateTimeUtils.yyyyMMddFormat.format(to.toDate())}"
    }

    private fun configEmptyUI() {
        binding.apply {
            correctRateTv.text = "-"
            ratingTv.text = "-"
            problemCntTv.text = "-"
        }
    }

    private fun configUI(from: LocalDate, to: LocalDate) {
        binding.apply {
            if (from > LocalDate.now()) {
                configEmptyUI()
                return
            }
            val period = DateTimeUtils.getPeriod(from, to)
            val formerDate = from.minusDays(period - 1)

            user!!.getAnalysis(this@AnalysisTabActivity, from.toDate(), to.toDate(), formerDate.toDate()) {
                this@AnalysisTabActivity.analysis = it

                if(it?.myScore != null) {
                    correctRateTv.text = "${it.myScore}%"
                } else {
                    correctRateTv.text = "-"
                }

                if(it?.myRating != null) {
                    ratingTv.text = "${it.myRating}등급"
                } else {
                    ratingTv.text = "-"
                }

                if(it?.problemTotalCount != null && it.problemTotalCount != 0) {
                    problemCntTv.text = "${it.problemTotalCount}문제"
                } else {
                    problemCntTv.text = "-"
                }

                analysisTab[tabLayout.selectedTabPosition].onPeriodSelected(dialog.from, dialog.to, dialog.period)

            }

            if (dialog.from.plusDays(dialog.period) > LocalDate.now()) {
                nextBtn.isEnabled = false
                nextBtn.setColorFilter(ContextCompat.getColor(this@AnalysisTabActivity, R.color.grey_c0c0c0))
            } else {
                nextBtn.isEnabled = true
                nextBtn.clearColorFilter()
            }

            setRangeText(from, to)
        }
    }

    private fun showMyAnalysisGuide() {
        binding.apply {
            Tutor.TooltipType.analysisMain.addShowingCnt()
            val window = BalloonWindow(this@AnalysisTabActivity, myAnalysisGuideBtn, BalloonWindow.Position.below, 16.toPx())
            window.balloonColor = ContextCompat.getColor(this@AnalysisTabActivity, R.color.purple_ACACFF)
            window.offset = -100
            window.setPadding(32.toPx(), 32.toPx(), 32.toPx(), 32.toPx());
            val view = LayoutInflater.from(this@AnalysisTabActivity).inflate(R.layout.tooltip_analysis, null)
            var chartContentTv: TextView = view.findViewById(R.id.chartContentTv)
            chartContentTv.text = "같은 등급 친구들에 비해 내가 잘하는 부분 & 더 채워야 할 부분을\n" +
                "보여주는 나만의 맞춤 학습 리포트입니다 :)\n" +
                "내가 푼 문제 중에서도 신뢰도 있는 문항만 선별하여\n" +
                "분석했으니 믿고 살펴보세요!"
            window.show(view)
        }
    }
}

class AnalysisTabView: ConstraintLayout {
    constructor(context: Context): super(context)

    var startBorder: View
    var tabTitleTv: TextView
    var endBorder: View
    init {
        LayoutInflater.from(context).inflate(R.layout.view_analysis_tab, this)
        startBorder = findViewById(R.id.startBorder)
        tabTitleTv = findViewById(R.id.tabTitleTv)
        endBorder = findViewById(R.id.endBorder)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if(selected)
            tabTitleTv.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
        else
            tabTitleTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
    }
}

