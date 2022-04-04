package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestPageFinishBinding
import com.freewheelin.pulley.model.contents.Test
import java.util.*

class TestPageFinishFragment : TestPageBaseFragment() {
    override var test: Test? = null
    lateinit var binding: FragmentTestPageFinishBinding
    companion object {
        fun newInstance(test: Test): TestPageFinishFragment {
            val fragment = TestPageFinishFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_page_finish, container, false)
        return binding.root
    }

    override fun configureBy(test: Test) {
        binding.titleTv.text = when(test.getTestType()) {
            Test.TestType.daily -> "데일리 테스트\n완료"
            Test.TestType.weekly -> "주간 테스트\n완료"
            Test.TestType.wrong -> "오답 테스트\n완료"
            Test.TestType.initial -> "첫 테스트\n완료!"
            else -> "테스트\n완료"
        }


        binding.descTv.text = when(test.getTestType()) {
            Test.TestType.daily -> curation.getDailyListFinishQ(Date())
            Test.TestType.weekly -> "다음 주간 테스트는\n토요일 오전 6시에 공개됩니다 :)"
            Test.TestType.wrong -> "오답 테스트는\n무제한 응시 가능합니다 :)"
            Test.TestType.initial -> "진짜 테스트는 이제부터 시작!\n매일 오전 6시에\n데일리 테스트가 공개됩니다!"
            else -> "테스트\n완료"
        }
    }
}
