package com.freewheelin.pulley.revision2023.model

import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import java.io.Serializable

data class V2LogUser(
    val studentID: String,
    val eventName: PulleyEvent,
    val itemCategory: String?,
    val itemName: String?,
    val itemValue: String?,
    val itemNote: String?,
    val deviceModel: String,
    val versionSdk: String,
    val versionCode: String,
)

data class V2LogUserResponseWrapper(
    val data: V2LogUserResponse,
    val error: String?,
    val message: String?,
    val current_time: String?
): Serializable

data class V2LogUserResponse(
    val isChallengeCourse: Boolean,
    val challengeStatus: List<Challenge>,
)