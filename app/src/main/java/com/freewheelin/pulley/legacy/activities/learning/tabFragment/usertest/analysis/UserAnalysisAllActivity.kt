package com.freewheelin.pulley.legacy.activities.learning.tabFragment.usertest.analysis

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.analysis.AnalysisTabDelegate
import com.freewheelin.pulley.legacy.activities.analysis.AnanlysisTabActivityInterface
import com.freewheelin.pulley.legacy.activities.analysis.tabFragment.AnalysisByLevelFragment
import com.freewheelin.pulley.legacy.activities.analysis.tabFragment.AnalysisStudyAmountFragment
import com.freewheelin.pulley.legacy.activities.analysis.tabFragment.AnalysisUnitFragment
import com.freewheelin.pulley.legacy.bases.BaseNavActivity
import com.freewheelin.pulley.legacy.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.ActivityAnalysisTabBinding
import com.freewheelin.pulley.databinding.ViewAnalysisTabBinding
import com.freewheelin.pulley.databinding.TooltipAnalysisBinding
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialogListener
import com.freewheelin.pulley.legacy.model.Analysis
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.extensionTouchArea
import com.freewheelin.pulley.legacy.utils.showBalloon
import com.freewheelin.pulley.legacy.utils.toPx
import com.google.android.material.tabs.TabLayout
import com.freewheelin.pulley.legacy.views.balloonWindow.BalloonWindow
import org.joda.time.LocalDate
import java.util.*


class UserAnalysisAllActivity : BaseNavActivity(),
        TabLayout.OnTabSelectedListener,
        View.OnScrollChangeListener,
        DateRangePickerDialogListener,
        AnanlysisTabActivityInterface {

    lateinit var user: UserV4

    override var analysis: com.freewheelin.pulley.legacy.model.Analysis? = null
    override var notExistDataText = "분석을 위한 학습 내역이 부족해요."
    val binding: ActivityAnalysisTabBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.activity_analysis_tab,
            null,
            false
        )
    }
    val dialog: DateRangePickerDialog by lazy {
        val to = LocalDate.now()
        val from = LocalDate.now().minusDays(6)
        val pickerDialog = DateRangePickerDialog(this, from, to)
        pickerDialog.listener = this
        pickerDialog
    }

    val analysisTab: List<AnalysisTabDelegate> = listOf(
            AnalysisUnitFragment.newInstance(false),
            AnalysisByLevelFragment.newInstance(),
            AnalysisStudyAmountFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        val studentID = intent.getStringExtra(UserAnalysisActivity.KEY_STUDENT_ID)?:"none"

//        user = User()
//        user.studentID = studentID
//        user.firstDate = Date(Date().time - 604800000L)

        configTab(0)
        configTab(1)
        configTab(2)
        replaceFragment(analysisTab[0])

        with(binding) {
            scrollView.setOnScrollChangeListener(this@UserAnalysisAllActivity)

            val to = LocalDate.now()
            val from = LocalDate.now().minusDays(6)
            configUI(from, to)

            tabLayout.addOnTabSelectedListener(this@UserAnalysisAllActivity)

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
        with(binding) {
            tabLayout.addTab(tabLayout.newTab())
            val view = AnalysisTabView(this@UserAnalysisAllActivity)
            val startBorder = view.binding.startBorder
            val endBorder = view.binding.endBorder
            view.binding.tabTitleTv.text = analysisTab[position].tabTitle
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

            val tab = (tabLayout.getChildAt(0) as LinearLayout).getChildAt(position)
            val layoutParam = tab.layoutParams

            layoutParam.width = resources.getDimension(R.dimen.Report_tab_width).toInt()
            tab.setPadding(0,0,0,0)
            tabLayout.getTabAt(position)?.customView = view
        }
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
        with(binding) {
            correctRateTv.text = "-"
            ratingTv.text = "-"
            problemCntTv.text = "-"
        }
    }

    private fun configUI(from: LocalDate, to: LocalDate) {
        if (from > LocalDate.now()) {
            configEmptyUI()
            return
        }
        val period = DateTimeUtils.getPeriod(from, to)
        val formerDate = from.minusDays(period - 1)

        with(binding) {
//            user.getAnalysis(this@UserAnalysisAllActivity, from.toDate(), to.toDate(), formerDate.toDate()) {
//                this@UserAnalysisAllActivity.analysis = it
//
//                if(it?.myScore != null) {
//                    correctRateTv.text = "${it.myScore}%"
//                } else {
//                    correctRateTv.text = "-"
//                }
//
//                if(it?.myRating != null) {
//                    ratingTv.text = "${it.myRating}등급"
//                } else {
//                    ratingTv.text = "-"
//                }
//
//                if(it?.problemTotalCount != null && it.problemTotalCount != 0) {
//                    problemCntTv.text = "${it.problemTotalCount}문제"
//                } else {
//                    problemCntTv.text = "-"
//                }
//
//                analysisTab[tabLayout.selectedTabPosition].onPeriodSelected(dialog.from, dialog.to, dialog.period)
//
//            }

            if (dialog.from.plusDays(dialog.period) > LocalDate.now()) {
                nextBtn.isEnabled = false
                nextBtn.setColorFilter(ContextCompat.getColor(this@UserAnalysisAllActivity, R.color.gray_500))
            } else {
                nextBtn.isEnabled = true
                nextBtn.clearColorFilter()
            }

            setRangeText(from, to)
        }
    }

    private fun showMyAnalysisGuide() {
        Tutor.TooltipType.analysisMain.addShowingCnt()

        val window = BalloonWindow(this, binding.myAnalysisGuideBtn, BalloonWindow.Position.below, 16.toPx())
        window.balloonColor = ContextCompat.getColor(this, R.color.purple_200)
        window.offset = -100
        window.setPadding(32.toPx(), 32.toPx(), 32.toPx(), 32.toPx());
        val tooltipBinding: TooltipAnalysisBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.tooltip_analysis, null, false)
        tooltipBinding.chartContentTv.text = "같은 등급 친구들에 비해 내가 잘하는 부분 & 더 채워야 할 부분을\n" +
                "보여주는 나만의 맞춤 학습 리포트입니다 :)\n" +
                "내가 푼 문제 중에서도 신뢰도 있는 문항만 선별하여\n" +
                "분석했으니 믿고 살펴보세요!"
        window.show(tooltipBinding.root)
    }
}

class AnalysisTabView: ConstraintLayout {
    var binding: ViewAnalysisTabBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_analysis_tab, this, true)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if(selected)
            binding.tabTitleTv.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
        else
            binding.tabTitleTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
    }
}

