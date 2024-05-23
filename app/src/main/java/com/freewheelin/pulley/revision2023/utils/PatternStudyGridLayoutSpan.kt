package com.freewheelin.pulley.revision2023.utils

import android.content.Context
import android.content.res.Configuration
import android.widget.FrameLayout
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.toPx

class PatternStudyLayoutUtils {
    companion object {
        private var instance: PatternStudyLayoutUtils? = null
        fun getInstance(): PatternStudyLayoutUtils {
            if (instance == null) instance = PatternStudyLayoutUtils()
            return instance!!
        }
    }
    fun spanSize(context: Context): Int {
//        val screenWidth = DisplayUtils.getScreenWidth(context)
        val isTablet = context.isTablet
//        val screenLayoutSize = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
//        val isConfigSizeXLarge = screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE

//        if (isTablet && screenWidth > 2560 && isConfigSizeXLarge) return 5

        return if (isTablet) 4 else 2
    }

    fun planCardWidth(context: Context, isGridLayout: Boolean): Int {
        val screenWidth = DisplayUtils.getScreenWidth(context)
        val screenLayoutSize = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isConfigSizeXLarge = screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE

        return if (isGridLayout) {
            FrameLayout.LayoutParams.MATCH_PARENT
        } else {
            if (screenWidth > 2560) {
                220.toPx()
            } else if (isConfigSizeXLarge) {
                220.toPx()
            } else {
                195.toPx()
            }
        }
    }

    fun planCardHeight(context: Context, isGridLayout: Boolean): Int {
        val screenWidth = DisplayUtils.getScreenWidth(context)
        val screenLayoutSize = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isConfigSizeXLarge = screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE


        return if (screenWidth > 2560) {
            if (isGridLayout) 350.toPx() else 310.toPx()
        } else if (isConfigSizeXLarge) {
            if (isGridLayout) 320.toPx() else 310.toPx()
        } else {
            290.toPx()
        }
    }
    fun filterViewMinHeight(context: Context): Int {
        val screenWidth = DisplayUtils.getScreenWidth(context)
        val screenLayoutSize = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isConfigSizeXLarge = screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
        val isTablet = context.isTablet

        return if (screenWidth > 2560 && isConfigSizeXLarge) {
            850.toPx()
        } else if (isTablet) {
            650.toPx()
        } else {
            550.toPx()
        }
    }
}