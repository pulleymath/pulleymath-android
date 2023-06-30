package com.freewheelin.pulley.legacy.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.legacy.bases.isMobileUI
import com.freewheelin.pulley.legacy.bases.isNeedOnboarding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.ActivityOnboardingBinding
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent

class OnboardingActivity : BaseActivity() {

    private val binding: ActivityOnboardingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_onboarding,null,false)
    }

    lateinit var firstLottieFragment: OnboardingPageFragment
    lateinit var secondLottieFragment: OnboardingPageFragment
    lateinit var thirdLottieFragment: OnboardingPageFragment

    val FIRST_FRAG_KEY = "FIRST_FRAG_KEY"
    val SECOND_FRAG_KEY = "SECOND_FRAG_KEY"
    val THIRD_FRAG_KEY = "THIRD_FRAG_KEY"

    val fragments: List<OnboardingPageFragment>
        get() = listOf(firstLottieFragment, secondLottieFragment, thirdLottieFragment)
    val keyFragment:List<String> = listOf(FIRST_FRAG_KEY, SECOND_FRAG_KEY, THIRD_FRAG_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var firstFragment: OnboardingPageFragment? = null
        var secondFragment: OnboardingPageFragment? = null
        var thirdFragment: OnboardingPageFragment? = null
        if(savedInstanceState != null) {
            firstFragment = supportFragmentManager.getFragment(savedInstanceState, FIRST_FRAG_KEY) as? OnboardingPageFragment
            secondFragment = supportFragmentManager.getFragment(savedInstanceState, SECOND_FRAG_KEY) as? OnboardingPageFragment
            thirdFragment = supportFragmentManager.getFragment(savedInstanceState, THIRD_FRAG_KEY) as? OnboardingPageFragment
        }


        firstLottieFragment = firstFragment ?: if(isMobileUI)
            OnboardingPageFragment.newInstance("m_onboarding_1.json", true, false)
        else
            OnboardingPageFragment.newInstance("onboarding_1.json", true, false)

        secondLottieFragment = secondFragment ?: if(isMobileUI)
            OnboardingPageFragment.newInstance("m_onboarding_2.json", false, false)
        else
            OnboardingPageFragment.newInstance("onboarding_2.json", false, false)

        thirdLottieFragment = thirdFragment ?: if(isMobileUI)
            OnboardingPageFragment.newInstance("m_onboarding_3.json", false, true)
        else
            OnboardingPageFragment.newInstance("onboarding_3.json", false, true)

        initUI()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        for(i in fragments.indices) {
            if(fragments[i].isAdded) supportFragmentManager.putFragment(outState, keyFragment[i], fragments[i])
        }
    }

    fun initUI() {
        binding.viewPager.adapter = OnboardingAdapter(supportFragmentManager)
        binding.viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val frag = fragments[binding.viewPager.currentItem]
                frag.runAnim()
            }
        })

        setStartButton()
    }

    fun setStartButton() {

        binding.startBtn.visibility = View.VISIBLE
        binding.startBtn.setOnClickListener {
            isNeedOnboarding = false
            val intent = StartActivity.getIntent(this)
            startActivity(intent)
            finish()
        }

        if(isMobileUI) binding.startBtn.text = "다음"
    }

    override fun onBackPressed() {
        LogUtils.logEvent(this, user, PulleyEvent.DIALOG,"이탈방지", "가지마팝업", "온보딩")
        DialogUtils.showReluctanceDialog(this, leftBtnCB = {
            finish()
        })
    }

    inner class OnboardingAdapter(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}
