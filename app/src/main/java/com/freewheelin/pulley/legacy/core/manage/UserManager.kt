package com.freewheelin.pulley.legacy.core.manage

import android.content.Context
import com.freewheelin.pulley.legacy.assets.*
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UserManager {

    const val ARG_DESSERT_TYPE = "ARG_DESSERT_TYPE"
    const val EVENT_USER_MODIFYING = "EVENT_USER_MODIFYING"
    const val EVENT_USER_UPDATE = "EVENT_USER_UPDATE"
    const val EVENT_SCHOOL_CHANGE = "EVENT_SCHOOL_CHANGE"
    const val FILTER_SESSION_EXPIRED = "FILTER_SESSION_EXPIRED"
    const val SCHOOL_TYPE = "SCHOOL_TYPE"
    const val RE_CONFIGURE_UI = "RE_CONFIGURE_UI"


    fun getProfile(context: Context, user: UserV4, successCB: (mainProfile: MainProfile) -> Unit) {
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


    fun setUserGoalCount(context: Context, user: UserV4, count: Int, successCB: () -> Unit) {
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

}
