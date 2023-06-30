package com.freewheelin.pulley.legacy.activities.auth.findEmailAndPw

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.databinding.ActivityFindEmailAndPwBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FindEmailAndPwActivity : BaseActivity() {

    companion object {
        const val PAGE = "page"
        const val IS_GUEST_USER = "IS_GUEST_USER"

        fun getIntent(context: Context, isGuestUser: Boolean): Intent {
            return Intent(context, FindEmailAndPwActivity::class.java).apply {
                putExtra(IS_GUEST_USER, isGuestUser)
            }
        }

    }
    private val binding: ActivityFindEmailAndPwBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_find_email_and_pw, null, false)
    }
    var isGuestUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isGuestUser = intent.getBooleanExtra(IS_GUEST_USER, false)
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }


            val pagerAdapter = FindPagerAdapter(
                listOf(
                    FindEmailFragment.newInstance(isGuestUser),
                    FindPwFragment.newInstance(isGuestUser)
                ), this@FindEmailAndPwActivity
            )
            viewPager.adapter = pagerAdapter

            val titles = listOf("이메일 찾기", "비밀번호 재설정")
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = titles.get(position)
            }.attach()

            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                // 딜레이 없이 실행할 경우 codeContainer 가 visible 될때 codeConfirmBtn가 보이지 않는 버그가 있음
                intent.getIntExtra(PAGE, 0).let { page ->
                    viewPager.currentItem = page
                }
            }

        }
    }

    class FindPagerAdapter(val fragmentList: List<Fragment>, fragmentActivity: FragmentActivity)
        : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount() = fragmentList.size
        override fun createFragment(position: Int) = fragmentList.get(position)
    }
}