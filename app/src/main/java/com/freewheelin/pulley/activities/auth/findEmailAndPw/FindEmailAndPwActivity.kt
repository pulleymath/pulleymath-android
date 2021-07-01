package com.freewheelin.pulley.activities.auth.findEmailAndPw

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_find_email_and_pw.*

class FindEmailAndPwActivity : BaseActivity() {

    companion object {
        const val PAGE = "page"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_email_and_pw)

        backBtn.setOnClickListener {
            finish()
        }

        val pagerAdapter = FindPagerAdapter(listOf(FindEmailFragment.newInstance(), FindPwFragment.newInstance()), this)
        viewPager.adapter = pagerAdapter

        val titles = listOf("이메일 찾기", "비밀번호 재설정")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles.get(position)
        }.attach()

        intent.getIntExtra(PAGE, 0).let { page ->
            viewPager.setCurrentItem(page, false)
        }
    }

    class FindPagerAdapter(val fragmentList: List<Fragment>, fragmentActivity: FragmentActivity)
        : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount() = fragmentList.size
        override fun createFragment(position: Int) = fragmentList.get(position)
    }
}