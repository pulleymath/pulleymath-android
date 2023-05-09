package com.freewheelin.pulley.activities.learning.tabFragment.snackTest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestMainBinding
import com.freewheelin.pulley.databinding.FragmentTestMainResultBinding
import com.freewheelin.pulley.databinding.FragmentTestMainUnavailableTestBinding
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.viewmodel.SnackTestFragViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.buttons.ButtonLockImage
import java.util.*

class TestMainDailyFragment : TestMainBaseFragment() {

    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.daily
    lateinit var binding: ViewDataBinding
    val viewModel: SnackTestFragViewModel by viewModels()

    companion object {
        fun newInstance(test: Test?): TestMainDailyFragment {
            val fragment = TestMainDailyFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = if (test == null) DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_unavailable_test, container, false)
        else if (test?.scoringTestPieceCount == 0) DataBindingUtil.inflate(inflater, R.layout.fragment_test_main, container, false)
        else DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_result, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(test == null)
            setUnavailableUI()
        else
            configureUI(test!!)

        viewModel.apply {
            userInRepo.observe(viewLifecycleOwner) { user ->
                test?.let {
                    if (!it.isCompleted()){
                        val showLockIv = user?.serviceType?.isFreeUser == true
                        if (binding is FragmentTestMainResultBinding) {
                            val resultBinding = binding as FragmentTestMainResultBinding
                            if (showLockIv) {
                                resultBinding.startBtn.setLock(ButtonLockImage.mid20)
//                                resultBinding.startBtn.toDisableUI()
                            } else {
                                resultBinding.startBtn.setUnlock()
                                resultBinding.startBtn.toEnableUI()
                            }
                        }

                    }
                }
            }
            recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
                (binding as? FragmentTestMainBinding)?.apply {
                    subjectTv.text = getSubjectNames(subjects)
                }
            }
//            recommendOptionalSubjects.observe(thisOwner) { subjects ->
//
//                binding.apply {
//
//                }
//            }
        }
    }
    private fun getSubjectNames(list: List<RecommendSubject>): String {
        return list
            .filter {
                it.chapters
                    .map { it.isSelected }
                    .reduce { p1, p2 ->
                        p1 || p2
                    }
            }
            .map { it.subjectName }
            .joinTo(StringBuilder(), ", ").toString()

    }
    override fun showMainContents() {

        if(test == null) {
            with(binding as FragmentTestMainUnavailableTestBinding) {
                imageView.show(duration)
                textView.show(duration)
            }
        } else if(test?.scoringTestPieceCount == 0) {
            showInitContents()
        } else if(test?.isCompleted() == false){
            showMiddleContents()
        } else {
            showAllFinishContents()
        }
    }

    private fun showInitContents() {
        with(binding as FragmentTestMainBinding) {
            headerTv.show(duration)
            titleTv.show(duration) {
                val duration = (200 * 2).toLong()
                contentTv.show(duration)
                settingContainerCl.show(duration)
                startBtn.show(duration)
            }
        }
    }

    private fun showMiddleContents() {
        with(binding as FragmentTestMainResultBinding) {
            titleTv.show(duration)
            scoreTv.show(duration)
            reportTv.show(duration)
            scoreLabel.show(duration) {
                val duration = (200 * 2).toLong()
                contentTv.show(duration)
                settingContainerCl.show(duration)
                startBtn.show(duration)
            }
        }
    }

    private fun showAllFinishContents() {
        with(binding as FragmentTestMainResultBinding) {
            giftIv.show(duration) {
                clearTv.show(duration * 2)
                reportTv2.show(duration * 2)
            }

            if(TestManager.isNeedToFullDailyResultInTab) {
                titleTv.show(duration)
                scoreTv.show(duration)
                reportTv.show(duration)
                scoreLabel.show(duration) {
                    val duration = (200 * 2).toLong()
                    contentTv.show(duration)
                    fullClearCl.show(duration)
                }
            }
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        if(test == null) {
            with(binding as FragmentTestMainUnavailableTestBinding) {
                imageView.show(duration)
                textView.show(duration)
            }
        } else if(test?.scoringTestPieceCount == 0) {
            hideInitContents(cb)
        } else if(test?.isCompleted() == false){
            hideMiddleContents(cb)
        } else {
            hideAllFinishContents()
        }
    }

    private fun hideInitContents(cb: () -> Unit) {
        with(binding as FragmentTestMainBinding) {
            headerTv.hide(duration)
            titleTv.hide(duration)
            contentTv.hide(duration)
            settingContainerCl.hide(duration)
            startBtn.hide(duration) {
                cb()
            }
        }
    }

    private fun hideMiddleContents(cb: () -> Unit) {
        with(binding as FragmentTestMainResultBinding) {
            titleTv.hide(duration)
            scoreTv.hide(duration)
            contentTv.hide(duration)
            settingContainerCl.hide(duration)
            reportTv.hide(duration)
            reportTv2.hide(duration)
            startBtn.hide(duration)
            scoreLabel.hide(duration) {
                cb()
            }
        }
    }

    private fun setUnavailableUI() {
        with(binding as FragmentTestMainUnavailableTestBinding) {
            imageView.setImageResource(R.drawable.ic_daily_unavailable)
            textView.text = "데일리 테스트는 평일에만 제공됩니다.\n" +
                "주말에는 주간 테스트를 풀어보세요 :)"
        }
    }

    private fun hideAllFinishContents() {
        with(binding as FragmentTestMainResultBinding) {
            clearTv.hide(duration)
            giftIv.hide(duration)
            reportTv2.hide(duration)
            fullClearCl.hide(duration)
            titleTv.hide(duration)
            scoreTv.hide(duration)
            contentTv.hide(duration)
            settingContainerCl.hide(duration)
            reportTv.hide(duration)
            reportTv2.hide(duration)
            startBtn.hide(duration)
            scoreLabel.hide(duration)
        }
    }

    override fun configureUI(test: Test) {

        Log.d("테스트","===> TestMainDailyFragment")
        user?.log()
//        viewModel.fetchRecommendSubject()

        if(test.scoringTestPieceCount == 0) {
            configureInitUI(test)
        } else if (!test.isCompleted()){
            configureMiddleUI(test)
        } else {
            configureFinishUI(test)
        }
    }

    private fun configureInitUI(test: Test) {
        with(binding as FragmentTestMainBinding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            isGuestUser = user?.serviceType?.isGuestUser == true

            val date = Date()
            contentTv.text = "데일리 테스트는 응시할 때마다 문항이 새로 출제됩니다.\n문항 추천 기준은 아래와 같습니다."
            headerTv.text = "${date.month()}월 ${date.day()}일"
            levelLabel.text = "난이도"
            levelTv.text = viewModel.getRecommendLevelText(test.dailyInfo.testLevel)

            rangeLabel.text ="출제 범위"
            rangeTv.text = viewModel.getRecommendRangeText(test.dailyInfo.testRange)
            settingBtn.visibility = View.GONE
            titleTv.text = test.subject
            startBtn.text = "${test.scoringTestPieceCount + 1}회차 테스트 시작하기"

            subjectTv.text = test.dailyInfo.subjectCode

            startBtn.setOnClickListener {
                listener?.onSolveBtnClicked(test)
            }

            settingBtn.setOnClickListener {
                listener?.onSettingBtnClicked(test)
            }
        }
    }

    private fun configureMiddleUI(test: Test) {
        with(binding as FragmentTestMainResultBinding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            giftContainerCl.visibility = View.INVISIBLE

            titleTv.text = "${test.scoringTestPieceCount}회차 결과"
            scoreTv.text = test.score.toString()
            contentTv.text = "자세한 내용은 보고서에서 확인해주세요 :)\n문항 추천 기준은 아래와 같습니다."
            levelLabel.text = "난이도"
            levelTv.text = viewModel.getRecommendLevelText(test.dailyInfo.testLevel)
            rangeLabel.text ="출제 범위"
            rangeTv.text = viewModel.getRecommendRangeText(test.dailyInfo.testRange)
            startBtn.text = "${test.scoringTestPieceCount + 1}회차 테스트 시작하기"
            reportTv.extensionTouchArea(24.toPx())
            settingBtn.visibility = View.GONE

            subjectTv.text = test.dailyInfo.subjectCode

            startBtn.setOnPaidUserClickListener(cb = {
                listener?.onSolveBtnClicked(test)
            },
            deniedCb = {
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "데일리테스트", "결제유도", "${test.scoringTestPieceCount + 1}회차 테스트 시작하기")
                val dialog = PurchaseGuideDialog()
                childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
            })

            settingBtn.setOnClickListener {
                listener?.onSettingBtnClicked(test)
            }

            reportTv.setOnClickListener { listener?.onReportBtnClicked(test) }
            reportTv2.setOnClickListener { listener?.onReportBtnClicked(test, true) }
        }
    }

    private fun configureFinishUI(test: Test) {
        with(binding as FragmentTestMainResultBinding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            if(TestManager.isNeedToFullDailyResultInTab) {
                scoreTv.text = test.score.toString()
                titleTv.text = "${test.scoringTestPieceCount}회차 결과"
                contentTv.text = "자세한 내용은 보고서에서 확인해주세요 :)\n데일리테스트는 월~금 오전 6시에 공개됩니다!"
                giftContainerCl.visibility = View.INVISIBLE
                settingContainerCl.visibility = View.INVISIBLE
                startBtn.visibility = View.INVISIBLE
                settingBtn.visibility = View.GONE

                TestManager.getDailyTestReport(requireContext(), user!!) {

                    firstScoreTv.text = "${it[0].score}점"
                    secondScoreTv.text = "${it[1].score}점"
                    thirdScoreTv.text = "${it[2].score}점"

                    firstScoreTv.show(duration)
                    secondScoreTv.show(duration)
                    thirdScoreTv.show(duration)
                    firstLabelTv.show(duration)
                    secondLabelTv.show(duration)
                    thirdLabelTv.show(duration)
                    fullClearGuideTv.show(duration)
                    TestManager.isNeedToFullDailyResultInTab = false
                }
            } else {
                giftContainerCl.visibility = View.VISIBLE
            }

            startBtn.setOnClickListener {
                listener?.onSolveBtnClicked(test)
            }

            settingBtn.setOnClickListener {
                listener?.onSettingBtnClicked(test)
            }

            reportTv.setOnClickListener { listener?.onReportBtnClicked(test) }
            reportTv2.setOnClickListener { listener?.onReportBtnClicked(test, true) }
        }
    }
}
