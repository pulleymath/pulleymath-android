package com.freewheelin.pulley.revision2023.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.databinding.ViewChallengeGuideBinding
import com.freewheelin.pulley.revision2023.viewmodel.ChallengeGuideDialogViewModel
import com.freewheelin.pulley.legacy.utils.partialFontAndColored
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.utils.underline
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChallengeGuideDialog(): DialogFragment() {

    private val viewModel: ChallengeGuideDialogViewModel by viewModels()
    var nextEvent : () -> Unit = {}
    var dismissEvent : () -> Unit = {}
    var exitEvent : () -> Unit = {}
    companion object {
        const val GUIDE_TXT = "GUIDE_TXT"
        const val NEXT_TXT = "NEXT_TXT"
        const val EXIT_TXT = "EXIT_TXT"
        const val HIGHLIGHT_TXT = "HIGHLIGHT_TXT"
        const val SHOW_BOTTOM_BUTTONS = "SHOW_BOTTOM_BUTTONS"
        const val SHOW_BOTTOM_DISMISS_BUTTONS = "SHOW_BOTTOM_DISMISS_BUTTONS"
        const val SHOW_SPRINKLE = "SHOW_SPRINKLE"
        const val CAN_DISMISS_OUTSIDE = "CAN_DISMISS_OUTSIDE"
        const val PULLING_IV_SRC = "PULLING_IV_SRC"
        fun newInstance (guideText: String = "",
                         nextText: String = "",
                         exitText: String = "",
                         highlightText: String? = null,
                         pullingIvSrc: PullingImage = PullingImage.Normal,
                         showBottomButtons: Boolean = false,
                         showBottomDismissButtons: Boolean = false,
                         showSprinkle: Boolean = false,
                         canDismissOutSide: Boolean = true): ChallengeGuideDialog {
            val args = Bundle().apply {
                putString(GUIDE_TXT, guideText)
                putString(NEXT_TXT, nextText)
                putString(EXIT_TXT, exitText)
                putString(HIGHLIGHT_TXT, highlightText)
                putBoolean(SHOW_BOTTOM_BUTTONS, showBottomButtons)
                putBoolean(SHOW_BOTTOM_DISMISS_BUTTONS, showBottomDismissButtons)
                putBoolean(SHOW_SPRINKLE, showSprinkle)
                putBoolean(CAN_DISMISS_OUTSIDE, canDismissOutSide)
                putSerializable(PULLING_IV_SRC, pullingIvSrc)
            }
            val instance = ChallengeGuideDialog()
            instance.arguments = args
            return instance
        }
    }
    val binding: ViewChallengeGuideBinding by lazy {
        DataBindingUtil.inflate(
            layoutInflater.cloneInContext(requireContext()),
            R.layout.view_challenge_guide,
            null,
            false
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissEvent()
        super.onDismiss(dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.apply {
            viewModel.highlightText = getString(HIGHLIGHT_TXT)
            viewModel.canDismissOutSide = getBoolean(CAN_DISMISS_OUTSIDE)
        }
        dialog?.setCanceledOnTouchOutside(viewModel.canDismissOutSide)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFullscreenDialog()
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            moveTargetChallengePlaceBtnCl.setOnClickListener {
                dismiss()
                nextEvent()
            }
            dismissTv.apply {
                underline()
                setOnClickListener {
                    dismiss()
                }
            }
            rootView.setOnClickListener {
                dismiss()
                nextEvent()
            }
            exitBtn.setOnClickListener {
                dismiss()
                exitEvent()
            }
        }

        setGuideText(arguments?.getString(GUIDE_TXT) ?: "")
        setNextText(arguments?.getString(NEXT_TXT) ?: "")
        setExitText(arguments?.getString(EXIT_TXT) ?: "")
        setSprinkleView(arguments?.getBoolean(SHOW_SPRINKLE) ?: false)
//        highlightText?.let { setHighlightText(it) }
        setPullingIvSrc(arguments?.getSerializable(PULLING_IV_SRC) as PullingImage)
        setShowBottomButtons(arguments?.getBoolean(SHOW_BOTTOM_BUTTONS) ?: false)
        setShowBottomDismissButtons(arguments?.getBoolean(SHOW_BOTTOM_DISMISS_BUTTONS) ?: false)

        viewModel.apply {
            guideTxt.observe(viewLifecycleOwner) { guide ->
                Log.w("ChallengeGuideDialog", "guideTxt:${guide} ")
                if (highlightText == null) {
                    binding.challengeGuideTv.text = guide
                } else {
                    Log.w("ChallengeGuideDialog", "highlightText:${highlightText} ")
                    binding.challengeGuideTv.text = guide.partialFontAndColored(
                        Theme.extraBold(requireContext()),
                        ContextCompat.getColor(requireContext(), R.color.red_300),
                        highlightText!!
                    )
                }
            }
            pullingImage.observe(viewLifecycleOwner) { image ->
                val resource = when (image) {
                    PullingImage.Normal -> Pair(R.drawable.pulling_character_normal, 170)
                    PullingImage.StampNormal -> Pair(R.drawable.pulling_character_stamp_normal, 220)
                    PullingImage.Happy -> Pair(R.drawable.pulling_character_happy, 170)
                    PullingImage.RightHandUp -> Pair(
                        R.drawable.pulling_character_right_hand_up,
                        170
                    )
                    PullingImage.LeftHandUp -> Pair(R.drawable.pulling_character_left_hand_up, 170)
                }
                binding.pullingIv.apply {
                    setImageResource(resource.first)
                    layoutParams.width = resource.second.toPx()
                }

            }
        }
    }

    private fun setFullscreenDialog() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    enum class PullingImage {
        Normal, StampNormal, Happy, RightHandUp, LeftHandUp
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(requireContext(), R.style.TransparentFragmentDialog)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    fun setGuideText(value: String) {
        viewModel.guideTxt.postValue(value)
    }

    fun setNextText(value: String) {
        viewModel.nextTxt.postValue(value)
    }
    fun setExitText(value: String) {
        viewModel.exitTxt.postValue(value)
    }
    fun setSprinkleView(value: Boolean) {
        viewModel.showSprinkleView.postValue(value)
        binding.sprinkleLottieView.playAnimation()
    }
    fun setHighlightText(value: String) {
        viewModel.highlightTxt.postValue(value)
    }
    fun setPullingIvSrc(value: PullingImage) {
        viewModel.pullingImage.postValue(value)
    }
    fun setShowBottomButtons(value: Boolean) {
        viewModel.showButtons.postValue(value)
    }
    fun setShowBottomDismissButtons(value: Boolean) {
        viewModel.showDismissButtons.postValue(value)
    }
}