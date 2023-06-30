package com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.LearningTabFragment
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentMockTestBinding
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.hide
import com.freewheelin.pulley.revision2023.ui.fragment.MainTabFragment
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.google.android.material.tabs.TabLayoutMediator

interface MockTabListener {
    fun onMockTestFinished()
    fun onNewExamBtnClicked()
}

class MockExamFragment : MainTabFragment(),
        MockTabListener {

    companion object {
        @JvmStatic
        fun newInstance() = MockExamFragment()

        const val RESULT_MOCK_FINISH = 20
        const val REQUEST_MOCK_TEST = 10
    }

    lateinit var binding: FragmentMockTestBinding
    var tabFragments: MutableList<Fragment> = mutableListOf(
        NewMockFragment.newInstance(),
        MyMockFragment.newInstance()
    )

    override var type: MainTab = MainTab.모의고사

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var count = 1
        tabFragments.forEach {
            if (count == 1) {
                (it as NewMockFragment).listener = this
            } else {
                (it as MyMockFragment).listener = this
            }
            count += 1
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mock_test, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onMockTestFinished() {
        binding.viewPager.setCurrentItem(1, false)
    }

    override fun onNewExamBtnClicked() {
        binding.viewPager.setCurrentItem(0, false)
    }
    fun checkPendingHide() {
        (tabFragments[0] as NewMockFragment).setHidePending()
    }

    private var tabTitles = arrayOf("새로 풀기", "나의 모의고사")
    fun initUI() {
        binding.apply {
            lifecycleOwner = this@MockExamFragment

            viewPager.adapter = ViewPagerAdapter(tabFragments, childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val menuName = tabTitles.get(position)
                    LogUtils.logEvent(requireContext(), user, PulleyEvent.MENU_CLICK, "모의고사", "${menuName}탭")
                    if (position == 0) {
                        checkPendingHide()
                    }
                }
            })
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()
        }
    }
}

class ViewPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
