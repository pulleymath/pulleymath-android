package com.freewheelin.pulley.revision2023.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem

data class LCPatternMapWrapper(
    val data: List<LCPatternMap>,
    val error: String?,
    val message: String?,
    val current_time: String?
)
@Entity(tableName = "lc_pattern_map_table")
data class LCPatternMap (
    @PrimaryKey(autoGenerate = false) val patternId: Int,
    val name: String,
    val imageUrl: String,
    val progress: List<LCPatternMapProgress>,
    val chapterId: Int,
    val studentId: String? = user?.studentID,
): BaseDiffItem {
    override fun getId(): String {
        return "$patternId"
    }

    val isCompleteCard: Boolean
        get() {
            if (progress.isEmpty()) return false
            return progress.all { it.isCorrect != null }
        }
    companion object {
        fun getHeader (chapterId: Int): LCPatternMap {
            // room에 들어갔다가 나올때 patternId 오름차순정렬되기때문에 패턴들의 id값중에 헤더의 id가 가장 작아야한다.
            val dummyPatternId = chapterId - 100000
            return LCPatternMap(dummyPatternId, "", "", listOf(), chapterId)
        }
    }

}

data class LCPatternMapProgress (
    val patternQuiz: Int,
    val isCorrect: Boolean?,
    val isFirstTry: Boolean?,
)