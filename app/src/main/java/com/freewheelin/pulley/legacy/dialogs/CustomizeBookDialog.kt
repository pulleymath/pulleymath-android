package com.freewheelin.pulley.legacy.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemCommercialListBinding
import com.freewheelin.pulley.databinding.ItemCommercialPageBinding
import com.freewheelin.pulley.databinding.ItemCommercialPageProblemBinding
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBookPage
import com.freewheelin.pulley.legacy.core.manage.BookManager
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.legacy.lib.ObservableHashSet
import com.freewheelin.pulley.legacy.lib.ObservableHashSetListener
import com.freewheelin.pulley.legacy.model.CurriculumSubject
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.hide
import com.freewheelin.pulley.legacy.utils.setOnPremiumClickListener
import com.freewheelin.pulley.legacy.utils.show
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.DabakTabRadioListener
import com.freewheelin.pulley.legacy.views.DaebakTabRadio
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.textViews.SortableListener
import com.freewheelin.pulley.legacy.views.textViews.SortableTextView
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.view.CommonButton
import com.freewheelin.pulley.revision2023.ui.view.TertiaryButton

interface CustomizeBookDialogListener {
    fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book)
    fun onDeniedUser()
    fun onGuestUser()
}

class CustomizeBookDialog : Dialog, DabakTabRadioListener, SortableListener,
    ObservableHashSetListener<CommercialBookPage> {
    var step = 1
        set(value) {
            field = value
            if(value == 1) {
                cancelBtn.text = "취소"
                actionBtn.text = "다음"
                actionBtn.layoutParams.width = 90.toPx()
                selectGuideLabel.visibility = View.VISIBLE
                nowCheckbox.visibility = View.GONE

                val isAvailable = user?.serviceType?.isTypeEqualOrHigher(PaidServiceType.PREMIUM) == true
                if (!isWorkbookStartChallengeInProgress && !isAvailable) {
                    actionBtn.showStartIcon(true)
                    actionBtn.showEndIcon(false)
                } else {
                    actionBtn.showStartIcon(false)
                    actionBtn.showEndIcon(true)
                }

            } else {
                if (step == 2)
                    cancelBtn.text = "이전"

                actionBtn.text = "워크북 만들기"
                actionBtn.layoutParams.width = 120.toPx()
                selectGuideLabel.visibility = View.INVISIBLE
                nowCheckbox.visibility = View.VISIBLE
                actionBtn.showEndIcon(false)
            }
        }

    var selectedPage: Int? = null
    var checkedPageProblem: ObservableHashSet<CommercialBookPage> = ObservableHashSet()
    var commercialBooks: List<CommercialBook>? = null
    var selectedBook: CommercialBook? = null
        set(value) {
            field = value
            if(step == 1 && value != null) {
                actionBtn.isEnabled = true
                selectedBookTv.text = "선택한 문제집 : [${field?.subject}] ${field?.bookName}"
                selectGuideLabel.text = "'${value.bookName}' 문제집이 선택되었습니다."
            } else
                actionBtn.isEnabled = false
        }

    var pages: Map<Int, List<CommercialBookPage>>? = null
    var problemPerCnt = 1

    var repeatUpdateHandler = Handler()
    var autoIncrement = false
    var autoDecrement = false
    var delayHandler = Handler()
    var listener: CustomizeBookDialogListener? = null
    var curriculumSubjects: List<CurriculumSubject> = listOf()

    // 체크박스 사용 막
    var blockCheck = false

    var isWorkbookStartChallengeInProgress = false
    constructor(context: Context, isStartChallengeInProgress: Boolean, listener: CustomizeBookDialogListener?): super(context) {
        setContentView(R.layout.dialog_book_customize)
        // fullscreen dialog
        if(!context.isTablet) {
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
        isWorkbookStartChallengeInProgress = isStartChallengeInProgress
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        TestManager.getAllSubjects(context) { curriculumSubjects ->
            this.curriculumSubjects = curriculumSubjects
            initUI()
            this.listener = listener
            step = 1
        }
    }

    constructor(context: Context, listener: CustomizeBookDialogListener?, book: Book): super(context) {
        setContentView(R.layout.dialog_book_customize)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        TestManager.getAllSubjects(context) { curriculumSubjects ->
            this.curriculumSubjects = curriculumSubjects
            initUI()
            selectedBook = CommercialBook(book)
            step1Container.visibility = View.INVISIBLE
            step2Container.visibility = View.VISIBLE
            initStep2()
            step = 1
            this.listener = listener
        }
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        val subjects = listOf("전체") + curriculumSubjects
            .filter { it.schoolType == schoolType.toString() }
            .sortedBy { it.seq }
            .map { it.name }
        val subject = if(index == 0) null else subjects[index]

        LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-과목선택",  subject ?: "전체")
        sync(subject)
    }

    override fun onOrderChanged(view: SortableTextView, order: SortableTextView.Order) {
        subjectSl.isSelected = false
        bookSl.isSelected = false
        publisherSl.isSelected = false

        view.isSelected = true
        sort()
        bookListRv.adapter?.notifyDataSetChanged()
    }

    private fun sort() {
        var list = commercialBooks
        if(subjectSl.isSelected) {
            val subjectNameList = curriculumSubjects
                .sortedBy { it.seq }
                .map { it.name }
            val indexComparatorAscend =
                Comparator { cbook1: CommercialBook, cbook2: CommercialBook ->
                    subjectNameList.indexOf(cbook1.subject) - subjectNameList.indexOf(cbook2.subject)
                }
            val indexComparatorDescend =
                Comparator { cbook1: CommercialBook, cbook2: CommercialBook ->
                    subjectNameList.indexOf(cbook2.subject) - subjectNameList.indexOf(cbook1.subject)
                }

            list = when (subjectSl.order) {
                SortableTextView.Order.ascend -> list?.sortedWith(indexComparatorAscend)
                SortableTextView.Order.descend -> list?.sortedWith(indexComparatorDescend)
            }
        }

        if(bookSl.isSelected) {
            list = when(bookSl.order) {
                SortableTextView.Order.ascend -> list?.sortedBy { it.bookName }
                SortableTextView.Order.descend -> list?.sortedByDescending { it.bookName }
            }
        }

        if(publisherSl.isSelected) {
            list = when(publisherSl.order) {
                SortableTextView.Order.ascend -> list?.sortedBy { it.publisher }
                SortableTextView.Order.descend -> list?.sortedByDescending { it.publisher }
            }
        }
        commercialBooks = list

    }

    override fun onItemChanged(set: ObservableHashSet<CommercialBookPage>) {
        problemRv.adapter?.notifyDataSetChanged()
        pageRv.adapter?.notifyDataSetChanged()

        if(set.size == 0) {
            delayHandler.removeCallbacksAndMessages(null)
            setProblemCnt(0)
        } else
            syncProblemCnt()
    }

    private fun initUI() {
//        setCancelable(false)
        initComponents()
        step = 1
        val subjectNames = listOf("전체") + curriculumSubjects
            .filter { it.schoolType == schoolType.toString() }
            .sortedBy { it.seq }
            .map { it.name }

        subjectTab.labels = subjectNames
        subjectTab.listener = this
        step2Container.visibility = View.GONE
        nowCheckbox.visibility = View.GONE
        bookListRv.adapter = CommercialAdapter()
        bookListRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bookEmptyTv.visibility = View.INVISIBLE
        challengeStampIv.visibility = if (isWorkbookStartChallengeInProgress) View.VISIBLE else View.GONE
        nowCheckbox.isEnabled = !isWorkbookStartChallengeInProgress
        val color = if (isWorkbookStartChallengeInProgress) R.color.gray_700_opa_30 else R.color.gray_800
        nowCheckbox.setTextColor(ContextCompat.getColor(context, color))

        actionBtn.isEnabled = false




        actionBtn.setOnPremiumClickListener(cb = {
            onActionBtnClicked()
        }, deniedCb = {
            if (user?.serviceType?.isGuestUser == true) {
                dismiss()
                listener?.onGuestUser()
            } else if (isWorkbookStartChallengeInProgress) {
                onActionBtnClicked()
            } else {
//                val dialog = PurchaseGuideDialog()
//                val fm = (context as AppCompatActivity).supportFragmentManager
//                fm.let { dialog.show(it, "purchaseGuideDialog")}
//                DialogUtils.confirmDialog(context, "[테스트]구독중이 아닙니다.", "여기서 또 다이얼로그 나와도 괜찮음? ")
                dismiss()
                listener?.onDeniedUser()
            }
        })

        cancelBtn.setOnClickListener {
            onCancelBtnClicked()
        }

        nowCheckbox.setOnCheckedChangeListener { _, isCheck ->
            val itemValue = if(isCheck) "체크" else "안체크"
            LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-바로풀기체크", itemValue)
        }
        subjectSl.listener = this
        subjectSl.order = SortableTextView.Order.ascend

        bookSl.listener = this
        bookSl.isSelected = true
        publisherSl.listener = this

        sync(null)
    }

    private fun sync(subject: String?) {
        BookManager.getCommercialBook(context) {
            if (subject != null) {
                this.commercialBooks = it?.filter { it.subject == subject }
            } else {
                this.commercialBooks = it
            }
            sort()
            bookListRv.adapter?.notifyDataSetChanged()

            bookEmptyTv.visibleIf(this.commercialBooks?.isEmpty() == true)
        }
    }

    private fun onActionBtnClicked() {
        if(!actionBtn.isEnabled) return

        when(step) {
            1 -> {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-과목선택다음")
                actionBtn.isEnabled = false
                this.step = 2
                val animDu = 75L
                step1Container.hide(duration = animDu) {
                    step2Container.visibility = View.VISIBLE
//                    step2Header.show(duration = animDu)
                    step2Body.show(duration = animDu)
                }
                initStep2()
            }
            else -> {
                val problemPerCnt = problemPerCnt
                val containClear = includeRb.isChecked
                val level = when (levelRg.checkedRadioButtonId) {
                    R.id.easyRb -> WrongManagementDialog.Level.easier
                    R.id.originRb -> WrongManagementDialog.Level.normal
                    else -> WrongManagementDialog.Level.harder
                }
//                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-만들기",
//                        "문제수: ${problemPerCnt}\n"+
//                        "난이도: ${level.eventValue}\n"+
//                        "클리어: ${if(containClear) "포함" else "미포함"}")
                BookManager.makeCustomBook(
                        context,
                        user!!,
                        selectedBook!!,
                        checkedPageProblem,
                        problemPerCnt,
                        containClear,
                        level) {

                    this.dismiss()
                    listener?.onMadeCustomBook(this, it)

                    if(nowCheckbox.isChecked) {
                        val intent = SolveActivity.getIntent(context, it)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initStep2() {
        checkedPageProblem.listener = this
        problemPerCnt = 1
        checkedPageProblem.clear()
        cntImpossibleCl.visibility = View.INVISIBLE
        selectedPage = null
        pages = null
        setProblemCnt(0)
        levelRg.setOnCheckedChangeListener { _, _ ->
            syncProblemCnt()
        }
        clearRg.setOnCheckedChangeListener { _, _ ->
            syncProblemCnt()
        }


        plusBtn.setOnClickListener {
            increment()
            syncProblemCnt()
        }

        minusBtn.setOnClickListener {
            decrement()
            syncProblemCnt()
        }

        plusBtn.setOnLongClickListener {
            autoIncrement = true
            repeatUpdateHandler.post(RptUpdater())
            false
        }

        plusBtn.setOnTouchListener { _, motionEvent ->
            if ((motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) && autoIncrement) {
                autoIncrement = false
                syncProblemCnt()
            }
            false
        }

        minusBtn.setOnLongClickListener {
            autoDecrement = true
            repeatUpdateHandler.post(RptUpdater())
            false
        }

        minusBtn.setOnTouchListener{ _, motionEvent ->
            if ((motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) && autoDecrement) {
                autoDecrement = false
                syncProblemCnt()
            }
            false
        }
        allCheckBox.setOnCheckedChangeListener(null)
        allCheckBox.isChecked = false
        allCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val problems = pages?.get(selectedPage)
            if(problems != null) {
                if (isChecked)
                    checkedPageProblem.addAll(problems)
                else
                    checkedPageProblem.removeAll(problems)
            }
        }
        
        BookManager.getCommercialBookPage(context, selectedBook!!) { list ->
            this.pages = list?.groupBy { it.page }
            pageRv.adapter = PageAdapter()
            pageRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            selectedPage = this.pages?.toList()?.getOrNull(0)?.first
            problemRv.adapter = ProblemAdapter()
            problemRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun onCancelBtnClicked() {
        when(step) {
            1, 0 -> {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-과목선택취소")
                dismiss()
            }
            2 -> {
                val animDu = 75L
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-과목선택이전")
                this.step = 1
                actionBtn.isEnabled = true
                step2Container.hide(duration = animDu) {
                    step1Container.visibility = View.VISIBLE
                    step1Header.show(duration = animDu)
                    step1Body.show(duration = animDu)
                }
            }
        }
    }

    override fun onBackPressed() {
        onCancelBtnClicked()
    }

    inner class PageAdapter: RecyclerView.Adapter<PageHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
            val itemBinding: ItemCommercialPageBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_commercial_page, parent, false)
            return PageHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return pages?.toList()?.size ?: 0
        }

        override fun onBindViewHolder(holder: PageHolder, position: Int) {
            val page = pages!!.toList()[position]
            holder.set(page.first)
            if(selectedPage == page.first) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_100))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            val problems = page.second
            holder.checkIv.visibility = View.INVISIBLE
            for(problem in problems) {
                if(checkedPageProblem.contains(problem)) {
                    holder.checkIv.visibility = View.VISIBLE
                    break
                }
            }

            holder.itemView.setOnClickListener {
                selectedPage = page.first
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_100))
                notifyDataSetChanged()
                problemRv.scrollTo(0,0)
                problemRv.adapter?.notifyDataSetChanged()
                configAllCheckBox(problems)
            }
        }
    }

    private fun configAllCheckBox(problems: List<CommercialBookPage>) {
        allCheckBox.setOnCheckedChangeListener(null)
        allCheckBox.isChecked = checkedPageProblem.containsAll(problems)
        allCheckBox.setOnCheckedChangeListener { compoundButton, isChecked ->
            val problems = pages?.get(selectedPage!!)
            if(problems != null) {
                if (isChecked) {
                    // 더이상 클릭할 수 없을 때 메시지
                    if(blockCheck) {
                        compoundButton.isChecked = false // false 로 변
                        showBlockMessage()
                        return@setOnCheckedChangeListener
                    }
                    checkedPageProblem.addAll(problems)
                } else
                    checkedPageProblem.removeAll(problems)
            }
        }
    }

    private fun showBlockMessage() {
        DaebakToast.show(context, "출제 가능한 문제수를 초과하였습니다.", bottomOffset = 64.toPx(), overDialog=true)
    }

    inner class ProblemAdapter: RecyclerView.Adapter<ProblemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemHolder {
            val itemBinding: ItemCommercialPageProblemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_commercial_page_problem, parent, false)
            return ProblemHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            val problems = pages?.get(selectedPage)
            return problems?.size ?: 0
        }

        override fun onBindViewHolder(holder: ProblemHolder, position: Int) {

            val page = pages!!.get(selectedPage)!!
            val problem = page[position]

            holder.set(problem, position)



            holder.checkbox.setOnCheckedChangeListener(null)
            holder.checkbox.isChecked = checkedPageProblem.contains(problem)

            if(holder.checkbox.isChecked) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_100))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            holder.checkbox.setOnCheckedChangeListener { compoundButton, isChecked ->
                if(isChecked) {
                    // 더이상 클릭할 수 없을 때 메시지
                    if(blockCheck) {
                        compoundButton.isChecked = false // false 로 변
                        showBlockMessage()
                        return@setOnCheckedChangeListener
                    }
                    checkedPageProblem.add(problem)
                }else
                    checkedPageProblem.remove(problem)

                configAllCheckBox(page)
            }

            holder.itemView.setOnClickListener {
                holder.checkbox.isChecked = !holder.checkbox.isChecked
                configAllCheckBox(page)
            }
        }
    }

    fun syncProblemCnt() {
        actionBtn.setLoading(true)
        delayHandler.removeCallbacksAndMessages(null)
        if(checkedPageProblem.isEmpty()) {
            setProblemCnt(0)
        } else {
            val problemPerCnt = problemPerCnt
            val containClear = includeRb.isChecked
            val level = when (levelRg.checkedRadioButtonId) {
                R.id.easyRb -> WrongManagementDialog.Level.easier
                R.id.originRb -> WrongManagementDialog.Level.normal
                else -> WrongManagementDialog.Level.harder
            }
            BookManager.getCommercialSimilarProblemCnt(context, user!!,
                    selectedBook!!.pieceID,
                    checkedPageProblem.toList(),
                    problemPerCnt,
                    containClear,
                    level) {
                if(it != null) {
                    delayHandler.postDelayed({setProblemCnt(it)}, 500)

                }
            }
        }
    }

    private fun setProblemCnt(cnt: Int) {
        totalCntTv.text = "$cnt"
        val isUserServiceTypePremium = user?.serviceType == PaidServiceType.PREMIUM
        if (isWorkbookStartChallengeInProgress && !isUserServiceTypePremium) {
            when (cnt) {
                0 -> {
                    totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                    cntImpossibleCl.visibility = View.INVISIBLE
                    blockCheck = false
                    actionBtn.isEnabled = false
                }
                in 1 .. 5 -> {
                    totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
                    cntImpossibleCl.visibility = View.INVISIBLE
                    blockCheck = false
                    actionBtn.isEnabled = true
                }
                else -> {
                    totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                    cntImpossibleCl.visibility = View.VISIBLE
                    cntImpossibleTv.text = "스타트 챌린지에서는 최대 5문제까지 만들 수 있습니다.\n범위를 다시 선택해주세요."
                    actionBtn.isEnabled = false
                    blockCheck = true
                }
            }
        } else if(cnt in 1 .. 100) {
            totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
            cntImpossibleCl.visibility = View.INVISIBLE
            blockCheck = false
            actionBtn.isEnabled = true
        } else {
            totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
            cntImpossibleTv.text = "최대 100문제까지 만들 수 있습니다."
            if(cnt == 0) {
                cntImpossibleCl.visibility = View.INVISIBLE
                blockCheck = false
            } else {
                cntImpossibleCl.visibility = View.VISIBLE
                blockCheck = true
            }
            actionBtn.isEnabled = false
        }
        actionBtn.setLoading(false)
    }

    inner class CommercialAdapter: RecyclerView.Adapter<CommercialBookHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommercialBookHolder {
            val itemBinding: ItemCommercialListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_commercial_list, parent, false)
            return CommercialBookHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return commercialBooks?.size ?: 0
        }

        override fun onBindViewHolder(holder: CommercialBookHolder, position: Int) {
            val book = commercialBooks!![position]
            holder.set(book)

            if(book == selectedBook) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_100))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }
            holder.itemView.setOnClickListener {
                selectedBook = book
                notifyDataSetChanged()
            }
        }
    }

    fun increment() {
        if (problemPerCnt < 5)
            problemPerCnt += 1
        cntTv.text = problemPerCnt.toString()
    }

    fun decrement() {
        if (problemPerCnt > 1)
            problemPerCnt -= 1
        cntTv.text = problemPerCnt.toString()
    }
    inner class RptUpdater(private var delay: Long = 300) : Runnable {

        override fun run() {
            val postDelay = if (delay < 0)  50 else delay
            if (autoIncrement) {
                increment()
                repeatUpdateHandler.postDelayed(RptUpdater(delay - 150), postDelay)
            } else if (autoDecrement) {
                decrement()
                repeatUpdateHandler.postDelayed(RptUpdater(delay - 150), postDelay)
            }
        }
    }


    lateinit var subjectTab: DaebakTabRadio
    lateinit var step1Container: LinearLayout
    lateinit var step2Container: LinearLayout
    lateinit var nowCheckbox: CheckBox
    lateinit var bookListRv: RecyclerView
    lateinit var problemRv: RecyclerView
    lateinit var pageRv: RecyclerView
    lateinit var actionBtn: CommonButton
    lateinit var bookEmptyTv: TextView
    lateinit var cancelBtn: TertiaryButton

    lateinit var subjectSl: SortableTextView
    lateinit var bookSl: SortableTextView
    lateinit var publisherSl: SortableTextView

    lateinit var selectedBookTv: TextView
    lateinit var selectGuideLabel: TextView
    lateinit var step2Body: ConstraintLayout
    lateinit var levelRg: RadioGroup
    lateinit var clearRg: RadioGroup
    lateinit var includeRb: RadioButton
    lateinit var excludeRb: RadioButton
    lateinit var cntImpossibleCl: ConstraintLayout
    lateinit var cntImpossibleTv: TextView
    lateinit var step1Header: ConstraintLayout
    lateinit var step1Body: ConstraintLayout
    lateinit var cntImpossibleIv: ImageView
    lateinit var plusBtn: ImageButton
    lateinit var minusBtn: ImageButton
    lateinit var challengeStampIv: ImageView
    lateinit var allCheckBox: CheckBox
    lateinit var totalCntTv: TextView
    lateinit var cntTv: TextView

    private fun initComponents() {

        subjectTab = findViewById(R.id.subjectTab)
        step1Container = findViewById(R.id.step1Container)
        step2Container = findViewById(R.id.step2Container)
        nowCheckbox = findViewById(R.id.nowCheckbox)
        bookListRv = findViewById(R.id.bookListRv)
        problemRv = findViewById(R.id.problemRv)
        pageRv = findViewById(R.id.pageRv)
        actionBtn = findViewById(R.id.actionBtn)
        bookEmptyTv = findViewById(R.id.bookEmptyTv)
        cancelBtn = findViewById(R.id.cancelBtn)
        subjectSl = findViewById(R.id.subjectSl)
        bookSl = findViewById(R.id.bookSl)
        publisherSl = findViewById(R.id.publisherSl)
        selectedBookTv = findViewById(R.id.selectedBookTv)
        selectGuideLabel = findViewById(R.id.selectGuideLabel)
        step2Body = findViewById(R.id.step2Body)
        levelRg = findViewById(R.id.levelRg)
        clearRg = findViewById(R.id.clearRg)
        includeRb = findViewById(R.id.includeRb)
        excludeRb = findViewById(R.id.excludeRb)
        cntImpossibleCl = findViewById(R.id.cntImpossibleCl)
        cntImpossibleTv = findViewById(R.id.cntImpossibleTv)
        cntImpossibleIv = findViewById(R.id.cntImpossibleIv)
        plusBtn = findViewById(R.id.plusBtn)
        minusBtn = findViewById(R.id.minusBtn)
        challengeStampIv = findViewById(R.id.challengeStampIv)
        allCheckBox = findViewById(R.id.allCheckBox)
        step1Header = findViewById(R.id.step1Header)
        step1Body = findViewById(R.id.step1Body)
        totalCntTv = findViewById(R.id.totalCntTv)
        cntTv = findViewById(R.id.cntTv)
    }
}

class CommercialBookHolder(val itemBinding: ItemCommercialListBinding): RecyclerView.ViewHolder(itemBinding.root) {

    fun set(commercialBook: CommercialBook) {
        itemBinding.apply {
            subjectTv.text = commercialBook.subject
            bookTv.text = commercialBook.bookName
//            bookSeriesTv.text = commercialBook.bookTag
            publisherTv.text = commercialBook.publisher
            hasBestTag = commercialBook.tag == CommercialBook.Tag.Best
            hasNewTag = commercialBook.tag == CommercialBook.Tag.New
        }
    }
}

class ProblemHolder(val itemBinding: ItemCommercialPageProblemBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val checkbox = itemBinding.checkbox
    val pageTitleTv = itemBinding.pageTitleTv

    fun set(problem: CommercialBookPage, position: Int) {
        checkbox.text = problem.problemNumber
        pageTitleTv.text = problem.title
        pageTitleTv.visibleIf(itemBinding.root.context.isTablet && position == 0)
    }
}

class PageHolder(val itemBinding: ItemCommercialPageBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val checkIv = itemBinding.checkIv
    val pageTv = itemBinding.pageTv

    fun set(page: Int) {
        pageTv.text = "${page}p"
    }
}