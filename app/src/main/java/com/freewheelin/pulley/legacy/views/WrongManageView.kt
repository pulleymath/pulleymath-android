package com.freewheelin.pulley.legacy.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.ViewWrongManageBinding
import com.freewheelin.pulley.revision2021.utils.getLifecycleOwner
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.ui.view.CommonButton
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteActViewModel

interface WrongManageViewListener {
    fun onTrashBtnClicked(view: WrongManageView) {}
    fun onDownloadBtnClicked(view: WrongManageView) {}
    fun onShareBtnClicked(view: WrongManageView) {}
    fun onMailBtnClicked(view: WrongManageView) {}
    fun onStudyBtnClicked(view: WrongManageView)
    fun onReviewBtnClicked(view: WrongManageView) {}
}
interface NoteStudyViewListener {
    fun onStudyBtnClicked(view: WrongManageView)
    fun onReviewBtnClicked(view: WrongManageView)
}
class WrongManageView: ConstraintLayout {
    enum class BtnType {
        trash, share, download, mail;

        val resId: Int
            get() {
                return when(this) {
                    trash -> R.drawable.ic_trash_active
                    share -> R.drawable.ic_share
                    download -> R.drawable.ic_download
                    mail -> R.drawable.ic_mail
                }
            }
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var buttons = ArrayList<ImageButton>()

    var isActive: Boolean = false
    var listener: WrongManageViewListener? = null
    var studyListener: NoteStudyViewListener? = null
    var studyWrongBtn: CommonButton

//    var containerCl: ConstraintLayout
//    var reviewBtn: SecondaryButton
//    var guideTv: TextView
//    var btnContainerLl: LinearLayout

    private var binding: ViewWrongManageBinding
    init {
        binding = ViewWrongManageBinding.inflate(LayoutInflater.from(context), this, true)
        binding.apply {
            lifecycleOwner = context.getLifecycleOwner()

            containerCl.isClickable = true
            // setLock first
//            studyWrongBtn.setLock(user!!.hasPulleyPlus, ButtonLockImage.mid20, ButtonMode.pulley_plus)
//            reviewBtn.setLock(user!!.hasPulleyPlus, ButtonLockImage.mid20, ButtonMode.pulley_plus, ButtonLockColor.purple)

            studyWrongBtn = studyBtn
            studyBtn.setOnBasicPOrHigherClickListener(cb = {

                if (isActive) {
                    listener?.onStudyBtnClicked(this@WrongManageView)
                    studyListener?.onStudyBtnClicked(this@WrongManageView)
                }
                else { showInactiveToast() }
            }, deniedCb = {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "오답노트", "결제유도", "학습지만들기")

                val dialog = PurchaseGuideDialog.newInstance()
                val fm = (context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}

            })
            reviewBtn.setOnBasicPOrHigherClickListener(cb = {
                listener?.onReviewBtnClicked(this@WrongManageView)
                studyListener?.onReviewBtnClicked(this@WrongManageView)
            }, deniedCb = {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "오답노트", "결제유도", "리뷰하기")
                val dialog = PurchaseGuideDialog.newInstance()
                val fm = (context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
            })
        }

        inactive()
    }

    fun hide(withAnim: Boolean = false) {
        val yOFF = 300
        if (!withAnim) {
            translationY = yOFF.toFloat()
            return
        }

        val fromValue = translationY / yOFF
        val hideAnimator = ValueAnimator.ofFloat(fromValue, 1f)
        hideAnimator.duration = (yOFF - translationY).toLong()
        hideAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            this.translationY = yOFF * value
        }
        hideAnimator.start()
    }

    fun show(withAnim: Boolean = false, cb: (() -> Unit)? = null) {
        if (!withAnim) {
            translationY = 0f
            return
        }

        val yOFF = 300
        val fromValue = translationY /yOFF

        val showAnimator = ValueAnimator.ofFloat(fromValue, 0f)
        showAnimator.duration = translationY.toLong()
        showAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            this.translationY = yOFF * value
        }
        showAnimator.addListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                if (cb != null) {
                    cb()
                }
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }
        })

        showAnimator.start()
    }


    fun makeBtn(type: BtnType) {
        val btn = ImageButton(context)
        val width = context.resources.getDimension(R.dimen.dp56).toInt()
        val height = context.resources.getDimension(R.dimen.dp36).toInt()

        btn.layoutParams = LinearLayout.LayoutParams(width, height).apply {
            marginStart = 8.toPx()
        }
        btn.setImageResource(type.resId)
        btn.background = ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round_ripple)
        binding.btnContainerLl.addView(btn)
        btn.setColorFilter(ContextCompat.getColor(context!!, R.color.gray_400))
        btn.setOnClickListener {
            if(!isActive)
                showInactiveToast()
            else {
                when(type) {
                    BtnType.mail -> listener?. onMailBtnClicked(this)
                    BtnType.trash -> listener?.onTrashBtnClicked(this)
                    BtnType.share -> listener?.onShareBtnClicked(this)
                    BtnType.download -> listener?.onDownloadBtnClicked(this)
                }
            }
        }
        buttons.add(btn)
    }

    fun active(guideText: String) {
        binding.apply {
//            studyWrongBtn.isEnabled = true
            guideTv.visibility = View.VISIBLE
            guideTv.text = guideText

            buttons.forEach {
                it.clearColorFilter()
            }

            isActive = true
        }
    }

    fun inactive() {
        binding.apply {
//            studyWrongBtn.isEnabled = false
            guideTv.visibility = View.GONE

            buttons.forEach {
                it.setColorFilter(ContextCompat.getColor(context!!, R.color.gray_400))
            }

            isActive = false
        }
    }

    fun showInactiveToast() {
        DaebakToast.show(context!!, "학습지를 선택해주세요.", bottomOffset = 72.toPx())
    }


    fun hideReviewBtn() {
        binding.reviewBtn.visibility = View.GONE
    }

    fun setViewModel(vm: BaseAndroidViewModel) {

        (vm as WrongNoteActViewModel).apply {
            binding.fragmentVM = this
        }
    }
}