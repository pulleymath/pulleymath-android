package com.freewheelin.pulley.activities.learning.tabFragment.book

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.contents.BookType
import com.freewheelin.pulley.model.contents.ClientBookType
import com.freewheelin.pulley.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_arduous_spinner.view.*
import kotlinx.android.synthetic.main.item_book_my_plan.view.*
import kotlinx.android.synthetic.main.item_book_my_plan.view.correctRateTv
import kotlinx.android.synthetic.main.item_book_my_plan.view.finishContainer
import kotlinx.android.synthetic.main.item_book_my_plan.view.problemCntTv
import kotlinx.android.synthetic.main.item_book_my_plan.view.reviewBtn
import kotlinx.android.synthetic.main.item_book_my_plan.view.solveCntTv
import kotlinx.android.synthetic.main.item_book_plan.view.*
//import kotlinx.android.synthetic.main.item_book_recommend_plan.view.*
import kotlinx.android.synthetic.main.item_book_total_plan.view.guideTv
import kotlinx.android.synthetic.main.item_book_total_plan.view.tag1
import kotlinx.android.synthetic.main.item_book_total_plan.view.tag2
import kotlinx.android.synthetic.main.view_action_list.view.*

enum class ActionType {
    pin,
    mail,
    delete
}

interface PlanListener {
    fun onActionBtnClicked(action: ActionType, book: Book, holder: PlanHolder)
    fun onReviewBtnClikced(holder: PlanHolder, book: Book)
    fun onSolveClicked(holder: PlanHolder, book: Book)
    fun onMakeCustomBookClicked(holder: PlanHolder, book: Book)
}

abstract class PlanHolder(open val view: View) : RecyclerView.ViewHolder(view) {
    lateinit var book: Book

    val pin get() = view.pin
    val bookNameTv get() = view.bookNameTv
    val seriesTv get() = view.seriesTv
    val subjectTv get() = view.subjectTv
    val chapterTv get() = view.chapterTv
    val actionBtn get() = view.actionBtn
    val workbookIv get() = view.workbookIv
    val backgroudCl get() = view.backgroundCl
    val reviewBtn get() = view.reviewBtn
    val makingCustomBookBtn get() = view.makingCustomBook
    val finishContainer get() = view.finishContainer

    open var actionList = listOf(ActionType.pin)
    var listener: PlanListener? = null
    var scaleAnim: ValueAnimator? = null
    var expandAnim: ValueAnimator? = null

    var popupWindow: PopupWindow? = null

    val bgIv get() = view.bgIv
    val foldIv get() = view.foldIv

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

        seriesTv.text = book.bookCategoryList?.bookSeries

        val width = view.context.resources.getDimension(R.dimen.dp168).toInt()
        val actionBtnWidth = view.context.resources.getDimension(R.dimen.dp64).toInt()
        val view = LayoutInflater.from(view.context).inflate(R.layout.view_action_list, null, false)
        view.recyclerView.adapter = ActionAdapter()
        view.recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
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

        if (book.pin) {
            pin.visibility = View.VISIBLE
        } else {
            pin.visibility = View.GONE
        }

        itemView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
                startExpandAnim()
            } else {
                startScaleAnim()
            }
            false
        }

        reviewBtn.setPermissionClickListener {
            listener?.onReviewBtnClikced(this, book)
        }
        makingCustomBookBtn.setPermissionClickListener {
            listener?.onMakeCustomBookClicked(this, book)
        }

        when (book.pieceCategoryTag) {
            BookType.COMMERCIAL -> setCommercialUI()
            BookType.BOOK -> setBookUI()
            BookType.CUSTOM_BOOK -> setCustomBookUI()
        }
    }

    fun setBookUI() {
        chapterTv.visibility = View.VISIBLE
        workbookIv.visibility = View.INVISIBLE
        seriesTv.visibility = View.VISIBLE
        backgroudCl.setBackgroundColor(ContextCompat.getColor(view.context, R.color.grey_f2f2f2))
        val bgImageUrl = book.backgroundImageUrl + "${DisplayUtils.getBgImgFolder(view.context)}/${"group_" + book.recommendType.toLowerCase() + ".png"}"
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
            itemView.setOnClickListener {
                startScaleAnim {
                    startExpandAnim {
                        listener?.onReviewBtnClikced(this, book)
                    }
                }
            }
        } else {
            reviewBtn.visibility = View.INVISIBLE
            finishContainer.visibility = View.INVISIBLE
            itemView.setPermissionClickListener {
                startScaleAnim {
                    startExpandAnim {
                        listener?.onSolveClicked(this, book)
                    }
                }
            }
        }
    }

    fun setTag(tags: List<TextView>) {
        if (book.isCompleted()) {
            tags.forEach { it.visibility = View.INVISIBLE }
        } else {
            tags.forEach { it.visibility = View.GONE }
            val tagStrings = book.tag.filter { !it.isEmpty() }
            for(i in tagStrings.indices) {
                val tagView = tags.getOrNull(i)
                tagView?.visibility = View.VISIBLE
                tagView?.text = tagStrings[i]
            }
        }
    }

    fun setCommercialUI() {
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
        itemView.setOnClickListener {
            startScaleAnim {
                startExpandAnim {}
            }
        }
    }

    fun setCustomBookUI() {
        chapterTv.visibility = View.VISIBLE
        workbookIv.visibility = View.VISIBLE
        seriesTv.visibility = View.INVISIBLE
        backgroudCl.setBackgroundColor(ContextCompat.getColor(view.context, R.color.yellow_fff0bc))
        bgIv.setImageDrawable(null)

        makingCustomBookBtn.visibility = View.INVISIBLE
        actionBtn.visibility = View.VISIBLE

        if (book.markedNumber == 0)
            foldIv.visibility = View.INVISIBLE
        else
            foldIv.visibility = View.VISIBLE

        if (book.isCompleted()) {
            reviewBtn.visibility = View.VISIBLE
            finishContainer.visibility = View.VISIBLE
            itemView.setOnClickListener {
                startScaleAnim {
                    startExpandAnim {
                        listener?.onReviewBtnClikced(this, book)
                    }
                }
            }
        } else {
            reviewBtn.visibility = View.INVISIBLE
            finishContainer.visibility = View.INVISIBLE
            itemView.setPermissionClickListener {
                startScaleAnim {
                    startExpandAnim {
                        listener?.onSolveClicked(this, book)
                    }
                }
            }
        }
    }

    fun startExpandAnim(cb: (() -> Unit)? = null) {
        val fromScale = itemView.scaleX
        scaleAnim?.cancel()
        scaleAnim = null
        if (expandAnim == null) {
            expandAnim = ValueAnimator.ofFloat(fromScale, 1f)
            expandAnim?.addUpdateListener {
                val value = it.animatedValue as Float
                itemView.scaleX = value
                itemView.scaleY = value
            }
            expandAnim?.duration = (3000 * (1 - fromScale)).toLong()
            expandAnim?.start()
        }

        expandAnim?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                if (cb != null)
                    cb()
            }

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}

        })
    }

    fun startScaleAnim(cb: (() -> Unit)? = null) {
        val fromScale = itemView.scaleX
        expandAnim?.cancel()
        expandAnim = null
        if (scaleAnim == null) {
            scaleAnim = ValueAnimator.ofFloat(fromScale, 0.95f)
            scaleAnim?.addUpdateListener {
                val value = it.animatedValue as Float
                itemView.scaleX = value
                itemView.scaleY = value
            }
            scaleAnim?.duration = (3000 * (fromScale - 0.95)).toLong()
            scaleAnim?.start()
        }

        scaleAnim?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                if (cb != null)
                    cb()
            }

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}

        })
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

            when (action) {
                ActionType.delete -> {
                    holder.button.text = "나의 플랜에서 빼기"
                }

                ActionType.mail -> {
                    holder.button.text = "메일 보내기"
                }

                ActionType.pin -> {
                    holder.button.text = if (book.pin) "핀 해제하기" else "핀 설정하기"
                }
            }
            if (action == ActionType.mail) {
                holder.button.setPermissionClickListener {
                    popupWindow?.dismiss()
                    listener?.onActionBtnClicked(action, book, this@PlanHolder)
                }
            } else {
                holder.button.setOnClickListener {
                    popupWindow?.dismiss()
                    listener?.onActionBtnClicked(action, book, this@PlanHolder)
                }
            }
        }
    }
}

class MyPlanHolder(override var view: View) : PlanHolder(view) {
    val solveDateLabel = view.solveDateLabel
    val solveDateTv = view.solveDateTv
    val solveCntTv = view.solveCntTv
    val problemCntTv = view.problemCntTv
    val correctRateTv = view.correctRateTv
    val solveCntLabel = view.solveCntLabel


    override var actionList = listOf(ActionType.pin, ActionType.mail, ActionType.delete)

    override fun set(book: Book) {
        book.clientBookType = ClientBookType.MY
        super.set(book)

        if (book.updateDateTime != null)
            solveDateTv.text = "${DateTimeUtils.mMddFormat.format(book.updateDateTime)}"
        if (book.isCompleted()) {
            reviewBtn.visibility = View.VISIBLE
            finishContainer.visibility = View.VISIBLE
            solveCntLabel.visibility = View.GONE
        } else {
            reviewBtn.visibility = View.INVISIBLE
            finishContainer.visibility = View.INVISIBLE
            solveCntLabel.visibility = View.VISIBLE
        }

        problemCntTv.text = book.totalNumber.toString() + "문제"
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

class TotalPlanHolder(override var view: View) : PlanHolder(view) {
    val solveCntTv = view.solveCntTv
    val problemCntTv = view.problemCntTv
    val correctRateTv = view.correctRateTv
    val tags = listOf(view.tag1, view.tag2)
    val guideTv = view.guideTv

    override fun set(book: Book) {
        book.clientBookType = ClientBookType.ALL
        super.set(book)

        setTag(tags)
        solveCntTv.text = "${book.markedNumber}/${book.totalNumber}"
        problemCntTv.text = book.totalNumber.toString() + "문제"
        correctRateTv.text = "${book.score}%"

        if (book.markedNumber == 0) {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
        } else {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
        }

        guideTv.text = book.description
    }

}

class RecommendPlanHolder(override var view: View) : PlanHolder(view) {
    val solveCntTv = view.solveCntTv
    val problemCntTv = view.problemCntTv
    val correctRateTv = view.correctRateTv

    val tags = listOf(view.tag1, view.tag2)
    override fun set(book: Book) {
        book.clientBookType = ClientBookType.RECOMMEND
        super.set(book)

        setTag(tags)
        solveCntTv.text = "${book.markedNumber}/${book.totalNumber}"
        problemCntTv.text = book.totalNumber.toString() + "문제"
        correctRateTv.text = "${book.score}%"

        if (book.markedNumber == 0) {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
        } else {
            solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
        }
    }
}

class PlanActionHolder(var view: View) : RecyclerView.ViewHolder(view) {
    val button = view.listItem
}