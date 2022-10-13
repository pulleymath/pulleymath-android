package com.freewheelin.pulley.activities.mypage

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyCommonUnitSettingFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyOptionalUnitSettingFragment
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.DialogMyRecommendSettingBinding
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.responseFailed
import com.freewheelin.pulley.views.DaebakToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyRecommendSettingDialog(context: Context, override val user: User, listener: MyPageSettingDialogListener): MyPageSettingBaseDialog(context, user, listener) {
    var binding: DialogMyRecommendSettingBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_my_recommend_setting, null, false)

    init {
        setContentView(binding.root)
        configureUI()
    }

    val difficultyButtonIDs: List<Int>
        get() = listOf(R.id.highButton, R.id.middleButton, R.id.lowButton)

    val coverRangeButtonIDs: List<Int>
        get() = listOf(R.id.sameButton, R.id.aheadButton, R.id.tailButton)

    lateinit var commonUnitFragment:Fragment
    lateinit var optionalUnitFragment:Fragment
    lateinit var stuiedUnitFragment:Fragment

    fun configureUI() {
        commonUnitFragment = StudyCommonUnitSettingFragment()
        optionalUnitFragment = StudyOptionalUnitSettingFragment()

        setButtonUI()
        setRadioUI()

        binding.xBtn.setOnClickListener {
            dismiss()
        }
    }

    fun setButtonUI() {
        with(binding) {
            sameCommonButton.setOnClickListener { moveTo(commonUnitFragment) }
            sameOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }
            sameMyButton.setOnClickListener {  }

            aheadOptionalButton.setOnClickListener {  }

            myChoiceCommonButton.setOnClickListener {  }
            myChoiceOptionalButton.setOnClickListener {  }
        }
    }

    fun setRadioUI() {
        with(binding) {
            val recommendLevel = user.recommendLevel
            val recommendChapter = user.recommendChapter

            if(recommendLevel != null && recommendLevel > -1) {
                levelRg.check(difficultyButtonIDs[recommendLevel])
            } else {
                levelRg.check(R.id.lowButton)
            }
            if(recommendChapter != null && recommendChapter > -1) {
                rangeRg.check(coverRangeButtonIDs[recommendChapter])
            } else {
                rangeRg.check(R.id.sameButton)
            }

            levelRg.setOnCheckedChangeListener { group, checkedId ->
                showSubView()
                sendConfigure()
            }
            rangeRg.setOnCheckedChangeListener { group, checkedId ->
                showSubView()
                sendConfigure()
            }

            showSubView()
        }
    }

    fun showSubView() {
        with(binding) {
            when(rangeRg.checkedRadioButtonId) {
                R.id.sameButton -> {
                    if(user.recentSubjectCode.isNotEmpty()) {
                        showRangeContainer(sameContainerOver50)
                    } else {
                        showRangeContainer(sameContainerBelow50)
                    }
                }
                R.id.aheadButton -> {showRangeContainer(aheadContainer)}
                R.id.tailButton -> {showRangeContainer(myChoiceContainer)}
                else -> {

                }
            }
        }
    }

    fun showRangeContainer(container:View) {
        with(binding) {
            sameContainerBelow50.visibility = View.GONE
            sameContainerOver50.visibility = View.GONE
            aheadContainer.visibility = View.GONE
            myChoiceContainer.visibility = View.GONE

            container.visibility = View.VISIBLE
        }
    }

    fun sendConfigure() {
        with(binding) {
            val recommendLevel = difficultyButtonIDs.indexOf(levelRg.checkedRadioButtonId)
            val recommendChapter = coverRangeButtonIDs.indexOf(rangeRg.checkedRadioButtonId)

            val param: Parameter = Parameter(
                "recommendLevel" to recommendLevel,
                "recommendChapter" to recommendChapter
            )

            API_V2.setUserDailySetup(user.studentID, param).enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    responseFailed(context, t)
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful) {
                        user.update(
                            recommendLevel = recommendLevel,
                            recommendChapter = recommendChapter
                        )
//                    dismiss()
//                    showCompleteDialog()
//                    listener?.onModifyCompleted(user)

                        val intent = Intent(TestManager.EVENT_TEST_SETTING)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    }
                }
            })
        }
    }

    fun moveTo(frag: Fragment) {
        val tran = frag.fragmentManager?.beginTransaction()
        tran?.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran?.add(R.id.childContainer, frag)
        tran?.commit()
    }

    fun pop(frag:Fragment) {
        val tran = frag.fragmentManager?.beginTransaction()
        tran?.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran?.remove(frag)
        tran?.commit()
    }
}