package com.freewheelin.pulley.legacy.activities.mypage


import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels

import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentMyChangeParentPhoneNumberBinding
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.isValidPhoneNum
import com.freewheelin.pulley.legacy.views.editText.InputFieldV2
import com.freewheelin.pulley.legacy.views.editText.InputFieldV2EnterListener
import com.freewheelin.pulley.legacy.views.editText.InputFieldV2Listener

class MyChangeParentPhoneNumberFragment : MyPageBaseFragment() {
    val user
        get() = requireActivity().application.user!!
    lateinit var binding: FragmentMyChangeParentPhoneNumberBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val themeWrapper = ContextThemeWrapper(requireActivity(), R.style.DialogTheme)
        val layoutInflater = requireActivity().layoutInflater.cloneInContext(themeWrapper)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_my_change_parent_phone_number, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        with(binding) {
            if (user.parentNumber?.isNotEmpty() == true) {
                parentPhoneNumField.text = user.parentNumber!!
            }
            setPhoneNumberField()

            changeBtn.isEnabled = false
            changeBtn.setOnClickListener {
                if(changeBtn.isEnabled) {
                    requestApi()
                }
            }
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    private fun setPhoneNumberField() {
        binding.apply {
            parentPhoneNumField.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            parentPhoneNumField.goneLabel()
            parentPhoneNumField.listener = object : InputFieldV2Listener {
                override fun onFieldValueChanged(view: InputFieldV2) {
                    if (parentPhoneNumField.text.isValidPhoneNum()) {
                        changeBtn.isEnabled = true
                    } else {
                        changeBtn.isEnabled = false
                    }
                }

                override fun onFieldFocusChanged(view: InputFieldV2, hasFocus: Boolean) {
                    if (!hasFocus) {
                        if(parentPhoneNumField.text.isEmpty())
                            parentPhoneNumField.showErrorMsg("휴대폰 번호를 입력해주세요.")
                        else if (!parentPhoneNumField.text.isValidPhoneNum())
                            parentPhoneNumField.showErrorMsg("전화번호 형식을 확인해주세요.")
                    }
                    if (parentPhoneNumField.text.isValidPhoneNum()) {
                        changeBtn.isEnabled = true
                    } else {
                        changeBtn.isEnabled = false
                    }
                }
            }
            parentPhoneNumField.enterListener = object : InputFieldV2EnterListener {
                override fun onEnter(view: View) {
                    if (changeBtn.isEnabled) {
                        requestApi()
                    } else {
                        parentPhoneNumField.showErrorMsg("번호를 확인해주세요.")
                    }
                }
            }
        }
    }
    fun requestApi() {

        binding.changeBtn.setLoading(true)
        // TODO 부모님 번호 변경 api
        val parentNumber = binding.parentPhoneNumField.text
        viewModel.changeParentPhoneNumber(parentNumber) {
            binding.changeBtn.setLoading(false)
            user.parentNumber = parentNumber
            viewModel.updateUser(user)
            removeThisPage()
        }
    }
}

