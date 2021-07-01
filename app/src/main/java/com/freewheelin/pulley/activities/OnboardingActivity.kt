package com.freewheelin.pulley.activities

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.isMobileUI
import com.freewheelin.pulley.bases.isNeedOnboarding
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import kotlinx.android.synthetic.main.activity_onboarding.*

class OnboardingActivity : BaseActivity() {




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
        setContentView(R.layout.activity_onboarding)

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
        viewPager.adapter = OnboardingAdapter(supportFragmentManager)
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val frag = fragments[viewPager.currentItem]
                frag.runAnim()
            }
        })

        setStartButton()
    }

    fun setStartButton() {

        startBtn.visibility = View.VISIBLE
        startBtn.setOnClickListener {
            isNeedOnboarding = false
            val intent = StartActivity.getIntent(this)
            startActivity(intent)
            finish()
        }

        if(isMobileUI) startBtn.text = "다음"
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
