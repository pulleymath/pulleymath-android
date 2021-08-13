package com.freewheelin.pulley.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.InitSettingActivity
import com.freewheelin.pulley.activities.auth.InitTestActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.tabFragment.main.serverInspection.ServerInspectionDialog
import com.freewheelin.pulley.bases.*
import com.freewheelin.pulley.core.manage.ServerStatusManager
import com.freewheelin.pulley.core.manage.VersionInfo
import com.freewheelin.pulley.core.manage.VersionManager
import com.freewheelin.pulley.dialogs.DeviceManagerDialog
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.dialog_daebak.*
import kotlinx.coroutines.*

class SplashActivity : BaseActivity(), InstallStateUpdatedListener {
    private var enableBack = true
    private var appUpdateManager : AppUpdateManager? = null
    private val UPDATE_IMMEDIATE = 700
    private val UPDATE_FLEXIBLE = 701

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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
            when(required) {
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
            val dialogTitle = info.updateTitle ?: "보다 나은 풀리 이용을 위해 지금 업데이트 해주세요 :)"
            val dialogContents = info.updateContent ?: "서비스 안정화"
            val dialog = DialogUtils.makeDialog(this, dialogTitle, dialogContents, "종료", "확인")
            dialog.leftBtn.setOnClickListener { finishAndRemoveTask() }
            dialog.setCancelable(false)
            dialog.setOnCancelListener {
                LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "스플래쉬", "업데이트취소", "업데이트확인")
            }
            dialog.rightBtn.setOnClickListener {
                requestAppUpdate(AppUpdateType.IMMEDIATE)
            }
            dialog.show()
        }
    }

    fun requestAppUpdate(updateType:Int) {
        val task = appUpdateManager?.appUpdateInfo
        Log.d("테스트", "task=${task}")
        task?.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability()
            Log.d("테스트", "updateAvailability=${isUpdateAvailable}")
            Log.d("테스트", "installStatus=${appUpdateInfo.installStatus()}")
            if(isUpdateAvailable == UpdateAvailability.UPDATE_AVAILABLE ||
                isUpdateAvailable == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateForResult(appUpdateInfo, updateType)
            } else {
                // 여기도 없애야 할수도 있음
                checkSign()
            }
        }?.addOnFailureListener { ex ->
            // 여기서 걍 앱스토어로 보내야함
            try {
                // BETA 앱은 앱스토어에 없기 때문에 제대로 동작하지 않음
                // BETA앱으로 테스트 시 packageName에 com.freewheelin.pulley 를 입력해야한다.

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}")))
            } catch (e: ActivityNotFoundException) {
                Log.d("테스트", "activity not found exception")
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.freewheelin.pulley")))
            }
            Log.e("테스트", "appUpdateInfo error=${ex.localizedMessage}")
//            checkSign()
        }
    }

    fun appUpdateForResult(appUpdateInfo: AppUpdateInfo, updateType:Int) {
        val requestCode = if(updateType == AppUpdateType.IMMEDIATE) UPDATE_IMMEDIATE else UPDATE_FLEXIBLE
        appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                updateType,
                this,
                requestCode
        )
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
            else -> startActivity(LearningTabActivity::class.java)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        continueUpdateProcess()
    }

    fun continueUpdateProcess() {
        val task = appUpdateManager?.appUpdateInfo
        task?.addOnSuccessListener { appUpdateInfo ->
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateForResult(appUpdateInfo, AppUpdateType.IMMEDIATE)
            } else checkSign()
        }?.addOnFailureListener { checkSign() }
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