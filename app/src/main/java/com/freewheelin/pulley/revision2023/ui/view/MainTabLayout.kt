package com.freewheelin.pulley.revision2023.ui.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewLayoutMainTabBinding
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfileV4
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.AffiliatedUniv
import com.freewheelin.pulley.revision2023.viewmodel.MainActViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun interface MainTabListener {
    fun onTabClicked(position: Int, newType: MainTab, prevPosition: Int, prevType: MainTab)
}

enum class MainTab(val indexOnTablet: Int, val indexOnMobile: Int, val names: List<String>) {
    메인(0, 0, listOf("메인")),
    개념(1, 1, listOf("개념")),
    문제풀이(2, 2, listOf("문제풀이")),
//    유형(2, 2, listOf("유형")),
//    모의고사(3, 3, listOf("모의고사")),
//    테스트(4, 4, listOf("테스트")),
//    오답노트(5 ,5, listOf("오답노트")),

    분석(3, 3, listOf("분석")),
    과외(4, -1, listOf("과외, 튜터")),
    대학(5, 4, listOf("대학", "SSU진단", "KU진단"));

    companion object {
        fun convertMainTab(value: String): MainTab {
            return when (value) {
                메인.name -> 메인
                개념.name -> 개념
                문제풀이.name -> 문제풀이
                분석.name -> 분석
                과외.name -> 과외
                대학.name -> 대학
                else -> 메인
            }
        }
        fun convertIndexToMainTab(isTablet: Boolean, index: Int): MainTab {
            return if (isTablet) {
                when (index) {
                    메인.indexOnTablet -> 메인
                    개념.indexOnTablet -> 개념
                    문제풀이.indexOnTablet -> 문제풀이
                    분석.indexOnTablet -> 분석
                    과외.indexOnTablet -> 과외
                    대학.indexOnTablet -> 대학
                    else -> 메인
                }
            } else {
                when (index) {
                    메인.indexOnMobile -> 메인
                    개념.indexOnMobile -> 개념
                    문제풀이.indexOnMobile -> 문제풀이
                    분석.indexOnMobile -> 분석
                    과외.indexOnMobile -> 과외
                    대학.indexOnMobile -> 대학
                    else -> 메인
                }
            }
        }
    }
}
class MainTabLayout: FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    var listener: MainTabListener? = null
    lateinit var binding: ViewLayoutMainTabBinding
    var prevTabType = MainTab.메인
    var prevSchoolType: SchoolType = SchoolType.HIGH
    var prevPosition = 0

    init {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_layout_main_tab, this, true)
        binding.apply {
            lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
            initOnDevice()
        }
    }
    fun setViewModel(viewModel: MainActViewModel) {
        binding.vm = viewModel
    }
    fun initOnDevice() {
        binding.apply {
            disableAllListener()
            if (context.isTablet) { initTabletUI() }
            if (context.isMobile) { initMobileUI() }
        }
    }
    private fun disableAllListener() {
        binding.apply {
            tabletTabRootLl.children.forEach {
                it.setOnClickListener(null)
            }
            mobileTabRootLl.children.forEach {
                it.setOnClickListener(null)
            }
        }
    }

    private fun initTabletUI () {
        tabletVisibleIf(true)
        mobileVisibleIf(false)
        binding.apply {
            tabletTabRootLl.children.forEachIndexed { index, view ->
                setTabClickListener(view, index)
            }
            getTabletUnivTab().apply {
                visibleIf(user?.schoolType == SchoolType.UNIVERSITY)
                text = getUnivTabText()
            }
        }
    }

    fun setTabClickListener(view: View, index: Int) {
        (view as? TextView)?.let {
            val newTabType = getTabType(it)
            view.setOnClickListener {
                onTabClick(index, newTabType, prevPosition, prevTabType)
            }
        }
    }

    private fun getTabType(tv: TextView): MainTab {
        return MainTab.convertMainTab(tv.text.toString())
    }

    fun selectTap(index: Int, tab: MainTab) {
        onTabClick(index, tab, prevPosition, prevTabType)
    }

    fun selectTap(selectedIndex: Int) {
        val selectedTab = getTabByIndex(selectedIndex)
        onTabClick(selectedIndex, selectedTab, prevPosition, prevTabType)
    }

    fun onTabClick(index: Int, selectedTab: MainTab, prevIndex: Int, prevTab: MainTab) {
        listener?.onTabClicked(index, selectedTab, prevIndex, prevTab)
        prevTabType = selectedTab
        prevPosition = index
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(300)
            binding.currentTab = selectedTab
//            binding.executePendingBindings()
//        }
    }

    fun addOnTabListener(tabListener: MainTabListener) {
        listener = tabListener
    }
    fun getCurrentTab(): MainTab? {
        return binding.currentTab
    }

    private fun initMobileUI() {
        tabletVisibleIf(false)
        mobileVisibleIf(true)
        binding.apply {
            mobileTabRootLl.children.forEachIndexed { index, view ->
                setTabClickListener(view, index)
            }

            getMobileUnivTab().apply {
                visibleIf(user?.schoolType == SchoolType.UNIVERSITY)
                text = getUnivTabText()
            }
        }
    }

    fun tabletVisibleIf(value: Boolean) {
        binding.apply {
            tabletTabRootLl.visibleIf(value)
        }
    }
    fun mobileVisibleIf(value: Boolean) {
        binding.apply {
            mobileTabScrollRoot.visibleIf(value)
        }
    }

    fun getUnivTabText(): String {
        return when(user?.schoolID) {
            AffiliatedUniv.Konkuk.schoolId -> "KU진단"
            AffiliatedUniv.Soongsil.schoolId -> "SSU진단"
            else -> "대학"
        }
    }


    fun getTabParentView(): ViewGroup {
        return if(context.isTablet) binding.tabletTabRootLl else binding.mobileTabRootLl
    }

    fun getTabByIndex(index: Int): MainTab {
        return MainTab.convertIndexToMainTab(context.isTablet, index)
    }

    fun getTabletUnivTab(): TextView = binding.tab5

    fun getMobileUnivTab(): TextView = binding.mobileTab4

    fun updateSchoolType() {
        prevSchoolType = when (binding.schoolType) {
            SchoolType.ELEMENTARY -> SchoolType.ELEMENTARY
            SchoolType.MIDDLE -> SchoolType.MIDDLE
            SchoolType.HIGH -> SchoolType.HIGH
            SchoolType.UNIVERSITY -> SchoolType.UNIVERSITY
            null -> SchoolType.HIGH
        }

        binding.schoolType = schoolType
    }

    private fun setTextColorWithAnim(textView: TextView) {
        binding.currentTab?.let { selected ->
            val isMiddle = schoolType.isMiddle
            val tabName = textView.text.toString()

            val fromTextColor = if (tabName in selected.names) {
                if (isMiddle) R.color.white else R.color.gray_800
            } else {
                R.color.gray_700
            }

            val toTextColor = if (tabName in selected.names) {
                if (isMiddle) R.color.gray_800 else R.color.white
            } else {
                R.color.gray_700
            }

            val colorFrom = ContextCompat.getColor(context, fromTextColor)
            val colorTo = ContextCompat.getColor(context, toTextColor)

            val colorAnimation: ValueAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.addUpdateListener { animator ->
                textView.setTextColor(animator.animatedValue as Int)
            }
            colorAnimation.start()
        }
    }

    private fun setSelectedTabTextColor(textView: TextView) {
        val fromTextColor = when (prevSchoolType) {
            SchoolType.ELEMENTARY -> R.color.white
            SchoolType.MIDDLE -> R.color.gray_800
            SchoolType.HIGH -> R.color.white
            SchoolType.UNIVERSITY -> R.color.white
        }
        val toTextColor = when (schoolType) {
            SchoolType.ELEMENTARY -> R.color.white
            SchoolType.MIDDLE -> R.color.gray_800
            SchoolType.HIGH -> R.color.white
            SchoolType.UNIVERSITY -> R.color.white
        }
        val colorFrom = ContextCompat.getColor(context, fromTextColor)
        val colorTo = ContextCompat.getColor(context, toTextColor)

        val colorAnimation: ValueAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animator ->
            textView.setTextColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }
    private fun setUnSelectedTabTextColor(textView: TextView) {
        val fromTextColor = when (prevSchoolType) {
            SchoolType.ELEMENTARY -> R.color.purple_150
            else -> R.color.gray_700
        }
        val toTextColor = when (schoolType) {
            SchoolType.ELEMENTARY -> R.color.purple_150
            else -> R.color.gray_700
        }
        val colorFrom = ContextCompat.getColor(context, fromTextColor)
        val colorTo = ContextCompat.getColor(context, toTextColor)

        val colorAnimation: ValueAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animator ->
            textView.setTextColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    fun setTabTextColorsBySchoolType() {
        binding.apply {
            currentTab?.let { selectedTab ->
                getTabParentView().children.forEach {
                    if (it is TextView) {
                        val tabName = it.text.toString()
                        if (tabName in selectedTab.names) {
//                            setSelectedTabTextAppearance(it)
                            setSelectedTabTextColor(it)
                        } else {
                            // TODO
//                            println("aspasp tabName : ${tabName} 선택되지 않은것인가? ")
//                            setUnSelectedTabTextAppearance(it)
                            setUnSelectedTabTextColor(it)
                        }
                    }
                }
            }
        }
    }

    fun makeHeaderTransitionDrawable(type: SchoolType): TransitionDrawable {
        val prevColor = when (prevSchoolType) {
            SchoolType.ELEMENTARY -> R.color.purple_300
            SchoolType.MIDDLE -> R.color.white
            SchoolType.HIGH -> R.color.black_200
            SchoolType.UNIVERSITY -> R.color.black_200
        }
        val nextColor = when (type) {
            SchoolType.ELEMENTARY -> R.color.purple_300
            SchoolType.MIDDLE -> R.color.white
            SchoolType.HIGH -> R.color.black_200
            SchoolType.UNIVERSITY -> R.color.black_200
        }
        val prevColorDrawable = ColorDrawable(ContextCompat.getColor(context, prevColor))
        val nextColorDrawable = ColorDrawable(ContextCompat.getColor(context, nextColor))
        val cd = arrayOf(prevColorDrawable, nextColorDrawable)
        return TransitionDrawable(cd)
    }

}