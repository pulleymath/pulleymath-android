package com.freewheelin.pulley.revision2023.model

import java.io.Serializable

data class SignInAppToken(
    val token: String,
    val isValidPhone: Boolean
): Serializable