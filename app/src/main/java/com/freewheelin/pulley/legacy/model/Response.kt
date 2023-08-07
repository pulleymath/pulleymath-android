package com.freewheelin.pulley.legacy.model


data class Template<T> (
    val result: String?,
    val data: T,
    val message: String,
    val isSessionExpired: Boolean?,
    val error: String?
)

data class ResponseBody<T> (
    val data: T?,
    val message: String?,
    val error: String?,
    val current_time: String?
)

data class ResponseForceBody<T> (
    val data: T,
    val message: String?,
    val error: String?,
    val current_time: String?
)

data class ResponseListBody<T> (
    val data: List<T> = listOf(),
    val message: String?,
    val error: String?,
    val current_time: String?
)
data class ResponseNullableListBody<T> (
    val data: List<T>?,
    val message: String?,
    val error: String?,
    val current_time: String?
)
