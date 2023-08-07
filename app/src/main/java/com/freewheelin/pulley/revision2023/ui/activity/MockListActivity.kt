package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityMockListBinding
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam.MyMockFragment
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam.NewMockFragment
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.showExpandVertical
import com.freewheelin.pulley.revision2023.viewmodel.MockListActViewModel
import com.google.android.material.tabs.TabLayoutMediator

interface MockTabListener {
    fun onMockTestFinished()
    fun onNewExamBtnClicked()
}


class MockListActivity : AppCompatActivity(), LifecycleObserver, MockTabListener {
    val binding: ActivityMockListBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_mock_list, null, false)
    }
    val viewModel: MockListActViewModel by viewModels()
    companion object {
        const val RESULT_MOCK_FINISH = 20
        const val REQUEST_MOCK_TEST = 10
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, MockListActivity::class.java).apply {

            }
        }
    }

    var tabFragments: MutableList<Fragment> = mutableListOf(
        NewMockFragment.newInstance(),
        MyMockFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUI()
        addBackBtnCallback()
    }
    private fun backBtnAction() {
        finish()
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
            vm = viewModel
            lifecycleOwner = this@MockListActivity

            var count = 1
            tabFragments.forEach {
                if (count == 1) {
                    (it as NewMockFragment).listener = this@MockListActivity
                } else {
                    (it as MyMockFragment).listener = this@MockListActivity
                }
                count += 1
            }

            backBtn.setOnClickListener {
                backBtnAction()
            }

            viewPager.adapter = ViewPagerAdapter(tabFragments, supportFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val menuName = tabTitles.get(position)
                    LogUtils.logEvent(this@MockListActivity, user, PulleyEvent.MENU_CLICK, "모의고사", "${menuName}탭")
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
    fun expandHeader (value: Boolean) {
        binding.headerLl.showExpandVertical(value)
    }
    private fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            val newMockFragment = tabFragments.find { it is NewMockFragment }
            if (newMockFragment is NewMockFragment && newMockFragment.getFilterLlHeight() < 10) {
                newMockFragment.expandFilterLl()
            } else {
                backBtnAction()
            }
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
