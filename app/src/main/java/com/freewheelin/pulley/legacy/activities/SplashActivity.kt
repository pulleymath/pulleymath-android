package com.freewheelin.pulley.legacy.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.freewheelin.pulley.R

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.serverInspection.ServerInspectionDialog
import com.freewheelin.pulley.legacy.bases.*
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.core.manage.ServerStatusManager
import com.freewheelin.pulley.legacy.core.manage.VersionInfo
import com.freewheelin.pulley.legacy.core.manage.VersionManager
import com.freewheelin.pulley.databinding.ActivitySplashBinding
import com.freewheelin.pulley.legacy.model.ServerStatus
import com.freewheelin.pulley.legacy.model.usesWebApp
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.viewmodel.SplashActViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.ui.activity.AiepWebViewActivity
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.OnBoardingActivity
import com.freewheelin.pulley.revision2023.ui.activity.WhaleSpaceLoginActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

// 웹앱 채널(교육청 AIEP·웨일스페이스) 계정 자동로그인 시 네이티브 Main 대신 WebView webapp으로 진입할지 여부 (롤백용 플래그)
private const val WEBAPP_ON_AUTOLOGIN = true

class SplashActivity : BaseActivity(), InstallStateUpdatedListener {
    private var enableBack = true
    private var appUpdateManager : AppUpdateManager? = null
    private val UPDATE_IMMEDIATE = 700
    private val UPDATE_FLEXIBLE = 701
    val viewModel: SplashActViewModel by viewModels()
    private val binding: ActivitySplashBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_splash,null,false)
    }


    private lateinit var referrerClient: InstallReferrerClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // 레이아웃 분기(sw600dp)와 동일 기준으로 판별해 방향-레이아웃 불일치를 막는다.
        requestedOrientation = if(resources.getBoolean(R.bool.isPortrait)) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE





        viewModel.goLoginActCallback = {
            startActivity(StartActivity::class.java)
            finishAffinity()
        }
        viewModel.user.observe(this) {
            MyApplication.user = it
            MyApplication.token = it?.token
        }
//        splashLottie.playAnimation()

        // 클라이언트 인스턴스 생성
        referrerClient = InstallReferrerClient.newBuilder(this).build()

        // Google Play와 연결 시작
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // 연결 성공, 이제 설치 리퍼러 정보를 가져올 수 있습니다
                        getInstallReferrerData()
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // 현재 Play 스토어 앱에서 API를 사용할 수 없음
                        serverCheckAndStart()
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // 연결을 설정할 수 없음
                        serverCheckAndStart()
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // 연결이 끊어진 경우 다시 연결 시도
                // 다음 요청 시 startConnection() 메서드를 호출하여 재연결
                serverCheckAndStart()
            }
        })

    }

    private fun serverCheckAndStart () {
        CoroutineScope(Dispatchers.Main).launch {
            val isServerUnderInspection = checkServerInspection()
            if (isServerUnderInspection.not()) {
                start(false)
            }
        }
    }
    private fun getInstallReferrerData() {
        try {
            val response: ReferrerDetails = referrerClient.installReferrer

            val referrerUrl: String = response.installReferrer

            val referrerClickTime: Long = response.referrerClickTimestampSeconds

            val appInstallTime: Long = response.installBeginTimestampSeconds

//            val currentTime = System.currentTimeMillis() / 1000
//            val isInstalledWithInOneHour = (currentTime - appInstallTime) < 3600
            val referrerParams = parseReferrerString(referrerUrl)
            val isWhaleSpaceTarget = referrerParams["target"] == "whalespace"
//            val whaleSpaceStart = isInstalledWithInOneHour && isWhaleSpaceTarget

            // Google Play 인스턴트 경험 실행 여부
//            val instantExperienceLaunched: Boolean = response.googlePlayInstantParam


            // 사용 후 연결 종료
            referrerClient.endConnection()

            // 여기서 리퍼러 데이터를 처리하는 로직 구현
            Log.d("InstallReferrer", "Referrer URL: $referrerUrl")
            Log.d("InstallReferrer", "Click time: $referrerClickTime")
            Log.d("InstallReferrer", "Install time: $appInstallTime")
            Log.d("InstallReferrer", "isWhaleSpaceTarget: $isWhaleSpaceTarget")

            CoroutineScope(Dispatchers.Main).launch {
                val isServerUnderInspection = checkServerInspection()
                if (isServerUnderInspection.not()) {
                    start(isWhaleSpaceTarget)
                }
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    suspend fun checkServerInspection(): Boolean {
        var status: ServerStatus? = null
        val inspectionJob = CoroutineScope(Dispatchers.IO).async {
//            delay(800)
            status = ServerStatusManager.requestInspectionFlag()

            withContext(Dispatchers.Main) {
                if (status != null) {
                    val dialog = ServerInspectionDialog(this@SplashActivity, status!!)
                    dialog.setCancelable(false)
                    dialog.show()
                }
            }

        }
        inspectionJob.await()
        return status != null
    }

    fun start(whaleSpaceStart: Boolean) {

        Log.d("테스트", "start()")

        if(appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager!!.registerListener(this)
        }

        VersionManager.requestVersionInfo(this) { required, info ->
            Log.d("테스트", "Required=${required}")
            when (required) {
                VersionManager.Required.NOT -> checkSign(whaleSpaceStart)
                VersionManager.Required.MINOR -> checkSign(whaleSpaceStart) //requestAppUpdate(AppUpdateType.FLEXIBLE) 일단 안쓰기로...
                VersionManager.Required.MAJOR -> updateDialog(info, whaleSpaceStart)
            }
        }
    }


    private fun updateDialog(info:VersionInfo?, whaleSpaceStart: Boolean) {
        if (info == null) {
            checkSign(whaleSpaceStart)
        } else {
            val dialogTitle = info.updateTitle ?: "보다 나은 풀리수학 이용을 위해 지금 업데이트 해주세요 :)"
            var dialogContents = info.updateContent ?: "서비스 안정화"

            Preferences.forceUpdateDialogCount.set(Preferences.forceUpdateDialogCount.get() + 1)
            val updateCount = Preferences.forceUpdateDialogCount.get()

            if (updateCount > 2) {
                dialogContents += "\n\n 문제가 있을 경우 앱 설정에서 '구글 플레이스토어' 캐시를 삭제하거나 \n카카오톡 @풀리는수학으로 문의주세요."
            }

            DialogUtils.confirmV2(
                context = this,
                title = dialogTitle,
                contents = dialogContents,
                leftBtnText = "종료",
                rightBtnText = "확인",
                isCancelable = false,
                successCb = {
                    requestAppUpdate(AppUpdateType.IMMEDIATE)
                },
                cancelCb = {
                    LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "스플래쉬", "업데이트취소", "업데이트확인")
                    finishAndRemoveTask()
                }
            )

        }
    }

    private fun requestAppUpdate(updateType:Int) {
        val task = appUpdateManager?.appUpdateInfo
        Log.d("테스트", "task=${task}")
        task?.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability()
            Log.d("테스트", "updateAvailability=${isUpdateAvailable}")
            Log.d("테스트", "installStatus=${appUpdateInfo.installStatus()}")

            requestAppStore()
        }?.addOnFailureListener { ex ->
            requestAppStore()
            Log.e("테스트", "appUpdateInfo error=${ex.localizedMessage}")
        }
    }

    private fun requestAppStore() {
        // BETA 앱은 앱스토어에 없기 때문에 제대로 동작하지 않음
        // BETA앱으로 테스트 시 packageName에 com.freewheelin.pulley 를 입력해야한다.
        try {
            IntentUtils.openMarketLink(this, packageName)
        } catch (e: ActivityNotFoundException) {
            IntentUtils.openWebLink(this, "https://play.google.com/store/apps/details?id=${packageName}", this.packageManager)
            Log.e("테스트", "requestAppStore error=${e.localizedMessage}")
        }
    }

    fun appUpdateForResult(appUpdateInfo: AppUpdateInfo, updateType:Int) {
        val requestCode = if(updateType == AppUpdateType.IMMEDIATE) UPDATE_IMMEDIATE else UPDATE_FLEXIBLE
        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                updateType,
                this,
                requestCode
            )
        } catch (e:Exception) {
            LogUtils.errorEvent(PulleyEvent.ERROR, user, "appUpdateForResult:${e.localizedMessage}")
        }
    }

    override fun onStateUpdate(state: InstallState) {
        when (state.installStatus()){
            InstallStatus.DOWNLOADING -> { Log.d("테스트", "InstallState = 다운로딩") }
            InstallStatus.DOWNLOADED ->  { Log.d("테스트", "InstallState = 다운로드완료") }
            InstallStatus.INSTALLING ->  { Log.d("테스트", "InstallState = 설치중") }
            InstallStatus.INSTALLED ->   { Log.d("테스트", "InstallState = 설치완료") }
        }
    }

    private fun checkSign(whaleSpaceStart: Boolean) {
        Preferences.forceUpdateDialogCount.set(0)
        Preferences.initTestData.set("")
        Log.d(javaClass.simpleName, "checkSign user=${MyApplication.user}")
        println("온보딩 :checkSign : isNeedNewOnBoarding: ${isNeedNewOnBoarding}")

        if (isNeedNewOnBoarding && !whaleSpaceStart) {
            viewModel.getOnBoardItems (
                successCb = { images ->
                    finishAffinity()
                    val intent = OnBoardingActivity.getIntent(this, images)
                    startActivity(intent)
                },
                deniedCb = {
                    checkTokenAndMoveActivity(whaleSpaceStart)
                }
            )
            return
        }
        println("asoaso SplashACt : MyApplication.user?.token : ${MyApplication.user?.token}")
        println("asoaso SplashACt : MyApplication.token : ${MyApplication.token}")
        checkTokenAndMoveActivity(whaleSpaceStart)
    }
    private fun checkTokenAndMoveActivity(whaleSpaceStart: Boolean) {
        val goStartActivity: () -> Unit = {
            if (whaleSpaceStart) {
                println("aspasp 5")
                val intent = Intent(this@SplashActivity, WhaleSpaceLoginActivity::class.java)
                intent.putExtra("AUTO_ACTION", true)
                startActivity(intent)
            } else {
                println("aspasp 6")
                startActivity(StartActivity::class.java)
            }
            finish()
        }
        if(MyApplication.user?.token?.isNotEmpty() == true) {
            viewModel.refreshAutoLoginToken(successCb = {
                viewModel.fetchUser { user ->
                    MyApplication.isAppFirstLaunch = true
                    MyApplication.user!!.commit("SplashActivity.isExceedDevice = true, after delete device [success]")
                    toLogin(user, whaleSpaceStart)
                }
            }, expiredCb = goStartActivity)

        } else {
            println("aspasp 4")
            goStartActivity()
        }
    }

    private fun toLogin(user: UserV4, whaleSpaceStart: Boolean) {
//        Log.d(javaClass.simpleName, "moveActivity() => user ${user.token.isEmpty() == true} =${user?.token}")
        user?.let { FirebaseCrashlytics.getInstance().setUserId(it.studentID) }
        println("온보딩 : toLogin : isNeedNewOnBoarding: ${isNeedNewOnBoarding}")

        when {
            isNeedNewOnBoarding && !whaleSpaceStart -> {
                viewModel.getOnBoardItems (
                    successCb = { images ->
                        finishAffinity()
                        val intent = OnBoardingActivity.getIntent(this, images)
                        startActivity(intent)
                    },
                    deniedCb = {
                        goMainActivity(user)
                    }
                )
            }
            else -> {
                goMainActivity(user)
            }
        }
    }
    private fun goMainActivity(user: UserV4) {
        loadAlimSetting(user)
        putFcmToken(user)
        viewModel.sendLoginLog(user, user.accountEmail)
        viewModel.fetchMainProfile {
            finishAffinity()
            // 교육청(AIEP)·웨일스페이스 사용자는 네이티브 Main 대신 WebView로 webapp을 사용한다
            val intent = if (WEBAPP_ON_AUTOLOGIN && user.signInChannel.usesWebApp) {
                AiepWebViewActivity.webAppIntent(this)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            val isServerUnderInspection = checkServerInspection()
            if (isServerUnderInspection.not()) {
//                start()
            }
        }

//        continueUpdateProcess()
    }

    fun putFcmToken(user: UserV4?) {
        if(user?.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    disposables += API_APP.putToken(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ _ ->
                            Log.d(javaClass.simpleName, "토큰등록=$token")
                        }, { error ->
                            Log.e(javaClass.simpleName, "splash putFcmToken error=${error.localizedMessage}")
                        })

                }
            })
        }
    }

    fun loadAlimSetting(user: UserV4?) {
        disposables += API_APP.getNotificationSetting()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                response.data.apply {
//                    user?.update(agreeAlimtalk = isAgreeAlimtalk, agreeAppPush = isAgreePush, agreeEmail = isAgreeEmail, agreeMarketing = isAgreeMarketing, schoolID = user.schoolID)
                }
            },{
                /* do nothing */
            })
    }
    fun parseReferrerString(referrerString: String): Map<String, String> {
        val result = mutableMapOf<String, String>()

        val pairs = referrerString.split("&")

        pairs.forEach { pair ->
            val keyValue = pair.split("=", limit = 2)
            if (keyValue.size == 2) {
                val key = keyValue[0].trim()
                val value = keyValue[1].trim()

                val decodedValue = java.net.URLDecoder.decode(value, "UTF-8")
                result[key] = decodedValue
            }
        }

        return result
    }
    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("테스트", "onActivityResult resultCode=$resultCode, requestCode=$requestCode")
        if(resultCode == RESULT_OK)
            appUpdateManager?.completeUpdate()
        else {
            when(requestCode) {
                UPDATE_FLEXIBLE -> checkSign(false)
                UPDATE_IMMEDIATE -> {
                    Toast.makeText(this, "필수 업데이트입니다. 업데이트를 하지 않으면 앱이 강제 종료됩니다.", Toast.LENGTH_LONG).show()
                    finishAndRemoveTask()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (enableBack) {
            super.onBackPressed()
        }
    }

}