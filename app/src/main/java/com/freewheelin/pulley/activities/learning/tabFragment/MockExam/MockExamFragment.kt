package com.freewheelin.pulley.activities.learning.tabFragment.mockExam

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.FragmentMockTestBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.item_arduous_spinner.*

interface MockTabListener {
    fun onMockTestFinished()
    fun onNewExamBtnClicked()
}


class MockExamFragment : LearningTabFragment(),
        MockTabListener {

    companion object {
        @JvmStatic
        fun newInstance() = MockExamFragment()

        const val RESULT_MOCK_FINISH = 20
        const val REQUEST_MOCK_TEST = 10
    }

    lateinit var binding: FragmentMockTestBinding
    var tabFragments: MutableList<Fragment> = mutableListOf(
        MyMockFragment.newInstance(),
        NewMockFragment.newInstance()
    )
//    val myMockFragment = MyMockFragment.newInstance()
//    val newMockFragment = NewMockFragment.newInstance()
    override var screenName = "모의고사"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var count = 1
        tabFragments.forEach {
            if (count == 1) {
                (it as MyMockFragment).listener = this
            } else {
                (it as NewMockFragment).listener = this
            }
            count += 1
        }
//        newMockFragment.listener = this
//        myMockFragment.listener = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mock_test, container, false)
        return binding.root
    }

    override fun onMockTestFinished() {
        binding.viewPager.setCurrentItem(1, false)
    }

    override fun onNewExamBtnClicked() {
        binding.viewPager.setCurrentItem(0, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    private var tabTitles = arrayOf("새로 풀기", "나의 모의고사")
    override fun initUI() {
        binding.apply {
            lifecycleOwner = this@MockExamFragment

//            viewPager.adapter = TabAdapter(childFragmentManager)
//            viewPager.setPagingEnabled(false)
//            tabLayout.setupWithViewPager(viewPager)
//            tabLayout.getTabAt(0)?.customView = TabTextView(requireContext(), "새로 풀기")
//            tabLayout.getTabAt(1)?.customView = TabTextView(requireContext(), "나의 모의고사")

            viewPager.adapter = ViewPagerAdapter(tabFragments, childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()
        }
    }

//    inner class TabAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
//        override fun getItem(position: Int): Fragment {
//            return when(position) {
//                0 -> newMockFragment
//                else -> myMockFragment
//            }
//        }
//
//        override fun getCount(): Int {
//            return 2
//        }
//    }
}

private class TabTextView: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context, title: String): super(context) {
        this.text = title
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if(selected) {
            typeface = Theme.extraBold(context)
            setTextColor(ContextCompat.getColor(context!!, R.color.purple_6D6DFF))
        } else {
            typeface = Theme.bold(context)
            setTextColor(ContextCompat.getColor(context!!, R.color.black_4c4c4c))
        }
    }
}


class ViewPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> fragments[position]
            1 -> {
                val fragment = fragments[position]
                fragment
            }
            else -> fragments[position]
        }
    }
}
