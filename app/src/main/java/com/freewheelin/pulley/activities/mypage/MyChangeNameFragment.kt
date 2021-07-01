package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult

import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.utils.isValidName
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_rename.*
import kotlinx.android.synthetic.main.view_input_daebak.view.*

class MyChangeNameFragment : MyPageBaseFragment() {

    val user
        get() = requireActivity().application.user!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_rename, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        name.text = user.fullName
        changeBtn.toDisableUI()
        name.editText.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val word = name.text
                if(word != user.fullName && name.text.isNotEmpty() && name.text.length > 1 && name.text.isValidName()) {
                    changeBtn.toEnableUI()
                } else {
                    changeBtn.toDisableUI()
                }
            }
        })

        changeBtn.setOnClickListener {
            if(changeBtn.isEnableUI()) {

                if (name.text.isEmpty()) {
                    name.showErrorMsg("이름을 입력해주세요.")
                } else if (name.text.isValidName() == false) {
                    name.showErrorMsg("올바른 형식이 아닙니다.")
                } else {
                    name.isShownError = false
                    requestApi()
                }
            }
        }
    }

    fun requestApi() {

        changeBtn.startLoding()
        API_V2.rename(name.text)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    changeBtn.completeLoading()
                    user.fullName = name.text
                    user.commit("MyRenameFragment requestApi")
                    setFragmentResult(MySignUpInfoFragment.RELOAD, bundleOf())
                    onBackBtnClicked()
                },{
                    Log.e(javaClass.simpleName, "error=${it.localizedMessage}")
                    changeBtn.completeLoading()
                })
    }
}

