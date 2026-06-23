package com.freewheelin.pulley.legacy.activities.mypage

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.core.manage.VersionManager
import com.freewheelin.pulley.databinding.FragmentMyAppSettingBinding
import com.freewheelin.pulley.databinding.FragmentMyVersionBinding
import com.freewheelin.pulley.legacy.utils.IntentUtils
import com.freewheelin.pulley.legacy.views.DaebakToast


class MyVersionFragment : MyPageBaseFragment() {
    lateinit var binding: FragmentMyVersionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_version, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            val appVersionInGsValidation = VersionManager.appVersion.split(".").dropLast(1).joinTo(StringBuilder(), ".").toString()
            val infoVersionInGsValidation = VersionManager.info!!.version.split(".").dropLast(1).joinTo(StringBuilder(), ".").toString()
            currentVersionTv.text = "V $appVersionInGsValidation"
            latestVersionTv.text = "V $infoVersionInGsValidation"
            backBtn.setOnClickListener { onBackBtnClicked() }
            if(VersionManager.isNeedToUpdate() == true) {
                currentVersionTv.typeface = Theme.bold(requireContext())
                currentVersionTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_800))
                updateBtn.text = "업데이트 확인하기"
                updateBtn.isEnabled = true
                updateBtn.setOnClickListener {
                    openAppMarket(requireContext())
                }
            } else {
                currentVersionTv.typeface = Theme.regular(requireContext())
                currentVersionTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_500))
                updateBtn.text = "최신 버전 사용 중"
                updateBtn.isEnabled = false
                updateBtn.setOnClickListener {
                    DaebakToast.show(requireContext(), "이미 최신버전 입니다.")
                }
            }
        }
    }


    fun openAppMarket(context: Context) {

        // you can also use BuildConfig.APPLICATION_ID
        val appId: String = context.getPackageName()
        val rateIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appId"))
        var marketFound = false

        // find all applications able to handle our rateIntent
        val otherApps: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                rateIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(rateIntent, 0)
        }
        for (otherApp in otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    == "com.android.vending") {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                )
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.component = componentName
                context.startActivity(rateIntent)
                marketFound = true
                break
            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            val url = "https://play.google.com/store/apps/details?id=$appId"
            IntentUtils.openWebLink(requireContext(), url, requireContext().packageManager)
        }
    }
}
