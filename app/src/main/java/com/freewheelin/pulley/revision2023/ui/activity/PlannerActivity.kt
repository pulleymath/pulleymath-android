package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityPlannerBinding
import com.freewheelin.pulley.revision2023.model.UserPlannerItem
import com.freewheelin.pulley.revision2023.ui.adapter.StudyPlannerAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.UserPlannerAdapter
import com.freewheelin.pulley.revision2023.utils.listeners.UserPlannerItemClickListener
import com.freewheelin.pulley.revision2023.viewmodel.PlannerActViewModel
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.model.CurriculumSubject
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.ui.fragment.MainFragment
import org.joda.time.LocalDate

class PlannerActivity : AppCompatActivity() {

    private val binding: ActivityPlannerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_planner, null, false)
    }

    private val viewModel: PlannerActViewModel by viewModels()
    companion object {
        const val PLANNER_MONDAY = "PLANNER_MONDAY"
        const val PLANNER_SUNDAY = "PLANNER_SUNDAY"
    }
    // 왼쪽
    private var plannerAdapter = UserPlannerAdapter(object : UserPlannerItemClickListener {
        override fun onPlanClick(item: UserPlannerItem) {
            viewModel.selectedUserPlan.postValue(item)
            viewModel.expandSelectedPlan(item)
        }

        override fun onRemoveItemClick(item: UserPlannerItem) {
            if (item.isHomework) {
                DaebakToast.show(this@PlannerActivity, "선생님이 추가한 일정은 삭제할 수 없어요.")
                return
            }
            viewModel.selectedUserPlan.postValue(item)
            viewModel.deleteUserPlan(item)
        }
    })
    // 오른쪽
    private var studyPlannerAdapter = StudyPlannerAdapter { studyPlanItem ->
        viewModel.selectedUserPlan.value?.let {
            viewModel.postDailyPlan(it, studyPlanItem)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            vm = viewModel
            lifecycleOwner = this@PlannerActivity
            initSchoolType()
            initUserPlanList()

            onBackPressedDispatcher.addCallback(this@PlannerActivity) {
                backBtnAction()
            }
            btnBack.setOnClickListener {
                backBtnAction()
            }
            plannerPrevNaviBtn.setOnClickListener {
                viewModel.movePrevPlannerWeek()
            }
            plannerNextNaviBtn.setOnClickListener { 
                viewModel.moveNextPlannerWeek()
            }

            plannerRv.apply {
                layoutManager = LinearLayoutManager(this@PlannerActivity, LinearLayoutManager.VERTICAL, false)
                adapter = plannerAdapter
                viewModel.plannerAdapter = plannerAdapter
            }
            expandablePlanRv.apply {
                layoutManager = LinearLayoutManager(this@PlannerActivity, LinearLayoutManager.VERTICAL, false)
                adapter = studyPlannerAdapter
                viewModel.studyPlannerAdapter = studyPlannerAdapter
            }
        }
        viewModel.apply {
            val owner = this@PlannerActivity
            fetchCurriculumSubjects()
            var plannerAdapterCallbackOnce : (List<UserPlannerItem>) -> Unit = { list ->
                list.find { it.isToday }?.let { todayItem ->
                    val todayItemPosition = list.indexOf(todayItem)
                    binding.plannerRv.scrollToPosition(todayItemPosition)
                    binding.plannerRv.scrollY -= 24
                }
            }
            userPlanItems.observe(owner) { list ->
                plannerAdapter.submitList(list) {
                    plannerAdapterCallbackOnce(list)
                    plannerAdapterCallbackOnce = {}
                }
            }
            studyPlanItems.observe(owner) { list ->
                this@PlannerActivity.studyPlannerAdapter.clearItems()
                this@PlannerActivity.studyPlannerAdapter.setItems(list)
                this@PlannerActivity.studyPlannerAdapter.notifyDataSetChanged()
            }
            selectedUserPlan.observe(owner) {
                userPlanItems.value?.let { items ->
                    items.forEach { item ->
                        item.isSelectedDate.set(item.userPlanId == it.userPlanId)
                        item.isSelectedUserPlan.set(item.itemId == it.itemId)
                    }
                }
            }
            schoolType.observe(owner) { school ->
                if (curriculumSubjects.value != null) {
                    val subjects = curriculumSubjects.value?.filter { it.schoolType == school.name }
                    val subjectLl = if(school.isHigh) {
                        binding.highSchoolSubjectLl
                    } else if (school.isMiddle) {
                        binding.middleSchoolSubjectLl
                    } else if (school.isElementary) {
                        binding.elementarySchoolSubjectLl
                    } else null
                    subjectLl?.let {
                        it.removeAllViews()
                        subjects?.forEach { subject ->
                            val subjectBtn = makeTextButton(subject)
                            it.addView(subjectBtn)
                        }
                    }
                }
            }
            curriculumSubjects.observe(owner) {
                schoolRenew()
            }
            selectedSubjectId.observe(owner) { selectedId ->
                val school = schoolType.value ?: SchoolType.HIGH
                val subjectLl = if(school.isHigh) {
                    binding.highSchoolSubjectLl
                } else if (school.isMiddle) {
                    binding.middleSchoolSubjectLl
                } else if (school.isElementary) {
                    binding.elementarySchoolSubjectLl
                } else null
                subjectLl?.let { ll ->
                    ll.children.forEach { view ->
                        if (view is TextView) {
                            val subjectId = view.tag as? Int ?: return@forEach
                            val isSelected = subjectId == selectedId

                            view.setBackgroundResource(
                                if (isSelected) R.drawable.bg_purple_100_round_28_ripple
                                else R.drawable.bg_white_round_28_ripple_gray200
                            )
                            view.setTextColor(
                                if (isSelected) ContextCompat.getColor(this@PlannerActivity, R.color.purple_300)
                                else ContextCompat.getColor(this@PlannerActivity, R.color.gray_600)
                            )
                        }
                    }
                }
            }
            errorMessage.observe(owner) {
                DaebakToast.show(owner, it)
            }
        }
    }
    private fun makeTextButton(subject: CurriculumSubject): TextView {
        return with(viewModel) {
            val subjectBtn = TextView(this@PlannerActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8f,
                        resources.displayMetrics
                    ).toInt()
                }
                text = subject.name
                tag = subject.id
                setBackgroundResource(if (selectedSubjectId.value == subject.id) R.drawable.bg_purple_100_round_28_ripple else R.drawable.bg_white_round_28_ripple_gray200)
                setTextColor(if (selectedSubjectId.value == subject.id) ContextCompat.getColor(this@PlannerActivity, R.color.purple_300) else ContextCompat.getColor(this@PlannerActivity, R.color.gray_600))
                TextViewCompat.setTextAppearance(this, R.style.b1)

                // 패딩 설정
                val horizontalPaddingPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16f,
                    resources.displayMetrics
                ).toInt()

                val verticalPaddingPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6f,
                    resources.displayMetrics
                ).toInt()

                setPadding(
                    horizontalPaddingPx,  // left
                    verticalPaddingPx,    // top
                    horizontalPaddingPx,  // right
                    verticalPaddingPx     // bottom
                )

                setOnClickListener {
                    onHeaderSubjectBtnClick(subject.id)
                }
            }
            return@with subjectBtn
        }

    }

    private fun initUserPlanList() {
        val monday = intent.getStringExtra(PLANNER_MONDAY)
        val sunday = intent.getStringExtra(PLANNER_SUNDAY)
        viewModel.updateUserPlanList(monday, sunday)
    }

    private fun initSchoolType() {
        viewModel.initSchoolType(schoolType)
    }

    private fun backBtnAction () {
        val intent = Intent().apply {
            val monday = LocalDate(viewModel.selectedMondayOfTheWeek.value).toString("yyyy-MM-dd")
            val sunday = LocalDate(viewModel.selectedSundayOfTheWeek.value).toString("yyyy-MM-dd")
            putExtra(PLANNER_MONDAY, monday)
            putExtra(PLANNER_SUNDAY, sunday)
        }
        setResult(MainFragment.PLANNER_RESULT, intent)
        finish()
    }
}