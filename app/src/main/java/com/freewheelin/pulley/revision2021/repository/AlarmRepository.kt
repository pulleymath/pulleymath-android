package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.*

class AlarmRepository {
    private val alarmService : AlarmService by lazy { AlarmApi.alarmService() }

    fun fetchMessages() = alarmService.fetchAlarmMessages()
    fun readMessage(messageID: Int) = alarmService.readAlarmMessage(messageID)
    fun readAllMessages() = alarmService.readAllAlarmMessages()

}