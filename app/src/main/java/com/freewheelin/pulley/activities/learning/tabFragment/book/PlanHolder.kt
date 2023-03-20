package com.freewheelin.pulley.activities.learning.tabFragment.book

import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ItemBookPlanV2Binding
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.contents.BookType
import com.freewheelin.pulley.model.contents.ClientBookType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.buttons.PrimaryButton
import com.squareup.picasso.Picasso

enum class ActionType {
    pin,
    mail,
    delete
}

interface PlanListener {
    fun onActionBtnClicked(action: ActionType, book: Book, holder: PlanHolder)
    fun onReviewBtnClicked(holder: PlanHolder, book: Book)
    fun onSolveClicked(holder: PlanHolder, book: Book)
    fun onMakeCustomBookClicked(holder: PlanHolder, book: Book)
}
interface PlanListenerV2 {
    fun onActionBtnClicked(action: ActionType, book: Book)
    fun onSolveClicked(book: Book)
    fun filterFromTagOnCard(filterType: String)
}
interface PatternStudyListener {
    fun filterFromTagOnCard(type: FilterType)
}

class BookPlanV2Holder(
    private val binding: ItemBookPlanV2Binding,
    private val planListener: PlanListenerV2,
    private val actions: List<ActionType>,
    private val isGridLayout: Boolean = true
): RecyclerView.ViewHolder(binding.root) {
//    var actionList = listOf(ActionType.pin)
    var popupWindow: PopupWindow? = null

    fun bind(item: Book, showChallengeStamp: Boolean = false) = with(binding) {
        this.item = item
        this.listener = planListener
//        actionList = actions
        cardContainer.layoutParams.width = if (isGridLayout) FrameLayout.LayoutParams.MATCH_PARENT else 195.toPx()
        itemView.setOnTouchListener(BoongthEffect())
        itemView.setOnClickListener {
            if (item.isLocked) {
                LogUtils.logEvent(itemView.context, user, PulleyEvent.BUTTON_CLICK, "유형카드", "결제유도", "잠금버튼")
                val dialog = PurchaseGuideDialog()
                val fm = (binding.root.context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
                return@setOnClickListener
            } else {
                checkRewardSelect(item)
            }
        }

        val width = binding.root.context.resources.getDimension(R.dimen.dp168).toInt()
        val actionBtnWidth = binding.root.context.resources.getDimension(R.dimen.dp64).toInt()
        val view = LayoutInflater.from(binding.root.context).inflate(R.layout.view_action_list, null, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = ActionAdapter(item, planListener)
        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        popupWindow = PopupWindow(view, width, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true
        popupWindow?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(view.context, android.R.color.transparent)))
        popupWindow?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.bg_white_ffffff_stroke_black_4c4c4c_round))

        actionDotBtn.setOnClickListener {
            val locationInts = IntArray(2)
            actionDotBtn.getLocationOnScreen(locationInts)
            val topMargin = 8.toPx()
            popupWindow?.showAtLocation(actionDotBtn, Gravity.NO_GRAVITY, locationInts[0] - width + actionBtnWidth + 16.toPx(), locationInts[1] + topMargin)
        }

        challengeStampIv.visibleIf(showChallengeStamp)
    }

    private fun checkRewardSelect(book: Book) {
        val isUnderBasicP = user?.serviceType?.isUnderBasicP() == true
        val isStartChallengeBook = book.isStartChallengePiece()
        val isStartChallengeRewardBook = book.isStartChallengeRewardPiece()
        if (isUnderBasicP && !isStartChallengeBook && !isStartChallengeRewardBook) {
            DialogUtils.confirmDialog(binding.root.context,
                "이 학습지로 선택하실 건가요?",
                "학습지 1개만 무료로 이용 가능해요 :)",
                "아니오",
                "네, 선택할래요",
                rightBtnCB = {
                    planListener.onSolveClicked(book)
                })
        } else {
            planListener.onSolveClicked(book)
        }
    }
    inner class ActionAdapter(private val item: Book, private val listener: PlanListenerV2) : RecyclerView.Adapter<PlanActionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanActionHolder {
            val view = LayoutInflater.from(binding.root.context).inflate(R.layout.item_arduous_spinner, parent, false)
            return PlanActionHolder(view)
        }

        override fun getItemCount(): Int {
            return actions.size
        }

        override fun onBindViewHolder(holder: PlanActionHolder, position: Int) {
            val action = actions[position]

            val message = when (action) {
                ActionType.delete -> "나의 문제집에서 빼기"
                ActionType.mail -> "메일 보내기"
                ActionType.pin -> if (item.isPinned) "핀 해제하기" else "핀 설정하기"
            }

            holder.button.apply {
                text = message
                setOnBasicPOrHigherClickListener(cb = {
                    popupWindow?.dismiss()
                    listener.onActionBtnClicked(action, item)
                }, deniedCb = {
                    // TODO change
                    DaebakToast.show(context, "핀 설정은 유형 베이직, 스탠다드, 프리미엄 회원만 이용 가능합니다 :)")
                })
            }
        }
    }
}
abstract class PlanHolder(open val view: View) : RecyclerView.ViewHolder(view) {
    lateinit var book: Book

    private val ivWrapperCl get() = view.findViewById<ConstraintLayout>(R.id.ivWrapperCl)
    private val pin get() = view.findViewById<ImageView>(R.id.pin)
    private val lockIv get() = view.findViewById<ImageView>(R.id.lockIv)
    private val bookNameTv get() = view.findViewById<TextView>(R.id.bookNameTv)
    private val seriesTv get() = view.findViewById<TextView>(R.id.seriesTv)
    private val subjectTv get() = view.findViewById<TextView>(R.id.subjectTv)
    private val chapterTv get() = view.findViewById<TextView>(R.id.chapterTv)
    private val actionBtn get() = view.findViewById<ImageView>(R.id.actionBtn)
    private val workbookIv get() = view.findViewById<ImageView>(R.id.workbookIv)
    private val backgroudCl get() = view.findViewById<ConstraintLayout>(R.id.backgroundCl)
    val reviewBtn: PrimaryButton get() = view.findViewById(R.id.reviewBtn)
    private val makingCustomBookBtn get() = view.findViewById<Button>(R.id.makingCustomBook)
    val finishContainer: LinearLayout get() = view.findViewById(R.id.finishContainer)

    open var actionList = listOf(ActionType.pin)
    var listener: PlanListener? = null
    var studyListener: PatternStudyListener? = null
    var scaleAnim: ValueAnimator? = null
    var expandAnim: ValueAnimator? = null

    var popupWindow: PopupWindow? = null

    private val bgIv get() = view.findViewById<ImageView>(R.id.bgIv)
    private val foldIv get() = view.findViewById<ImageView>(R.id.foldIv)

    open fun set(book: Book) {
        this.book = book
        bookNameTv.text = book.bookName
        subjectTv.text = book.subject
        chapterTv.text = book.chapter

        val textColor = if (book.isCompleted()) {
            ContextCompat.getColor(view.context, R.color.grey_c0c0c0)
        } else {
            ContextCompat.getColor(view.context, R.color.black_4c4c4c)
        }
        bookNameTv.setTextColor(textColor)
        subjectTv.setTextColor(textColor)
        chapterTv.setTextColor(textColor)

        seriesTv.text = "${book.bookCategoryList?.bookSeries} ${book.bookCategoryList?.bookCategory}"

        val width = view.context.resources.getDimension(R.dimen.dp168).toInt()
        val actionBtnWidth = view.context.resources.getDimension(R.dimen.dp64).toInt()
        val view = LayoutInflater.from(view.context).inflate(R.layout.view_action_list, null, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = ActionAdapter()
        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        popupWindow = PopupWindow(view, width, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true
        popupWindow?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(view.context, android.R.color.transparent)))
        popupWindow?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.bg_white_ffffff_stroke_black_4c4c4c_round))
        actionBtn.setOnClickListener {
            val locationInts = IntArray(2)
            actionBtn.getLocationOnScreen(locationInts)
            val topMargin = 8.toPx()
            popupWindow?.showAtLocation(actionBtn, Gravity.NO_GRAVITY, locationInts[0] - width + actionBtnWidth + 16.toPx(), locationInts[1] + topMargin)
        }

        if (book.isLocked) {
            ivWrapperCl.visibility = View.VISIBLE
            lockIv.showIfNeed(100)
            pin.hideGoneIfNeed(100)
        } else if (book.isPinned) {
            ivWrapperCl.visibility = View.VISIBLE
            pin.showIfNeed(100)
            lockIv.hideGoneIfNeed(100)
        } else {

            pin.hideGoneIfNeed(100)
            lockIv.hideGoneIfNeed(100)
            ivWrapperCl.hideGoneIfNeed(100)
        }

        itemView.setOnTouchListener(BoongthEffect())
        reviewBtn.setOnClickListener {
            if (book.isLocked) {
                //TODO
                val dialog = PurchaseGuideDialog()
                val fm = (view.context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
                return@setOnClickListener
            }
            listener?.onSolveClicked(this, book)
        }

        when (book.pieceCategoryTag) {
            BookType.COMMERCIAL -> setCommercialUI()
            BookType.BOOK -> setBookUI()
            BookType.CUSTOM_BOOK -> setCustomBookUI()
            else -> {}
        }
    }

    fun setBookUI() {
        chapterTv.visibility = View.VISIBLE
        workbookIv.visibility = View.INVISIBLE
        seriesTv.visibility = View.VISIBLE
        backgroudCl.setBackgroundColor(ContextCompat.getColor(view.context, R.color.grey_f2f2f2))
        val bgImageUrl = book.backgroundImageUrl + "${DisplayUtils.getBgImgFolder(view.context)}/${"group_" + book.recommendType.lowercase() + ".png"}"
        Picasso.get().load(bgImageUrl).into(bgIv)

        makingCustomBookBtn.visibility = View.INVISIBLE
        actionBtn.visibility = View.VISIBLE

        if (book.markedNumber == 0)
            foldIv.visibility = View.INVISIBLE
        else
            foldIv.visibility = View.VISIBLE

        if (book.isCompleted()) {
            reviewBtn.visibility = View.VISIBLE
            finishContainer.visibility = View.VISIBLE
            itemView.setOnTouchListener(BoongthEffect())
            itemView.setOnClickListener {
                if (book.isLocked) {
                    val dialog = PurchaseGuideDialog()
                    val fm = (view.context as AppCompatActivity).supportFragmentManager
                    fm.let { dialog.show(it, "purchaseGuideDialog")}
                    return@setOnClickListener
                } else {
                    checkRewardSelect()
                }
            }
        } else {
            reviewBtn.visibility = View.INVISIBLE
            finishContainer.visibility = View.INVISIBLE
            itemView.setOnTouchListener(BoongthEffect())
            itemView.setOnClickListener {
                if (book.isLocked) {
                    val dialog = PurchaseGuideDialog()
                    val fm = (view.context as AppCompatActivity).supportFragmentManager
                    fm.let { dialog.show(it, "purchaseGuideDialog")}
                    return@setOnClickListener
                } else {
                    checkRewardSelect()
                }
            }
        }
    }

    fun setTag(tags: List<TextView>, cb: ((String) -> Unit) = {}) {
        if (book.isCompleted()) {
            tags.forEach { it.visibility = View.INVISIBLE }
        } else {
            tags.forEach { it.visibility = View.GONE }
            val tagStrList = book.splitedTag
            book.splitedTag.forEachIndexed { index, tag ->
                val tagView = tags.getOrNull(index)
                tagView?.visibility = View.VISIBLE
                tagView?.text = tag
                tagView?.setOnClickListener {
                    cb(tag)
                }
            }
            for(i in tagStrList.indices) {

            }
        }
    }

    fun setCommercialUI() {
        println("asoaso 삐용삐용 setCommercialUI setCommercialUI setCommercialUI setCommercialUI")
        chapterTv.visibility = View.GONE
        workbookIv.visibility = View.VISIBLE
        seriesTv.visibility = View.INVISIBLE
        backgroudCl.setBackgroundColor(ContextCompat.getColor(view.context, R.color.yellow_fff0bc))
        bgIv.setImageDrawable(null)

        foldIv.visibility = View.INVISIBLE
        makingCustomBookBtn.visibility = View.VISIBLE
        actionBtn.visibility = View.INVISIBLE

        reviewBtn.visibility = View.INVISIBLE
        finishContainer.visibility = View.INVISIBLE
        itemView.setOnTouchListener(BoongthEffect())
        itemView.setOnPremiumClickListener(cb = {

        })
    }

    fun setCustomBookUI() {
        chapterTv.visibility = View.VISIBLE
        workbookIv.visibility = View.VISIBLE
        seriesTv.visibility = View.INVISIBLE
        backgroudCl.setBackgroundColor(ContextCompat.getColor(view.context, R.color.yellow_fff0bc))
        bgIv.setImageDrawable(null)

        makingCustomBookBtn.visibility = View.INVISIBLE
        actionBtn.visibility = View.VISIBLE

        foldIv.visibility = if (book.markedNumber == 0) View.INVISIBLE else View.VISIBLE
        reviewBtn.visibility = if (book.isCompleted()) View.VISIBLE else View.INVISIBLE
        finishContainer.visibility = if (book.isCompleted()) View.VISIBLE else View.INVISIBLE

        itemView.setOnTouchListener(BoongthEffect())
        itemView.setOnClickListener {
            if (book.isLocked) {
                val dialog = PurchaseGuideDialog()
                val fm = (view.context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
                return@setOnClickListener
            }
            listener?.onSolveClicked(this, book)
        }
    }
    fun checkRewardSelect() {
        val isUnderBasicP = user?.serviceType?.isUnderBasicP() == true
        val isStartChallengeBook = book.isStartChallengePiece()
        val isStartChallengeRewardBook = book.isStartChallengeRewardPiece()
        if (isUnderBasicP && !isStartChallengeBook && !isStartChallengeRewardBook) {
            DialogUtils.confirmDialog(view.context,
                "이 학습지로 선택하실 건가요?",
                "학습지 1개만 무료로 이용 가능해요 :)",
                "아니오",
                "네, 선택할래요",
                rightBtnCB = {
                    listener?.onSolveClicked(this, book)
                })
        } else {
            listener?.onSolveClicked(this, book)
        }
    }

    inner class ActionAdapter : RecyclerView.Adapter<PlanActionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanActionHolder {
            val view = LayoutInflater.from(view.context).inflate(R.layout.item_arduous_spinner, parent, false)
            return PlanActionHolder(view)
        }

        override fun getItemCount(): Int {
            return actionList.size
        }

        override fun onBindViewHolder(holder: PlanActionHolder, position: Int) {
            val action = actionList[position]

            val message = when (action) {
                ActionType.delete -> "나의 문제집에서 빼기"
                ActionType.mail -> "메일 보내기"
                ActionType.pin -> if (book.isPinned) "핀 해제하기" else "핀 설정하기"
            }

            holder.button.apply {
                text = message
                setOnBasicPOrHigherClickListener(cb = {
                    popupWindow?.dismiss()
                    listener?.onActionBtnClicked(action, book, this@PlanHolder)
                }, deniedCb = {
                    // TODO change
                    DaebakToast.show(context, "핀 설정은 유형 베이직, 스탠다드, 프리미엄 회원만 이용 가능합니다 :)")
                })
            }
        }
    }
}

class MyPlanHolder(override var view: View) : PlanHolder(view) {

    private val solveDateLabel = view.findViewById<TextView>(R.id.solveDateLabel)
    private val solveDateTv = view.findViewById<TextView>(R.id.solveDateTv)
    private val solveCntTv = view.findViewById<TextView>(R.id.solveCntTv)
    private val problemCntTv = view.findViewById<TextView>(R.id.problemCntTv)
    private val correctRateTv = view.findViewById<TextView>(R.id.correctRateTv)
    private val solveCntLabel = view.findViewById<TextView>(R.id.solveCntLabel)


    override var actionList = listOf(ActionType.pin, ActionType.mail, ActionType.delete)

    override fun set(book: Book) {
        book.clientBookType = ClientBookType.MY
        super.set(book)

        if (book.updateDateTime != null)
            solveDateTv.text = book.updateDateTime?.let { DateTimeUtils.mMddFormat.format(it) }
        if (book.isCompleted()) {
            reviewBtn.visibility = View.VISIBLE
            finishContainer.visibility = View.VISIBLE
            solveCntLabel.visibility = View.GONE
        } else {
            reviewBtn.visibility = View.INVISIBLE
            finishContainer.visibility = View.INVISIBLE
            solveCntLabel.visibility = View.VISIBLE
        }

        problemCntTv.text = "${book.totalNumber}문제"
        solveCntTv.text = "${book.markedNumber}/${book.totalNumber}"
        correctRateTv.text = "${book.score}%"

        if (book.markedNumber == 0) {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
        } else {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
        }

        if (book.assignID == null || book.isCompleted()) {
            solveDateLabel.visibility = View.GONE
            solveDateTv.visibility = View.GONE
        } else {
            solveDateLabel.visibility = View.VISIBLE
            solveDateTv.visibility = View.VISIBLE
        }
    }
}

class RecommendPlanHolder(override var view: View) : PlanHolder(view) {
    private val solveCntTv = view.findViewById<TextView>(R.id.solveCntTv)
    private val problemCntTv = view.findViewById<TextView>(R.id.problemCntTv)
    private val correctRateTv = view.findViewById<TextView>(R.id.correctRateTv)

    val tags = listOf(view.findViewById<TextView>(R.id.tag1), view.findViewById<TextView>(R.id.tag2))
    override fun set(book: Book) {
        book.clientBookType = ClientBookType.RECOMMEND
        super.set(book)

        setTag(tags)
        solveCntTv.text = "${book.markedNumber}/${book.totalNumber}"
        problemCntTv.text = "${book.totalNumber}문제"
        correctRateTv.text = "${book.score}%"

        if (book.markedNumber == 0) {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
        } else {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
        }
    }
}

class PlanActionHolder(var view: View) : RecyclerView.ViewHolder(view) {
    val button: Button = view.findViewById(R.id.listItem)
}