package com.freewheelin.pulley.revision2023.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogSnackTestRecommendSettingBinding
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2023.ui.fragment.*
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx

class SnackTestRecommendSettingDialog(val test: Test, val dismissCallback: () -> Unit): DialogFragment() {

    private val viewModel: RecommendSettingViewModel by viewModels()

    private val binding: DialogSnackTestRecommendSettingBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_snack_test_recommend_setting, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    override fun onDismiss(dialog: DialogInterface) {
        dismissCallback()
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()
            viewModel.setStep = { setChildFragment(it) }
            viewModel.replaceStep = { replaceChildFragment(it) }
            viewModel.removeStep = { removeFragment(it) }
            setChildFragment(ViewType.출제범위선택)

            viewModel.onExitClickCallback = {
                dismiss()
            }



        }
    }

    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootCl.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        lp.width = 640.toPx()
        println("zxpzxp ${lp.width}, ${lp.height}")
        binding.rootCl.layoutParams = lp
    }
    fun setChildFragment(step: ViewType) {
        val frag = getFragment(step)
        sendViewModel(frag)
        moveTo(frag)
    }

    fun moveTo(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_left, R.anim.exit_to_right)
            add(R.id.containerFl, frag)
            addToBackStack(null)
            commit()
        }
    }
    fun replaceChildFragment(step: ViewType) {
        val frag = getFragment(step)
        sendViewModel(frag)
        replaceTo(frag)
    }
    fun replaceTo(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
//            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right, R.anim.enter_to_right, R.anim.exit_to_left)
            setCustomAnimations(R.anim.enter_to_left, 0)
            replace(R.id.containerFl, frag)
            addToBackStack(null)
            commit()
        }
    }
    fun removeFragment(frag: Fragment) {
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
            remove(frag)
            commit()
        }
    }

    enum class ViewType {
        출제범위선택,
        고등공통과목수정,
        고등선택과목수정,
        중등과목수정;
    }
    fun getFragment(step: ViewType): Fragment {
        return when (step) {
            ViewType.출제범위선택 -> SnackTestSelectExamRangeFragment.newInstance(test)
            ViewType.고등공통과목수정 -> SnackTestHighCommonSubjectModifyFragment.newInstance()
            ViewType.고등선택과목수정 -> SnackTestHighOptionalSubjectModifyFragment.newInstance()
            ViewType.중등과목수정 -> SnackTestMiddleSubjectModifyFragment.newInstance()
        }
    }
    fun sendViewModel(frag: Fragment) {
        (frag as? SnackTestSelectExamRangeFragment)?.apply {
            viewModel = this@SnackTestRecommendSettingDialog.viewModel
        }
        (frag as? SnackTestHighCommonSubjectModifyFragment)?.apply {
            viewModel = this@SnackTestRecommendSettingDialog.viewModel
        }
        (frag as? SnackTestHighOptionalSubjectModifyFragment)?.apply {
            viewModel = this@SnackTestRecommendSettingDialog.viewModel
        }
        (frag as? SnackTestMiddleSubjectModifyFragment)?.apply {
            viewModel = this@SnackTestRecommendSettingDialog.viewModel
        }
    }

}