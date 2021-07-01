package com.freewheelin.pulley.activities.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.signup.StudentInfoFragment
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyCommonUnitSettingFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyOptionalUnitSettingFragment

import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_my_study_info.*

class MyStudyInfoFragment : MyPageBaseFragment(), MyPageSettingDialogListener {

    companion object {
        const val RELOAD = "study_info_reload"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_study_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        val user = requireActivity().application!!.user!!
        configureUI(user)

        setFragmentResultListener(RELOAD) { key, bundle ->
            reload()
        }
    }

    private fun reload() {
        val user = MyApplication.user!!
        Log.d(javaClass.simpleName, "user=${user.fullName}")
        configureUI(user)
    }

    private fun initUI() {
        modifyBtn.setOnClickListener { moveTo(MyStudyInfoSettingFragment()) }
        modifyCommonBtn.setOnClickListener { moveTo(StudyCommonUnitSettingFragment()) }
        modifySelectBtn.setOnClickListener { moveTo(StudyOptionalUnitSettingFragment()) }
//        deleteAllBtn.setOnClickListener { deleteAll() }
    }

    // 이번에 보류
    private fun deleteAll() {
        DialogUtils.confirmDeleteAllStudy(requireActivity()) {

        }
    }

    private fun configureUI(user: User) {
        gradeTv.text = user.grade.text

        configureSchool(user)

        if (user.grade == Grade.BeforeHigh) {
            majorTv.text = "-"
            ratingTv.text = "-"
        } else {
            majorTv.text = user.major.title
            ratingTv.text = user.ratingText
        }

        configureStudy(user)
    }

    private fun configureSchool(user: User) {
        if(user.schoolID?:0 > 0) {
            schoolLabel.text = "학교"
            schoolTv.text = user.schoolName?:""
        }else if(user.regionID?:0 > 0) {
            schoolLabel.text = "지역"
            schoolTv.text = user.regionName?:""
        }
    }

    private fun configureStudy(user: User) {
        commonSubjectTv.text = user!!.getCommonSubjectText()
        optionalSubjectTv.text = calcNoneText(user?.getOptionalSubjectText())
    }

    private fun calcNoneText(text: String?): String {
        return if (text?.isNotEmpty() == true) text
        else getString(R.string.text_none)
    }

    override fun onModifyCompleted(user: User) {
        CompleteDialog(requireContext(), "수정 완료!\n업데이트되었습니다.", "해당 수정 내역은 추천 문항에 반영됩니다.").showFor()
        configureUI(user)
    }

    fun moveTo(frag: Fragment) {
        (activity as LearningTabActivity).moveTo(frag)
    }
}
