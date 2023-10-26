package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityOnBoardingBinding
import com.freewheelin.pulley.legacy.activities.StartActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.isNeedNewOnBoarding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.ui.fragment.OnBoardingItemFragment
import com.freewheelin.pulley.revision2023.viewmodel.OnBoardingViewModel

class OnBoardingActivity : AppCompatActivity() {
    private val binding: ActivityOnBoardingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_on_boarding,null,false)
    }
    companion object {
        const val ON_BOARDING_IMAGES = "ON_BOARDING_IMAGES"
        fun getIntent(context: Context, images: List<String>): Intent {
            val intent = Intent(context, OnBoardingActivity::class.java)
            intent.putExtra(ON_BOARDING_IMAGES, ArrayList<String>(images))
            return intent
        }
    }
    private val viewModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        println("asoaso 온보딩")
        val images = intent.getSerializableExtra(ON_BOARDING_IMAGES) as? ArrayList<String> ?: listOf()

        binding.apply {
            val imagesSize = images.size
            val tabFragments = images.mapIndexed { index, img ->
                val isLast = index + 1 == imagesSize
                OnBoardingItemFragment.newInstance(img, isLast)
            }
            pager.adapter = PagerAdapter(tabFragments, supportFragmentManager, lifecycle)
            pager.offscreenPageLimit = 3
        }
        viewModel.apply {
            goLoginActCallback = {
                val intent = Intent(this@OnBoardingActivity, StartActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }

    fun moveNextActivity() {
        isNeedNewOnBoarding = false
        if(MyApplication.user?.token?.isNotEmpty() == true) {
            viewModel.fetchUser { user ->
                MyApplication.isAppFirstLaunch = true
                MyApplication.user!!.commit("SplashActivity.isExceedDevice = true, after delete device [success]")
                viewModel.fetchMainProfile {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        LogUtils.logEvent(this, user, PulleyEvent.DIALOG,"이탈방지", "가지마팝업", "온보딩")
        DialogUtils.showReluctanceDialog(this, leftBtnCB = {
            finishAffinity()
        })
    }

    inner class PagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}