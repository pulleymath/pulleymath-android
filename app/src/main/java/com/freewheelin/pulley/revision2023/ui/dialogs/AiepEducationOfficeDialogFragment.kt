package com.freewheelin.pulley.revision2023.ui.dialogs

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogAiepEducationOfficeSelectBinding
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2021.repository.remote.Network

class AiepEducationOfficeDialogFragment : DialogFragment() {

    private val binding: DialogAiepEducationOfficeSelectBinding by lazy {
        DataBindingUtil.inflate(
            layoutInflater.cloneInContext(requireContext()),
            R.layout.dialog_aiep_education_office_select, null, false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated server=${Preferences.onServerAPI.get()}")
        binding.apply {
            closeBtn.setOnClickListener { dismiss() }
            seoulCard.setOnClickListener {
                Log.d(TAG, "seoulCard clicked")
                openCustomTab(seoulUrl())
                dismiss()
            }
            jeonnamCard.setOnClickListener {
                Log.d(TAG, "jeonnamCard clicked")
                openCustomTab(jeonnamUrl())
                dismiss()
            }
        }
    }

    private fun isLiveServer(): Boolean =
        Preferences.onServerAPI.get() == Network.Server.live.toString()

    private fun seoulUrl(): String =
        if (isLiveServer()) SEOUL_AIEP_LOGIN_URL_LIVE else SEOUL_AIEP_LOGIN_URL_DEV

    private fun jeonnamUrl(): String =
        if (isLiveServer()) JEONNAM_AIEP_LOGIN_URL_LIVE else JEONNAM_AIEP_LOGIN_URL_DEV

    override fun onStart() {
        super.onStart()
        val widthPx = (resources.displayMetrics.density * 448).toInt()
        dialog?.apply {
            setCanceledOnTouchOutside(true)
            window?.apply {
                setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes = attributes.apply { dimAmount = 0.5f }
            }
        }
        Log.d(TAG, "onStart widthPx=$widthPx")
    }

    private fun openCustomTab(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }

    companion object {
        private const val TAG = "AiepEducationOfficeDialogFragment"

        private const val AIEP_LINK_URL_LIVE = "https://pulleymath.com/link/aiep"
        private const val AIEP_LINK_URL_DEV = "https://dev.pulleymath.com/link/aiep"

        fun openAiepLink(context: Context) {
            val isLive = Preferences.onServerAPI.get() == Network.Server.live.toString()
            val url = if (isLive) AIEP_LINK_URL_LIVE else AIEP_LINK_URL_DEV
            Log.d(TAG, "openAiepLink url=$url")
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            customTabsIntent.launchUrl(context, Uri.parse(url))
        }

        private const val SEOUL_AIEP_LOGIN_URL_LIVE =
            "https://auth.senedu.kr/login/?next=%2Fidp%2Fsso%2Finit%2F%3Fsp%3Dhttps%253A%252F%252Fsen-live.pulleymath.com%252Fsp%252Fmetadata%252F%26RelayState%3Dapp&mode=form"
        private const val SEOUL_AIEP_LOGIN_URL_DEV =
            "https://ai-auth.sen.go.kr/login/?next=%2Fidp%2Fsso%2Finit%2F%3Fsp%3Dhttps%253A%252F%252Fsen-dev.pulleymath.com%252Fsp%252Fmetadata%252F%26RelayState%3D&mode=form"

        private const val JEONNAM_AIEP_LOGIN_URL_LIVE =
            "https://ai-auth.jne.kr/login/?next=%2Fidp%2Fsso%2Finit%2F%3Fsp%3Dhttps%253A%252F%252Fsen-live.pulleymath.com%252Fsp%252Fmetadata%252F%26RelayState%3Dapp&mode=form"
        private const val JEONNAM_AIEP_LOGIN_URL_DEV =
            "https://ai-auth.jne.kr/login/?next=%2Fidp%2Fsso%2Finit%2F%3Fsp%3Dhttps%253A%252F%252Fsen-dev.pulleymath.com%252Fsp%252Fmetadata%252F%26RelayState%3D&mode=form"

        fun newInstance() = AiepEducationOfficeDialogFragment()
    }
}
