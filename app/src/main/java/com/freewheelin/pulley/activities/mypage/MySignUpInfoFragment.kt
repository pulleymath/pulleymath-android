package com.freewheelin.pulley.activities.mypage


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMySignupInfoBinding
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.FacebookEvent
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.views.DaebakToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


class MySignUpInfoFragment : MyPageBaseFragment(), MyPageSettingDialogListener {
    lateinit var binding: FragmentMySignupInfoBinding

    companion object {
        const val RELOAD = "reload"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_signup_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            nameModifyBtn.setOnClickListener { moveTo(MyChangeNameFragment()) }
            emailModifyBtn.setOnClickListener { moveTo(MyChangeEmailFragment()) }
            phoneModifyBtn.setOnClickListener { moveTo(MyChangePhoneFragment()) }
            passwordModifyBtn.setOnClickListener { moveTo(MyChangePasswordFragment()) }
            deviceBtn.setOnClickListener { moveTo(MyDeviceManagerFragment()) }
//        membershipBtn.setOnClickListener { onMemebershipBtnClicked() }
            backBtn.setOnClickListener { onBackBtnClicked() }

            val user = MyApplication.user ?: return
            configureUI(user)

            setFragmentResultListener(RELOAD) { key, bundle ->
                reload()
            }
        }
    }

    private fun reload() {
        val user = MyApplication.user!!
        Log.d(javaClass.simpleName, "user=${user.fullName}")
        configureUI(user)
    }

    override fun onModifyCompleted(user: User) {
        configureUI(user)
    }

    fun configureUI(user: User) {
        with(binding) {
            nameTv.text = user.fullName
            emailTv.text = user.email
            phoneTv.text = user.cellPhone

            emailModifyBtn.text = if(user.isValidEmail) "변경하기" else "인증하기"

//        if(user.isExpiredUser() || user.serviceName == null) {
//            noSeviceLabel.visibility = View.VISIBLE
//            membershipBtn.visibility = View.VISIBLE
//            serviceNameLabel.visibility = View.GONE
//            availableDurationLabel.visibility = View.GONE
//            serviceTv.visibility = View.GONE
//            availableDurationTv.visibility = View.GONE
//        } else {
//            noSeviceLabel.visibility = View.GONE
//            membershipBtn.visibility = View.GONE
//            serviceNameLabel.visibility = View.VISIBLE
//            availableDurationLabel.visibility = View.VISIBLE
//            serviceTv.visibility = View.VISIBLE
//            availableDurationTv.visibility = View.VISIBLE
//
//            serviceTv.text = user.serviceName
//            availableDurationTv.text = getDurationText(user)
//        }

            ivConfirmPhone.visibility = if(user.isValidPhone) View.VISIBLE else View.GONE
            ivConfirmEmail.visibility = if(user.isValidEmail) View.VISIBLE else View.GONE

            loadDeviceCount()
        }
    }

    fun loadDeviceCount() {
        API_V2.getDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    val count = response.data?.size?:1
                    binding.deviceCount.text = "등록 기기 : ${count}대"
                }, {
                    Log.e(javaClass.simpleName, "${it.localizedMessage}")
                })
    }

    fun onMemebershipBtnClicked() {
        // facebook
        FacebookEvent.log(requireContext(), FacebookEvent.SUBSCRIBE_STARTED)
        IntentUtils.openWebLink(requireContext(), URL.홈페이지, requireContext().packageManager)
    }

    fun moveTo(fragment: Fragment) {
        (activity as LearningTabActivity).moveTo(fragment)
    }

    private fun getDurationText(user: User): String {
        if(user.startDate == null || user.endDate == null) {
            Log.e(javaClass.simpleName, "유저 start 또는 enddate가 존재하지 않음 " +
                    "studentID: ${user.studentID}, " +
                    "hasPulleyPlus: ${user.hasPulleyPlus}, " +
                    "startDate: ${user.startDate}, " +
                    "endDate: ${user.endDate}")
            return ""
        } else {
            val now = Date()
            val startDate: Date = if(now > user.startDate) now else user.startDate!!
            val endDate = user.endDate!!

            DateTimeUtils.yyyyMMddFormat?.run {
                return if (startDate < endDate) "${format(user.startDate)} - ${format(user.endDate)}"
                else "${format(user.startDate)} - ${format(user.endDate)}"
            }
        }
    }
}
