package com.freewheelin.pulley.model

import io.realm.RealmObject
import java.util.*

open class SolveLog(
        var problemID: Int = 0,
        var testID: Int = 0,
        var dateTime: Date = Date()
): RealmObject() {
    enum class Result {
        correct,
        incorrect
    }

    var userAnswer: String? = null
    var result: Result
        get() = Result.values().first { it.name == resultField }
        set(value) {
            resultField = value.name
        }

    private var resultField: String = Result.correct.name



}
