package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageActionListener
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.ServerCommunicator
import com.freewheelin.pulley.databinding.ActivityMyPageBinding
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*

class MyPageActivity : AppCompatActivity(), MyPageActionListener {
    private val binding: ActivityMyPageBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_my_page, null, false)
    }
    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, MyPageActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpUI()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        finish()
        return super.onTouchEvent(event)
    }

    fun setUpUI() {
        with(binding) {
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val topBottomMargin = resources.getDimension(R.dimen.dp16) * 2
            val lp = rootView.layoutParams
            lp.height = DisplayUtils.getScrenHeight(this@MyPageActivity) - topBottomMargin.toInt()
            rootView.layoutParams = lp

            pwModifyBtn.setOnClickListener { onPwModifyBtnClicked() }
            membershipBtn.setOnClickListener { onMemebershipBtnClicked() }
            phoneModifyBtn.setOnClickListener { onPhoneModifyBtnClicked() }
            recommendUnitSettingBtn.setOnClickListener { onRecommendUnitSettingBtnClickked() }
            optionalUnitSettingBtn.setOnClickListener { onOptionalUnitSettingBtnClicked() }
            studyInfoSettingBtn.setOnClickListener { onStudyInfoSettingBtnClicked() }
            myPageContainer.setOnTouchListener { view, motionEvent -> true }

            val user = user ?: return
            greetingLabel.text =
                String.format(getString(R.string.greeting_name_format), user.fullName)
            nameTv.text = user.fullName
            emailTv.text = user.email
            phoneTv.text = user.cellPhone
            serviceTv.text = user.serviceName
            availableDurationTv.text = getDurationText(user)
            pwdModifyGuideTv.text = String.format(
                getString(R.string.guide_reset_password_email_info_format),
                user.email
            )

            user.log()

//        val unit = user.studiedUnit
//        studiedUnitTv.text = unit.fold("", { acc, unit ->
//            if(acc.isEmpty())
//                unit.title
//            else
//                acc + ", " + unit.title
//        })
            studiedUnitTv.text = user!!.getCommonSubjectText()

//        val optional = user.optionalUnit
//        optionalUnitTv.text = optional.fold("", { acc, optional ->
//            if(acc.isEmpty())
//                optional.title
//            else
//                acc + ", " + optional.title
//        })
            optionalUnitTv.text = calcNoneText(user?.getOptionalSubjectText())

            if (user.hasPulleyPlus) {
                payUserContainer.visibility = View.VISIBLE
                freeUserContainer.visibility = View.GONE
            } else {
                payUserContainer.visibility = View.GONE
                freeUserContainer.visibility = View.VISIBLE
            }

            var gradeText = user.grade.text

            if (user.grade == Grade.High_2 || user.grade == Grade.High_3 || user.grade == Grade.AfterHigh) {
                gradeText += ", ${user.major.title}"
            }

            if (user.grade == Grade.High_1 || user.grade == Grade.High_2 || user.grade == Grade.High_3 || user.grade == Grade.AfterHigh) {
                gradeText += ", ${user.ratingText}"
            }

            gradeTv.text = gradeText
        }
    }

    private fun calcNoneText(text: String?): String {
        return if (text?.isNotEmpty() == true) text
        else getString(R.string.text_none)
    }

    private fun onPwModifyBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-비밀번호")
        ServerCommunicator.findPassword(this, user!!.email) {
            CompleteDialog(this, getString(R.string.guide_complete_send_password_reset_email), "").showFor(2000)
        }
    }

    private fun onMemebershipBtnClicked() {
        FacebookEvent.log(this, FacebookEvent.SUBSCRIBE_STARTED)

        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "구독하기버튼")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(URL.구매촉구_마이페이지)
        startActivity(intent)
    }

    private fun onRecommendUnitSettingBtnClickked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-공통단원")
        val fragment = StudyCommonUnitSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    private fun onOptionalUnitSettingBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-선택단원")
        val fragment = StudyOptionalUnitSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    private fun onStudyInfoSettingBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-학년정보")
        val fragment = StudyInfoSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    private fun onPhoneModifyBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-핸드폰")
        val fragment = PhoneSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    fun moveTo(frag: Fragment, withAnim: Boolean = true) {
        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran.add(R.id.childContainer, frag)
        tran.commit()
    }


    fun back(frag: Fragment, withAnim: Boolean = true) {
        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran.remove(frag)
        tran.commit()
    }

    private fun getDurationText(user: User): String {
        return if (user.startDate == null || user.endDate == null) {
//            LogUtils.assert(false, "유저 start 또 enddate가 존재하지 않음 " +
//                    "studentID: ${user.studentID}" +
//                    "mebership: ${user.memberExperiencedType}" +
//                    "startDate: ${user.startDate}" +
//                    "endDate: ${user.endDate}")
            ""
        } else {
            String.format("%s - %s",
                    DateTimeUtils.yyyyMMddFormat.format(user.startDate),
                    DateTimeUtils.yyyyMMddFormat.format(user.endDate))
        }
    }

    override fun onModifyCompleted() {
        setUpUI()
    }
}