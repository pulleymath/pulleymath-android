package com.freewheelin.pulley.legacy.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.API.ResponseModel.Device
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.ui.view.CommonButton
import com.freewheelin.pulley.revision2023.ui.view.TertiaryButton
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class DeviceManagerDialog(val activity: Activity, val successCB:()->Unit, val failCB:()->Unit): Dialog(activity) {

    var deviceList = mutableListOf<Device>()
    var selectedDevice:Device? = null

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_device_manager)
        initComponents()
        initUI()
    }

    private fun initUI() {
        deviceList.clear()

        setCancelable(false)

        btnClose.setOnClickListener { close() }
        cancelBtn.setOnClickListener { close() }

        deviceContainer.setOnCheckedChangeListener { group, checkedId ->
            selectedDevice = deviceList.firstOrNull { it.id == checkedId }
            deleteBtn.isEnabled = true
            Log.d(javaClass.simpleName, "selectedDevice=$selectedDevice")
        }

        deleteBtn.isEnabled = false
        deleteBtn.setOnClickListener {
            selectedDevice?.let { device ->
                if(deleteBtn.isEnabled) {
                    DialogUtils.confirmLogoutDevice(activity, device.deviceName) {
                        startLoading()
                        deleteDevice(device.id) // 선택된 라디오 버튼의 디바이스 삭
                    }
                }
            }

        }

        loadDevices()
    }

    private fun close() {
        dismiss()
        failCB()
    }

    private fun loadDevices() {
        API_V2.getDevices()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                setList(response.data?: listOf())
            },{
                if(it is HttpException) {
                    val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)

                } else {
                    DialogUtils.serverErrDialog(activity)
                }
            })
    }

    private fun setList(devices: List<Device>) {
        deviceList.addAll(devices)
        deviceContainer.removeAllViews()
        for(device in deviceList) {
            val radio  = LayoutInflater.from(activity).inflate(R.layout.item_device_radio, deviceContainer, false) as RadioButton
            radio.text = device.deviceName
            radio.id = device.id
            deviceContainer.addView(radio)
        }
    }

    private fun deleteDevice(id:Int) {

        API_V2.deleteDevice(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                stopLoading()
                dismiss()
                successCB()
            },{
                stopLoading()
                if(it is HttpException) {
                    val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                    DaebakToast.show(activity, error.message?:"", overDialog = true)
                } else {
                    DialogUtils.serverErrDialog(activity)
                }
            })
    }


    private fun startLoading() {
        deleteBtn.setLoading(true)
    }

    private fun stopLoading() {
        deleteBtn.setLoading(false)
    }

    lateinit var btnClose: ImageButton
    lateinit var cancelBtn: TertiaryButton
    lateinit var deviceContainer: RadioGroup
    lateinit var deleteBtn: CommonButton
    private fun initComponents() {
        btnClose = findViewById(R.id.btnClose)
        cancelBtn = findViewById(R.id.cancelBtn)
        deviceContainer = findViewById(R.id.deviceContainer)
        deleteBtn = findViewById(R.id.deleteBtn)
    }
}