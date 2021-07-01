package com.freewheelin.pulley.activities.mypage


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.fragment_my_app_setting.*
import android.widget.LinearLayout
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.Device
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.utils.DialogUtils
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_device_manager.*
import kotlinx.android.synthetic.main.item_device.view.*
import retrofit2.HttpException


class MyDeviceManagerFragment : MyPageBaseFragment() {

    val user
        get() = requireActivity().application.user!!

    lateinit var adapter:DeviceListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_device_manager, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setAdapter()
    }

    private fun logoutAll() {
        API_V2.deleteAllDevice()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    adapter.reload()
                },{
                    if(it is HttpException) {
                        val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                        DaebakToast.show(requireContext(), error.message?:"", overDialog = true)
                    } else {
                        DialogUtils.serverErrDialog(requireContext())
                    }
                })
    }

    private fun setAdapter() {
        adapter = DeviceListAdapter(requireActivity(), deviceListContainer)
        adapter.reload()
    }
}

class DeviceListAdapter(val activity: Activity, val parent:LinearLayout) {

    fun reload() {
        API_V2.getDevices()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                setList(response.data)
            },{
                if(it is HttpException) {
                    val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                } else {
                    DialogUtils.serverErrDialog(activity)
                }
            })
    }

    private fun setList(devices: List<Device>?) {
        parent.removeAllViews()
        var count = devices?.size?:0

        for(device in devices?: listOf()) {
            val view  = LayoutInflater.from(activity).inflate(R.layout.item_device, parent, false)
            parent.addView(view)

            view.textName.text = device.deviceName
            view.lastAccess.text = device.lastAccessDate
            if(device.isTarget) {
                view.currentDevice.visibility = View.VISIBLE
                view.deleteBtn.visibility = View.GONE
            }
            view.deleteBtn.setOnClickListener {
                DialogUtils.confirmLogoutDevice(activity, device.deviceName) {
                    deleteDevice(device.id, view)
                }
            }
            view.noDeviceContainer.visibility = View.GONE
            Log.d(javaClass.simpleName, "device=$device")
        }

        if(count < 3) // 3대 보다 적으면 빈칸
            for(id in count..2) {
                val view  = LayoutInflater.from(activity).inflate(R.layout.item_device, parent, false)
                parent.addView(view)
            }
    }

    private fun deleteDevice(id:Int, view:View) {
        startLoading(view)
        API_V2.deleteDevice(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                stopLoading(view)
                reload()
            },{
                stopLoading(view)
                if(it is HttpException) {
                    val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                    DaebakToast.show(activity, error.message?:"", overDialog = true)
                } else {
                    DialogUtils.serverErrDialog(activity)
                }
            })
    }

    private fun startLoading(view:View) {
        view.loadingContainer.visibility = View.VISIBLE
    }

    private fun stopLoading(view:View) {
        view.loadingContainer.visibility = View.GONE
    }
}