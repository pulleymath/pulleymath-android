package com.freewheelin.pulley.views

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
import androidx.fragment.app.FragmentManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.WrongNoteFragment
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ViewWrongManageBinding
import com.freewheelin.pulley.revision2021.activity.AffiliatedTestSolveActivity
import com.freewheelin.pulley.revision2021.utils.getLifecycleOwner
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedTestSolveViewModel
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteFragViewModel
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.utils.setOnBasicPOrHigherClickListener
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.buttons.*

interface WrongManageViewListener {
    fun onTrashBtnClicked(view: WrongManageView) {}
    fun onDownloadBtnClicked(view: WrongManageView) {}
    fun onShareBtnClicked(view: WrongManageView) {}
    fun onMailBtnClicked(view: WrongManageView) {}
    fun onStudyBtnClicked(view: WrongManageView)
    fun onReviewBtnClicked(view: WrongManageView) {}
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
    var studyWrongBtn: TextView

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

            studyWrongBtn = studyWrongTv
            studyWrongCl.setOnBasicPOrHigherClickListener(cb = {
                if(isActive) { listener?.onStudyBtnClicked(this@WrongManageView) }
                else { showInactiveToast() }
            }, deniedCb = {
                val dialog = PurchaseGuideDialog()
                val fm = (context as AppCompatActivity).supportFragmentManager
                fm.let { dialog.show(it, "purchaseGuideDialog")}
//                FragmentManager.findFragment<WrongNoteFragment>(it).childFragmentManager.let { dialog.show(it, "purchaseGuideDialog")}
//                DialogUtils.confirmDialog(context, "[테스트]구독중이 아닙니다.", "열려라 참깨")

            })
            reviewCl.setOnBasicPOrHigherClickListener(cb = {
                listener?.onReviewBtnClicked(this@WrongManageView)
            }, deniedCb = {
                val dialog = PurchaseGuideDialog()
//                FragmentManager.findFragment<WrongNoteFragment>(it).childFragmentManager.let { dialog.show(it, "purchaseGuideDialog")}
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
        btn.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_f2f2f2_round)
        binding.btnContainerLl.addView(btn)
        btn.setColorFilter(ContextCompat.getColor(context!!, R.color.grey_e0e0e0))
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
//            studyWrongBtn.toEnableUI()
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
//            studyWrongBtn.toDisableUI()
            guideTv.visibility = View.GONE

            buttons.forEach {
                it.setColorFilter(ContextCompat.getColor(context!!, R.color.grey_e0e0e0))
            }

            isActive = false
        }
    }

    fun showInactiveToast() {
        DaebakToast.show(context!!, "학습지를 선택해주세요.", bottomOffset = 72.toPx())
    }


    fun hideReviewBtn() {
        binding.reviewCl.visibility = View.GONE
    }

    fun setViewModel(vm: BaseAndroidViewModel) {

        (vm as WrongNoteFragViewModel).apply {
            binding.fragmentVM = this
        }
    }
}