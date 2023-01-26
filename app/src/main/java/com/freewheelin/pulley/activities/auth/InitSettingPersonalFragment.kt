package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentInitSettingPersonalBinding
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio

class InitSettingPersonalFragment : Fragment(), DabakTabRadioListener {

    var parent:InitSettingActivity? = null
    lateinit var binding: FragmentInitSettingPersonalBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is InitSettingActivity) parent = activity as InitSettingActivity
    }

    private val grades = listOf(Grade.Middle_1, Grade.Middle_2, Grade.Middle_3, Grade.High_1, Grade.High_2, Grade.High_3, Grade.AfterHigh)
    private val majors = listOf(Major.common, Major.liberal_arts, Major.natural_sciences)
    private val ratings = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_init_setting_personal, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
    }

    private fun setUI() {
        binding.apply {
            nextBtn.toDisableUI()

            firstStepGuideTv.text = "${user?.fullName}님에게 최적화된 서비스를 위해\n" + "정보가 필요해요! :)"

            gradeRadio.labels = grades.map { it.tabTitle }
            majorRadio.labels = majors.map { it.title }
            ratingRadio.labels = ratings.map { it.toString() }

            gradeRadio.listener = this@InitSettingPersonalFragment
            majorRadio.listener = this@InitSettingPersonalFragment
            ratingRadio.listener = this@InitSettingPersonalFragment

            Log.d("개인정보", "grade=${user?.rawGrade}, major=${user?.major}, rating=${user?.rating?:0}")

            gradeRadio.selectedIndex = userGradeToGradeRadioIndex(user?.rawGrade?: -1)
            majorRadio.selectedIndex =  if(user?.major == null) -1 else Major.list.indexOf(user?.major?:0)
            ratingRadio.selectedIndex = (user?.rating?:0) - 1

            nextBtn.setOnClickListener { if(nextBtn.isEnableUI()) parent?.next() }

            setNextBtn()

            setLayout()
        }
    }

    private fun userGradeToGradeRadioIndex(rawGrade: Int): Int {
        return when (rawGrade) {
            1, 2, 3, 4 -> rawGrade + 3
            5, 6, 7 -> rawGrade - 4
            else -> -1
        }
    }
    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        when(radio.id) {
            R.id.gradeRadio -> {
                setLayout()
            }
        }

        setNextBtn()
    }

    private fun setLayout() {
        when(binding.gradeRadio.selectedIndex) {
            0, 1 ,2 -> {
                showRating(false)
                showMajor(false)
            }
            3 -> {
                showRating(true)
                showMajor(false)
            }
            else -> {
                showRating(true)
                showMajor(true)
            }
        }
    }

    private fun showMajor(show:Boolean) {
        binding.apply {
            if(show) {
                majorLabel.visibility = View.VISIBLE
                majorRadio.visibility = View.VISIBLE
            } else {
                majorLabel.visibility = View.GONE
                majorRadio.visibility = View.GONE
                majorRadio.selectedIndex = -1
            }
        }
    }

    private fun showRating(show:Boolean) {
        binding.apply {
            if(show) {
                ratingLabel.visibility = View.VISIBLE
                ratingRadio.visibility = View.VISIBLE
            } else {
                ratingLabel.visibility = View.GONE
                ratingRadio.visibility = View.GONE
                ratingRadio.selectedIndex = -1
            }
        }
    }

    private fun setNextBtn() {
        binding.apply {
            val grade = grades.getOrNull(gradeRadio?.selectedIndex?:-1)
            val major = majors.getOrNull(majorRadio?.selectedIndex?:-1)
            val rating = ratings.getOrNull(ratingRadio?.selectedIndex?:-1)

            if(grade?.isMiddle == true
                || (grade == Grade.High_1 && rating != null)
                || (grade == Grade.High_2 || grade == Grade.High_3 || grade == Grade.AfterHigh) && rating != null && major != null) {
                nextBtn.toEnableUI()
            } else
                nextBtn.toDisableUI()
        }
    }

    fun getGrade() = grades.getOrNull(binding.gradeRadio.selectedIndex?:-1)
    fun getMajor() : Major? = majors.getOrNull(binding.majorRadio.selectedIndex?:-1)
    fun getRating() = ratings.getOrNull(binding.ratingRadio.selectedIndex?:-1)
}