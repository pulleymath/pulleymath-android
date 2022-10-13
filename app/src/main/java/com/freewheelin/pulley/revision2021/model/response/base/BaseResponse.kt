package com.freewheelin.pulley.revision2021.model.response.base

import java.io.Serializable

/** Spring */
/** Pageable */
abstract class BaseResponsePageable<T> {
    lateinit var data: BaseDataPageable<T>
    lateinit var error: Any
    lateinit var message: Any
}
data class BaseDataPageable<T>(
    val content: List<T>,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val numberOfElements: Int,
    val pageable: Pageable,
    val size: Int,
    val sort: Sort,
    val totalElements: Int,
    val totalPages: Int
)

/** None Pageable */
abstract class BaseResponse<T> {
    lateinit var data: List<T>
    lateinit var error: Any
    lateinit var message: Any
}

/** Node */
/** Pageable */
abstract class BaseResponseNode<T> : Serializable {
    lateinit var data: PageableDataNode<T>
    var error: Any? = null
    var message: Any? = null
}
data class PageableDataNode<T> (
    var content:List<T>,
    var size:Int,
    var totalSize:Int,
    var totalPage:Int,
    var page:Int
) : Serializable

/** Single */
abstract class BaseSingleResponseNode<T> : Serializable {
    var data: T? = null
    var error: Any? = null
    var message: Any? = null
    var current_time: String? = null
}

abstract class BaseAlarmResponse<T>: Serializable {
    lateinit var data: List<T>
    lateinit var error: Any
    lateinit var message: Any
    var current_time: String? = null
}

class BaseCookingResponse<T> : Serializable {
    var data: T? = null
    var error: Any? = null
    var message: Any? = null
    var current_time: String? = null
}

class BaseCookingListResponse<T> : Serializable {
    var data: List<T>? = null
    var error: Any? = null
    var message: Any? = null
    var current_time: String? = null
}