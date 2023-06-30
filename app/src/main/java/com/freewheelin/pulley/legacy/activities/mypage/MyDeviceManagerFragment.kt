package com.freewheelin.pulley.legacy.activities.mypage


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.Device
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMyDeviceManagerBinding
import com.freewheelin.pulley.databinding.ItemDeviceBinding
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class MyDeviceManagerFragment : MyPageBaseFragment() {

    val user
        get() = requireActivity().application.user!!

    lateinit var adapter:DeviceListAdapter
    lateinit var binding: FragmentMyDeviceManagerBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_device_manager, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setAdapter()
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
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
        adapter = DeviceListAdapter(requireActivity(), binding.deviceListContainer)
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
            val itemBinding: ItemDeviceBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_device, parent, false)
//            val view  = LayoutInflater.from(activity).inflate(R.layout.item_device, parent, false)
            parent.addView(itemBinding.root)

            itemBinding.textName.text = device.deviceName
            itemBinding.lastAccess.text = device.lastAccessDate
            if(device.isTarget) {
                itemBinding.currentDevice.visibility = View.VISIBLE
                itemBinding.deleteBtn.visibility = View.GONE
            }
            itemBinding.deleteBtn.setOnClickListener {
                DialogUtils.confirmLogoutDevice(activity, device.deviceName) {
                    deleteDevice(device.id, itemBinding)
                }
            }
            itemBinding.noDeviceContainer.visibility = View.GONE
            Log.d(javaClass.simpleName, "device=$device")
        }

        if(count < 3) // 3대 보다 적으면 빈칸
            for(id in count..2) {
                val itemBinding: ItemDeviceBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_device, parent, false)
                parent.addView(itemBinding.root)
            }
    }

    private fun deleteDevice(id:Int, itemBinding: ItemDeviceBinding) {
        startLoading(itemBinding)
        API_V2.deleteDevice(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                stopLoading(itemBinding)
                reload()
            },{
                stopLoading(itemBinding)
                if(it is HttpException) {
                    val error = Gson().fromJson(it.response()?.errorBody()?.string(), ResponseBody::class.java)
                    DaebakToast.show(activity, error.message?:"", overDialog = true)
                } else {
                    DialogUtils.serverErrDialog(activity)
                }
            })
    }

    private fun startLoading(itemBinding:ItemDeviceBinding) {
        itemBinding.loadingContainer.visibility = View.VISIBLE
    }

    private fun stopLoading(itemBinding:ItemDeviceBinding) {
        itemBinding.loadingContainer.visibility = View.GONE
    }
}