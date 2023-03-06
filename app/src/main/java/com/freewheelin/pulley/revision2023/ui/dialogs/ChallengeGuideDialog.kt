package com.freewheelin.pulley.revision2023.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.ViewChallengeGuideBinding
import com.freewheelin.pulley.revision2023.viewmodel.ChallengeGuideDialogViewModel
import com.freewheelin.pulley.utils.partialFontAndColored
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.utils.underline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChallengeGuideDialog(val guideText: String = "",
                           val nextText: String = "",
                           val exitText: String = "",
                           val highlightText: String? = null,
                           val pullingIvSrc: PullingImage = PullingImage.Normal,
                           val showBottomButtons: Boolean = false,
                           val showSprinkle: Boolean = false,
                           val nextEvent: () -> Unit = {},
                           val dismissEvent: () -> Unit = {},
                           val exitEvent: () -> Unit = {}
): DialogFragment() {

    private val viewModel: ChallengeGuideDialogViewModel by viewModels()
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    exitEvent()
                }
            }
            rootView.setOnClickListener {
                dismiss()
                nextEvent()
            }
        }

        setGuideText(guideText)
        setNextText(nextText)
        setExitText(exitText)
        setSprinkleView(showSprinkle)
        highlightText?.let { setHighlightText(it) }
        setPullingIvSrc(pullingIvSrc)
        setShowBottomButtons(showBottomButtons)

        viewModel.apply {
            highlightTxt.observe(viewLifecycleOwner) {
                if (it.isEmpty()) return@observe
                CoroutineScope(Dispatchers.Main).launch {
                    if (guideText.contains(it)) {
                        binding.challengeGuideTv.text = binding.challengeGuideTv.text
                            .partialFontAndColored(
                                Theme.extraBold(requireContext()),
                                ContextCompat.getColor(requireContext(), R.color.red_300),
                                it
                            )
                    }
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
}