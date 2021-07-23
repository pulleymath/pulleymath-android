package com.freewheelin.pulley.activities.learning.tabFragment.main.serverInspection

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogServerInspectionBinding
import com.freewheelin.pulley.model.ServerStatus
import kotlin.system.exitProcess

class ServerInspectionDialog(activity: Activity, val status: ServerStatus): Dialog(activity) {

    private val binding: DialogServerInspectionBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_server_inspection, null, false)
    }
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        initUI(activity)
    }

    private fun initUI(activity: Activity) {
        val startDate = dateParsingToGoodWordWithYear(status.checkStart)
        val endDate = dateParsingToGoodWordWithoutYear(status.endStart)
        binding.dialogInfomationTv.text = "점검시간\n$startDate ~ $endDate"
        binding.inspectionTextBtn.setOnClickListener {
            dismiss()
            activity.moveTaskToBack(true)
            activity.finishAndRemoveTask()
            exitProcess(0)

        }
    }

    private fun dateParsingToGoodWordWithYear(dateStr: String): String {
        val yearMonthDay = dateStr.substringBefore(" ")
        val hoursMinSec = dateStr.substringAfter(" ")
        val ymdList = yearMonthDay.split("-")
        if (ymdList.size != 3) return ""
        val hmsList = hoursMinSec.split(":")
        if (hmsList.size != 3) return ""

        return "${ymdList[0]}년 ${ymdList[1]}월 ${ymdList[2]}일 ${hmsList[0]}:${hmsList[1]}"
    }

    private fun dateParsingToGoodWordWithoutYear(dateStr: String): String {
        val yearMonthDay = dateStr.substringBefore(" ")
        val hoursMinSec = dateStr.substringAfter(" ")
        val ymdList = yearMonthDay.split("-")
        if (ymdList.size != 3) return ""
        val hmsList = hoursMinSec.split(":")
        if (hmsList.size != 3) return ""

        return "${ymdList[1]}월 ${ymdList[2]}일 ${hmsList[0]}:${hmsList[1]}"
    }
}