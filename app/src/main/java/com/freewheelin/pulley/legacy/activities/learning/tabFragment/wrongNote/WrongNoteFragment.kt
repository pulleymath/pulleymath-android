package com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import android.content.res.Resources
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.legacy.views.balloonWindow.BalloonWindow
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.LearningTabActivity
import com.freewheelin.pulley.legacy.activities.learning.LearningTabFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote.component.NoteFilterFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote.component.NoteFilterFragmentListener
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.is10InchUI
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.core.manage.PieceManager
import com.freewheelin.pulley.legacy.core.manage.ProblemManager
import com.freewheelin.pulley.legacy.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.FragmentWrongNoteBinding
import com.freewheelin.pulley.databinding.ItemNoteContentsHeaderBinding
import com.freewheelin.pulley.databinding.ItemNoteContentsProblemBinding
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.legacy.dialogs.NoteDetailDialog
import com.freewheelin.pulley.legacy.dialogs.WrongManagementDialog
import com.freewheelin.pulley.legacy.lib.ObservableHashSet
import com.freewheelin.pulley.legacy.lib.ObservableHashSetListener
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.Result
import com.freewheelin.pulley.legacy.model.contents.PieceCategory
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.model.LearningFilterType.*
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteFragViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.WrongManageView
import com.freewheelin.pulley.legacy.views.WrongManageViewListener
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.google.android.material.tabs.TabLayout
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import com.squareup.picasso.Picasso
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.*

enum class OrderType(val rawValue: Int) {
    recent(0),
    old(1),
    subject(2),
    level(3)
}

private interface HeaderHolderListener {
    fun onAllSelectedClicked(isChecked: Boolean)
    fun onOrderBtnClicked(type: OrderType)
}

class WrongNoteFragment : LearningTabFragment(),
    HeaderHolderListener,
    NoteFilterFragmentListener,
    ObservableHashSetListener<Problem>,
        WrongManageViewListener {

    var selectedOrder = OrderType.recent
    var selectedProblem: ObservableHashSet<Problem> = ObservableHashSet()

    var wrongNoteFilterFragment: NoteFilterFragment = NoteFilterFragment.newWrongInstance()
    var scrapNoteFilterFragment: NoteFilterFragment = NoteFilterFragment.newScrapInstance()

    var groupedProblemsByOrder: List<Pair<String, List<Problem>>> = listOf()

    lateinit var binding: FragmentWrongNoteBinding
    val viewModel: WrongNoteFragViewModel by viewModels()

    override var screenName = "오답노트"

    lateinit var changeReceiver: BroadcastReceiver

    val selectedFragment: NoteFilterFragment
        get() {
            return if(binding.viewPager.currentItem == 0) wrongNoteFilterFragment else scrapNoteFilterFragment
        }

    companion object {
        @JvmStatic
        fun newInstance() = WrongNoteFragment()
        const val WRONG_PROBLEM_FRAG = "WRONG_PROBLEM_FRAG"
        const val SCRAP_PROBLEM_FRAG = "SCRAP_PROBLEM_FRAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            val wrongBeforeFragment = childFragmentManager.getFragment(savedInstanceState, WRONG_PROBLEM_FRAG) as? NoteFilterFragment
            if(wrongBeforeFragment != null)
                wrongNoteFilterFragment = wrongBeforeFragment

            val scrapBeforeFragment = childFragmentManager.getFragment(savedInstanceState, SCRAP_PROBLEM_FRAG) as? NoteFilterFragment
            if(scrapBeforeFragment != null)
                scrapNoteFilterFragment = scrapBeforeFragment
        }

        changeReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                onFragmentSelected()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(changeReceiver, IntentFilter(ProblemManager.EVENT_WRONG_NOTE_CHANGED))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(wrongNoteFilterFragment.isAdded && scrapNoteFilterFragment.isAdded) {
            childFragmentManager.putFragment(outState, WRONG_PROBLEM_FRAG, wrongNoteFilterFragment)
            childFragmentManager.putFragment(outState, SCRAP_PROBLEM_FRAG, wrongNoteFilterFragment)
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(changeReceiver)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wrong_note, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedProblem.listener = this
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel.apply {
            user.observe(viewLifecycleOwner) { user ->
                user?.let {
                    println("asoaso WrongNoteFrag User Observe")
                    val available = it.serviceType.isTypeEqualOrHigher(PaidServiceType.BASIC_P)
                    showLockIcon.postValue(!available)
                }
            }
            wrongProblem.observe(viewLifecycleOwner) {
                val tabPosition = binding.tabLayout.selectedTabPosition
                setGroupedProblem(tabPosition)
            }
            scrapProblem.observe(viewLifecycleOwner) {
                val tabPosition = binding.tabLayout.selectedTabPosition
                setGroupedProblem(tabPosition)
            }
            updateNotes.observe(viewLifecycleOwner) {
                val tabPosition = binding.tabLayout.selectedTabPosition
                setGroupedProblem(tabPosition)
            }
        }
    }

    override fun onAllSelectedClicked(isChecked: Boolean) {
        val problems = groupedProblemsByOrder.flatMap { it.second }
        if(isChecked)
            selectedProblem.addAll(problems)
        else
            selectedProblem.removeAll(problems)

        if(problems.isNotEmpty())
            binding.contentsRv.adapter?.notifyDataSetChanged()
    }

    fun onProblemCheckBoxChanged(isChecked: Boolean, problem: Problem, position: Int) {
        if (isChecked) {
            selectedProblem.add(problem)
        } else {
            selectedProblem.remove(problem)
        }
        binding.contentsRv.adapter?.notifyItemChanged(position)
    }

    override fun onOrderBtnClicked(type: OrderType) {
        selectedOrder = type
        setGroupedProblem(binding.tabLayout.selectedTabPosition)
        binding.contentsRv.adapter?.notifyDataSetChanged()
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
        val tabPosition = binding.tabLayout.selectedTabPosition
//        if (tabPosition == 0) {
//            viewModel.fetchWrongNotes ()
//        } else {
//            viewModel.fetchScrapNotes ()
//        }
    }

    override fun onDateSet(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type) {

        val tabPosition = binding.tabLayout.selectedTabPosition
        viewModel.selectedFilterTabPosition = binding.tabLayout.selectedTabPosition
//        val fromDate = when(type) {
//            DateRangePickerDialog.Type.RECENT7 -> LocalDate.now().minusDays(6)
//            DateRangePickerDialog.Type.RECENT14 -> LocalDate.now().minusDays(13)
//            DateRangePickerDialog.Type.RECENT30 -> LocalDate.now().minusDays(29)
//            else -> from
//        }
//        viewModel.from = fromDate
//
//        val toDate = when(type) {
//            DateRangePickerDialog.Type.RECENT7, DateRangePickerDialog.Type.RECENT14, DateRangePickerDialog.Type.RECENT30 -> LocalDate.now()
//            else -> to
//        }
//        viewModel.to = toDate

//        Log.d("날짜설정", "from=$fromDate, to=$toDate")

//        if (tabPosition == 0) {
//            ProblemManager.getWrongProblems(requireContext(), user!!, fromDate.toDate(), toDate.toDate()) {
//                wrongProblems = it
//                setGroupedProblem(tabPosition)
//            }
//        } else if (tabPosition == 1) {
//            ProblemManager.getScrapProblems(requireContext(), user!!, fromDate.toDate(), toDate.toDate()) {
//                scrapProblems = it
//                setGroupedProblem(tabPosition)
//            }
//        }
    }

    override fun onFilterTypeChanged(fragment: NoteFilterFragment, filters: Set<LearningFilterType>) {
        setGroupedProblem(binding.tabLayout.selectedTabPosition)
    }

    override fun onItemChanged(set: ObservableHashSet<Problem>) {
        binding.apply {
            if (set.isEmpty()) {
                wrongManageView.inactive()
                wrongManageView.hide(true)
                viewPager.setPaddingBottom(0)

            } else {
                val isWrongNoteFragment = tabLayout.selectedTabPosition == 0
                wrongManageView.studyWrongBtn.text = if (isWrongNoteFragment) {
                    "학습지 만들기"
                } else {
                    "추가 학습하기"
                }
                wrongManageView.active("${set.size}문제가 선택되었습니다.")
                wrongManageView.show(true)
                viewPager.setPaddingBottom(64.toPx())
            }
        }
    }

    override fun onStudyBtnClicked(view: WrongManageView) {
        if(binding.tabLayout.selectedTabPosition == 0)
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답학습하기")
        else
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "추가학습하기")

        val dialogType = if(binding.tabLayout.selectedTabPosition == 0) WrongManagementDialog.Type.wrongProblem
                        else WrongManagementDialog.Type.scrap
        val dialog = WrongManagementDialog(requireContext(), dialogType)
        dialog.wrongCnt = selectedProblem.size
        dialog.show()

        dialog.binding.makeBtn.setOnClickListener {
            if(binding.tabLayout.selectedTabPosition == 0)
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답 학습지 만들기")
            else
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "즐겨찾기 학습지 만들기")
            dialog.binding.makeBtn.setLoading(true)
            val problems = selectedProblem.toList()
            val cntPerProblem = dialog.cnt
            val isSimilar = dialog.pieceProblemType == WrongManagementDialog.PieceProblemType.custom
            val level = dialog.level

            val isIncludeClearProblem = dialog.isClearInclude

            PieceManager.makeWeakPiece(requireContext(), user!!, problems, isSimilar, level, cntPerProblem, isIncludeClearProblem,
                    if(dialogType == WrongManagementDialog.Type.wrongProblem) "WRONG_NOTE" else "SCRAP",
                    successCB = {
                        dialog.dismiss()

                        if(dialog.binding.checkbox.isChecked) {
                            val intent = SolveActivity.getIntent(requireContext(), it)
                            startActivity(intent)
                        } else {
                            var text = if(dialogType == WrongManagementDialog.Type.wrongProblem) "오답문제" else "즐겨찾기 문제"
                            text = text + " ${selectedProblem.size}개로 학습지를 만들었습니다."
                            (activity as MainActivity).showSnackBar(text, "바로가기")
                        }
                        selectedProblem.clear()
                        binding.contentsRv.adapter?.notifyDataSetChanged()
                    }, failCB = {
                        dialog.dismiss()
                        DaebakToast.showFailedMakePiece(requireContext())
                    }
            )
        }
    }

    override fun onReviewBtnClicked(view: WrongManageView) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "리뷰하기")
        val subject = if (binding.tabLayout.selectedTabPosition == 0) "오답노트 리뷰" else "즐겨찾기 리뷰"
        val intent = SolveActivity.getReviewIntent(requireContext(), subject, selectedProblem.toList())
        startActivity(intent)
    }

    override fun initUI() {
        if (!::binding.isInitialized) return
        binding.apply {
            viewPager.adapter = TabAdapter(childFragmentManager)
            viewPager.setPagingEnabled(false)

            tabLayout.setupWithViewPager(viewPager)
            tabLayout.getTabAt(0)?.customView = TabTextView(requireContext(), "오답노트")
            tabLayout.getTabAt(1)?.customView = TabTextView(requireContext(), "즐겨찾기")

            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    (activity as MainActivity).hideSnackBar()
                }

                override fun onPageSelected(position: Int) {
                    // TODO 필터뷰 리로드

                }

            })
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab) {}

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == 0) {
                        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답노트")
//                        viewModel.fetchWrongNotes()
                    } else if (tab.position == 1) {
                        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "즐겨찾기")
//                        viewModel.fetchScrapNotes()
                    }
                }
            })

            wrongManageView.listener = this@WrongNoteFragment
            wrongManageView.hide(false)

            val noteAdapter = NoteAdapter()
            noteAdapter.sectionType = SectionType.header

            contentsRv.addItemDecoration(SpaceItemDecoration())


            val spanCount = if (requireContext().is10InchUI) 4 else 3
            contentsRv.layoutManager =
                GridLayoutManager(context, spanCount, RecyclerView.VERTICAL, false).also {
                    it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            val indexPath = noteAdapter.getIndexPath(position)
                            return if (indexPath.type == Type.header) {
                                spanCount
                            } else {
                                1
                            }
                        }
                    }
                }
            contentsRv.adapter = noteAdapter
        }
    }

    private fun setGroupedProblem(tabPosition: Int, withSelectedClear: Boolean = true) {
        println("qwoqwo tabPosition : ${tabPosition}")
        val selectedTabProblem = viewModel.getSelectedFilterProblem() ?: listOf()
        var problems = filterProblems(selectedTabProblem, tabPosition)
        println("qwoqwo problems size: ${problems.size}")



        problems = when(selectedOrder) {
            OrderType.recent -> problems.sortedByDescending { if(tabPosition == 0) it.updateDateTime else it.scrapDateTime }
            OrderType.old -> problems.sortedBy { if(tabPosition == 0) it.updateDateTime else it.scrapDateTime }
            OrderType.level -> problems.sortedBy { it.problemLevel }
            OrderType.subject -> problems.sortedBy { it.unitCode }
        }

        val mapper = LinkedHashMap<String, MutableList<Problem>>()

        for (problem in problems) {
            var targetTime = if(tabPosition == 0 ) problem.updateDateTime ?: Date() else problem.scrapDateTime ?: Date()

            var headerStr = DateTimeUtils.yyyyMMddFormat.format(targetTime)

            when(selectedOrder) {
                OrderType.recent -> headerStr = DateTimeUtils.yyyyMMddFormat.format(targetTime)
                OrderType.old -> headerStr = DateTimeUtils.yyyyMMddFormat.format(targetTime)
                OrderType.level -> headerStr = problem.getProblemLevel()
                OrderType.subject -> headerStr = problem.getSubject().filterText
            }

            if (mapper[headerStr] == null)
                mapper[headerStr] = mutableListOf(problem)
            else
                mapper[headerStr]?.add(problem)
        }
        groupedProblemsByOrder = mapper.toList()

        if(withSelectedClear)
            selectedProblem.clear()
        binding.contentsRv.adapter?.notifyDataSetChanged()
        binding.contentsRv.scrollToPosition(0)
    }

    private fun filterProblems(problems: List<Problem>, tabPosition: Int): List<Problem> {
        var filteredProblem = problems
        val filters = viewModel.selectedFilterTypes

        val from = viewModel.from
        val to = viewModel.to
        if(tabPosition == 0) {
            filteredProblem = filteredProblem
                .filter { LocalDate(it.updateDateTime) in from..to }
                .filter {
                var clearCondition = false
                if(filters.contains(보기설정_클리어_미포함))
                    clearCondition = clearCondition || it.isClear == false

                if(filters.contains(보기설정_클리어_포함))
                    clearCondition = true

                clearCondition
            }
            //스크랩 필터는 초기화
//            viewModel.setFilterStatus
//            scrapNoteFilterFragment.setFiltersStatus(filters) // TODO 이걸왜함?  동기화작업
        } else {
            filteredProblem = filteredProblem
                .filter { LocalDate(it.scrapDateTime) in from..to }
                .filter {
                var correctCondition = false

                if(filters.contains(보기설정_전체))
                    correctCondition = true

                if(filters.contains(보기설정_맞은문제))
                    correctCondition = (correctCondition || it.getResultByScoring() == Result.correct)

                if(filters.contains(보기설정_틀린문제))
                    correctCondition = (correctCondition || it.getResultByScoring() == Result.incorrect)

                if(filters.contains(보기설정_안_푼_문제))
                    correctCondition = (correctCondition || it.getResultByScoring() == Result.yet)

                correctCondition
            }
//            wrongNoteFilterFragment.setFiltersStatus(filters) // 동기화인듯?
        }

        filteredProblem = filteredProblem.filter {
            val subject = it.getSubject()
            var subjectCondition = false

            if(filters.contains(과목_전체))
                subjectCondition = true

            if(filters.contains(과목_수학_상))
                subjectCondition = (subjectCondition || subject.isMathSang)

            if(filters.contains(과목_수학_하))
                subjectCondition = (subjectCondition || subject.isMathHa)

            if(filters.contains(과목_수학1))
                subjectCondition = (subjectCondition || subject.isMath1)

            if(filters.contains(과목_수학2))
                subjectCondition = (subjectCondition || subject.isMath2)

            if(filters.contains(과목_확통))
                subjectCondition = (subjectCondition || subject.isProbabilityAndStatistics)

            if(filters.contains(과목_미적분))
                subjectCondition = (subjectCondition || subject.isCalculus)

            if(filters.contains(과목_기하))
                subjectCondition = (subjectCondition || subject.isGeometry)


            var levelCondition = false

            if(filters.contains(난이도_전체))
                levelCondition = true

            if(filters.contains(난이도_하)) levelCondition = (levelCondition || it.problemLevel == 1)
            if(filters.contains(난이도_중하)) levelCondition = (levelCondition || it.problemLevel == 2)
            if(filters.contains(난이도_중)) levelCondition = (levelCondition || it.problemLevel == 3)
            if(filters.contains(난이도_상)) levelCondition = (levelCondition || it.problemLevel == 4)
            if(filters.contains(난이도_최상)) levelCondition = (levelCondition || it.problemLevel == 5)


            var pieceCategoryCondition = false

            if(filters.contains(학습유형_전체))
                pieceCategoryCondition = true

            if (filters.contains(학습유형_유형학습))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.book))
            if (filters.contains(학습유형_워크북))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.workbook))
            if (filters.contains(학습유형_모의고사))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.mockExam))
            if (filters.contains(학습유형_오답학습))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.note) || it.getPieceCategory().contains(PieceCategory.reference))
            if (filters.contains(학습유형_테스트))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.dailyTest))
            if (filters.contains(학습유형_추천학습))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.recommned))

            subjectCondition && levelCondition && pieceCategoryCondition
        }

        return filteredProblem
    }

    inner class TabAdapter(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return if(position == 0 )
                wrongNoteFilterFragment
            else
                scrapNoteFilterFragment
        }

        override fun getCount(): Int {
            return 2
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val createdFragment =  super.instantiateItem(container, position) as NoteFilterFragment
            createdFragment.listener = this@WrongNoteFragment
            return createdFragment
        }
    }

//    fun getWrongNoteFilterAndTitle(): List<Pair<String, List<FilterType>>> {
//        return listOf(
//            Pair("과목", listOf(과목_전체, 과목_수학_상, 과목_수학_하, 과목_수학1, 과목_수학2, 과목_확률과통계, 과목_미적분, 과목_기하)),
//            Pair("학습 유형", listOf(모든_학습유형, 유형_유형학습, 유형_워크북, 유형_모의고사, 유형_오답학습, 유형_테스트, 유형_추천학습)),
//            Pair("난이도", listOf(모든_난이도, 난이도_하, 난이도_중하, 난이도_중, 난이도_상, 난이도_최상)),
//            Pair("보기 설정", listOf(클리어_미포함, 클리어_포함))
//        )
//    }
//
//    fun getScrapBookFilterAndTitle(): List<Pair<String, List<FilterType>>> {
//        return listOf(
//            Pair("과목", listOf(과목_전체, 과목_수학_상, 과목_수학_하, 과목_수학1, 과목_수학2, 과목_확률과통계, 과목_미적분, 과목_기하)),
//            Pair("학습 유형", listOf(모든_학습유형, 유형_유형학습, 유형_워크북, 유형_모의고사, 유형_오답학습, 유형_테스트, 유형_추천학습)),
//            Pair("난이도", listOf(모든_난이도, 난이도_하, 난이도_중하, 난이도_중, 난이도_상, 난이도_최상)),
//            Pair("보기 설정", listOf(모든_보기설정, 맞은_문제, 틀린_문제, 안_푼_문제))
//        )
//    }

    inner class NoteAdapter : SectionAdapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(indexPath: IndexPath): Int {
            return when (indexPath.type) {
                Type.header -> {
                    if (indexPath.section == 0) 0
                    else 1
                }
                else -> 2
            }
        }

        override fun numberOfRows(section: Int): Int {
            if(section == 0)
                return 0
            else
                return groupedProblemsByOrder[section - 1].second.size
        }

        override fun numberOfSection(): Int {
            return groupedProblemsByOrder.size + 1
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            val section = indexPath.section
            val row = indexPath.row

            (holder as? HeaderHolder)?.apply {
                val problems = groupedProblemsByOrder.flatMap { it.second }

                this.selectedOrder = this@WrongNoteFragment.selectedOrder

                itemBinding.clearGuideTv.text = "전체 ${problems.size}문제 중 ${problems.filter { it.isClear }.size}개 클리어"
                itemBinding.problemCntTv.text = "${problems.size}개의 문제가 있습니다."

                itemBinding.checkBox.setOnCheckedChangeListener(null)

                itemBinding.checkBox.isChecked = problems.isNotEmpty() && selectedProblem.containsAll(problems)
                itemBinding.checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                    itemBinding.checkBox.isChecked = isChecked
                    listener?.onAllSelectedClicked(isChecked)
                }

                if (problems.isEmpty()) {
                    itemView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    if (binding.viewPager.currentItem == 0) {
                        setWrongEmptyGuide()
                    } else {
                        setTagEmptyGuide()
                    }
                    itemBinding.guideView.visibility = View.VISIBLE
                } else {
                    itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    itemBinding.guideView.visibility = View.GONE
                }
            }

            (holder as? GroupHeaderHolder)?.apply {
                val headerText = groupedProblemsByOrder[section - 1].first
                headerTv.text = headerText
            }

            (holder as? ProblemHolder)?.apply {
                val view = binding.root
                val problem  = groupedProblemsByOrder[section - 1].second[row]
                holder.set(problem)

                itemBinding.checkBox.setOnCheckedChangeListener(null)

                holder.isChecked = selectedProblem.contains(problem)

                view.setOnClickListener {
                    onProblemCheckBoxChanged(!holder.isChecked, problem, getRawPosition(indexPath))
                }

                itemBinding.checkBox.setOnCheckedChangeListener { button, isChecked ->
                    onProblemCheckBoxChanged(!holder.isChecked, problem, getRawPosition(indexPath))
                }

                itemBinding.detailBtn.setOnClickListener {
                    val dialog = NoteDetailDialog(requireContext(), problem, user!!)
                    dialog.nextProblem = this@NoteAdapter.getNextProblem(problem)
                    dialog.prevProblem = this@NoteAdapter.getPrevProblem(problem)
                    dialog.show()
                    dialog.binding.leftArrowIb.setOnClickListener {
                        dialog.nextProblem = dialog.problem
                        dialog.configureUI(dialog.prevProblem!!)
                        dialog.prevProblem = getPrevProblem(dialog.problem)
                    }

                    dialog.binding.rightArrowIb.setOnClickListener {
                        dialog.prevProblem = dialog.problem
                        dialog.configureUI(dialog.nextProblem!!)
                        dialog.nextProblem = getNextProblem(dialog.problem)
                    }

                    dialog.binding.clearBtn.setOnClickListener {
                        val isClear = !dialog.isClear
                        ProblemManager.clear(requireContext(), user!!, dialog.problem, isClear) {
                            dialog.problem.isClear = isClear
                            dialog.problem.rawClearDateTime = LocalDateTime().toString()
                            dialog.configureUI(dialog.problem)
                            setGroupedProblem(binding.tabLayout.selectedTabPosition, false)
                            val problems = groupedProblemsByOrder.flatMap { it.second }.toSet()
                            if(problems.contains(dialog.problem) == false)
                                selectedProblem.remove(problem)
                        }
                    }

                    dialog.binding.scrapBtn.setOnClickListener {
                        val isScrap = !dialog.isScrap
                        ProblemManager.scrap(requireContext(), user!!, dialog.problem, isScrap) {
                            dialog.problem.isScrap = isScrap
                            dialog.problem.rawScrapDateTime = LocalDateTime().toString()
                            dialog.configureUI(dialog.problem)
                            setGroupedProblem(binding.tabLayout.selectedTabPosition, false)
                            val problems = groupedProblemsByOrder.flatMap { it.second }.toSet()
                            if(problems.contains(dialog.problem) == false)
                                selectedProblem.remove(problem)
                        }
                    }
                }

                if (indexPath.row == 0 && indexPath.section == 1 && binding.viewPager.currentItem == 0 ) {
                    Tutor.showToolTipIfNeed(holder.itemView, Tutor.TooltipType.additionalStudyInWrongNote)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == 0) {
                val holder = HeaderHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_note_contents_header, parent, false))
                holder.listener = this@WrongNoteFragment
                return holder
            } else if (viewType == 1) {
                val textView = TextView(context)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_800))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
                textView.typeface = Theme.regular(requireContext())
                return GroupHeaderHolder(textView)
            } else {
                val holder = ProblemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_note_contents_problem, parent, false))
                return holder
            }
        }

        fun getNextProblem(problem: Problem): Problem? {
            val problems = groupedProblemsByOrder.flatMap { it.second }
            val currentIndex = problems.indexOf(problem)
            return problems.getOrNull(currentIndex + 1)
        }

        fun getPrevProblem(problem: Problem): Problem? {
            val problems = groupedProblemsByOrder.flatMap { it.second }
            val currentIndex = problems.indexOf(problem)
            return problems.getOrNull(currentIndex - 1)
        }
    }

    inner class SpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val sectionAdapter = parent.adapter as SectionAdapter
            val indexPath = sectionAdapter.getIndexPath(parent.getChildLayoutPosition(view))

            when (indexPath.type) {
                Type.header -> {
                    val headerSpace = resources.getDimension(R.dimen.dp32)
                    if (indexPath.section != 0)
                        outRect.top = headerSpace.toInt()
                }

                Type.row -> {
                    val space = resources.getDimension(R.dimen.dp24)
                    outRect.top = space.toInt()
                    if(requireContext().is10InchUI) {
                        when {
                            indexPath.row % 4 == 0 -> {
                                outRect.right = (space * (2f / 3f)).toInt()
                            }
                            indexPath.row % 4 == 1 -> {
                                outRect.left = (space * (1f / 3f)).toInt()
                                outRect.right = (space * (1f / 2f)).toInt()
                            }
                            indexPath.row % 4 == 2 -> {
                                outRect.left = (space * (1f / 2f)).toInt()
                                outRect.right = (space * (1f / 3f)).toInt()
                            }
                            indexPath.row % 4 == 3 -> {
                                outRect.left = (space * (2f / 3f)).toInt()
                            }
                        }
                    } else {
                        when {
                            indexPath.row % 3 == 0 -> {
                                outRect.right = (space * (2f / 3f)).toInt()
                            }

                            indexPath.row % 3 == 1 -> {
                                outRect.left = (space * (1f / 3f)).toInt()
                                outRect.right = (space * (1f / 3f)).toInt()
                            }

                            indexPath.row % 3 == 2 -> {
                                outRect.left = (space * (2f / 3f)).toInt()
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private class HeaderHolder(val itemBinding: ItemNoteContentsHeaderBinding) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {
        var listener: HeaderHolderListener? = null
        var orderBtns = listOf<Button>()

        var selectedOrder: OrderType = OrderType.recent
            set(value) {
                field = value
                orderBtns.forEach { setOrderBtnUnselected(it) }
                setOrderBtnSelected(orderBtns[value.rawValue])
            }

        init {
            itemBinding.apply {
                orderBtns = listOf(recentOrder, oldOrder, subjectOrder, levelOrder)
                orderBtns.forEach {
                    it.setOnClickListener(this@HeaderHolder)
                }
                checkBox.extensionTouchArea(12.toPx())
                questionBalloonBtn.setOnClickListener {
                    val context = itemBinding.root.context

                    val balloonWindow = BalloonWindow(context, questionBalloonBtn, BalloonWindow.Position.below, 8.toPx())
                    balloonWindow.balloonColor = ContextCompat.getColor(context, R.color.purple_200)
                    balloonWindow.offset = -120
                    balloonWindow.setPadding(16.toPx())

                    val linearLayout = LinearLayout(context)
                    linearLayout.orientation = LinearLayout.VERTICAL
                    linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                    val titleTvParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    val titleTv = TextView(context)
                    titleTv.layoutParams = titleTvParams
                    titleTv.text = "학습과정순이란?"
                    titleTv.setTextAppearance(R.style.h5)
                    titleTv.typeface = Theme.extraBold(context)
                    titleTv.setTextColor(ContextCompat.getColor(context, R.color.white))

                    val contentTvParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    contentTvParams.topMargin = 12.toPx()
                    val contentTv = TextView(context)
                    contentTv.layoutParams = contentTvParams
                    contentTv.text = "학습과정순은 아래 과목 및 단원순으로 문제집을 정렬하여 표시합니다.\n" +
                        "1. 과목: 수학(상) > 수학(하) > 수학1 > 수학2 > 확률과 통계 >\n 미적분 > 기하순으로 표시\n" +
                        "2. 과목 내 단원: 현행 교육 단원순을 반영하여 표시"
                    contentTv.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,  Resources.getSystem().getDisplayMetrics()), 1.0f);
                    contentTv.setTextAppearance(R.style.mo_h4_sb)
                    contentTv.setTextColor(ContextCompat.getColor(context, R.color.white))

                    linearLayout.addView(titleTv)
                    linearLayout.addView(contentTv)
                    balloonWindow.show(linearLayout)
                }
            }
        }

        private fun setOrderBtnUnselected(button: Button) {
            val view = itemBinding.root
            button.typeface = Theme.regular(view.context)
        }

        private fun setOrderBtnSelected(button: Button) {
            val view = itemBinding.root
            button.typeface = Theme.bold(view.context)
        }

        fun setWrongEmptyGuide() {
            itemBinding.apply {
                guideIv.setImageResource(R.drawable.guide_empty_wrong)
                guideTv.text = "오답문제가 이곳에 모여요!\n" +
                    "간편한 오답학습을 경험해보세요 :)"
            }
        }

        fun setTagEmptyGuide() {
            itemBinding.apply {
                guideIv.setImageResource(R.drawable.guide_empty_tag)
                guideTv.text = "즐겨찾기한 문제가 이곳에 모여요!\n" +
                    "다시 보고 싶거나, 중요하다고 생각한 문제를 모아보세요 :)"
            }
        }

        override fun onClick(view: View) {
            itemBinding.apply {

                when (view) {
                    recentOrder -> selectedOrder = OrderType.recent
                    oldOrder -> selectedOrder = OrderType.old
                    subjectOrder -> selectedOrder = OrderType.subject
                    levelOrder -> selectedOrder = OrderType.level
                }
                listener?.onOrderBtnClicked(selectedOrder)
            }
        }
    }

    private class ProblemHolder(val itemBinding: ItemNoteContentsProblemBinding): RecyclerView.ViewHolder(itemBinding.root) {
        var isChecked: Boolean = false
            set(value) {
                field = value
                val view = itemBinding.root
                if(value) {
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.bg_white_stroke_purple_300)
                } else {
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.bg_white_stroke_gray_300)
                }
                itemBinding.checkBox.isChecked = value
            }

        fun set(problem: Problem) {
            itemBinding.apply {
                Picasso.get()
                    .load(problem.getProblemUrl())
                    .fit()
                    .centerInside()
                    .into(problemSdv)

                if (problem.getResultByScoring() == Result.correct) {
                    resultIv.visibility = View.VISIBLE
                    resultIv.setImageResource(R.drawable.ic_result_correct)
                } else if (problem.getResultByScoring() == Result.incorrect) {
                    resultIv.visibility = View.VISIBLE
                    resultIv.setImageResource(R.drawable.ic_result_incorrect)
                } else if (problem.getResultByScoring() == Result.yet)
                    resultIv.visibility = View.GONE

                if (problem.isClear)
                    clearIv.visibility = View.VISIBLE
                else
                    clearIv.visibility = View.GONE

                if (problem.isScrap)
                    tagIv.visibility = View.VISIBLE
                else
                    tagIv.visibility = View.GONE
            }
        }
    }

    private class GroupHeaderHolder(val headerTv: TextView) : RecyclerView.ViewHolder(headerTv)

}


private class TabTextView: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context, title: String): super(context) {
        this.text = title
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if(selected) {
            typeface = Theme.extraBold(context)
            setTextColor(ContextCompat.getColor(context, R.color.purple_300))
        } else {
            typeface = Theme.bold(context)
            setTextColor(ContextCompat.getColor(context, R.color.gray_800))
        }
    }
}
