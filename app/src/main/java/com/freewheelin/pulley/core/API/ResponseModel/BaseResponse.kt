package com.freewheelin.pulley.core.API.ResponseModel

abstract class BaseResponse<T> {
    var data: List<T>? = null
    var error: String? = null
    var message: String? = null
}