package com.freewheelin.pulley.revision2023.ui.activity

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityWrongNoteBinding
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote.component.NoteFilterChangeListener
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote.component.NoteFilterFragment
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.token
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.ProblemManager
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.legacy.dialogs.NoteDetailDialog
import com.freewheelin.pulley.legacy.dialogs.WrongManagementDialog
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.setPaddingBottom
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.utils.autoCloseOnChatBotExit
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.NoteStudyViewListener
import com.freewheelin.pulley.legacy.views.WrongManageView
import com.freewheelin.pulley.legacy.views.snackBar.SnackBar
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarView
import com.freewheelin.pulley.legacy.views.snackBar.SnackBarViewListener
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.NoteStudyProblemWrapper
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.adapter.NoteStudyCardAdapter
import com.freewheelin.pulley.revision2023.utils.listeners.ChatBotClientClickEventListener
import com.freewheelin.pulley.revision2023.utils.listeners.NoteStudyClickListener
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteActViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

enum class OrderType(val rawValue: Int) {
    recent(0),
    old(1),
    subject(2),

    level(3)
}

class WrongNoteActivity : AppCompatActivity(), NoteFilterChangeListener,
    NoteStudyClickListener, NoteStudyViewListener {
    private val binding: ActivityWrongNoteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_wrong_note, null, false)
    }
    private val viewModel: WrongNoteActViewModel by viewModels()

    lateinit var reConfigureReceiver: BroadcastReceiver
    lateinit var noteCardAdapter: NoteStudyCardAdapter
    private var tabFragments: MutableList<Fragment> = mutableListOf()

    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, WrongNoteActivity::class.java).apply {

            }
        }
    }

    private fun changeFilterAndFetchNotes(tabIndex: Int) {
        val vm: WrongNoteActViewModel by viewModels()
        if (tabIndex == 0) {
            if (tabFragments[0] is NoteFilterFragment) {
                (tabFragments[0] as NoteFilterFragment).updateParentFilters()

                vm.fetchWrongNotes()
            }
        } else {
            if (tabFragments[1] is NoteFilterFragment) {
                (tabFragments[1] as NoteFilterFragment).updateParentFilters()
                vm.fetchScrapNotes()
            }
        }
    }
    private fun changeBookFilterAndFetchNotes(tabIndex: Int) {
        val vm: WrongNoteActViewModel by viewModels()
        if (tabIndex == 0) {
            if (tabFragments[0] is NoteFilterFragment) {
                (tabFragments[0] as NoteFilterFragment).updateBookFilters()

                vm.fetchWrongNotes()
            }
        } else {
            if (tabFragments[1] is NoteFilterFragment) {
                (tabFragments[1] as NoteFilterFragment).updateBookFilters()
                vm.fetchScrapNotes()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initReceiver()
        binding.apply {
            lifecycleOwner = this@WrongNoteActivity
            vm = viewModel
//                init()
            initAdapter()
            initChatBot()
            addBackBtnCallback()
//            changeFilterAndFetchNotes(binding.tabLayout.selectedTabPosition)
            backBtn.setOnClickListener {
                finish()
            }
            wrongManageView.studyListener = this@WrongNoteActivity

            tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val tabPosition = tab?.position ?: 0
                    viewModel.tabPosition = tabPosition
                    changeFilterAndFetchNotes(tabPosition)
                    changeBookFilterAndFetchNotes(tabPosition)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
        viewModel.apply {
            fetchCurriculumSubjects()
            errorAction.observe(this@WrongNoteActivity) {
                dialog?.let {
                    it.dismiss()
                    DaebakToast.showFailedMakePiece(this@WrongNoteActivity)
                }
            }
            userInRepo.observe(this@WrongNoteActivity) { user ->
                user?.let {
                    val available = it.serviceType.isTypeEqualOrHigher(PaidServiceType.BASIC_P)
                    showLockIcon.postValue(!available)
                }
            }
            schoolTypeInRepo.observe(this@WrongNoteActivity) {
                tabFragments = mutableListOf(
                    NoteFilterFragment.newWrongInstance().apply { changeListener = this@WrongNoteActivity },
                    NoteFilterFragment.newScrapInstance().apply { changeListener = this@WrongNoteActivity },
                )
                binding.apply {
                    pager.adapter =
                        WrongNoteFilterPagerAdapter(tabFragments, supportFragmentManager, lifecycle)
                    val tabTitles = listOf<String>("오답노트", "즐겨찾기")
                    TabLayoutMediator(tabLayout, pager) { tab, position ->
                        tab.text = tabTitles[position]
                    }.attach()
                }
                this@WrongNoteActivity.init()
            }
            noteWrapper.observe(this@WrongNoteActivity) {
                CoroutineScope(Dispatchers.Main).launch {
                    noteCardAdapter.submitList(it)
                    delay(300)
                    noteCardAdapter.notifyItemChanged(0)
                }
            }
            selectedProblem.observe(this@WrongNoteActivity) {
                binding.apply {
                    if (it.isEmpty()) {
//                            wrongManageView.visibleIf(false)
                        wrongManageView.inactive()
                        wrongManageView.hide(true)
                        pager.setPaddingBottom(0)
                        notesRv.setPaddingBottom(24.toPx())
                    } else {
                        val isWrongNoteFragment = tabLayout.selectedTabPosition == 0
//                            wrongManageView.visibleIf(true)
                        wrongManageView.studyWrongBtn.text = if (isWrongNoteFragment) {
                            "학습지 만들기"
                        } else {
                            "추가 학습하기"
                        }
                        wrongManageView.active("${it.size}문제가 선택되었습니다.")
                        wrongManageView.show(true)
                        pager.setPaddingBottom(64.toPx())
                        notesRv.setPaddingBottom(76.toPx())
                    }
                }

            }
            filterElements.observe(this@WrongNoteActivity) { filter ->
                setGroupedProblem() {}
//                println("aspasp filterElement changed! observer, length : ${filter.size}")
//                filter.forEach { it -> println("isSelected? ${it.name}, ${it.isSelected.get()}")}
//                filter.forEach {
//                    if (it.isSelected.get()) {
//                        println("isSelected? true = ${it.value}")
//                    }
//                }
            }
        }
    }

    private fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.chatBotBgCl.isVisible) {
                binding.chatBotBgCl.visibleIf(false)
                binding.chatBotCv.visibleIf(false)
                binding.chatBotBtn.startLongClickDescAnim()
                return@addCallback
            } else {
                finish()
            }
        }
    }
    private fun initChatBot() {
        binding.apply {
            chatBotBtn.setOnClickListener {
                if (chatBotBgCl.isVisible) {
                    chatBotBgCl.visibleIf(false)
                    chatBotCv.visibleIf(false)
                    chatBotBtn.startLongClickDescAnim()
                }  else {
                    val url = Network.webAppUrl + "/ottway?token=$token&uri=chat-bot"
                    binding.webView.loadUrl(url)
                    chatBotBgCl.visibleIf(true)
                    chatBotCv.visibleIf(true)
                }
            }
            webView.let {
                val onClose = {
                    runOnUiThread {
                        chatBotBgCl.visibleIf(false)
                        chatBotBtn.startLongClickDescAnim()
                    }
                }
                it.autoCloseOnChatBotExit(onClose)
                it.addJavascriptInterface(
                    ChatBotClientClickEventListener (
                        onCloseListener = onClose,
                        errorCloseListener = onClose
                    ), "android")

                it.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
            }
        }
    }

    private fun initReceiver() {
        reConfigureReceiver = object: BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    viewModel.initMyPlanAdapterItem()
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(reConfigureReceiver, IntentFilter(
            UserManager.RE_CONFIGURE_UI)
        )
    }

    private fun initAdapter () {
        binding.apply {

            noteCardAdapter = NoteStudyCardAdapter(viewModel, this@WrongNoteActivity)
            val spanCount = if (this@WrongNoteActivity.isTablet) 4 else 2
            notesRv.layoutManager =
                GridLayoutManager(this@WrongNoteActivity, spanCount, RecyclerView.VERTICAL, false).also {
                    it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            val type = noteCardAdapter.getItemViewType(position)
                            val typeHeader = 0
                            val typeGroupHeader = 1
                            val typeItem = 2
                            return when (type) {
                                typeHeader -> spanCount
                                typeGroupHeader -> spanCount
                                else -> 1
                            }
                        }
                    }
                }
            notesRv.adapter = noteCardAdapter
        }
    }

    fun init() {
        viewModel.init()
        onOrderChanged(viewModel.selectedOrder)
    }

    override fun onResume() {
        super.onResume()
        init()
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reConfigureReceiver)
    }

    inner class WrongNoteFilterPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
    fun updateFilter(filters: Set<LearningFilterType>) {
        viewModel.selectedFilterTypes = filters
    }
    fun updateBookFilter(filterElements: List<BookFilterElement>) {
        viewModel.filterElements.postValue(filterElements)
    }

    override fun onUpdateFilter(filterElements: List<BookFilterElement>) {
        viewModel.filterElements.postValue(filterElements)

    }

    override fun onDateChanged(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type) {
        viewModel.from = from
        viewModel.to = to
        viewModel.datePickerType = type
        when (binding.tabLayout.selectedTabPosition == 0) {
            true -> viewModel.fetchWrongNotes()
            false -> viewModel.fetchScrapNotes()
        }
    }

    override fun onAllSelectedClicked(isChecked: Boolean) {
        viewModel.onAllSelectedClicked(isChecked)
    }

    override fun onCardCheckBoxClicked(isChecked: Boolean, item: NoteStudyProblemWrapper) {
        viewModel.onCardCheckBoxClicked(isChecked, item) { position ->
        }
    }

    override fun onCardItemDetail(item: NoteStudyProblemWrapper) {
        val problem = item.problem ?: return
        val dialog = NoteDetailDialog(this, problem, user!!, viewModel.filterElements.value)
        dialog.nextProblem = viewModel.getNextProblem(problem)
        dialog.prevProblem = viewModel.getPrevProblem(problem)
        dialog.show()
        dialog.binding.leftArrowIb.setOnClickListener {
            dialog.nextProblem = dialog.problem
            dialog.configureUI(dialog.prevProblem!!)
            dialog.prevProblem = viewModel.getPrevProblem(dialog.problem)
        }

        dialog.binding.rightArrowIb.setOnClickListener {
            dialog.prevProblem = dialog.problem
            dialog.configureUI(dialog.nextProblem!!)
            dialog.nextProblem = viewModel.getNextProblem(dialog.problem)
        }

        dialog.binding.clearBtn.setOnClickListener {
            val isClear = !dialog.isClear
            ProblemManager.clear(this, user!!, dialog.problem, isClear) {
                dialog.problem.isClear = isClear
                dialog.problem.rawClearDateTime = LocalDateTime().toString()

                val changedIndex = noteCardAdapter.currentList.indexOfFirst { it.problem == dialog.problem }
                noteCardAdapter.notifyItemChanged(changedIndex)

                dialog.configureUI(dialog.problem)
                viewModel.setGroupedProblem(false) {
                    binding.notesRv.scrollToPosition(0)
                }
//                setGroupedProblem(binding.tabLayout.selectedTabPosition, false)
//                val problems = groupedProblemsByOrder.flatMap { it.second }.toSet()
//                if(problems.contains(dialog.problem) == false)
//                    viewModel.selectedProblem.value?.minus(problem)
            }
        }

        dialog.binding.scrapBtn.setOnClickListener {
            val isScrap = !dialog.isScrap
            ProblemManager.scrap(this, user!!, dialog.problem, isScrap) {
                dialog.problem.isScrap = isScrap
                dialog.problem.rawScrapDateTime = LocalDateTime().toString()
                val changedIndex = noteCardAdapter.currentList.indexOfFirst { it.problem == dialog.problem }
                noteCardAdapter.notifyItemChanged(changedIndex)
                dialog.configureUI(dialog.problem)
                viewModel.setGroupedProblem(false) {
                    binding.notesRv.scrollToPosition(0)
                }
//                val problems = groupedProblemsByOrder.flatMap { it.second }.toSet()
//                if(problems.contains(dialog.problem) == false)
//                    viewModel.selectedProblem.value?.minus(problem)
            }
        }
    }

    override fun onOrderChanged(type: OrderType) {
        viewModel.onOrderChanged(type) {
            viewModel.setGroupedProblem() {
                binding.notesRv.scrollToPosition(0)
            }
        }
    }

    var dialog: Dialog? = null
    override fun onStudyBtnClicked(view: WrongManageView) {
        val selectedProblem = viewModel.selectedProblem.value ?: return
        if(binding.tabLayout.selectedTabPosition == 0)
            LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답학습하기")
        else
            LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "추가학습하기")

        val dialogType = if(binding.tabLayout.selectedTabPosition == 0) WrongManagementDialog.Type.wrongProblem
        else WrongManagementDialog.Type.scrap
        val dialog = WrongManagementDialog(this, dialogType)
        this.dialog = dialog
        dialog.wrongCnt = selectedProblem.size
        dialog.show()

        dialog.binding.makeBtn.setOnClickListener {
            if(binding.tabLayout.selectedTabPosition == 0)
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답 학습지 만들기")
            else
                LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "즐겨찾기 학습지 만들기")
            dialog.binding.makeBtn.setLoading(true)
            val problems = selectedProblem.toList()
            val cntPerProblem = dialog.cnt
            val isSimilar = dialog.pieceProblemType == WrongManagementDialog.PieceProblemType.custom
            val similarStr = if(isSimilar) "SIMILAR" else "SAME"
            val level = dialog.level ?: WrongManagementDialog.Level.normal

            val isIncludeClearProblem = dialog.isClearInclude
            val noteType = if(dialogType == WrongManagementDialog.Type.wrongProblem) "WRONG_NOTE" else "SCRAP"
            viewModel.makeAdvancedLearning(problems, similarStr, level.text, cntPerProblem, isIncludeClearProblem, noteType, cb = {
                dialog.dismiss()
                if(dialog.binding.checkbox.isChecked) {
                    val intent = SolveActivity.getIntent(this, it)
                    startActivity(intent)
                } else {
                    var text = if(dialogType == WrongManagementDialog.Type.wrongProblem) "오답문제" else "즐겨찾기 문제"
                    text += " ${selectedProblem.size}개로 학습지를 만들었습니다."
                    // TODO snackback
                    showSnackBar(text, "바로가기")
                }
                viewModel.selectedProblem.postValue(listOf())

            })
        }
    }

    var snackBar: SnackBar? = null
    fun showSnackBar(text: String, buttonText: String, action: (() -> Unit)? = null) {
        if (snackBar?.isShowing == true) { snackBar?.dismiss() }

        val newSnackBar = snackBar ?: SnackBar(this)
        newSnackBar.setText(text, buttonText)
        newSnackBar.setSnackBarViewListener(object : SnackBarViewListener {
            override fun onXBtnClicked(view: SnackBarView) {
                newSnackBar.dismiss()
            }

            override fun onActionBtnClicked(view: SnackBarView) {
                if (action == null) {
                    finish()
                } else {
                    action()
                }
                newSnackBar.dismiss()

            }
        })

        snackBar = newSnackBar
        newSnackBar.show()
    }

    override fun onReviewBtnClicked(view: WrongManageView) {
        val selectedProblem = viewModel.selectedProblem.value ?: return
        LogUtils.logEvent(this, user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "리뷰하기")
        val subject = if (binding.tabLayout.selectedTabPosition == 0) "오답노트" else "즐겨찾기"
        val intent = SolveActivity.getReviewIntent(this, subject, selectedProblem.toList())
        startActivity(intent)
    }

}