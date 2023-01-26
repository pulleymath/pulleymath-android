package com.freewheelin.pulley.core.manage

import android.content.Context
import com.freewheelin.pulley.assets.*
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.API_V3
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.responseError
import com.freewheelin.pulley.utils.responseFailed
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UserManager {

    const val ARG_DESSERT_TYPE = "ARG_DESSERT_TYPE"
    const val EVENT_USER_MODIFYING = "EVENT_USER_MODIFYING"

    fun setInitStudy(context: Context, user: User, units: Collection<BigUnitV3>, successCB: () -> Unit, faildCB: (() -> Unit)? = null) {
        val param: Parameter = Parameter(
            "initStudied" to units.map { it.id }
        )
        API_V3.changeCommonSubject(user.studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(faildCB != null) {
                    faildCB()
                }
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200) {
                    user.setStudiedUnit(units)
                    successCB()
                } else {
                    if(faildCB != null)
                        faildCB()
                    responseError(context, response, param)
                }
            }
        })
    }

    fun setRecentExclude(context: Context, user: User, units: Collection<BigUnitV3>, successCB: () -> Unit, faildCB: (() -> Unit)? = null) {
        val param: Parameter = Parameter(
            "excludeSubject" to units.map { it.id }
        )
        API_V3.setExcludeStudied(user.studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(faildCB != null) {
                    faildCB()
                }
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200) {
                    user.setExcludeUnit(units)
                    successCB()
                } else {
                    if(faildCB != null)
                        faildCB()
                    responseError(context, response, param)
                }
            }
        })
    }

    fun setInitOptional(context: Context, user: User, units: Collection<BigUnitV3>, successCB: () -> Unit, faildCB: (() -> Unit)? = null) {
        val param: Parameter = Parameter(
            "initOptional" to units.map { it.id }
        )
        API_V3.changeOptionalSubject(user.studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(faildCB != null) {
                    faildCB()
                }
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200) {
                    user.setOptionalUnit(units)
                    successCB()
                } else {
                    if(faildCB != null)
                        faildCB()
                    responseError(context, response, param)
                }
            }
        })
    }

    fun addInitOptionalSubject(context: Context, user: User, subjects: Collection<SubjectV3>, successCB: () -> Unit, faildCB: (() -> Unit)? = null) {
        val param: Parameter = Parameter(
                "initOptionalSubject" to subjects.map { it.id } // subjectV3 변경하며 AddOptionalToast 를 사용하지 않게되었다.
        )
        // 원래 빅챕터 보내던건데 변경된 빅챕터 코드를 보내면 된다.
        API_V2.addInitOptionalSubject(user.studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(faildCB != null) {
                    faildCB()
                }
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200) {
                    user.setOptionalUnitBySubjects(subjects)
                    successCB()
                } else {
                    if(faildCB != null)
                        faildCB()
                    responseError(context, response, param)
                }
            }
        })
    }

    fun getProfile(context: Context, user: User, successCB: (mainProfile: MainProfile) -> Unit) {
        API_V2.getProfile(user.studentID).enqueue(object: Callback<MainProfile> {
            override fun onFailure(call: Call<MainProfile>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<MainProfile>, response: Response<MainProfile>) {
                val mainProfile = response.body()
                if(mainProfile != null) {
                    successCB(mainProfile)
                } else {
                    responseError(context, response)
                }
            }
        })
    }


    fun setUserGoalCount(context: Context, user: User, count: Int, successCB: () -> Unit) {
        API_V2.setUserGoalCount(user.studentID, count).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful)
                    successCB()
            }
        })
    }

    fun setUserInitSetting(context: Context, user: User, grade: Grade, major: Major?, rating: Int, commonSubject: Collection<BigUnitV3>, optionalSubject: Collection<BigUnitV3>, cb: (() -> Unit)? = null) {
        // TODO SubjectV3 변경관련 확인해봐야함
        val param: Parameter = Parameter(
                "grade" to grade.value,
                "version" to "v3",
                "initStudied" to commonSubject.map { it.id },
                "initOptional" to optionalSubject.map { it.id }
        )

        if(major != null) {
            param["majorType"] = major.value
        }
        if(rating != null) {
            param["initMoGrade"] = rating
        }

        API_V2.setUserInitInfo(user.studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    user.setStudyInfoVal(grade.value, major?.value?:"", rating, commonSubject.map { it.id }, optionalSubject.map { it.id })
                    user.initSettingCompleted = true
                    user.commit("setUserInitSetting")
                    if(cb != null) cb()
                }
            }
        })
    }

}
