package com.freewheelin.pulley.revision2023.ui.dialogs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogSnackTestRecommendSettingBinding
import com.freewheelin.pulley.databinding.DialogSpyBinding
import com.freewheelin.pulley.databinding.DialogTeacherUtilityBinding
import com.freewheelin.pulley.legacy.activities.SplashActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.isNeedNewOnBoarding
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.manage.BookManager
import com.freewheelin.pulley.legacy.core.manage.MockExamManager
import com.freewheelin.pulley.legacy.core.manage.ProblemManager
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.utils.DialogType
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.revision2023.ui.fragment.*
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.revision2023.viewmodel.TeacherUtilityViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.activity.AiepWebViewActivity
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.view.SpyItemView
import com.freewheelin.pulley.revision2023.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class SpyItem(val description: String, val isSwitch: Boolean = false) {
    LiveAPI("라이브 서버", true),
    StagingAPI("스테이징 서버", true),
//    InitSetting("초기설정으로가기"),
    ClearOnBoard("온보딩 초기화"),
    ClearAllStudy("학습내역 전체 삭제"),
    ClearBooks("유형학습 내역 전체 삭제"),
    ClearMockExam("모의고사 풀이내역 전체 삭제"),
    ClearTests("테스트 내역 전체 삭제"),
//    ClearAllClearHistory("클리어내역 전체 삭제"),
    ClearAllScrapHistory("즐겨찾기내역 전체 삭제"),
//    CrashlyticsCrash("강제 크래시"),
//    CrashlyticsReport("크래시리틱 리포트"),
//    ClearTutorialHistory("튜토리얼 보인 내역 삭제"),


    ShowEventLogging("이벤트 로깅 보이기", true),
//    SHOW_UPDATE_DIALOG("강제업데이트 UI확인하기"),
    OpenAiepWebView("AIEP 웹뷰 열기"),
    OffSpyMode("SPY 종료")
}
class SpyDialog(): DialogFragment() {

    private val viewModel: TeacherUtilityViewModel by viewModels()

    private val binding: DialogSpyBinding by lazy {
        DataBindingUtil.inflate(
            layoutInflater.cloneInContext(requireContext()),
            R.layout.dialog_spy,
            null,
            false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    val spyItems = listOf(
        SpyItem.LiveAPI,
        SpyItem.StagingAPI,
        SpyItem.ClearOnBoard,
        SpyItem.ClearAllStudy,
        SpyItem.ClearBooks,
        SpyItem.ClearMockExam,
        SpyItem.ClearTests,
        SpyItem.ClearAllScrapHistory,
        SpyItem.ShowEventLogging,
        SpyItem.OpenAiepWebView,
        SpyItem.OffSpyMode
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
//            setScreen()

            viewModel.onExitClickCallback = {
                dismiss()
            }

            exitBtn.setOnClickListener {
                dismiss()
            }
            headerTitleTv.text = StringUtils.getEmojiByUnicode(0x1F977)

            spyItems.forEach {
                val spyItemView = SpyItemView(requireContext()).apply {

                    setTitle(it.description)
                    setOnClickListener(it) { spyItem ->
                        when(spyItem) {
                            SpyItem.LiveAPI -> changeApi(true)
                            SpyItem.StagingAPI -> changeApi(false)
                            SpyItem.ClearOnBoard -> {
                                DaebakToast.show(context, "앱종료 후 다시 온보딩 확인 가능!")
                                isNeedNewOnBoarding = true
                            }
                            SpyItem.ClearAllStudy -> {
                                DialogUtils.DaebakDialog(requireContext()).apply {
                                    title = "채점 내역을 모두 삭제합니다"
                                    contents = "복구 할 수 없어요"
                                    type = DialogType.alert
                                    binding.leftBtn.text = "취소"
                                    binding.rightBtn.text = "확인"
                                    binding.rightBtn.setOnClickListener {
                                        ProblemManager.clearAllScoring(context, user!!) {
                                            dismiss()
                                        }
                                    }
                                }.show()
                            }
                            SpyItem.ClearBooks -> {
                                DialogUtils.DaebakDialog(requireContext()).apply {
                                    title = "유형학습 풀이 내역을 모두 삭제합니다"
                                    contents = "유형학습 탭에서 전부 지워져요"
                                    type = DialogType.alert
                                    binding.leftBtn.text = "취소"
                                    binding.rightBtn.text = "확인"
                                    binding.rightBtn.setOnClickListener {
                                        BookManager.clearBooks(context,user!!) {
                                            dismiss()
                                        }
                                    }
                                }.show()
                            }
                            SpyItem.ClearMockExam -> {
                                DialogUtils.DaebakDialog(requireContext()).apply {
                                    title = "모의고사 풀이 내역을 모두 삭제합니다"
                                    contents = "나의모의고사에서 전부 지워져요"
                                    type = DialogType.alert
                                    binding.leftBtn.text = "취소"
                                    binding.rightBtn.text = "확인"
                                    binding.rightBtn.setOnClickListener {
                                        MockExamManager.clearExam(context, user!!) {
                                            dismiss()
                                        }
                                    }
                                }.show()
                            }
                            SpyItem.ClearTests -> {
                                DialogUtils.DaebakDialog(requireContext()).apply {
                                    title = "테스트 풀이 내역을 모두 삭제합니다"
                                    contents = "전부 지워져요 초기설정까지도"
                                    type = DialogType.alert
                                    binding.leftBtn.text = "취소"
                                    binding.rightBtn.text = "확인"
                                    binding.rightBtn.setOnClickListener {
                                        TestManager.clearTests(context,user!!) {
                                            dismiss()
                                        }
                                    }
                                }.show()
                            }
                            SpyItem.ClearAllScrapHistory -> {
                                DialogUtils.DaebakDialog(requireContext()).apply {
                                    title = "즐겨찾기 내역을 모두 삭제합니다"
                                    contents = "복구 할 수 없어요"
                                    type = DialogType.alert
                                    binding.leftBtn.text = "취소"
                                    binding.rightBtn.text = "확인"
                                    binding.rightBtn.setOnClickListener {
                                        ProblemManager.clearAllScrap(context, user!!) {
                                            dismiss()
                                        }
                                    }
                                }.show()
                            }
                            SpyItem.ShowEventLogging -> {
                                Preferences.onLoggingEvent.set(!Preferences.onLoggingEvent.get())
                            }
                            SpyItem.OpenAiepWebView -> {
                                dismiss()
                                startActivity(AiepWebViewActivity.webAppIntent(requireContext()))
                            }
                            SpyItem.OffSpyMode -> {
                                DialogUtils.DaebakDialog(requireContext()).apply {
                                    title="스파이모드를 종료합니다"
                                    contents="다시 키려면.. 봉인을 푸셔야해요"
                                    type = DialogType.alert
                                    binding.leftBtn.text ="취소"
                                    binding.rightBtn.text="확인"
                                    binding.rightBtn.setOnClickListener {
                                        dismiss()
                                        (activity as MainActivity).spyOff()

                                    }
                                }.show()
                            }
                        }
                    }

                    val initChecked = when (it) {
                        SpyItem.StagingAPI -> {
                            Preferences.onServerAPI.get() == Network.Server.staging.toString()
                        }
                        SpyItem.LiveAPI -> {
                            Preferences.onServerAPI.get() == Network.Server.live.toString()
                        }
                        SpyItem.ShowEventLogging -> {
                            Preferences.onLoggingEvent.get()
                        }
                        else -> { false }
                    }
                    setSwitch(it, initChecked) { spyItem, isChecked ->
                        when (spyItem) {
                            SpyItem.LiveAPI -> { changeApi(true) }
                            SpyItem.StagingAPI -> { changeApi(false) }
                            SpyItem.ShowEventLogging -> { Preferences.onLoggingEvent.set(isChecked) }
                            else -> {}
                        }
                    }

                }


                scrollRootLl.addView(spyItemView)
            }

        }
    }

    private fun changeApi(isLive: Boolean) {
        val callback: (String?) -> Unit = { errorMsg ->
            val api = if (isLive) Network.Server.live.toString() else Network.Server.staging.toString()
            Preferences.onServerAPI.set(api)

            activity?.let { act ->
                val pm = act.packageManager
                val intent = pm.getLaunchIntentForPackage(act.packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                startActivity(mainIntent)
                System.exit(0)

            }
        }
        API_V2.signout().enqueue(object: Callback<Template<String?>> {
            override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                Log.d(javaClass.simpleName, "로그아웃 성공")
                MyApplication.token = ""
                MyApplication.user?.token = ""
                MyApplication.user = null
                MyApplication.isAppFirstLaunch = true
                Preferences.userDataString.set("")

                callback(null)
            }

            override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                Log.e(javaClass.simpleName, "로그아웃 실패")
                callback(t.localizedMessage)
            }
        })
    }

    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootCl.layoutParams
        lp.height = (DisplayUtils.getScreenHeight(requireContext()) * 0.66).toInt()
        lp.width = 640.toPx()
        binding.rootCl.layoutParams = lp
    }
}