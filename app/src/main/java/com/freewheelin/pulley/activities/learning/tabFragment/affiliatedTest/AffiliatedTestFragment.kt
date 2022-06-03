package com.freewheelin.pulley.activities.learning.tabFragment.affiliatedTest

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedTestViewModel
import androidx.databinding.BindingAdapter
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.FragmentAffiliatedTestBinding
import com.freewheelin.pulley.databinding.ItemAffiliatedTestBinding
import com.freewheelin.pulley.revision2021.activity.AffiliatedTestSolveActivity
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestCard
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AffiliatedTestFragment: LearningTabFragment() {
    companion object {
//        const val SHOW_REPORT = "SHOW_REPORT"
        const val SHOW_REPORT_INT = 1000
        fun newInstance(): AffiliatedTestFragment {
            return AffiliatedTestFragment()
        }
    }

    private val viewModel: AffiliatedTestViewModel by viewModels()

    override var screenName = "KU진단"
    override fun initUI() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var binding: FragmentAffiliatedTestBinding
    private lateinit var solveResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_affiliated_test, container, false)

        solveResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == SHOW_REPORT_INT) {
                val workbook = it.data?.getSerializableExtra(AffiliatedTestSolveActivity.SELECTED_WORKBOOK) as? AffiliatedTestWorkbook ?: return@registerForActivityResult
                showReportDialog(workbook)
            }
        }

        bindingUI()
        return binding.root
    }

    private fun bindingUI () {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            val adapter = UnivTestAdapter(viewModel)
            testListRv.adapter = adapter
            uuiTv.movementMethod = ScrollingMovementMethod()

            reportBtn.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "테스트", "보고서-보기")
                val workbookId = viewModel.selectedUnivTestCard.value?.selectedWorkbook?.id ?: return@setOnClickListener
                val version = viewModel.selectedUnivTestCard.value?.selectedWorkbook?.version ?: return@setOnClickListener
                val card = viewModel.selectedUnivTestCard.value ?: return@setOnClickListener
                AffiliatedTestReportDialog(requireContext(), workbookId, version, card.selectedWorkbook).show()
            }

            webLinkTv.setOnClickListener {
                IntentUtils.openWebLink(requireContext(), URL.건국대_시험_로그인, requireContext().packageManager)
            }
            webLinkTv.text = webLinkTv.text
                .partialUnderline("웹으로 시험 응시하기") {
                    // 동작 안해서 걍 setOnClickListener 달아놓음
                    // 이거 왜 동작을 안하지?
                }
                .partialFontAndColored(Theme.bold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_6D6DFF), "웹으로 시험 응시하기")

            testStartBtn.setOnClickListener { view ->
                viewModel.selectedUnivTestCard.value?.let {
                    if (it.isTestEnable(viewModel.currentTimeString.value)) {
                        val selectedWorkbook = it.selectedWorkbook
                        val testStartedAt = it.selectedWorkbook.started_at
                        if (selectedWorkbook.seq == 1 || !testStartedAt.isNullOrEmpty()) {
                            val intent = AffiliatedTestSolveActivity.getIntent(requireContext(), it)
                            solveResultLauncher.launch(intent)
                        } else {
                            DialogUtils.v2AffiliatedTestStartWarningDialog(requireContext()) {
                                val intent = AffiliatedTestSolveActivity.getIntent(requireContext(), it.selectedWorkbook )
                                solveResultLauncher.launch(intent)
                            }
                        }

                    } else {
                        DaebakToast.show(requireContext(), "시험시작 30분 전부터 입장할 수 있습니다.")
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
//        syncTestList()
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
        viewModel.fetchUnivTestGroup {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.affiliatedTestCardList.value?.forEach {
                    when {
                        it.thirdWorkbook.isFinished() -> {
                            it.remainingTimeText.set("")
                            it.isCompleted.set(true)
                        }
                        it.secondWorkbook.isFinished() -> {
                            when {
                                it.thirdWorkbook.isTestNotStartedYet() -> {
                                    it.remainingTimeText.set("자가진단 1차 완료")
                                }
                                else -> {
                                    it.remainingTimeText.set("자가진단 2차 진행중")
                                }
                            }
                        }
                        it.firstWorkbook.isFinished() -> {
                            when {
                                it.secondWorkbook.isTestNotStartedYet() -> {
                                    it.remainingTimeText.set("자가진단 1차 진행 가능")
                                }
                                else -> {
                                    it.remainingTimeText.set("자가진단 1차 진행중")
                                }
                            }
                        }
                        else -> {
//                    val testStartedAt = "2022-02-05 19:50:26"
                            val testStartedAt = it.firstWorkbook.test_started_at ?: return@postDelayed
                            setTestStartCountDownTimer(it, testStartedAt)

//                    val testFinishedAt = "2022-02-05 19:51:26"
                            val testFinishedAt = it.firstWorkbook.test_finished_at ?: return@postDelayed
                            setTestFinishCountDownTimer(it, testFinishedAt)
                        }
                    }
                }
            }, 100)
        }


    }

    private fun setTestStartCountDownTimer(card: AffiliatedTestCard, timeStr: String) {
        val timeDiffMilli = getTimeDiffMilli(timeStr)

        val remainingTimerInStartTime = object : CountDownTimer(timeDiffMilli, 1000) {
            override fun onTick(diff: Long) {
                val day = diff / 1000 / 3600 / 24
                val hour = (diff / 1000 / 3600) - (day * 24)
                val min = (diff / 1000 / 60) - (hour * 60) - (day * 24 * 60)

                val hourStr = if (hour.toString().length < 2) "0$hour" else hour.toString()
                val minStr = if (min.toString().length < 2) "0$min" else min.toString()
                card.remainingTimeText.set("사전진단 시작까지 ${day}일 ${hourStr}시간 ${minStr}분 남음")
            }
            override fun onFinish() {
                card.remainingTimeText.set("사전진단 진행 중")
            }
        }.start()
        remainingTimerListInStartTime.add(remainingTimerInStartTime)
    }
    private fun setTestFinishCountDownTimer(card: AffiliatedTestCard, timeStr: String) {
        val finishedTimeDiffMilli = getTimeDiffMilli(timeStr)

        val remainingTimerInFinishedTime = object : CountDownTimer(finishedTimeDiffMilli, 1000) {
            override fun onTick(diff: Long) {}
            override fun onFinish() {
                card.remainingTimeText.set("사전진단평가가 종료되었습니다.")
            }
        }.start()
        remainingTimerListInFinishedTime.add(remainingTimerInFinishedTime)
    }

    private fun getTimeDiffMilli (timeStr: String): Long {
        val paredDate = sdf.parse(timeStr)
        viewModel.currentTimeString.value?.let { serverTime ->
            val parsedCurrentSerevrDate = sdf.parse(serverTime)
            return paredDate.time - parsedCurrentSerevrDate.time
        }
        var nowDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        return paredDate.time - nowDate.time.time
    }

    fun getCardOnGroupId(groupId: Int): AffiliatedTestCard? {
        return viewModel.affiliatedTestCardList.value?.filter { it.groupId == groupId}?.get(0)
    }
    fun showReportDialog(workbook: AffiliatedTestWorkbook) {
        val workbookId = workbook.id
        val version = workbook.version
        AffiliatedTestReportDialog(requireContext(), workbookId, version, workbook).show()
    }

    inner class UnivTestAdapter(private val viewModel: AffiliatedTestViewModel) :
        ListAdapter<AffiliatedTestCard, AffiliatedTestHolder>(
            DiffCallback<AffiliatedTestCard>()
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AffiliatedTestHolder {
            return AffiliatedTestHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_affiliated_test, parent, false))
        }

        override fun onBindViewHolder(holder: AffiliatedTestHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    interface AffiliatedTestItemClickListener {
        fun onItemClick(ut: AffiliatedTestCard)
    }

    inner class AffiliatedTestHolder(val binding: ItemAffiliatedTestBinding):
        RecyclerView.ViewHolder(binding.root),
        AffiliatedTestItemClickListener {

        fun bind(item: AffiliatedTestCard) {
            binding.listener = this
            binding.item = item
            binding.vm = viewModel
        }
        override fun onItemClick(ut: AffiliatedTestCard) {
            viewModel.selectCard(ut)
        }
    }
    interface TestStartClickListener {
        fun onItemClick()
    }
}
@BindingAdapter("bind_affiliated_test_list")
fun bindItem(recyclerView: RecyclerView, item: List<AffiliatedTestCard>?) {
    item?.let { workbookList ->
        val adapter = recyclerView.adapter as AffiliatedTestFragment.UnivTestAdapter
        adapter.submitList(workbookList.toMutableList())
    }
}

@BindingAdapter("android:src")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}