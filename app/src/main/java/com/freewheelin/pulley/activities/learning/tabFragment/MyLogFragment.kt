package com.freewheelin.pulley.activities.learning.tabFragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.MockReportActivity
import com.freewheelin.pulley.activities.OMRActivity
import com.freewheelin.pulley.activities.WeeklyTestReportActivity
import com.freewheelin.pulley.activities.WrongTestReportActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment.Companion.REQUEST_MOCK_TEST
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.ContentManager
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.FragmentMyLogBinding
import com.freewheelin.pulley.databinding.ItemLearningTabListBinding
import com.freewheelin.pulley.dialogs.*
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import com.freewheelin.pulley.views.textViews.SortableListener
import com.freewheelin.pulley.views.textViews.SortableTextView
import java.util.*


class MyLogFragment : LearningTabFragment(), SortableListener, DabakTabRadioListener, WrongManageViewListener, ObservableHashSetListener<Content>, MockExamGuideDialogListener {

    override var screenName = "나의학습"
    var contents: List<Content> = listOf()
    var filteredContents: List<Content>? = null
    var checkedContent = ObservableHashSet<Content>()
    lateinit var binding: FragmentMyLogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_log, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyLogFragment()
    }

    override fun onResume() {
        super.onResume()
        if(ContentManager.isNeedToSyncMyContentList)
            syncContentList()
    }
    override fun initUI() {
        if (!::binding.isInitialized) return
        checkedContent.listener = this
        with(binding) {
            studyTypeRadio.labels = listOf("전체", "기본학습", "오답학습")
            pieceTypeRadio.labels = listOf("전체", "테스트", "유형학습", "모의고사")
            finishRadio.labels = listOf("전체", "푼 것", "안 푼 것")

            studyTypeRadio.listener = this@MyLogFragment
            pieceTypeRadio.listener = this@MyLogFragment
            finishRadio.listener = this@MyLogFragment

            rv.adapter = ListAdapter()
            rv.layoutManager = LinearLayoutManager(context)


            wrongManageView.hide(false)
            wrongManageView.listener = this@MyLogFragment
            wrongManageView.hideReviewBtn()
            wrongManageView.makeBtn(WrongManageView.BtnType.mail)

            allCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    checkedContent.addAll(getContentList())
                } else {
                    checkedContent.clear()
                }

                rv.adapter?.notifyDataSetChanged()
            }
            categorySl.listener = this@MyLogFragment
            problemCntSl.listener = this@MyLogFragment
            titleSl.listener = this@MyLogFragment
            scoreSl.listener = this@MyLogFragment
            createDateSl.listener = this@MyLogFragment
            studyDateSl.listener = this@MyLogFragment

            syncContentList()
        }
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
        checkedContent.clear()
        configureAllCheckBoxUI()
        binding.rv.adapter?.notifyDataSetChanged()
        if(ContentManager.isNeedToSyncMyContentList)
            syncContentList()
    }

    private fun syncContentList() {
        ContentManager.getMyContentList(requireContext(), user!!,
                successCB = {
                    this.contents = it
                    checkedContent.clear()
                    filterAndSort()
                    configureAllCheckBoxUI()
                },
                failCB = {}
        )
    }

    private fun configureAllCheckBoxUI() {
        with(binding) {
            allCheckBox.setOnCheckedChangeListener(null)
            allCheckBox.isChecked = (checkedContent.isNotEmpty() && checkedContent.containsAll(getContentList()))
            allCheckBox.setOnCheckedChangeListener { button, isChecked ->
                if(isChecked) {
                    checkedContent.addAll(getContentList())
                } else {
                    checkedContent.clear()
                }

                rv.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun filterAndSort() {
        var filteredList = contents
        with(binding) {
            filteredList = when(studyTypeRadio.selectedIndex) {
                1 -> filteredList.filter { !it.isDerivedContent() }
                2 -> filteredList.filter { it.isDerivedContent() }
                else -> filteredList
            }

            filteredList = when(pieceTypeRadio.selectedIndex) {
                1 -> filteredList.filter { it.getPieceCategory().contains(PieceCategory.dailyTest) }
                2 -> filteredList.filter { it.getPieceCategory().contains(PieceCategory.book) }
                3 -> filteredList.filter { it.getPieceCategory().contains(PieceCategory.mockExam) }
                else -> filteredList
            }

            filteredList = when(finishRadio.selectedIndex) {
                1 -> filteredList.filter { it.markedNumber > 0 }
                2 -> filteredList.filter { it.markedNumber == 0 }
                else -> filteredList
            }

            if(categorySl.isSelected == true)
                filteredList = when(categorySl.order) {
                    SortableTextView.Order.ascend -> filteredList.sortedBy { it.category.getContentCategoryTitle()}
                    SortableTextView.Order.descend -> filteredList.sortedByDescending { it.category.getContentCategoryTitle() }
                }

            if(problemCntSl.isSelected)
                filteredList = when(problemCntSl.order) {
                    SortableTextView.Order.ascend -> filteredList.sortedBy { it.markedNumber }
                    SortableTextView.Order.descend -> filteredList.sortedByDescending { it.markedNumber }
                }

            if(titleSl.isSelected)
                filteredList = when(titleSl.order) {
                    SortableTextView.Order.ascend -> filteredList.sortedBy { it.subject }
                    SortableTextView.Order.descend -> filteredList.sortedByDescending { it.subject }
                }

            if(scoreSl.isSelected)
                filteredList = when(scoreSl.order) {
                    SortableTextView.Order.ascend -> filteredList.sortedBy { it.score }
                    SortableTextView.Order.descend -> filteredList.sortedByDescending { it.score }
                }

            if(createDateSl.isSelected)
                filteredList = when(createDateSl.order) {
                    SortableTextView.Order.ascend -> filteredList.sortedBy { it.createDateTime }
                    SortableTextView.Order.descend -> filteredList.sortedByDescending { it.createDateTime }
                }

            if(studyDateSl.isSelected)
                filteredList = when(studyDateSl.order) {
                    SortableTextView.Order.ascend -> filteredList.sortedBy { it.solveDateTime }
                    SortableTextView.Order.descend -> filteredList.sortedByDescending { it.solveDateTime }
                }


            filteredContents = filteredList
            rv.adapter?.notifyDataSetChanged()

            if(getContentList().isEmpty())
                emptyGuideTv.visibility = View.VISIBLE
            else
                emptyGuideTv.visibility = View.INVISIBLE
        }
    }

    override fun onStudyBtnClicked(view: WrongManageView) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "나의학습", "오답학습하기")
        val dialogType = WrongManagementDialog.Type.wrongPiece
        val dialog = WrongManagementDialog(requireContext(), dialogType)
        dialog.binding.wrongCntTv.text = "나의학습 ${checkedContent.size}개로 오답학습지를 만듭니다."
        if (checkedContent.size == 1)
            dialog.binding.testTitleTv.text = "'${checkedContent.first().subject}'"
        else
            dialog.binding.testTitleTv.text = "'${checkedContent.first().subject}' 외 ${checkedContent.size - 1}건"

        dialog.show()

        dialog.binding.makeBtn.setOnClickListener {
            dialog.binding.makeBtn.startLoding()
            val cntPerProblem = dialog.cnt
            val isSimilar = dialog.pieceProblemType == WrongManagementDialog.PieceProblemType.custom
            val level = dialog.level

            val isIncludeClearProblem = dialog.isClearInclude

            PieceManager.makeWeakPieceUsingPiece(requireContext(), user!!, checkedContent.toList(), isSimilar, level, cntPerProblem, isIncludeClearProblem,
                    successCB = {
                        dialog.dismiss()

                        if (dialog.binding.checkbox.isChecked) {
                            val intent = SolveActivity.getIntent(requireContext(), it)
                            startActivity(intent)
                        } else {
                            val text: String
                            if (checkedContent.size == 1)
                                text = "'${checkedContent.first().subject}'의 오답관리 문제가 만들어졌습니다."
                            else
                                text = "'${checkedContent.first().subject}' 외 ${checkedContent.size - 1}개의 오답관리 문제가 만들어졌습니다."

                            DaebakToast.show(requireContext(), text)
                        }

                        checkedContent.clear()
                        val mutableContents = ArrayList(contents)
                        mutableContents.add(0, it)
                        this.contents = mutableContents
                        filterAndSort()
                    },
                    failCB = {
                        dialog.dismiss()
                        DaebakToast.showFailedMakePiece(requireContext())
                    }
            )
        }
    }

    override fun onItemChanged(set: ObservableHashSet<Content>) {
        if(set.isEmpty()) {
            binding.wrongManageView.inactive()
            binding.wrongManageView.hide(true)
        } else {
            if (set.size == 1)
                binding.wrongManageView.active("'${set.first().subject}'이 선택되었습니다.")
            else
                binding.wrongManageView.active("'${set.first().subject}' 외 ${set.size - 1}건이 선택되었습니다.")

            binding.wrongManageView.show(true) {
                Tutor.showToolTipIfNeed(binding.wrongManageView.buttons.first(), Tutor.TooltipType.mailInMyStudy)
            }
        }
    }

    inner class ListAdapter: RecyclerView.Adapter<MyLogHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLogHolder {
            val itemBinding: ItemLearningTabListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_learning_tab_list, parent, false)
            return MyLogHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return getContentList().size
        }

        override fun onBindViewHolder(holder: MyLogHolder, position: Int) {
            val content = getContentList()[position]
            holder.set(content)
            holder.checkbox.setOnCheckedChangeListener(null)
            if (checkedContent.contains(content)) {
                holder.checkbox.isChecked = true
                holder.containerCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white_fafafa))
            } else {
                holder.checkbox.isChecked = false
                holder.containerCl.setBackgroundColor(Color.TRANSPARENT)
            }
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    checkedContent.add(content)
                    holder.containerCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white_fafafa))
                } else {
                    checkedContent.remove(content)
                    holder.containerCl.setBackgroundColor(Color.TRANSPARENT)
                }
                configureAllCheckBoxUI()
            }
            holder.itemView.setOnClickListener {
                holder.checkbox.isChecked = !holder.checkbox.isChecked
            }

            holder.solveBtn.setOnClickListener { onSolveBtnClicked(content) }
            holder.reviewBtn.setOnClickListener { onReviewBtnClicked(content) }
            holder.reportBtn.setOnClickListener { onReportBtnClicked(content) }

            if(position == itemCount - 1)
                holder.horizontalBorder.visibility = View.GONE
            else
                holder.horizontalBorder.visibility = View.VISIBLE
        }
    }

    override fun onOrderChanged(view: SortableTextView, order: SortableTextView.Order) {
        with(binding) {
            categorySl.isSelected = false
            problemCntSl.isSelected = false
            titleSl.isSelected = false
            scoreSl.isSelected = false
            studyDateSl.isSelected = false
            createDateSl.isSelected = false

            view.isSelected = true
            filterAndSort()
        }
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        checkedContent.clear()
        configureAllCheckBoxUI()
        filterAndSort()
    }

    override fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = OMRActivity.getIntent(requireContext(), mockExam, makeNew)
        startActivityForResult(intent, REQUEST_MOCK_TEST)
    }

    override fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean) {
        val intent = SolveActivity.getIntent(requireContext(), mockExam, makeNew)
        startActivityForResult(intent, REQUEST_MOCK_TEST)

    }

    private fun getContentList(): List<Content> {
        return filteredContents ?: contents
    }

    private fun onSolveBtnClicked(content: Content) {
        when(content.category) {
            PieceCategory.mockExam -> {
                val exam = MockExam(content)
                MockExamGuideDialog(requireContext(), exam, true, this).show()
            }
            PieceCategory.book -> {
                val intent = SolveActivity.getIntent(requireContext(), Book(content))
                startActivity(intent)
            }

            PieceCategory.note -> {
                val intent = SolveActivity.getIntent(requireContext(), Piece(content))
                startActivity(intent)
            }

            PieceCategory.dailyTest -> {
                LogUtils.assert(false, "unexpected case: onSolveBtn Clicked type: ${content.category}")
                DialogUtils.showIndevelopingErr(requireContext())
            }
            else -> {}
        }
    }

    private fun onReviewBtnClicked(content: Content) {
        when(content.category) {
            PieceCategory.mockExam -> {
                val intent = SolveActivity.getReviewIntent(requireContext(), MockExam(content))
                startActivity(intent)

            }
            PieceCategory.book -> {
                val intent = SolveActivity.getReviewIntent(requireContext(), Book(content))
                startActivity(intent)
            }
            PieceCategory.note -> {
                val intent = SolveActivity.getReviewIntent(requireContext(), Piece(content))
                startActivity(intent)
            }

            PieceCategory.dailyTest -> {
                val intent = SolveActivity.getReviewIntent(requireContext(), Test(content))
                startActivity(intent)
            }
            else -> {
                LogUtils.assert(false, "예상치 못한 카테고리 ${content.category}")
            }
        }
    }

    private fun onReportBtnClicked(content: Content) {


        when(content.category) {
            PieceCategory.mockExam -> {
                val intent = MockReportActivity.getIntent(requireContext(), MockExam(content))
                startActivity(intent)
            }
            PieceCategory.dailyTest -> {
                val test = Test(content)
                when(test.getTestType()) {
                    Test.TestType.weekly ->  {
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
                LogUtils.assert(false, "예상치 못한 카테고리 ${content.category}")
            }
        }
    }

    override fun onMailBtnClicked(view: WrongManageView) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "나의학습", "메일")
        val dialog = EmailInputDialog(requireContext(), checkedContent.toList(), user!!, object: EmailInputDialogListener {
            override fun onSendEmailBtnClicked() {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "나의학습", "메일 보내기")
            }
            override fun onSentEmail() {
                checkedContent.clear()
                binding.allCheckBox.isChecked = false
                binding.rv.adapter?.notifyDataSetChanged()
                DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
            }
        })

        dialog.show()
    }
}

class MyLogHolder(val itemBinding: ItemLearningTabListBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val checkbox = itemBinding.checkBox
    val studyTypeTv = itemBinding.studyTypeTv
    val problemCntTv = itemBinding.problemCntTv
    val titleTv = itemBinding.titleTv
    val newTag = itemBinding.newTag
    val createDateTv = itemBinding.createDateTv
    val studyDateTv = itemBinding.studyDateTv
    val solveBtn = itemBinding.solveBtn
    val scoreTv = itemBinding.scoreTv
    val reportBtn = itemBinding.reportBtn
    val reviewBtn = itemBinding.reviewBtn
    val horizontalBorder = itemBinding.horizontalBorder
    val containerCl = itemBinding.containerCl

    init {
        checkbox.extensionTouchArea(24.toPx())
    }

    fun set(content: Content) {
        setCategoryText(content)
        problemCntTv.text = getProblemCntText(content)
        titleTv.text = content.subject
        scoreTv.text = getScoreText(content)
        createDateTv.text = DateTimeUtils.mMDashddFormat.format(content.createDateTime)
        studyDateTv.text = getSolvedDateText(content.solveDateTime)
        configureNewTag(content)
        configureBtnByProgress(content)

    }

    private fun getProblemCntText(content: Content): String {
        val similarProblemCnt = content.similarProblemNumber
        val originCnt = content.markedNumber

        return if(similarProblemCnt == 0) originCnt.toString() else "${originCnt}(+${similarProblemCnt})"
    }

    private fun getSolvedDateText(date: Date?): String {
        return if(date == null)
            return "-"
        else
            DateTimeUtils.mMDashddFormat.format(date)
    }

    private fun getScoreText(content: Content): String {
        return if(content.isCompleted())
            "${content.score}점"
        else
            "-"
    }

    private fun configureNewTag(content: Content) {
        val today = Date()

        if(DateTimeUtils.getDayDifferences(content.createDateTime, today) < 3) {
            newTag.visibility = View.VISIBLE
        } else {
            newTag.visibility = View.INVISIBLE
        }
    }

    private fun setCategoryText(content: Content) {
        if(content.isDerivedContent()) {
            studyTypeTv.setTextColor(ContextCompat.getColor(itemBinding.root.context, R.color.purple_6D6DFF))
        } else {
            studyTypeTv.setTextColor(ContextCompat.getColor(itemBinding.root.context, R.color.black_4c4c4c))
        }
        studyTypeTv.text = content.category.getContentCategoryTitle()
    }

    private fun configureBtnByProgress(content: Content) {
        if(content.markedNumber > 0) {
            if(content.category == PieceCategory.mockExam && content.isCompleted() == false)
                reviewBtn.visibility = View.INVISIBLE
            else
                reviewBtn.visibility = View.VISIBLE

        } else {
            if (content.similarProblemNumber > 0)
                reviewBtn.visibility = View.VISIBLE
            else
                reviewBtn.visibility = View.INVISIBLE
        }


        if(content.isCompleted() == false) {
            solveBtn.visibility = View.VISIBLE

            if(content.markedNumber == 0)
                solveBtn.text = "풀기"
            else
                solveBtn.text = "이어 풀기"
        } else {
            solveBtn.visibility = View.INVISIBLE
        }

        if(content.isCompleted() && content.isDerivedContent() == false) {

            if(content.category == PieceCategory.mockExam || content.category == PieceCategory.dailyTest)
                reportBtn.visibility = View.VISIBLE
            else
                reportBtn.visibility = View.INVISIBLE

        } else {
            reportBtn.visibility = View.INVISIBLE
        }
    }
}

