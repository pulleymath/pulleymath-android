package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.repository.remote.LCPatternMapApi
import com.freewheelin.pulley.revision2021.repository.remote.LCPatternMapService

class LCPatternMapRepository {

    private val patternMapService: LCPatternMapService by lazy { LCPatternMapApi.lcPatternMapService() }

    fun fetchPatternMapInfo(chapterId: Int, studentId: String) = patternMapService.fetchPatternMapInfo(chapterId, studentId)
}