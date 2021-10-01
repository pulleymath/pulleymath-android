package com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component.FilterType.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.assets.Subject
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.core.manage.ProblemManager
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.dialogs.NoteDetailDialog
import com.freewheelin.pulley.dialogs.WrongManagementDialog
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.PieceCategory
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.WrongManageView
import com.freewheelin.pulley.views.WrongManageViewListener
import com.google.android.material.tabs.TabLayout
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_note_detail.*
import kotlinx.android.synthetic.main.dialog_wrong_management.*
import kotlinx.android.synthetic.main.fragment_wrong_note.*
import kotlinx.android.synthetic.main.item_note_contents_header.view.*
import kotlinx.android.synthetic.main.item_note_contents_problem.view.*
import kotlinx.android.synthetic.main.view_wrong_manage.view.*
import org.joda.time.LocalDate
import java.util.*
import kotlinx.android.synthetic.main.item_note_contents_header.view.guideTv as emptyGuideTv

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

    var wrongNoteFilterFragment: NoteFilterFragment = NoteFilterFragment.newInstance(getWrongNoteFilterAndTitle())
    var scrapNoteFilterFragment: NoteFilterFragment = NoteFilterFragment.newInstance(getScrapBookFilterAndTitle())

    var wrongProblems: List<Problem>? = null
    var scrapProblems: List<Problem>? = null
    var groupedProblemsByOrder: List<Pair<String, List<Problem>>> = listOf()

    override var screenName = "오답노트"

    lateinit var changeRecevier: BroadcastReceiver

    val from: LocalDate
        get() {
            return selectedFragment.from
        }

    val to: LocalDate
        get() {
            return selectedFragment.to
        }

    val selectedFragment: NoteFilterFragment
        get() {
            return if(viewPager.currentItem == 0) wrongNoteFilterFragment else scrapNoteFilterFragment
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

        changeRecevier = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                onFragmentSelected()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(changeRecevier, IntentFilter(ProblemManager.EVENT_WRONG_NOTE_CHANGED))
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(wrongNoteFilterFragment.isAdded && scrapNoteFilterFragment.isAdded) {
            childFragmentManager.putFragment(outState, WRONG_PROBLEM_FRAG, wrongNoteFilterFragment)
            childFragmentManager.putFragment(outState, SCRAP_PROBLEM_FRAG, wrongNoteFilterFragment)
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(changeRecevier)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wrong_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedProblem.listener = this
    }

    override fun onAllSelectedClicked(isChecked: Boolean) {
        val problems = groupedProblemsByOrder.flatMap { it.second }
        if(isChecked)
            selectedProblem.addAll(problems)
        else
            selectedProblem.removeAll(problems)

        if(problems.isNotEmpty())
            contentsRv.adapter?.notifyDataSetChanged()
    }

    fun onProblemCheckBoxChanged(isChecked: Boolean, problem: Problem, position: Int) {
        if (isChecked) {
            selectedProblem.add(problem)
        } else {
            selectedProblem.remove(problem)
        }
        contentsRv.adapter?.notifyItemChanged(position)
    }

    override fun onOrderBtnClicked(type: OrderType) {
        selectedOrder = type
        setGroupedProblem(tabLayout.selectedTabPosition)
        contentsRv.adapter?.notifyDataSetChanged()
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
        val tabPosition = tabLayout.selectedTabPosition
        if (tabPosition == 0) {
            ProblemManager.getWrongProblems(requireContext(), user!!, from.toDate(), to.toDate()) {
                wrongProblems = it
                setGroupedProblem(tabPosition)
            }
        } else {
            ProblemManager.getScrapProblems(requireContext(), user!!, from.toDate(), to.toDate()) {
                scrapProblems = it
                setGroupedProblem(tabPosition)
            }
        }
    }

    override fun onDateSet(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type) {

        val tabPosition = tabLayout.selectedTabPosition

        val fromDate = when(type) {
            DateRangePickerDialog.Type.RECENT7 -> LocalDate.now().minusDays(6)
            DateRangePickerDialog.Type.RECENT14 -> LocalDate.now().minusDays(13)
            DateRangePickerDialog.Type.RECENT30 -> LocalDate.now().minusDays(29)
            else -> from
        }
        selectedFragment.from = fromDate

        val toDate = when(type) {
            DateRangePickerDialog.Type.RECENT7, DateRangePickerDialog.Type.RECENT14, DateRangePickerDialog.Type.RECENT30 -> LocalDate.now()
            else -> to
        }
        selectedFragment.to = toDate

        Log.d("날짜설정", "from=$fromDate, to=$toDate")

        if (tabPosition == 0) {
            ProblemManager.getWrongProblems(requireContext(), user!!, fromDate.toDate(), toDate.toDate()) {
                wrongProblems = it
                setGroupedProblem(tabPosition)
            }
        } else if (tabPosition == 1) {
            ProblemManager.getScrapProblems(requireContext(), user!!, fromDate.toDate(), toDate.toDate()) {
                scrapProblems = it
                setGroupedProblem(tabPosition)
            }
        }
    }

    override fun onFilterTypeChanged(fragment: NoteFilterFragment, filters: Set<FilterType>) {
        setGroupedProblem(tabLayout.selectedTabPosition)
    }

    override fun onItemChanged(set: ObservableHashSet<Problem>) {

        if (set.isEmpty()) {
            wrongManageView.inactive()
            wrongManageView.hide(true)
            viewPager.setPaddingBottom(0)

        } else {
            if(tabLayout.selectedTabPosition == 0) {
                wrongManageView.studyWrongBtn.text = "오답학습하기"
            } else {
                wrongManageView.studyWrongBtn.text = "추가학습하기"
            }
            wrongManageView.active("${set.size}문제가 선택되었습니다.")
            wrongManageView.show(true)
            viewPager.setPaddingBottom(64.toPx())
        }

    }

    override fun onStudyBtnClicked(view: WrongManageView) {
        if(tabLayout.selectedTabPosition == 0)
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답학습하기")
        else
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "추가학습하기")

        val dialogType = if(tabLayout.selectedTabPosition == 0) WrongManagementDialog.Type.wrongProblem
                        else WrongManagementDialog.Type.scrap
        val dialog = WrongManagementDialog(requireContext(), dialogType)
        dialog.wrongCnt = selectedProblem.size
        dialog.show()

        dialog.makeBtn.setOnClickListener {
            if(tabLayout.selectedTabPosition == 0)
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "오답 학습지 만들기")
            else
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "즐겨찾기 학습지 만들기")
            dialog.makeBtn.startLoding()
            val problems = selectedProblem.toList()
            val cntPerProblem = dialog.cnt
            val isSimilar = dialog.pieceProblemType == WrongManagementDialog.PieceProblemType.custom
            val level = dialog.level

            val isIncludeClearProblem = dialog.isClearInclude

            PieceManager.makeWeakPiece(requireContext(), user!!, problems, isSimilar, level, cntPerProblem, isIncludeClearProblem,
                    if(dialogType == WrongManagementDialog.Type.wrongProblem) "WRONG_NOTE" else "SCRAP",
                    successCB = {
                        dialog.dismiss()

                        if(dialog.checkbox.isChecked) {
                            val intent = SolveActivity.getIntent(requireContext(), it)
                            startActivity(intent)
                        } else {
                            var text = if(dialogType == WrongManagementDialog.Type.wrongProblem) "오답문제" else "즐겨찾기 문제"
                            text = text + " ${selectedProblem.size}개로 학습지를 만들었습니다."
                            (activity as LearningTabActivity).showSnackBar(text, "바로가기")
                        }
                        selectedProblem.clear()
                        contentsRv.adapter?.notifyDataSetChanged()
                    }, failCB = {
                        dialog.dismiss()
                        DaebakToast.showFailedMakePiece(requireContext())
                    }
            )
        }
    }

    override fun onReviewBtnClicked(view: WrongManageView) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "리뷰하기")
        val subject = if (tabLayout.selectedTabPosition == 0) "오답노트 리뷰" else "즐겨찾기 리뷰"
        val intent = SolveActivity.getReviewIntent(requireContext(), subject, selectedProblem.toList())
        startActivity(intent)
    }

    override fun initUI() {

        viewPager.adapter = TabAdapter(childFragmentManager)
        viewPager.setPagingEnabled(false)

        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)?.customView = TabTextView(requireContext(), "오답노트")
        tabLayout.getTabAt(1)?.customView = TabTextView(requireContext(), "즐겨찾기")

        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                (activity as LearningTabActivity).hideSnackBar()
            }

            override fun onPageSelected(position: Int) {}

        })
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                if(tab.position == 0) {
                    ProblemManager.getWrongProblems(requireContext(), user!!, from.toDate(), to.toDate()) {
                        wrongProblems = it
                        setGroupedProblem(tab.position)
                    }
                }

                if(tab.position == 1) {
                    LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "오답노트", "즐겨찾기")
                    ProblemManager.getScrapProblems(requireContext(), user!!, from.toDate(), to.toDate()) {
                        scrapProblems = it
                        setGroupedProblem(tab.position)
                    }
                }
            }
        })

        wrongManageView.listener = this
        wrongManageView.hide(false)

        val noteAdapter = NoteAdapter()
        noteAdapter.sectionType = SectionType.header

        contentsRv.addItemDecoration(SpaceItemDecoration())


        val spanCount = if (requireContext().is10InchUI) 4 else 3
        contentsRv.layoutManager = GridLayoutManager(context, spanCount, RecyclerView.VERTICAL, false).also {
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

    private fun setGroupedProblem(tabPosition: Int, withSelectedClear: Boolean = true) {
        var problems = filterProblems((if (tabPosition == 0) wrongProblems else scrapProblems) ?: listOf(), tabPosition)

        problems = when(selectedOrder) {
            OrderType.recent -> problems.sortedByDescending { it.updateDateTime }
            OrderType.old -> problems.sortedBy { it.updateDateTime }
            OrderType.level -> problems.sortedBy { it.problemLevel }
            OrderType.subject -> problems.sortedBy { it.unitCode }
        }

        val mapper = LinkedHashMap<String, MutableList<Problem>>()
        for (problem in problems) {
            var headerStr = DateTimeUtils.yyyyMMddFormat.format(problem.updateDateTime ?: Date())
            when(selectedOrder) {
                OrderType.recent -> headerStr = DateTimeUtils.yyyyMMddFormat.format(problem.updateDateTime)
                OrderType.old -> headerStr = DateTimeUtils.yyyyMMddFormat.format(problem.updateDateTime)
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
        contentsRv.adapter?.notifyDataSetChanged()
        contentsRv.scrollToPosition(0)
    }

    private fun filterProblems(problems: List<Problem>, tabPosition: Int): List<Problem> {
        var filteredProblem = problems
        val filters = selectedFragment.selectedFilterTypes

        if(tabPosition == 0) {
            filteredProblem = filteredProblem.filter { LocalDate(it.updateDateTime) in from..to }
            filteredProblem = filteredProblem.filter {
                var clearCondition = false
                if(filters.contains(exceptClear))
                    clearCondition = clearCondition || it.isClear == false

                if(filters.contains(includeClear))
                    clearCondition = true

                clearCondition
            }
            scrapNoteFilterFragment.setFiltersStatus(filters)
        } else {
            filteredProblem = filteredProblem.filter { LocalDate(it.scrapDateTime) in from..to }
            filteredProblem = filteredProblem.filter {
                var correctCondition = false

                if(filters.contains(allViewType))
                    correctCondition = true

                if(filters.contains(correctProblem))
                    correctCondition = (correctCondition || it.getResultByScoring() == Result.correct)

                if(filters.contains(incorrectProblem))
                    correctCondition = (correctCondition || it.getResultByScoring() == Result.incorrect)

                if(filters.contains(notSolvedProblem))
                    correctCondition = (correctCondition || it.getResultByScoring() == Result.yet)

                correctCondition
            }
            wrongNoteFilterFragment.setFiltersStatus(filters)
        }

        filteredProblem = filteredProblem.filter {
            val subject = it.getSubject()
            var subjectCondition = false

            if(filters.contains(과목_전체))
                subjectCondition = true

            if(filters.contains(수학_상))
                subjectCondition = (subjectCondition || subject == Subject.수학_상)

            if(filters.contains(수학_하))
                subjectCondition = (subjectCondition || subject == Subject.수학_하)

            if(filters.contains(math1))
                subjectCondition = (subjectCondition || subject == Subject.수학I)

            if(filters.contains(math2))
                subjectCondition = (subjectCondition || subject == Subject.수학II)

            if(filters.contains(probabilityAndStatistics))
                subjectCondition = (subjectCondition || subject == Subject.확률과통계)

            if(filters.contains(calculus))
                subjectCondition = (subjectCondition || subject == Subject.미적분)

            if(filters.contains(geometry))
                subjectCondition = (subjectCondition || subject == Subject.기하)


            var levelCondition = false

            if(filters.contains(allLevel))
                levelCondition = true

            if(filters.contains(low))
                levelCondition = (levelCondition || it.problemLevel == 1)

            if(filters.contains(middleLow))
                levelCondition = (levelCondition || it.problemLevel == 2)

            if(filters.contains(middle))
                levelCondition = (levelCondition || it.problemLevel == 3)

            if(filters.contains(high))
                levelCondition = (levelCondition || it.problemLevel == 4)

            if(filters.contains(highest))
                levelCondition = (levelCondition || it.problemLevel == 5)

            var pieceCategoryCondition = false

            if(filters.contains(allCategory))
                pieceCategoryCondition = true

            if(filters.contains(test))
                pieceCategoryCondition = (pieceCategoryCondition || it.getPieceCategory().contains(PieceCategory.test))

            if(filters.contains(unitStudy))
                pieceCategoryCondition = (pieceCategoryCondition ||
                        it.getPieceCategory().contains(PieceCategory.book))

            if(filters.contains(mockText))
                pieceCategoryCondition = (pieceCategoryCondition ||
                        it.getPieceCategory().contains(PieceCategory.mockExam))

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

    fun getWrongNoteFilterAndTitle(): List<Pair<String, List<FilterType>>> {
        return listOf(
                Pair("과목", listOf(과목_전체, 수학_상, 수학_하, math1, math2, probabilityAndStatistics, calculus, geometry)),
                Pair("난이도", listOf(allLevel, low, middleLow, middle, high, highest)),
                Pair("카테고리", listOf(allCategory, test, unitStudy, mockText)),
                Pair("보기 설정", listOf(includeClear, exceptClear))
        )
    }

    fun getScrapBookFilterAndTitle(): List<Pair<String, List<FilterType>>> {
        return listOf(
                Pair("과목", listOf(과목_전체, 수학_상, 수학_하, math1, math2, probabilityAndStatistics, calculus, geometry)),
                Pair("난이도", listOf(allLevel, low, middleLow, middle, high, highest)),
                Pair("카테고리", listOf(allCategory, test, unitStudy, mockText)),
                Pair("보기 설정", listOf(allViewType, correctProblem, incorrectProblem, notSolvedProblem))
        )
    }

    inner class NoteAdapter : SectionAdapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(indexPath: IndexPath): Int {
            return when (indexPath.type) {
                Type.header -> {
                    if (indexPath.section == 0)
                        0
                    else
                        1
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
                clearGuideTv.text = "전체 ${problems.size}문제 중 ${problems.filter { it.isClear }.size}개 클리어"
                problemCntTv.text = "${problems.size}개의 문제가 있습니다."

                checkBox.setOnCheckedChangeListener(null)

                checkBox.isChecked = problems.isNotEmpty() && selectedProblem.containsAll(problems)
                checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                    checkBox.isChecked = isChecked
                    listener?.onAllSelectedClicked(isChecked)
                }

                if (problems.isEmpty()) {
                    itemView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    if (viewPager.currentItem == 0) {
                        setWrongEmptyGuide()
                    } else {
                        setTagEmptyGuide()
                    }
                    guideView.visibility = View.VISIBLE
                } else {
                    itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    guideView.visibility = View.GONE
                }
            }

            (holder as? GroupHeaderHolder)?.apply {
                val headerText = groupedProblemsByOrder[section - 1].first
                headerTv.text = headerText
            }

            (holder as? ProblemHolder)?.apply {
                val problem  = groupedProblemsByOrder[section - 1].second[row]
                holder.set(problem)

                checkBox.setOnCheckedChangeListener(null)

                holder.isChecked = selectedProblem.contains(problem)

                view.setOnClickListener {
                    onProblemCheckBoxChanged(!holder.isChecked, problem, getRawPosition(indexPath))
                }

                checkBox.setOnCheckedChangeListener { button, isChecked ->
                    onProblemCheckBoxChanged(!holder.isChecked, problem, getRawPosition(indexPath))
                }

                detailBtn.setOnClickListener {
                    val dialog = NoteDetailDialog(requireContext(), problem, user!!)
                    dialog.nextProblem = this@NoteAdapter.getNextProblem(problem)
                    dialog.prevProblem = this@NoteAdapter.getPrevProblem(problem)
                    dialog.show()
                    dialog.leftArrowIb.setOnClickListener {
                        dialog.nextProblem = dialog.problem
                        dialog.configureUI(dialog.prevProblem!!)
                        dialog.prevProblem = getPrevProblem(dialog.problem)
                    }

                    dialog.rightArrowIb.setOnClickListener {
                        dialog.prevProblem = dialog.problem
                        dialog.configureUI(dialog.nextProblem!!)
                        dialog.nextProblem = getNextProblem(dialog.problem)
                    }

                    dialog.clearBtn.setOnClickListener {
                        val isClear = !dialog.isClear
                        ProblemManager.clear(requireContext(), user!!, dialog.problem, isClear) {
                            dialog.problem.isClear = isClear
                            dialog.problem.clearDateTime = Date()
                            dialog.configureUI(dialog.problem)
                            setGroupedProblem(tabLayout.selectedTabPosition, false)
                            val problems = groupedProblemsByOrder.flatMap { it.second }.toSet()
                            if(problems.contains(dialog.problem) == false)
                                selectedProblem.remove(problem)
                        }
                    }

                    dialog.scrapBtn.setOnClickListener {
                        val isScrap = !dialog.isScrap
                        ProblemManager.scrap(requireContext(), user!!, dialog.problem, isScrap) {
                            dialog.problem.isScrap = isScrap
                            dialog.problem.scrapDateTime = Date()
                            dialog.configureUI(dialog.problem)
                            setGroupedProblem(tabLayout.selectedTabPosition, false)
                            val problems = groupedProblemsByOrder.flatMap { it.second }.toSet()
                            if(problems.contains(dialog.problem) == false)
                                selectedProblem.remove(problem)
                        }
                    }
                }

                if (indexPath.row == 0 && indexPath.section == 1 && viewPager.currentItem == 0 ) {
                    Tutor.showToolTipIfNeed(holder.itemView, Tutor.TooltipType.additionalStudyInWrongNote)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == 0) {
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_note_contents_header, parent, false)
                val holder =  HeaderHolder(view)
                holder.listener = this@WrongNoteFragment
                return holder
            } else if (viewType == 1) {
                val textView = TextView(context)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_4c4c4c))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
                textView.typeface = Theme.regular(requireContext())
                return GroupHeaderHolder(textView)
            } else {
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_note_contents_problem, parent, false)
                val holder = ProblemHolder(view)
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
            }
        }
    }

    private class HeaderHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val clearGuideTv = view.clearGuideTv
        val problemCntTv = view.problemCntTV
        val checkBox: CheckBox = view.findViewById<CheckBox>(R.id.checkBox)
        val recentOrderBtn = view.recentOrder
        val oldOrderBtn = view.oldOrder
        val subjectOrderBtn = view.subjectOrder
        val levelOrderBtn = view.levelOrder

        val guideView = view.guideView
        val guideIv = view.guideIv
        val guideTv = view.emptyGuideTv

        var listener: HeaderHolderListener? = null

        val orderBtns = listOf(recentOrderBtn, oldOrderBtn, subjectOrderBtn, levelOrderBtn)

        var selectedOrder: OrderType = OrderType.recent
            set(value) {
                field = value
                orderBtns.forEach { setOrderBtnUnselected(it) }
                setOrderBtnSelected(orderBtns[value.rawValue])
            }

        init {
            orderBtns.forEach {
                it.setOnClickListener(this)
            }
            checkBox.extensionTouchArea(12.toPx())
        }

        private fun setOrderBtnUnselected(button: Button) {
            button.typeface = Theme.regular(view.context)
        }

        private fun setOrderBtnSelected(button: Button) {
            button.typeface = Theme.bold(view.context)
        }

        fun setWrongEmptyGuide() {
            guideIv.setImageResource(R.drawable.guide_empty_wrong)
            guideTv.text = "오답문제가 이곳에 모여요!\n" +
                    "세상 간편한 오답학습을 경험해보세요 :)"
        }

        fun setTagEmptyGuide() {
            guideIv.setImageResource(R.drawable.guide_empty_tag)
            guideTv.text = "즐겨찾기한 문제가 이곳에 모여요!\n" +
                    "다시 보고 싶거나, 중요하다고 생각한 문제를 모아보세요 :)"
        }

        override fun onClick(view: View) {
            when (view) {
                recentOrderBtn -> selectedOrder = OrderType.recent
                oldOrderBtn -> selectedOrder = OrderType.old
                subjectOrderBtn -> selectedOrder = OrderType.subject
                levelOrderBtn -> selectedOrder = OrderType.level
            }
            listener?.onOrderBtnClicked(selectedOrder)
        }
    }

    private class ProblemHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val problemSdv = view.problemSdv
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val resultIv = view.resultIv
        val detailBtn = view.detailBtn
        val clearIv = view.clearIv
        val tagIv = view.tagIv
        var isChecked: Boolean = false
            set(value) {
                field = value
                if(value) {
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
                } else {
                    view.background = ContextCompat.getDrawable(view.context, R.drawable.bg_white_ffffff_stroke_grey_e8e8e8)
                }
                checkBox.isChecked = value
            }

        fun set(problem: Problem) {
//            problemSdv.setImageURI(problem.getProblemUrl())

//            CoroutineScope(Dispatchers.IO).launch {
//                val problemImage = GlideApp.with(problemSdv).asBitmap().load(problem.getProblemUrl()).submit().get()
//                withContext(Dispatchers.Main) {
//                    problemSdv.setImageBitmap(problemImage)
//                }
//            }

//            GlideApp.with(problemSdv)
//                    .load(problem.getProblemUrl())
//                    .into(problemSdv)

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

            if(problem.isClear)
                clearIv.visibility = View.VISIBLE
            else
                clearIv.visibility = View.GONE

            if(problem.isScrap)
                tagIv.visibility = View.VISIBLE
            else
                tagIv.visibility = View.GONE

        }
    }

    private class GroupHeaderHolder(val headerTv: TextView) : RecyclerView.ViewHolder(headerTv)

//    object PicassoTransformations {
//
//        val targetWidth = 200
//
//        val resizeTransformation = object : Transformation {
//
//            override fun transform(source:Bitmap) : Bitmap {
//                val aspectRatio = source.height / source.width
//                val targetHeight = targetWidth * aspectRatio
//                val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
//                if (result != source) {
//                    source.recycle()
//                }
//                return result
//            }
//
//            override fun key() : String{
//                return "resizeTransformation#" + System.currentTimeMillis()
//            }
//        }
//    }
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
            setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
        } else {
            typeface = Theme.bold(context)
            setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
        }
    }
}
