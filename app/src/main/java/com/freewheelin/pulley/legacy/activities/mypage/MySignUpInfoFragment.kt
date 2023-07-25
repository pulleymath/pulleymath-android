package com.freewheelin.pulley.legacy.activities.mypage


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R

import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentMySignupInfoBinding
import com.freewheelin.pulley.legacy.model.SignInChannel
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity


class MySignUpInfoFragment : MyPageBaseFragment(), MyPageSettingDialogListener {
    lateinit var binding: FragmentMySignupInfoBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

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
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            nameModifyBtn.setOnClickListener { moveTo(MyChangeNameFragment()) }
            emailModifyBtn.setOnClickListener { moveTo(MyChangeEmailFragment()) }
            phoneModifyBtn.setOnClickListener { moveTo(MyChangePhoneFragment()) }
            parentPhoneModifyBtn.setOnClickListener { moveTo(MyChangeParentPhoneNumberFragment()) }
            passwordModifyBtn.setOnClickListener { moveTo(MyChangePasswordFragment()) }
            backBtn.setOnClickListener { onBackBtnClicked() }

            loginBtn.setOnClickListener {
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "가입유도", "연동하기")
                (activity as? MainActivity)?.showGuestJoinInduceDialog {
                    viewModel.errorStatusReset()
                }
            }

            setFragmentResultListener(RELOAD) { key, bundle ->
                reload()
            }
            viewModel.user.observe(viewLifecycleOwner) {
                it?.let {
                    isGuestUser = it.serviceType.isGuestUser == true
                    nameTv.text = it.fullName
                    emailTv.text = it.email

                    studentIdTv.text = it.studentID
                    studentIdTv.visibleIf(BuildConfig.FLAVOR == "beta")
                    phoneTv.text = it.cellPhone
                    parentPhoneTv.text = it.parentNumber

                    emailModifyBtn.text = "변경하기"

                    ivConfirmPhone.visibleIf(it.isValidPhone)
                    ivConfirmEmail.visibleIf(it.isValidEmail)

                    socialIconIv.visibleIf(it.signInChannel == SignInChannel.WHALESPACE)
                    passwordLl.visibleIf(it.signInChannel == SignInChannel.PULLEY)
                }

            }
        }
    }

    private fun reload() {
        val user = MyApplication.user!!
        Log.d(javaClass.simpleName, "user=${user.fullName}")
        viewModel.updateUser(user)
    }

    override fun onModifyCompleted(user: UserV4) {
        viewModel.updateUser(user)
    }

    fun onMemebershipBtnClicked() {
        // facebook
        FacebookEvent.log(requireContext(), FacebookEvent.SUBSCRIBE_STARTED)
        if (user?.serviceType?.isGuestUser == true) {
            val targetUrl = URL.홈페이지
            IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
        } else {
            viewModel.getTempToken { shortToken ->
                val relativeUrl = URL.홈페이지.substringAfter("https://pulleymath.com")
                val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
            }
        }
    }

    fun moveTo(fragment: Fragment) {
        (activity as MainActivity).addMyPage(fragment)
    }
}
