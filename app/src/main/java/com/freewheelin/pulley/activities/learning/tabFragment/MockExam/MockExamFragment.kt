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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.core.Theme
import kotlinx.android.synthetic.main.fragment_mock_test.*

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

    val myMockFragment = MyMockFragment.newInstance()
    val newMockFragment = NewMockFragment.newInstance()
    override var screenName = "모의고사"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newMockFragment.listener = this
        myMockFragment.listener = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mock_test, container, false)
    }

    override fun onMockTestFinished() {
        viewPager.setCurrentItem(1, false)
    }

    override fun onNewExamBtnClicked() {
        viewPager.setCurrentItem(0, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initUI() {
        viewPager.adapter = TabAdapter(childFragmentManager)
        viewPager.setPagingEnabled(false)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)?.customView = TabTextView(context!!, "새로 풀기")
        tabLayout.getTabAt(1)?.customView = TabTextView(context!!, "나의 모의고사")
    }

    inner class TabAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> newMockFragment
                else -> myMockFragment
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }
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

