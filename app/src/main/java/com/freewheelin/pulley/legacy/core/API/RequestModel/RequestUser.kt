package com.freewheelin.pulley.legacy.core.API.RequestModel

data class RequestLogin(val email: String, val password: String)

data class RequestReset(val type:String, val target:String)

data class RequestResetPassword(val authNumber:String, val changePassword:String, val type:String, val target:String)

data class RequestCheckCode(val code:String, val channel:String)

class RequestSignup {
    var studentId: String? = null
    var countryCode: String = "82"
    var name: String = ""
    var email: String = ""
    var password: String = ""
    var cellphone: String = ""
    var isAgreeMarketing: Boolean = true
    var schoolInfo: SchoolInfo
    var recommendCode: String? = null

    init {
        schoolInfo = SchoolInfo()
    }
}

class SchoolInfo {
    var schoolID: Int? = null
    var regionID: Int? = null
    var grade: Int = 2
    var initMoGrade: Int = 4
    var majorType: String = ""
}

data class RequestChangeEmail(val email:String, val auth:String)
data class RequestChangePhone(val cellphone:String, val auth:String, val countryCode: String)
data class RequestChangePassword(val current:String, val password:String)
data class RequestAgreeInfo(val agreePush:Boolean, val agreeMarketing:Boolean)