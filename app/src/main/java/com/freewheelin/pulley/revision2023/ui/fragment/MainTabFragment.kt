package com.freewheelin.pulley.revision2023.ui.fragment

import androidx.fragment.app.Fragment
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.google.firebase.analytics.FirebaseAnalytics

abstract class MainTabFragment : Fragment() {
    abstract var type: MainTab
    open fun onFragmentSelected() {
        if(context != null && activity != null) {
            val firebase = FirebaseAnalytics.getInstance(requireContext())
            firebase.setCurrentScreen(requireActivity(), type.name, MainTabFragment::class.java.name)
            LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, type.name)
        }
    }
    open fun resetHeaderControlParams() {

    }
}