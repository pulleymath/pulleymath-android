package com.freewheelin.pulley.activities.mypage

import android.content.Context
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import kotlinx.android.synthetic.main.dialog_my_study_info_setting.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyStudyInfoSettingDialog(context: Context, override val user: User, listener: MyPageSettingDialogListener) : MyPageSettingBaseDialog(context, user, listener), DabakTabRadioListener {


    val grades = listOf(Grade.BeforeHigh, Grade.High_1, Grade.High_2, Grade.High_3, Grade.AfterHigh)
    val ratings = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val majors = listOf(Major.liberal_arts, Major.natural_sciences)

    init {
        setContentView(R.layout.dialog_my_study_info_setting)
        initUI()
    }

    fun initUI() {
        gradeTab.labels = grades.map { it.tabTitle }
        majorTab.labels = majors.map { it.title }
        ratingTab.labels = ratings.map { it.toString() }

        gradeTab.selectedIndex = grades.indexOf(user.grade)
        majorTab.selectedIndex = majors.indexOf(user.major)
        ratingTab.selectedIndex = ratings.indexOf(user.rating)
        configUI()

        gradeTab.listener = this
        majorTab.listener = this
        ratingTab.listener = this

        gradeTab.tabs?.getOrNull(0)?.apply {
            val lp = this.layoutParams
            lp.width = 155.toPx()
            gradeTab.requestLayout()
        }

        gradeTab.tabs?.last()?.apply {
            val lp = this.layoutParams
            lp.width = 89.toPx()
            gradeTab.requestLayout()
        }

        modifyBtn.setOnClickListener {
            onModifyBtnClicked()
        }

        modifyBtn.toDisableUI()
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        val grade = grades.getOrNull(gradeTab.selectedIndex)
        val major = majors.getOrNull(majorTab.selectedIndex)
        val rating = ratings.getOrNull(ratingTab.selectedIndex)

        configUI()

        if (checkBtn(grade, major, rating)) {
            modifyBtn.toEnableUI()
        } else {
            modifyBtn.toDisableUI()
        }
    }

    fun configUI() {
        val grade = grades.getOrNull(gradeTab.selectedIndex)

        if (grade == Grade.BeforeHigh) {
            majorLabel.visibility = View.GONE
            majorTab.visibility = View.GONE
            ratingLabel.visibility = View.GONE
            ratingTab.visibility = View.GONE
        } else if (grade == Grade.High_1) {
            majorLabel.visibility = View.GONE
            majorTab.visibility = View.GONE
            ratingLabel.visibility = View.VISIBLE
            ratingTab.visibility = View.VISIBLE
        } else {
            majorLabel.visibility = View.VISIBLE
            majorTab.visibility = View.VISIBLE
            ratingLabel.visibility = View.VISIBLE
            ratingTab.visibility = View.VISIBLE
        }
    }


    fun isAvailableRating(rating: Int?): Boolean {
        val ratingSet = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        return ratingSet.contains(rating)
    }

    fun isAvailableMajor(major: Major?): Boolean {
        return major == Major.natural_sciences || major == Major.liberal_arts
    }

    fun checkBtn(grade: Grade?, major: Major?, rating: Int?): Boolean {
        if (grade == null) {
            return false
        } else if (grade == Grade.BeforeHigh) {
            return true
        } else {
            if (grade == Grade.High_1) {
                return isAvailableRating(rating)
            } else {
                return isAvailableRating(rating) && isAvailableMajor(major)
            }
        }
    }


    private fun onModifyBtnClicked() {
        val grade = grades.getOrNull(gradeTab.selectedIndex)
        var major = majors.getOrNull(majorTab.selectedIndex)
        var rating = ratings.getOrNull(ratingTab.selectedIndex)

        if (grade == Grade.BeforeHigh) {
            major = Major.common
            rating = 0
        } else if(grade == Grade.High_1) {
            major = Major.common
        }
        if (!modifyBtn.isEnableUI() || grade == null || major == null || rating == null) return

        val param = Parameter(
                "grade" to grade.value
                ,"majorType" to major.value
                ,"initMoGrade" to rating
                ,"regionID" to user.regionID
                ,"schoolID" to user.schoolID
        )

        API_V2.setUserInfo(user.studentID, param as Parameter).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {}

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    user.update(grade = grade.value, majorType = major.value, initMoGrade = rating)
                    dismiss()
                    listener?.onModifyCompleted(user)
                }
            }
        })

    }
}