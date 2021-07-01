package com.freewheelin.pulley.core.API.ResponseModel

class ResponseDevice : BaseResponse<Device>()

data class Device (val id:Int, val deviceName:String, val lastAccessDate:String, val isTarget:Boolean)