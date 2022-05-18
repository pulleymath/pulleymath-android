package com.freewheelin.pulley.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.InitSettingActivity
import com.freewheelin.pulley.activities.auth.InitTestActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.tabFragment.main.serverInspection.ServerInspectionDialog
import com.freewheelin.pulley.bases.*
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.core.manage.ServerStatusManager
import com.freewheelin.pulley.core.manage.VersionInfo
import com.freewheelin.pulley.core.manage.VersionManager
import com.freewheelin.pulley.databinding.ActivityOnboardingBinding
import com.freewheelin.pulley.databinding.ActivitySplashBinding
import com.freewheelin.pulley.dialogs.DeviceManagerDialog
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.Preferences
import com.freewheelin.pulley.utils.PulleyEvent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

class SplashActivity : BaseActivity(), InstallStateUpdatedListener {
    private var enableBack = true
    private var appUpdateManager : AppUpdateManager? = null
    private val UPDATE_IMMEDIATE = 700
    private val UPDATE_FLEXIBLE = 701
    private val binding: ActivitySplashBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_splash,null,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        requestedOrientation = if(isMobileUI) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

//        splashLottie.playAnimation()

        CoroutineScope(Dispatchers.IO).launch {
            delay(800)
            val status = ServerStatusManager.requestInspectionFlag()

            if (status != null) {
                withContext(Dispatchers.Main) {
                    val dialog = ServerInspectionDialog(this@SplashActivity, status)
                    dialog.setCancelable(false)
                    dialog.show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    start()
                }
            }
        }
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


    fun updateDialog(info:VersionInfo?) {
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

            val dialog = DialogUtils.makeDialog(this, dialogTitle, dialogContents, "종료", "확인")
            dialog.binding.leftBtn.setOnClickListener { finishAndRemoveTask() }
            dialog.setCancelable(false)
            dialog.setOnCancelListener {
                LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "스플래쉬", "업데이트취소", "업데이트확인")
            }
            dialog.binding.rightBtn.setOnClickListener {
                requestAppUpdate(AppUpdateType.IMMEDIATE)
            }

            if(!isFinishing) dialog.show()
        }
    }

    fun requestAppUpdate(updateType:Int) {
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

    fun requestAppStore() {
        // BETA 앱은 앱스토어에 없기 때문에 제대로 동작하지 않음
        // BETA앱으로 테스트 시 packageName에 com.freewheelin.pulley 를 입력해야한다.
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")))
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

    fun checkSign() {
        Preferences.forceUpdateDialogCount.set(0)
        Log.d(javaClass.simpleName, "checkSign user=${MyApplication.user}")

        if(MyApplication.user?.token?.isNotEmpty() == true) {
            MyApplication.user?.syncMyInfo(this) { user ->

                MyApplication.user = user
                MyApplication.user!!.commit("SplashActivity.isExceedDevice = true, after delete device [success]")

                if (user.isExceedDevice) {
                    // commit 은 기기 삭제후에
                    DeviceManagerDialog(this, successCB = {
                        startActivity(Intent(this, LearningTabActivity::class.java))
                        finishAffinity()
                    }, failCB = {
                        MyApplication.user!!.token = ""
                        MyApplication.user!!.commit("SplashActivity.isExceedDevice = true, after delete device [failed]")
                        finishAffinity()
                    }).show()

                } else {
                    toLogin()
                }
            }
        } else {
            startActivity(StartActivity::class.java)
            finish()
        }
    }

    fun toLogin() {
        Log.d(javaClass.simpleName, "moveActivity() => user ${user?.token?.isEmpty() == true} =${user?.token}")
        user?.connectToCrashlytics()

        when {
            isNeedOnboarding -> startActivity(OnboardingActivity::class.java)
            user?.initSettingCompleted != true -> { startActivity(InitSettingActivity.getIntent(this)) }
            user?.studentType == null -> { startActivity(InitTestActivity.getIntent(this)) }
            else -> {
                loadAlimSetting(user)
                putFcmToken(user)

                val pushParam = intent.getStringExtra("target_android")
                Intent(this, LearningTabActivity::class.java)
                    .putExtra("target_android", pushParam).apply {
                        startActivity(this)
                    }
            }
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        start()
//        continueUpdateProcess()
    }

    fun putFcmToken(user: User?) {
        if(user?.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    API_APP.putToken(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ _ ->
                            Log.d(javaClass.simpleName, "토큰등록=$token")
                        }, { })
                }
            })
        }
    }

    fun loadAlimSetting(user: User?) {
        API_APP.getNotificationSetting()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                response.data?.apply {
                    user?.update(agreeAlimtalk = isAgreeAlimtalk, agreeAppPush = isAgreePush, agreeEmail = isAgreeEmail, agreeMarketing = isAgreeMarketing, schoolID = user?.schoolID)
                }
            },{
                /* do nothing */
            })
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