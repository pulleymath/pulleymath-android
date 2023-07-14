package com.freewheelin.pulley.legacy.activities.learning.tabFragment.book

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
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.ItemBookPlanV2Binding
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.model.contents.BookType
import com.freewheelin.pulley.legacy.model.contents.ClientBookType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.squareup.picasso.Picasso

enum class ActionType {
    pin,
    mail,
    delete
}


interface PlanListenerV2 {
    fun onActionBtnClicked(action: ActionType, book: Book)
    fun onSolveClicked(book: Book)
    fun filterFromTagOnCard(filterType: String)
}

class BookPlanV2Holder(
    private val binding: ItemBookPlanV2Binding,
    private val planListener: PlanListenerV2,
    private val actions: List<ActionType>,
    private val isGridLayout: Boolean = true,
    private val viewModel: BaseAndroidViewModel? = null
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
            if (user?.serviceType?.isGuestUser == true) {
                LogUtils.logEvent(itemView.context, user!!, PulleyEvent.LEARNING_CARD_CLICK, "유형카드", "${item.bookName}", "${item.subject}-${item.chapter}")
                viewModel?.guestException()
                return@setOnClickListener
            }
            if (item.isLocked) {
                LogUtils.logEvent(itemView.context, user, PulleyEvent.BUTTON_CLICK, "유형카드", "결제유도", "잠금버튼")
                val dialog = PurchaseGuideDialog.newInstance()
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
        popupWindow?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.bg_white_stroke_gray_800_round))

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
                ActionType.delete -> "최근 문제집에서 빼기"
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
class PlanActionHolder(var view: View) : RecyclerView.ViewHolder(view) {
    val button: Button = view.findViewById(R.id.listItem)
}