package com.freewheelin.pulley.legacy.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.serverInspection.ServerInspectionDialog
import com.freewheelin.pulley.legacy.bases.*
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.core.manage.ServerStatusManager
import com.freewheelin.pulley.legacy.core.manage.VersionInfo
import com.freewheelin.pulley.legacy.core.manage.VersionManager
import com.freewheelin.pulley.databinding.ActivitySplashBinding
import com.freewheelin.pulley.legacy.dialogs.DeviceManagerDialog
import com.freewheelin.pulley.legacy.model.ServerStatus
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.viewmodel.SplashActViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.OnBoardingItem
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.OnBoardingActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.CommonDialog
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
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat

class SplashActivity : BaseActivity(), InstallStateUpdatedListener {
    private var enableBack = true
    private var appUpdateManager : AppUpdateManager? = null
    private val UPDATE_IMMEDIATE = 700
    private val UPDATE_FLEXIBLE = 701
    val viewModel: SplashActViewModel by viewModels()
    private val binding: ActivitySplashBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_splash,null,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        requestedOrientation = if(isMobileUI) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

        CoroutineScope(Dispatchers.Main).launch {
            val isServerUnderInspection = checkServerInspection()
            if (isServerUnderInspection.not()) {
                start()
            }
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

    fun start() {

        Log.d("테스트", "start()")

        if(appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager!!.registerListener(this)
        }

        VersionManager.requestVersionInfo(this) { required, info ->
            Log.d("테스트", "Required=${required}")
            when (required) {
                VersionManager.Required.NOT -> checkSign()
                VersionManager.Required.MINOR -> checkSign() //requestAppUpdate(AppUpdateType.FLEXIBLE) 일단 안쓰기로...
                VersionManager.Required.MAJOR -> updateDialog(info)
            }
        }
    }


    private fun updateDialog(info:VersionInfo?) {
        if (info == null) {
            checkSign()
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

    private fun checkSign() {
        Preferences.forceUpdateDialogCount.set(0)
        Preferences.initTestData.set("")
        Log.d(javaClass.simpleName, "checkSign user=${MyApplication.user}")
        println("온보딩 :checkSign : isNeedNewOnBoarding: ${isNeedNewOnBoarding}")

        if (isNeedNewOnBoarding) {
            viewModel.getOnBoardItems (
                successCb = { images ->

                    val intent = OnBoardingActivity.getIntent(this, images)
                    startActivity(intent)
                },
                deniedCb = {
                    checkTokenAndMoveActivity()
                }
            )
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                finishAffinity()
            }
            return
        }
        println("asoaso SplashACt : MyApplication.user?.token : ${MyApplication.user?.token}")
        println("asoaso SplashACt : MyApplication.token : ${MyApplication.token}")
        checkTokenAndMoveActivity()
    }
    private fun checkTokenAndMoveActivity() {
        if(MyApplication.user?.token?.isNotEmpty() == true) {
            viewModel.fetchUser { user ->
                MyApplication.isAppFirstLaunch = true
                MyApplication.user!!.commit("SplashActivity.isExceedDevice = true, after delete device [success]")
                toLogin()
            }
        } else {
            startActivity(StartActivity::class.java)
            finish()
        }
    }

    private fun toLogin() {
        Log.d(javaClass.simpleName, "moveActivity() => user ${user?.token?.isEmpty() == true} =${user?.token}")
        user?.let { FirebaseCrashlytics.getInstance().setUserId(it.studentID) }
        println("온보딩 : toLogin : isNeedNewOnBoarding: ${isNeedNewOnBoarding}")

        when {
            isNeedNewOnBoarding -> {
                viewModel.getOnBoardItems (
                    successCb = { images ->
                        val intent = OnBoardingActivity.getIntent(this, images)
                        startActivity(intent)
                    },
                    deniedCb = {
                        goMainActivity()
                    }
                )
            }
            else -> {
                goMainActivity()
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            finishAffinity()
        }
    }
    private fun goMainActivity() {
        loadAlimSetting(user)
        putFcmToken(user)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
                        }, { })
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
                UPDATE_FLEXIBLE -> checkSign()
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