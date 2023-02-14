package com.freewheelin.pulley.revision2021.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.freewheelin.pulley.revision2021.model.CookingInfoItem
import com.freewheelin.pulley.revision2021.model.LCCookingWrapper
import com.freewheelin.pulley.revision2021.model.QuizFormat
import com.freewheelin.pulley.revision2021.model.request.ScoringReq
import com.freewheelin.pulley.revision2021.repository.remote.LCCookingApi
import com.freewheelin.pulley.revision2021.repository.remote.LCCookingService
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.room.cooking.CookingInfoDao
import com.freewheelin.pulley.revision2023.room.cooking.CookingInfoDatabase
import com.freewheelin.pulley.revision2023.room.cookinginfoitem.CookingInfoItemDao
import com.freewheelin.pulley.revision2023.room.cookinginfoitem.CookingInfoItemDatabase
import io.channel.plugin.android.extension.orElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class LCCookingRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val cookingService: LCCookingService by lazy { LCCookingApi.lcCookingService() }
    private val dao: CookingInfoDao = CookingInfoDatabase.getDatabase(context, applicationScope).cookingInfoDao()
    private val infoItemDao: CookingInfoItemDao = CookingInfoItemDatabase.getDatabase(context, applicationScope).cookingInfoItemDao()

    fun fetchCookingGroceries(conceptCookingId: Int, studentId: String) = cookingService.fetchCookingGroceries(conceptCookingId, studentId)
    fun useHint(exerciseQuizId: Int, studentId: String) = cookingService.useHint(exerciseQuizId, studentId)
    fun scoringCookingQuiz(exerciseQuizId: Int, studentId: String, userAnswer: ScoringReq) = cookingService.scoringCookingQuiz(exerciseQuizId, studentId, userAnswer)

    private val _cookingInfo = MutableLiveData<CookingInfo>()
    val cookingInfo : LiveData<CookingInfo> = _cookingInfo

    fun flowAllCookingInfoItem(conceptCookingId: Int): Flow<List<CookingInfoItem>> {
        return infoItemDao.getAllCookingInfoItem(conceptCookingId)
    }
    suspend fun fetchCookingInfoItems(conceptCookingId: Int): Pair<List<CookingInfoItem>, String> {
        val res = cookingService.fetchCookingInfo(conceptCookingId)
            .data.let {
                _cookingInfo.postValue(it)
                val video = listOf(CookingInfoItem.getVideoItem(it))
                val footer = listOf(CookingInfoItem.getFooter(it))
                val exerciseList = listOf(CookingInfoItem.getExercise(it)).map { item ->
                    item.exerciseList?.forEach { exec ->
                        exec.exerciseQuizzes?.forEach { quiz ->
                            val isSolved = quiz.userAnswer != null
                            val isCorrectAnswer = quiz.userAnswer == quiz.answer
                            quiz.afterTryAnswered.set(isSolved)
                            quiz.isAnswerEntered.set(isSolved)
                            quiz.isCorrectAnswer.set(isCorrectAnswer)
                            if (quiz.format == QuizFormat.Single) {
                                quiz.userAnswer?.toInt()?.let { position ->
                                    val selectedImageUrl = quiz.sortedAnswerOptions[position - 1].imageUrl
                                    quiz.selectedQuizAnswerImageUrl.set(selectedImageUrl)
                                }
                            }
                        }
                    }
                    item
                }

                Pair((video + exerciseList + footer).sortedBy { it.order }, it.imageUrl)
            }
        return res
    }

    suspend fun compareInfo(id: String): Boolean {
        return dao.compareInfo(id) > 0
    }

    suspend fun insertInfo(info: CookingInfo) {
        dao.insert(info)
    }
    suspend fun insertInfoItem(item: CookingInfoItem) {
        infoItemDao.insert(item)
    }

    suspend fun upsertAllInfoItem(items: List<CookingInfoItem>) {
        infoItemDao.upsertAll(items)
    }

    suspend fun delete(info: CookingInfo) {
        dao.delete(info)
    }

}