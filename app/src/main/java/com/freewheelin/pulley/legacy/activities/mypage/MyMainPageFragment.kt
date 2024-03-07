package com.freewheelin.pulley.legacy.activities.mypage

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.StartActivity
import com.freewheelin.pulley.legacy.activities.mypage.Setting.*
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.core.manage.*
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.databinding.FragmentMyMainPageBinding
import com.freewheelin.pulley.databinding.ItemMypageListBinding
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.CommonDialog
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class SettingCategory(val title: String) {
    PRIVATE("내 정보"),
    PRIVATE_CHALLENGE_NOT_CONTAIN("내 정보"),
    SERVICE("서비스 이용"),
    SETTING("설정"),
    SUPPORT("지원"),
    ETC("");

//    SPY("개발 테스트용");
    val isPrivate: Boolean
        get() {
            return this == PRIVATE || this == PRIVATE_CHALLENGE_NOT_CONTAIN
        }

    val setting: List<Setting>
        get() {
            return when (this) {
                PRIVATE -> {
                    if (user?.serviceType?.isGuestUser == true)
                        listOf(SignUpInfo)
                    else
                        listOf(SignUpInfo, StudyInfo, StartChallenge)
                }
                PRIVATE_CHALLENGE_NOT_CONTAIN -> {
                    if (user?.serviceType?.isGuestUser == true)
                        listOf(SignUpInfo)
                    else
                        listOf(SignUpInfo, StudyInfo)
                }
                SERVICE -> listOf(PulleyPlus, PulleyLesson, PulleyBooks, CouponBox)
                SETTING -> listOf(AppSetting)
                SUPPORT -> listOf(Home, Guide, Notice, Customer, Version) // 고객지원 -> , FAQ, Contact, Policy
                ETC -> listOf(Logout)
//                SPY -> {
//                    listOf(InitSetting, ClearMockExam, ClearBooks, ClearTests,
//                            ClearAllClearHistory, ClearAllScrapHistory,
//                            ClearAllStudy,
//                            CrashlyticsCrash, CrashlyticsReport,
//                            ClearTutorialHistory,
//                            StagingAPI,
//                            TestAPI,
//                            ShowEventLogging,
//                            ShowProblem,
//                            Flutter_INIT_TEST,
//                            SHOW_UPDATE_DIALOG,
//                            START_FREE,
//                            SHOW_ALWAYS_COMPLETE_TOAST,
//                            SNACKTEST_SETTING,
//                            EventSignUp,
//                            OffSpyMode)
//                }
            }
        }
}

enum class Setting(val title: String) {
    SignUpInfo("개인 정보"),
    StudyInfo("학습 정보"),
    StartChallenge("스타트 챌린지"),

    PulleyPlus("풀리수학+"),
    PulleyLesson("풀리과외"),
    PulleyBooks("풀리북스"),
    CouponBox("쿠폰함"),
//    PaymentMethod("결제정보"),

//    Recommend("추천 설정"),
    AppSetting("알림 설정"),

    Home("풀리수학 홈페이지 바로가기"),
    Guide("풀리수학 200% 활용가이드"),
    Notice("풀리수학 새소식"),
    Customer("고객 지원"),
    FAQ("자주 묻는 질문"),
    Contact("1:1 문의하기"),
    Version("버전 정보"),
    Policy("이용약관 · 개인정보취급방침"),

    Logout("로그아웃"),

//    EventSignUp("SignUp 이벤트 생성"),

//    InitSetting("초기설정으로가기"),
//    ClearMockExam("모의고사 풀이내역 전체삭제"),
//    ClearBooks("내 유형학습 내역 전체삭제"),
//    ClearTests("내 테스트 내역 전체삭제"),
//    ClearAllClearHistory("클리어내역 전체 삭제"),
//    ClearAllStudy("학습내역 전체 삭제"),
//    ClearAllScrapHistory("즐겨찾기내역 전체 삭제"),
//    CrashlyticsCrash("강제 크래시"),
//    CrashlyticsReport("크래시리틱 리포트"),
//    ClearTutorialHistory("튜토리얼 보인 내역 삭제"),
//    StagingAPI("Staging서버로 접속"),
//    TestAPI("Test서버로 접속"),
//    ShowEventLogging("이벤트 로깅 보이기"),
//    ShowProblem("문제가 잘나오나?"),
//    Flutter_INIT_TEST("[FLUTTER] 초기테스트 화면"),
//    SHOW_UPDATE_DIALOG("강제업데이트 UI확인하기"),
//    SHOW_ALWAYS_COMPLETE_TOAST("항상 문제 다풀었다 토스트 뜨도록하기"),
//    START_FREE("무료체험 시작하기"),
//    SNACKTEST_SETTING("스낵테스트 세팅하기"),
//    OffSpyMode("SPY 종료")
}

class MyMainPageFragment : Fragment() {
    val settingCategory = mutableListOf(
            SettingCategory.PRIVATE,
            SettingCategory.SERVICE,
            SettingCategory.SETTING,
            SettingCategory.SUPPORT,
            SettingCategory.ETC)

    lateinit var binding: FragmentMyMainPageBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()
    lateinit var reconfigureReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initReceiver()
    }
    private fun initReceiver() {
        reconfigureReceiver = object: BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    binding.myPageMenuRv.adapter?.notifyDataSetChanged()
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reconfigureReceiver, IntentFilter(RE_CONFIGURE_UI))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_main_page, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.myPageMenuRv.adapter = MenuAdapter().apply { this.sectionType = SectionType.header }
        binding.myPageMenuRv.layoutManager = LinearLayoutManager(context)


        setFragmentResultListener(MyStartChallengeFragment.CHALLENGE_MENU_REMOVED) { key, bundle ->
            updateNewCategory(false)
            val intent = Intent(ChallengeManager.CHALLENGE_UPDATE)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        viewModel.apply {
            joinedChallengeList.observe(viewLifecycleOwner) { list ->
                list.find { it.isStartChallenge }?.let {
                    updateNewCategory(it.userStatus != ChallengeUserStatus.FAILED)
                }
            }
        }
    }
    fun updateNewCategory (containChallenge: Boolean) {
        settingCategory.clear()
        val newCategory = listOf(
            if (containChallenge) SettingCategory.PRIVATE else SettingCategory.PRIVATE_CHALLENGE_NOT_CONTAIN,
            SettingCategory.SERVICE,
            SettingCategory.SETTING,
            SettingCategory.SUPPORT,
            SettingCategory.ETC)

        settingCategory.addAll(newCategory)
        CoroutineScope(Dispatchers.Main).launch {
            binding.myPageMenuRv.adapter?.notifyDataSetChanged()
        }
    }


    fun onSettingClicked(setting: Setting) {
        when (setting) {
            SignUpInfo -> moveTo(MySignUpInfoFragment())
            StudyInfo -> moveTo(MyStudyInfoFragment())
            StartChallenge -> moveTo(MyStartChallengeFragment())

            PulleyPlus   -> moveTo(MyPulleyPlusFragment())
            PulleyLesson -> moveTo(MyPulleyLessonFragment())
            PulleyBooks  -> moveTo(MyPulleyBooksFragment())
            CouponBox    -> moveTo(MyPulleyCouponFragment())

            AppSetting -> moveTo(MyAppSettingFragment())
            Home -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK,"마이페이지","홈페이지 바로 가기")
                if (user?.serviceType?.isGuestUser == true) {
                    val pulleyHome = Preferences.shopUrl.get()
                    IntentUtils.openWebLink(requireContext(), pulleyHome, requireContext().packageManager)
                } else {
                    viewModel.getTempToken { shortToken ->
                        val relativeUrl = URL.홈페이지.substringAfter("https://pulleymath.com")
                        val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                        IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                    }
                }
            }
            Guide -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK,"마이페이지","활용가이드보기")
                IntentUtils.openWebLink(requireContext(), URL.풀리활용가이드_마이페이지, requireContext().packageManager)
            }
            Notice -> {
                if (user?.serviceType?.isGuestUser == true) {
                    val targetUrl = URL.공지사항
                    IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                } else {
                    viewModel.getTempToken { shortToken ->
                        val relativeUrl = URL.공지사항.substringAfter("https://pulleymath.com")
                        val targetUrl = "${Network.webRedirectUrlOnShortToken}${shortToken}&uri=${relativeUrl}"
                        IntentUtils.openWebLink(requireContext(), targetUrl, requireContext().packageManager)
                    }
                }
            }
            FAQ -> {
                IntentUtils.openWebLink(requireContext(), URL.FAQ, requireContext().packageManager)
            }
            Contact -> moveTo(MyContactFragment())
            Policy -> moveTo(MyPolicyFragment())
            Version -> moveTo(MyVersionFragment())

            Customer -> moveTo(MyCustomerFragment())

            Logout -> {
                DialogUtils.confirmV2(
                    requireContext(),
                    "알림",
                "로그아웃 하시겠습니까?",
                    "아니요",
                    "로그아웃",
                    type = CommonDialog.DialogType.Alert,
                    successCb = {
                        user?.let { viewModel.sendLogoutLog(it) }
                        val callback: (String?) -> Unit = {
                            viewModel.updateUser(MyApplication.user)
                            activity?.finishAffinity()
                            val intent = Intent(activity, StartActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            activity?.startActivity(intent)
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
                )
            }
            else -> {}
        }
    }

    fun moveTo(frag: Fragment) {
        (activity as MainActivity).addMyPage(frag)
    }

    inner class MenuAdapter : SectionAdapter<RecyclerView.ViewHolder>() {

        override fun numberOfSection(): Int {
            return settingCategory.size
        }

        override fun numberOfRows(section: Int): Int {
            return settingCategory[section].setting.size
        }

        override fun getItemViewType(indexPath: IndexPath): Int {
            return when (indexPath.type) {
                Type.header -> 0
                else -> 1
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                HeaderHolder.create(parent, viewModel)
            } else {
                val binding: ItemMypageListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mypage_list, parent, false)
                ListHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            (holder as? HeaderHolder)?.apply {
                val category = settingCategory[indexPath.section]

                set(category, indexPath)
            }

            (holder as? ListHolder)?.apply {
                val setting = settingCategory[indexPath.section].setting[indexPath.row]
                set(setting, getSubText(setting))

                if(setting == Setting.Notice && NoticeManager.isNeedUpdateTag()) {
                    titleTv.typeface = Theme.bold(requireContext())
                    updateTag.visibility = View.VISIBLE
                } else if (setting == Setting.Version && VersionManager.isNeedToUpdate() == true) {
                    titleTv.typeface = Theme.bold(requireContext())
//                    updateTag.visibility = View.VISIBLE // GS인증때문에 버전 업데이트정보관련 로직 따운

//                } else if (setting == StagingAPI || setting == TestAPI || setting == ShowEventLogging || setting == SHOW_ALWAYS_COMPLETE_TOAST) {
//                    updateTag.visibility = View.VISIBLE
//                    switch.setOnCheckedChangeListener { compoundButton, value ->
//                        onCheckChanged(setting, value)
//                    }
                } else {
                    titleTv.typeface = Theme.regular(requireContext())
                    updateTag.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    onSettingClicked(setting)
                }
            }
        }
    }

    private fun getSubText(setting: Setting): String {
        requireActivity().application.user?.let { user ->
            return when(setting) {
                AppSetting -> {
                    var agrees: MutableList<String> = mutableListOf()
                    if(user.agreeAlimtalk) agrees.add("알림톡(문자)")
                    if(user.agreeAppPush) agrees.add("푸시")
                    if(user.agreeEmail) agrees.add("이메일")
                    if(user.agreeMarketing) agrees.add("마케팅")

                    if(agrees.isEmpty()) {
                        "수신 거부"
                    } else {
                        "${agrees.joinToString(",")} 알림 허용"
                    }
                }
                Version -> {
                    val isNeedUpdate = VersionManager.isNeedToUpdate()
                    when (isNeedUpdate) {
//                        true -> "V ${VersionManager.appVersion}"
                        true -> "업데이트 확인하기"
                        false -> "최신 버전입니다."
                        else -> ""
                    }
                }
                else -> ""
            }
        }
        return ""
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(reconfigureReceiver)
        super.onDestroy()
    }
}

class ListHolder(val binding: ItemMypageListBinding) : RecyclerView.ViewHolder(binding.root) {
    val titleTv = binding.titleTv
    val updateTag = binding.updateTag
    val subTitleTv = binding.subTitleTv
    val clampIv = binding.clampIv
    val switch = binding.onOffSwitch
    val textDescription = binding.textDescription

    fun set(setting: Setting, subText: String) {
        titleTv.text = setting.title
        subTitleTv.text = subText

        textDescription.visibility = View.GONE

        when(setting) {
            Guide, Notice -> clampIv.setImageResource(R.drawable.ic_new_window)
            Home -> {
                textDescription.visibility = View.VISIBLE
                textDescription.text = "결제수단 변경 및 주문 취소는 풀리수학 홈페이지에서 가능합니다."
                clampIv.setImageResource(R.drawable.ic_new_window)
            }
            else -> clampIv.setImageResource(R.drawable.ic_tailless_arrow_right_8_14)
        }

        clampIv.visibility = if(setting != Logout) View.VISIBLE else View.GONE
        switch.setOnCheckedChangeListener(null)

        when(setting){
//            StagingAPI -> {
//                titleTv.text = "현재 API주소 ${v1.base} -> Staging API로 변경하기"
//                switch.visibility = View.VISIBLE
//                switch.isChecked = Preferences.onServerAPI.get() == Network.Server.staging.toString()
//            }
//            TestAPI -> {
//                titleTv.text = "현재 API주소 ${v1.base} -> Dev API로 변경하기"
//                switch.visibility = View.VISIBLE
//                switch.isChecked = Preferences.onServerAPI.get() == Network.Server.dev.toString()
//            }
//            ShowEventLogging -> {
//                switch.visibility = View.VISIBLE
//                switch.isChecked = Preferences.onLoggingEvent.get()
//            }
//            SHOW_ALWAYS_COMPLETE_TOAST -> {
//                switch.visibility = View.VISIBLE
//                switch.isChecked = Preferences.onSuccessToast.get()
//            }
            else -> switch.visibility = View.GONE
        }
    }
}