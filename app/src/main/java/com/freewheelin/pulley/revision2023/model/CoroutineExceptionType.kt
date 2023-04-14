package com.freewheelin.pulley.revision2023.model

enum class CoroutineExceptionType {
    NONE,
    Cancellation,
    UnknownHost,
    NullPointer,
    HttpException,
    HttpException400,
    HttpException401,
    HttpException403,
    HttpException502,
    GuestException,
}