package com.freewheelin.pulley.legacy.core

import android.content.Context
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.curation.MainCuration
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ServerCommunicator {
    fun updateUser(studentID: String,
                   schoolType: String? = null,
                   initSettingCompleted: Boolean? = null,
                   majorType: String? = null,
                   schoolLocation: String? = null,
                   schoolName: String? = null,
                   initMoGrade: Int? = null,
                   initStudied: String? = null,
                   recommendLevel: Int? = null,
                   recommendChapter: Int? = null,
                   recommendStudyPoint: Int? = null,
                   grade: Int? = null,
                   agreeAppPush: Boolean? = null,
                   agreeMarketing: Boolean? = null
    ): Call<Template<Map<String, String>>> {
        val param: Parameter = hashMapOf("studentID" to studentID)

        if (initSettingCompleted != null) param["initSettingCompleted"] = initSettingCompleted

        if (grade != null) param["grade"] = grade

        if (majorType != null) param["majorType"] = majorType

        if (schoolLocation != null) param["schoolLocation"] = schoolLocation

        if (schoolName != null) param["schoolName"] = schoolName

        if (schoolType != null) param["schoolType"] = schoolType

        if (initMoGrade != null) param["initMoGrade"] = initMoGrade

        if (initStudied != null) param["initStudied"] = initStudied

        if (recommendLevel != null) param["recommendLevel"] = recommendLevel

        if (recommendChapter != null) param["recommendChapter"] = recommendChapter

        if (recommendStudyPoint != null) param["recommendStudyPoint"] = recommendStudyPoint

        if (agreeAppPush != null) param["agreeAppPush"] = agreeAppPush

        if (agreeMarketing != null) param["agreeMarketing"] = agreeMarketing


        return API_V1.update2(param)
    }

    fun getMainCuration(context: Context, user: User, successCB: ((curation: MainCuration) -> Unit), failCB: (() -> Unit)) {
        val param: Parameter = Parameter("studentID" to user.studentID)

        API_V1.getMainCuration(param).enqueue(object: Callback<Template<MainCuration>> {
            override fun onFailure(call: Call<Template<MainCuration>>, t: Throwable) {
                failCB()
            }

            override fun onResponse(call: Call<Template<MainCuration>>, response: Response<Template<MainCuration>>) {
                val curation = response.body()?.data
                if(response.isSuccessful && curation != null) {
                    successCB(curation)
                } else {
                    failCB()
                }
            }

        })
    }


    fun authPhone(context: Context, user: User, phone: String, successCB: () -> Unit, failCB: (Int, String) -> Unit)  {
        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "cellPhone" to phone
        )
        API_V1.authPhoneNum(param).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    successCB()
                } else {
                    val errorJson = JSONObject(response.errorBody()?.string())
                    failCB(response.code(), errorJson.getString("error"))
                }
            }
        })
    }

    fun authPhone(context: Context, phone: String, successCB: () -> Unit, failCB: (Int, String) -> Unit) {
        val param: Parameter = Parameter(
                "cellPhone" to phone
        )
        API_V1.authAnonyPhoneNum(param).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    successCB()
                } else {
                    val errorJson = JSONObject(response.errorBody()?.string())
                    failCB(response.code(), errorJson.getString("error"))
                }
            }
        })
    }


    fun authCode(context: Context, user: User, phone: String, code: String, successCB: () -> Unit, failCB: (Int, String) -> Unit) {
        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "cellPhone" to phone,
                "authNumber" to code
        )
        API_V1.authCodeNum(param).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    successCB()
                } else {
                    val errorJson = JSONObject(response.errorBody()?.string())
                    failCB(response.code(), errorJson.getString("error"))
                }
            }
        })
    }

    fun authCode(context: Context, phone: String, code: String, successCB: () -> Unit, failCB: (Int, String) -> Unit) {
        val param: Parameter = Parameter(
                "cellPhone" to phone,
                "authNumber" to code
        )
        API_V1.authAnonyCodeNum(param).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    successCB()
                } else {
                    val errorJson = JSONObject(response.errorBody()?.string())
                    failCB(response.code(), errorJson.getString("error"))
                }
            }
        })
    }

    fun checkPhoneExist(context: Context, phone: String, successCB: () -> Unit, failCB: (Int) -> Unit) {
        val param :Parameter = Parameter(
                "cellPhone" to phone
        )

        API_V1.checkPhoneExist(param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    successCB()
                } else {
                    failCB(response.code())
                }
            }

        })
    }

    fun findPassword(context: Context, email:String, successCB: () -> Unit) {
        val param: Parameter = Parameter(
                "loginID" to email
        )

        API_V1.findPassword(param).enqueue(object: Callback<Template<String>> {
            override fun onFailure(call: Call<Template<String>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<String>>, response: Response<Template<String>>) {
                if(response.isSuccessful) {
                    successCB()
                } else {
                    responseError(context, response, param)
                }
            }
        })

    }
}