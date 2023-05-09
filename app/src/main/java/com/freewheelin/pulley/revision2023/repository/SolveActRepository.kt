package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2023.service.SolveApi
import com.freewheelin.pulley.revision2023.service.SolveService
import kotlinx.coroutines.CoroutineScope

class SolveActRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val solveApi: SolveService by lazy { SolveApi.solveService() }

    suspend fun getDailyTest(type: String): Test {
        return solveApi.getDailyTest(type).data
    }
}