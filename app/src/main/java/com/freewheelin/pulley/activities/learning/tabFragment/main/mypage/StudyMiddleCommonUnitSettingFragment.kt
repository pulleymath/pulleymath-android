package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.activities.mypage.MyStudyInfoFragment
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.databinding.FragmentStudyUnitMiddleSchoolCommonSettingBinding
import com.freewheelin.pulley.revision2023.model.request.UpdateCommonSubjectRequest
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.repository.MyPageRepository
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelection
import com.freewheelin.pulley.revision2023.ui.view.MiddleSchoolUnitSelectionListener
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.views.DaebakToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class StudyMiddleCommonUnitSettingFragment : MyPageBaseFragment(),
    MiddleSchoolUnitSelectionListener {
    lateinit var binding: FragmentStudyUnitMiddleSchoolCommonSettingBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_unit_middle_school_common_setting, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
        initObserve()

    }

    fun initObserve () {
        viewModel.recommendCommonSubjects.observe(viewLifecycleOwner) { subjects ->
            subjects.forEach {
                val subject = SubjectV3.idOfNonNull(it.subjectId)
                binding.apply {
                    val selection = when (subject) {
                        SubjectV3.중1_1 -> middle11Selection
                        SubjectV3.중1_2 -> middle12Selection
                        SubjectV3.중2_1 -> middle21Selection
                        SubjectV3.중2_2 -> middle22Selection
                        SubjectV3.중3_1 -> middle31Selection
                        SubjectV3.중3_2 -> middle32Selection
                        else -> { middle11Selection }
                    }
                    selection.initSelected(it)

                    if (getUnitClicked()) {
                        modifyBtn.toEnableUI()
                    } else {
                        modifyBtn.toDisableUI()
                    }
                }
            }
        }
    }

    fun setUpUI() {
        with(binding) {
            viewModel.fetchRecommendSubject()

            selectionContainer.children.forEach {
                (it as MiddleSchoolUnitSelection).listener = this@StudyMiddleCommonUnitSettingFragment
            }
            modifyBtn.setOnClickListener { onModifyBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    fun setRecommendCommonSubject(subjects: List<RecommendSubject>) {
        subjects.forEach {
            val subject = SubjectV3.idOfNonNull(it.subjectId)
            binding.apply {
                val selection = when (subject) {
                    SubjectV3.중1_1 -> middle11Selection
                    SubjectV3.중1_2 -> middle12Selection
                    SubjectV3.중2_1 -> middle21Selection
                    SubjectV3.중2_2 -> middle22Selection
                    SubjectV3.중3_1 -> middle31Selection
                    SubjectV3.중3_2 -> middle32Selection
                    else -> { middle11Selection }
                }
                selection.initSelected(it)

                if (getUnitClicked()) {
                    modifyBtn.toEnableUI()
                } else {
                    modifyBtn.toDisableUI()
                }
            }
        }

    }
    private fun onModifyBtnClicked() {
        val selectedIds = getClickedUnit()
        if (selectedIds != null) {
            viewModel.updateCommonSubject(selectedIds) {
                setFragmentResult(MyStudyInfoFragment.RELOAD, bundleOf())
                DaebakToast.show(requireContext(), "수정되었습니다")
                onBackBtnClicked()
            }
        } else {
            DaebakToast.show(requireContext(), "한개 이상 선택해주세요.")
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSelectionChanged(view: View) {
        if (getUnitClicked()) {
            binding.modifyBtn.toEnableUI()
        } else {
            binding.modifyBtn.toDisableUI()
        }
    }

    private fun getUnitClicked (): Boolean {
        binding.apply {
            val allBtnList = middle11Selection.getAllBtn()
                .plus(middle12Selection.getAllBtn())
                .plus(middle21Selection.getAllBtn())
                .plus(middle22Selection.getAllBtn())
                .plus(middle31Selection.getAllBtn())
                .plus(middle32Selection.getAllBtn())

            return@getUnitClicked allBtnList.map { it.isSelected }
                .reduce { p1, p2 ->
                    p1 || p2
                }
        }
    }
    private fun getClickedUnit(): List<Int>? {
        binding.apply {
            val allBtnList = middle11Selection.getAllBtn()
                .plus(middle12Selection.getAllBtn())
                .plus(middle21Selection.getAllBtn())
                .plus(middle22Selection.getAllBtn())
                .plus(middle31Selection.getAllBtn())
                .plus(middle32Selection.getAllBtn())

            return allBtnList.filter { it.isSelected }
                .map { it.bigUnits }
                .takeIf { it.isNotEmpty() }
                ?.reduce { p1, p2 ->
                    p1.plus(p2).toMutableList()
                }
                ?.map { it.id }
        }

    }
}
