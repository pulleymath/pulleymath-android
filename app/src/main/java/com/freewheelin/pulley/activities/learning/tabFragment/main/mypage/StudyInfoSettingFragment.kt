package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentStudyInfoSettingBinding
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.DabakTabRadioListener
import com.freewheelin.pulley.views.DaebakTabRadio
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StudyInfoSettingFragment : MyPageBaseFragment(), DabakTabRadioListener {

    // 학년정보 수정이 더이상 snack test에서 일어나지 않는것 같다.
    val grades = listOf(Grade.Middle_1, Grade.Middle_2, Grade.Middle_3, Grade.High_1, Grade.High_2, Grade.High_3, Grade.AfterHigh)
    val ratings = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val majors = listOf(Major.liberal_arts, Major.natural_sciences)

    lateinit var binding: FragmentStudyInfoSettingBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_study_info_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }


    fun setUpUI() {
        with(binding) {
            val user = user ?: return
            gradeTab.labels = grades.map { it.tabTitle }
            majorTab.labels = majors.map { it.title }
            ratingTab.labels = ratings.map { it.toString() }

            gradeTab.selectedIndex = grades.indexOf(user.grade)
            majorTab.selectedIndex = majors.indexOf(user.major)
            ratingTab.selectedIndex = ratings.indexOf(user.rating)
            configUI()

            gradeTab.listener = this@StudyInfoSettingFragment
            majorTab.listener = this@StudyInfoSettingFragment
            ratingTab.listener = this@StudyInfoSettingFragment

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
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    fun configUI() {
        with(binding) {
            val grade = grades.getOrNull(gradeTab.selectedIndex)

            if (grade?.isMiddle == true) {
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
    }

    override fun onTabSelected(radio: DaebakTabRadio, index: Int) {
        configUI()
    }

    private fun onModifyBtnClicked() {
        with(binding) {
            val grade = grades.getOrNull(gradeTab.selectedIndex)
            var major = majors.getOrNull(majorTab.selectedIndex)
            var rating = ratings.getOrNull(ratingTab.selectedIndex)

            if (grade?.isMiddle == true) {
                major = Major.common
                rating = 0
            } else if(grade == Grade.High_1) {
                major = Major.common
            }
            if (!modifyBtn.isEnableUI() || grade == null || major == null || rating == null) return

            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "학년성적")
            val param = Parameter(
                    "grade" to grade.value,
                    "majorType" to major.value,
                    "initMoGrade" to rating
            )

            API_V2.setUserInfo(user!!.studentID, param as Parameter).enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {}

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.code() == 200) {
                        user!!.update(grade = grade.value, majorType = major.value, initMoGrade = rating)
                        CompleteDialog(requireContext(), "수정 완료!\n업데이트되었습니다.", "해당 수정 내역은 추천 문항에 반영됩니다.").showFor()
                        val intent = Intent(UserManager.EVENT_USER_MODIFYING)
                        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                        listener?.onModifyCompleted()
                        Handler(Looper.getMainLooper()).postDelayed({
                            onBackBtnClicked()
                        }, 2000)
                    }
                }
            })
        }
    }
}
