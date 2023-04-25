package com.freewheelin.pulley.activities.mypage


import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.SplashActivity
import com.freewheelin.pulley.activities.StartActivity
import com.freewheelin.pulley.activities.auth.InitSettingActivity
import com.freewheelin.pulley.activities.auth.login.LoginActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.mypage.Setting.*
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.isSPYMode
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.Version.v1
import com.freewheelin.pulley.core.manage.*
import com.freewheelin.pulley.databinding.FragmentMyMainPageBinding
import com.freewheelin.pulley.databinding.ItemMypageListBinding
import com.freewheelin.pulley.dialogs.UpdateDialog
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.utils.*
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type

enum class SettingCategory(val title: String) {
    PRIVATE("개인정보 설정"),
    SERVICE("서비스 이용"),
    SETTING("설정"),
    SUPPORT("지원"),
    ETC(""),

    SPY("개발 테스트용");

    val setting: List<Setting>
        get() {
            return when (this) {
                PRIVATE -> {
                    if (user?.serviceType?.isGuestUser == true)
                        listOf(SignUpInfo)
                    else
                        listOf(SignUpInfo, StudyInfo)
                }
                SERVICE -> listOf(PulleyPlus, PulleyLesson, PulleyBooks, CouponBox)
                SETTING -> listOf(AppSetting)
                SUPPORT -> listOf(Home, Guide, Notice, Customer, Version) // 고객지원 -> , FAQ, Contact, Policy
                ETC -> listOf(Logout)
                SPY -> {
                    listOf(InitSetting, ClearMockExam, ClearBooks, ClearTests, Recommend,
                            ClearAllClearHistory, ClearAllScrapHistory,
                            ClearAllStudy,
                            CrashlyticsCrash, CrashlyticsReport,
                            ClearTutorialHistory,
                            StagingAPI,
                            TestAPI,
                            ShowEventLogging,
                            ShowProblem,
                            Flutter_INIT_TEST,
                            SHOW_UPDATE_DIALOG,
                            START_FREE,
                            SHOW_ALWAYS_COMPLETE_TOAST,
                            SNACKTEST_SETTING,
                            EventSignUp,
                            OffSpyMode)
                }
            }
        }
}

enum class Setting(val title: String) {
    SignUpInfo("개인 정보"),
    StudyInfo("학습 정보"),

    PulleyPlus("풀리수학+"),
    PulleyLesson("풀리과외"),
    PulleyBooks("풀리북스"),
    CouponBox("쿠폰함"),
//    PaymentMethod("결제정보"),

    Recommend("추천 설정"),
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

    EventSignUp("SignUp 이벤트 생성"),

    InitSetting("초기설정으로가기"),
    ClearMockExam("모의고사 풀이내역 전체삭제"),
    ClearBooks("내 유형학습 내역 전체삭제"),
    ClearTests("내 테스트 내역 전체삭제"),
    ClearAllClearHistory("클리어내역 전체 삭제"),
    ClearAllStudy("학습내역 전체 삭제"),
    ClearAllScrapHistory("즐겨찾기내역 전체 삭제"),
    CrashlyticsCrash("강제 크래시"),
    CrashlyticsReport("크래시리틱 리포트"),
    ClearTutorialHistory("튜토리얼 보인 내역 삭제"),
    StagingAPI("Staging서버로 접속"),
    TestAPI("Test서버로 접속"),
    ShowEventLogging("이벤트 로깅 보이기"),
    ShowProblem("문제가 잘나오나?"),
    Flutter_INIT_TEST("[FLUTTER] 초기테스트 화면"),
    SHOW_UPDATE_DIALOG("강제업데이트 UI확인하기"),
    SHOW_ALWAYS_COMPLETE_TOAST("항상 문제 다풀었다 토스트 뜨도록하기"),
    START_FREE("무료체험 시작하기"),
    SNACKTEST_SETTING("스낵테스트 세팅하기"),
    OffSpyMode("SPY 종료")
}

class MyMainPageFragment : Fragment() {
    val settingCategory = mutableListOf(
            SettingCategory.PRIVATE,
            SettingCategory.SERVICE,
            SettingCategory.SETTING,
            SettingCategory.SUPPORT,
            SettingCategory.ETC)

    lateinit var typeReceiver: BroadcastReceiver
    lateinit var binding: FragmentMyMainPageBinding
    private val viewModel: MyMainPageFragViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        typeReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {}
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(typeReceiver, IntentFilter(User.EVENT_STUDENT_TYPE_SETTING))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(typeReceiver)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_main_page, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = MenuAdapter().apply { this.sectionType = SectionType.header }
        binding.rv.layoutManager = LinearLayoutManager(context)

        if(isSPYMode) {
            settingCategory.add(SettingCategory.SPY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun onSettingClicked(setting: Setting) {
        when (setting) {
            SignUpInfo -> moveTo(MySignUpInfoFragment())
            StudyInfo -> moveTo(MyStudyInfoFragment())

            PulleyPlus   -> moveTo(MyPulleyPlusFragment())
            PulleyLesson -> moveTo(MyPulleyLessonFragment())
            PulleyBooks  -> moveTo(MyPulleyBooksFragment())
            CouponBox    -> moveTo(MyPulleyCouponFragment())
//            PaymentMethod -> {
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.data = Uri.parse(URL.결제정보)
//                startActivity(intent)
//            }

            Recommend -> moveTo(MyRecommendFragement())
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
                DialogUtils.DaebakTitleOnlyDialog(requireContext()).apply {
                    title = "로그아웃하시겠습니까?"
                    type = DialogType.alert
                    binding.leftBtn.text = "아니요"
                    binding.rightBtn.text = "로그아웃"
                    binding.rightBtn.setOnClickListener {
                        this.dismiss()
                        MyApplication.user?.logout {
                            viewModel.updateUser(MyApplication.user)
                            activity?.finishAffinity()
                            val intent = Intent(activity, StartActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            activity?.startActivity(intent)
                        }
                    }
                }.show()

            }

            InitSetting -> {
                DialogUtils.DaebakDialog(requireContext()).apply {
                    title = "초기설정화면으로 돌아갑니다."
                    contents = "초기설정을 완료해야 다시 Main으로 돌아갈 수 있어요"
                    type = DialogType.alert
                    binding.leftBtn.text = "취소"
                    binding.rightBtn.text = "확인"
                    binding.rightBtn.setOnClickListener {
                        activity?.finish()
                        val user = requireActivity().application!!.user!!
                        user.initSettingCompleted = false
                        user.commit("MyMainPageFragment.onSettingClicked:InitSetting")
                        val intent = InitSettingActivity.getIntent(context)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }.show()
            }

            ClearMockExam -> {
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

            ClearBooks -> {
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

            ClearTests -> {
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
            ClearAllClearHistory -> {
                DialogUtils.DaebakDialog(requireContext()).apply {
                    title = "클리어 내역을 모두 삭제합니다"
                    contents = "복구 할 수 없어요"
                    type = DialogType.alert
                    binding.leftBtn.text = "취소"
                    binding.rightBtn.text = "확인"
                    binding.rightBtn.setOnClickListener {
                        ProblemManager.clearAllClear(context, user!!) {
                            dismiss()
                        }
                    }
                }.show()
            }

            ClearAllStudy -> {
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
            ClearAllScrapHistory -> {
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

            CrashlyticsCrash -> {
                DialogUtils.DaebakDialog(requireContext()).apply {
                    title = "강제로 크래시를 냅니다"
                    contents = "앱은 종료되고, 크래시리틱에 보고가 가야해요"
                    type = DialogType.alert
                    binding.leftBtn.text ="취소"
                    binding.rightBtn.text="확인"
                    binding.rightBtn.setOnClickListener {
                        throw RuntimeException("Firebase Test Crash")
                    }
                }.show()
            }

            CrashlyticsReport -> {
                DialogUtils.DaebakDialog(requireContext()).apply {
                    title = "강제로 크래시를 냅니다"
                    contents = "개발용이면 앱이 꺼지고, 배포용이면 앱의 로그가 남습니다"
                    type = DialogType.alert
                    binding.leftBtn.text ="취소"
                    binding.rightBtn.text="확인"
                    binding.rightBtn.setOnClickListener {
                        dismiss()
                        LogUtils.assert(false, "SPY에서 심각하지 않은 에러")
                    }
                }.show()
            }

            EventSignUp -> {
                LogUtils.logSignUpEvent(requireContext(), user?.studentID?:"None")
            }

            ClearTutorialHistory -> {
                DialogUtils.DaebakDialog(requireContext()).apply {
                    title = "튜토리얼 내역을 모두 삭제합니다"
                    contents = "앱이 종료되었다 다시 켜져야 적용됩니다"
                    type = DialogType.alert
                    binding.leftBtn.text ="취소"
                    binding.rightBtn.text="확인"
                    binding.rightBtn.setOnClickListener {
                        Preferences.tooltipShowingCntTakeNoteScroll.set(0)
                        Preferences.tooltipShowingCntAddSimilar.set(0)
                        Preferences.tooltipShowingCntChangeSimilar.set(0)
                        Preferences.tooltipShowingCntAdditionalStudyInAnalysis.set(0)
                        Preferences.tooltipShowingCntAdditionalStudyInWrongNote.set(0)
                        Preferences.tooltipShowingCntMail.set(0)
                        Preferences.tooltipShowingCntAnalysisMain.set(0)
                        Preferences.galleryClickCnt.set(0)
                        Preferences.tooltipShowingCntRecommendPlan.set(0)

                        val intent = Intent(context, SplashActivity::class.java)
                        val mPendingIntentId = 123456
                        val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_IMMUTABLE)
                        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
                        System.exit(0)
                    }
                }.show()
            }

            StagingAPI -> {

            }
            TestAPI -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("현재 테스트 URL")

                val input = EditText(requireContext())
                input.setText(Preferences.testBaseURL.get())
                builder.setView(input)

                builder.setPositiveButton("OK") { _, _ ->
                    Preferences.testBaseURL.set(input.text.toString())
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

                builder.show()
            }
            Flutter_INIT_TEST -> {
                startActivity(InitSettingActivity.getIntent(requireContext()))
            } 

            ShowProblem -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("문제ID들을 입력해봐바,\n,로 구분해서..\n 이렇게 만들어진건 [나의학습] 추가될거임!!")

                val input = EditText(requireContext())
                builder.setView(input)

                builder.setPositiveButton("OK") { _, _ ->
                    var ids = input.text.toString().split(",")
                    ids = ids.map { it.trim() }
                    PieceManager.spyMakePiece(requireContext(), user!!, ids,
                            successCB = {
                                val intent = SolveActivity.getIntent(requireContext(), it)
                                startActivity(intent)
                            },
                            failCB = {
                                Toast.makeText(requireContext(), "실패애", Toast.LENGTH_LONG).show()
                            })
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                builder.show()
            }
            
            SHOW_UPDATE_DIALOG -> {
                val dialog = UpdateDialog(requireContext())
                dialog.show()
            }
            START_FREE -> {
                user?.startFreeMembership(requireContext()) {
                    Toast.makeText(requireContext(), "성고옹", Toast.LENGTH_LONG).show()
                }
            }
            SNACKTEST_SETTING -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("SPY-어떤타입??")
                builder.setItems(listOf(
                        "레모네이드",
                        "파이",
                        "솜사탕",
                        "바나나우유",
                        "마카롱",
                        "치즈볼"
                ).toTypedArray()) { dialog, position ->
                    val type = when (position) {
                        0 -> DessertType.LEMONADE
                        1 -> DessertType.PIE
                        2 -> DessertType.CANDY
                        3 -> DessertType.BANANA
                        4 -> DessertType.MACAROON
                        else -> DessertType.CHEESE_BALL
                    }

                    user?.setUserType(requireContext(),
                            type,
                            10,
                            10,
                            10,
                            10) {
                        dialog.dismiss()
                    }
                }.show()

            }
            OffSpyMode -> {
                DialogUtils.DaebakDialog(requireContext()).apply {
                    title="스파이모드를 종료합니다"
                    contents="다시 키려면.. 봉인을 푸셔야해요"
                    type = DialogType.alert
                    binding.leftBtn.text ="취소"
                    binding.rightBtn.text="확인"
                    binding.rightBtn.setOnClickListener {
                        dismiss()
                        spyOff()
                        (activity as LearningTabActivity).spyOff()

                    }
                }.show()
            }
            else -> {}
        }
    }

    fun spyOn() {
        isSPYMode = true
        settingCategory.add(SettingCategory.SPY)
        try {
            binding.rv.adapter?.notifyDataSetChanged()
        }catch (e:Exception) {}
    }

    fun spyOff() {
        isSPYMode = false
        settingCategory.remove(SettingCategory.SPY)
        try {
            binding.rv.adapter?.notifyDataSetChanged()
        }catch (e:Exception) {}
    }

    fun moveTo(frag: Fragment) {
        (activity as LearningTabActivity).addMyPage(frag)
    }

    fun onCheckChanged(setting: Setting, value: Boolean) {

        when (setting) {
            StagingAPI -> {
                MyApplication.user?.logout { errorMsg ->
                    val api = if (value) Network.Server.staging.toString() else Network.Server.live.toString()
                    Preferences.onServerAPI.set(api)
                    val intent = Intent(context, SplashActivity::class.java)
                    val mPendingIntentId = 123456
                    val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_IMMUTABLE)
                    val mgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
                    System.exit(0)
                }
            }
            TestAPI -> {
                MyApplication.user?.logout { errorMsg ->
//                    Preferences.onTestAPI.set(value)
                    val api = if (value) Network.Server.dev.toString() else Network.Server.live.toString()
                    Preferences.onServerAPI.set(api)
                    val intent = Intent(context, SplashActivity::class.java)
                    val mPendingIntentId = 123456
                    val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_IMMUTABLE)
                    val mgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
                    System.exit(0)
                }
            }
            ShowEventLogging -> {
                Preferences.onLoggingEvent.set(value)
            }
            SHOW_ALWAYS_COMPLETE_TOAST -> {
                Preferences.onSuccessToast.set(value)
            }

            else -> {}
        }
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
//                val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_mypage_header, parent, false)
                HeaderHolder.create(parent)
            } else {
                val binding: ItemMypageListBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mypage_list, parent, false)
//                val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_mypage_list, parent, false)
                ListHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            (holder as? HeaderHolder)?.apply {
                val category = settingCategory[indexPath.section]

                set(category)
            }

            (holder as? ListHolder)?.apply {
                val setting = settingCategory[indexPath.section].setting[indexPath.row]
                set(setting, getSubText(setting))

                if(setting == Setting.Notice && NoticeManager.isNeedUpdateTag()) {
                    titleTv.typeface = Theme.bold(requireContext())
                    updateTag.visibility = View.VISIBLE
                } else if (setting == Setting.Version && VersionManager.isNeedToUpdate() == true) {
                    titleTv.typeface = Theme.bold(requireContext())
                    updateTag.visibility = View.VISIBLE

                } else if (setting == StagingAPI || setting == TestAPI || setting == ShowEventLogging || setting == SHOW_ALWAYS_COMPLETE_TOAST) {
                    updateTag.visibility = View.GONE
                    switch.setOnCheckedChangeListener { compoundButton, value ->
                        onCheckChanged(setting, value)
                    }
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
        val user = requireActivity().application.user!!

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
                    true -> "V ${VersionManager.appVersion}"
                    false -> "최신 버전입니다."
                    else -> ""
                }
            }
            else -> ""
        }
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
            else -> clampIv.setImageResource(R.drawable.ic_clamp)
        }

        clampIv.visibility = if(setting != Logout) View.VISIBLE else View.GONE

        when(setting){
            StagingAPI -> {
                titleTv.text = "현재 API주소 ${v1.base} -> Staging API로 변경하기"
                switch.visibility = View.VISIBLE
                switch.isChecked = Preferences.onServerAPI.get() == Network.Server.staging.toString()
            }
            TestAPI -> {
                titleTv.text = "현재 API주소 ${v1.base} -> Dev API로 변경하기"
                switch.visibility = View.VISIBLE
                switch.isChecked = Preferences.onServerAPI.get() == Network.Server.dev.toString()
            }
            ShowEventLogging -> {
                switch.visibility = View.VISIBLE
                switch.isChecked = Preferences.onLoggingEvent.get()
            }
            SHOW_ALWAYS_COMPLETE_TOAST -> {
                switch.visibility = View.VISIBLE
                switch.isChecked = Preferences.onSuccessToast.get()
            }
            else -> switch.visibility = View.GONE
        }
    }
}