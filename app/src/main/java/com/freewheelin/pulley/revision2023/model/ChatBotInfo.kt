package com.freewheelin.pulley.revision2023.model

import com.google.gson.Gson

enum class ChatBotInitViewType {
    NORMAL, SOLVE_PROBLEM, CONCEPT_LEARNING;
}

data class ChatBotInfo(
    val viewType: ChatBotInitViewType,
    val images: List<ChatBotInfoImage>?
) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

enum class ChatBotInfoImageType {
    SOLVE_PROBLEM, CL_CONCEPT, CL_EXERCISE;
}


data class ChatBotInfoImage(
    val type: ChatBotInfoImageType,
    val seq: Int,
    val urls: List<String>
) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

}