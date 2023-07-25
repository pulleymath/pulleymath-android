package com.freewheelin.pulley.revision2023.model.request

data class ChangeEmailRequest(
    val auth: String,
    val email: String
) {
}