package com.freewheelin.pulley.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.core.API.ResponseModel.CommercialBookPage
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.manage.BookManager
import com.freewheelin.pulley.databinding.ItemCommercialListBinding
import com.freewheelin.pulley.databinding.ItemCommercialPageBinding
import com.freewheelin.pulley.databinding.ItemCommercialPageProblemBinding
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.buttons.ButtonLockImage
import com.freewheelin.pulley.views.buttons.PrimaryButton
import com.freewheelin.pulley.views.buttons.SecondaryButton
import com.freewheelin.pulley.views.textViews.SortableListener
import com.freewheelin.pulley.views.textViews.SortableTextView

interface CustomizeBookDialogListener {
    fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book)
    fun onDeniedUser()
}

class CustomizeBookDialog : Dialog, DabakTabRadioListener, SortableListener, ObservableHashSetListener<CommercialBookPage> {
    var step = 1
        set(value) {
            field = value
            if(value == 1) {
                cancelBtn.text = "취소"
                actionBtn.text = "다음"
                selectGuideLabel.visibility = View.VISIBLE
                nowCheckbox.visibility = View.GONE
            } else {
                if (step == 2)
                    cancelBtn.text = "이전"

                actionBtn.text = "워크북 만들기"
                selectGuideLabel.visibility = View.INVISIBLE
                nowCheckbox.visibility = View.VISIBLE
            }
        }

    var selectedPage: Int? = null
    var checkedPageProblem: ObservableHashSet<CommercialBookPage> = ObservableHashSet()
    var commercialBooks: List<CommercialBook>? = null
    var selectedBook: CommercialBook? = null
        set(value) {
            field = value
            if(step == 1 && value != null) {
                actionBtn.toEnableUI()
                selectedBookTv.text = "선택한 문제집 : [${field?.subjectType?.text}] ${field?.bookName}"
                selectGuideLabel.text = "'${value.bookName}' 문제집이 선택되었습니다."
            } else
                actionBtn.toDisableUI()
        }

    var pages: Map<Int, List<CommercialBookPage>>? = null
    var problemPerCnt = 1

    var repeatUpdateHandler = Handler()
    var autoIncrement = false
    var autoDecrement = false
    var delayHandler = Handler()
    var listener: CustomizeBookDialogListener? = null

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
        initUI()
        this.listener = listener
    }

    constructor(context: Context, listener: CustomizeBookDialogListener?, book: Book): super(context) {
        setContentView(R.layout.dialog_book_customize)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initUI()
        selectedBook = CommercialBook(book)
        step1Container.visibility = View.INVISIBLE
        step2Container.visibility = View.VISIBLE
        initStep2()
        step = 0
        this.listener = listener
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        val subjects = CommercialSubject.values()
        val subject = if(index == 0) null else subjects[index - 1]

        LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-과목선택",  subject?.text ?: "전체")

        sync(subject)
    }

    override fun onOrderChanged(view: SortableTextView, order: SortableTextView.Order) {
        subjectSl.isSelected = false
        bookSl.isSelected = false
        bookSeriesSl.isSelected = false
        publisherSl.isSelected = false

        view.isSelected = true
        sort()
        bookListRv.adapter?.notifyDataSetChanged()
    }

    private fun sort() {
        var list = commercialBooks
        if(subjectSl.isSelected) {
            val subjectList = listOf("수학(상)", "수학(하)", "수학1", "수학2", "확률과 통계", "미적분", "기하")
            val indexComparatorAscend =
                Comparator { cbook1: CommercialBook, cbook2: CommercialBook ->
                    subjectList.indexOf(cbook1.subjectType?.text) - subjectList.indexOf(cbook2.subjectType?.text)
                }
            val indexComparatorDescend =
                Comparator { cbook1: CommercialBook, cbook2: CommercialBook ->
                    subjectList.indexOf(cbook2.subjectType?.text) - subjectList.indexOf(cbook1.subjectType?.text)
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

        if(bookSeriesSl.isSelected) {
            list = when(bookSeriesSl.order) {
                SortableTextView.Order.ascend -> list?.sortedBy { it.bookTag }
                SortableTextView.Order.descend -> list?.sortedByDescending { it.bookTag }
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

        subjectTab.labels = listOf("전체", "수학(상)", "수학(하)", "수학1", "수학2", "확률과 통계", "미적분", "기하")
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

        actionBtn.toDisableUI()

        val isAvailable = user?.serviceType?.isTypeEqualOrHigher(PaidServiceType.PREMIUM) == true

        if (!isWorkbookStartChallengeInProgress && !isAvailable) {
            actionBtn.setLock(ButtonLockImage.mid20)
        } else {
            actionBtn.setUnlock()
        }

        actionBtn.setOnPremiumClickListener(cb = {
            onActionBtnClicked()
        }, deniedCb = {
            if (isWorkbookStartChallengeInProgress) {
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
        bookSeriesSl.listener = this
        publisherSl.listener = this

        sync(null)
    }

    private fun sync(subject: CommercialSubject?) {
        //did
        BookManager.getCommercialBook(context, subject) {
            this.commercialBooks = it
            sort()
            bookListRv.adapter?.notifyDataSetChanged()

            if(this.commercialBooks?.isEmpty() == true)
                bookEmptyTv.visibility = View.VISIBLE
            else
                bookEmptyTv.visibility = View.GONE
        }
    }

    private fun onActionBtnClicked() {
        if(!actionBtn.isEnableUI()) return

        when(step) {
            1 -> {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "시중교재-과목선택다음")
                actionBtn.toDisableUI()
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
                actionBtn.toEnableUI()
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
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_fafafa))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_ffffff))
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
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_fafafa))
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

            holder.set(problem)

            if(position == 0)
                holder.pageTitleTv.visibility = View.VISIBLE
            else
                holder.pageTitleTv.visibility = View.INVISIBLE

            holder.checkbox.setOnCheckedChangeListener(null)
            holder.checkbox.isChecked = checkedPageProblem.contains(problem)

            if(holder.checkbox.isChecked) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_fafafa))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_ffffff))
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
        actionBtn.startLoding()
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
                    totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                    cntImpossibleCl.visibility = View.INVISIBLE
                    blockCheck = false
                    actionBtn.toDisableUI()
                }
                in 1 .. 5 -> {
                    totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                    cntImpossibleCl.visibility = View.INVISIBLE
                    blockCheck = false
                    actionBtn.toEnableUI()
                }
                else -> {
                    totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                    cntImpossibleCl.visibility = View.VISIBLE
                    cntImpossibleTv.text = "스타트 챌린지에서는 최대 5문제까지 만들 수 있습니다.\n범위를 다시 선택해주세요."
                    actionBtn.toDisableUI()
                    blockCheck = true
                }
            }
        } else if(cnt in 1 .. 100) {
            totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
            cntImpossibleCl.visibility = View.INVISIBLE
            blockCheck = false
            actionBtn.toEnableUI()
        } else {
            totalCntTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
            cntImpossibleTv.text = "최대 100문제까지 만들 수 있습니다."
            if(cnt == 0) {
                cntImpossibleCl.visibility = View.INVISIBLE
                blockCheck = false
            } else {
                cntImpossibleCl.visibility = View.VISIBLE
                blockCheck = true
            }
            actionBtn.toDisableUI()
        }
        actionBtn.completeLoading()
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
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_fafafa))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white_ffffff))
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
    lateinit var actionBtn: PrimaryButton
    lateinit var bookEmptyTv: TextView
    lateinit var cancelBtn: SecondaryButton

    lateinit var subjectSl: SortableTextView
    lateinit var bookSl: SortableTextView
    lateinit var bookSeriesSl: SortableTextView
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
        bookSeriesSl = findViewById(R.id.bookSeriesSl)
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
            subjectTv.text = commercialBook.subjectType?.text
            bookTv.text = commercialBook.bookName
            bookSeriesTv.text = commercialBook.bookTag
            publisherTv.text = commercialBook.publisher
            hasBestTag = commercialBook.tag == CommercialBook.Tag.Best
            hasNewTag = commercialBook.tag == CommercialBook.Tag.New
        }
    }
}

class ProblemHolder(val itemBinding: ItemCommercialPageProblemBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val checkbox = itemBinding.checkbox
    val pageTitleTv = itemBinding.pageTitleTv

    fun set(problem: CommercialBookPage) {
        checkbox.text = problem.problemNumber
        pageTitleTv.text = problem.title
    }
}

class PageHolder(val itemBinding: ItemCommercialPageBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val checkIv = itemBinding.checkIv
    val pageTv = itemBinding.pageTv

    fun set(page: Int) {
        pageTv.text = "${page}p"
    }
}