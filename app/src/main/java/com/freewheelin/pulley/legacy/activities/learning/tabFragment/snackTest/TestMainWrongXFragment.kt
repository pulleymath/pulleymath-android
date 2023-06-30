package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestMainWrongXBinding
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.*

class TestMainWrongXFragment : TestMainBaseFragment() {
    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.wrong

    lateinit var binding: FragmentTestMainWrongXBinding
    companion object {
        fun newInstance(test: Test): TestMainWrongXFragment {
            val fragment = TestMainWrongXFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_wrong_x, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI(test!!)
        with(binding) {
            moveTv.extensionTouchArea(24.toPx())
            moveTv.setOnClickListener {
                listener?.onMoveBtnClikced(test!!)
            }
        }
    }

    override fun configureUI(test: Test) {
        with(binding) {
            if(test.wrongInfo.totalProblemCount == 0) {
                headerTv.text = "${test.startDate.month()}월 ${test.startDate.day()}일" +
                    " - ${test.endDate.month()}월 ${test.endDate.day()}일 동안\n학습한 기록이 없어요!"
                contentsTv.text = "공부를 해야 나의 약점도 알 수 있어요 :)\n유형학습 먼저 해볼까요?"
                moveTv.text = "유형학습으로 가기 >"
            } else {
                headerTv.text = "${test.startDate.month()}월 ${test.startDate.day()}일" +
                    " - ${test.endDate.month()}월 ${test.endDate.day()}일의\n오답 문항이 없어요!"
                contentsTv.text = "대단해요! :)\n문제를 모두 클리어했군요?\n지난 주 외 다른 기간의 오답도 확인해볼까요?"
                moveTv.text = "오답노트로 가기 >"
            }
        }
    }

    override fun showMainContents() {
        with(binding) {
            val duration: Long = 200
            headerTv.show(duration) {
                val duration = duration * 2
                contentsTv.show(duration)
                moveTv.show(duration)
            }
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        with(binding) {
            val duration: Long = 200
            headerTv.hide(duration)
            contentsTv.hide(duration)
            moveTv.hide(duration) {
                cb()
            }
        }
    }
}
