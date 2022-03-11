package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.utils.DialogUtils
import kotlinx.android.synthetic.main.activity_init_setting.*
import java.util.*

class InitSettingActivity : AppCompatActivity() {

    var adapter:InitSettingAdapter? = null
    private lateinit var personalFragment:InitSettingPersonalFragment
    private lateinit var learningFragment:InitSettingLearningFragment
    private lateinit var selectionFragment:InitSettingSelectionFragment

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InitSettingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeStatusBar()
        setContentView(R.layout.activity_init_setting)
        setUpUI()
    }

    override fun onResume() {
        super.onResume()
        removeStatusBar()
    }

    private fun setUpUI() {
        setViewPager(makeFragments())
    }

    private fun makeFragments() : List<Fragment> {
        personalFragment = InitSettingPersonalFragment()
        learningFragment = InitSettingLearningFragment()
        selectionFragment = InitSettingSelectionFragment()
        return listOf(personalFragment, learningFragment, selectionFragment)
    }

    private fun setViewPager(fragments:List<Fragment>) {
        adapter = InitSettingAdapter(fragments,this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
    }

    private fun removeStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.insetsController?.hide(WindowInsets.Type.statusBars())
//        } else {
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN
//            )
//        }
    }

    override fun onBackPressed() {
        when(viewPager.currentItem) {
            0 -> DialogUtils.showReluctanceDialog(this, leftBtnCB = {
                    finish()
                })
            1,2 -> viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun prev() {
        if(viewPager.currentItem - 1 >= 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun next() {
        if(adapter?.itemCount?:0 > viewPager.currentItem + 1) {
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    fun complete() {
        val grade = personalFragment.getGrade()
        val major = personalFragment.getMajor()
        var rating = personalFragment.getRating()

        if (grade == Grade.BeforeHigh) {
            rating = 0
        }

        if (grade == null || rating == null) return

        val commonSubject = learningFragment.getSelectedUnit()
        val optionalSubject = selectionFragment.getSelectedUnit()

        UserManager.setUserInitSetting(this, user!!, grade, major, rating, commonSubject, optionalSubject) {
            val intent = InitSettingCompleteActivity.getIntent(this)
            startActivity(intent)
            finish()
        }
    }
}

class InitSettingAdapter(private val fragmentList:List<Fragment>, fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}