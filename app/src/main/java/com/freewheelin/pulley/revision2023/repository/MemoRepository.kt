package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.revision2023.model.StudyMemo
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.model.StudyMemoOS
import com.freewheelin.pulley.revision2023.model.StudyMemoRequest
import com.freewheelin.pulley.revision2023.room.studymemo.StudyMemoDao
import com.freewheelin.pulley.revision2023.room.studymemo.StudyMemoDatabase
import com.freewheelin.pulley.revision2023.service.StudyMemoApi
import kotlinx.coroutines.CoroutineScope
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MemoRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val api = StudyMemoApi.studyMemoService()
    private val dao: StudyMemoDao = StudyMemoDatabase.getDatabase(context).studyMemoDao()

    suspend fun fetchMemo (studentId: String, mainId: Int, memoCase: StudyMemoCase, subId: Int?, latest: Long?): List<StudyMemo> {
        val memos = api.fetchMemos(
            studentId = studentId,
            mainId = mainId,
            subId = subId,
            os = "android",
            memoCase = memoCase.name,
            latest = latest).data
//        upsert(memos)
        return memos
    }
    suspend fun uploadMemo(image: MultipartBody.Part,
                           studentId: RequestBody,
                           mainId: RequestBody,
                           subId: RequestBody,
                           os: RequestBody,
                           memoCase: RequestBody,
                           latest: RequestBody) {
        api.uploadMemoByteArray(
            image = image,
            studentId = studentId,
            mainId = mainId,
            subId = subId,
            os = os,
            memoCase = memoCase,
            updatedAt = latest).data?.let { memo ->
//            upsert(listOf(memo))
        }
    }
    suspend fun uploadMemo(body: StudyMemoRequest): StudyMemo? {
        return api.uploadMemo(body).data

    }
    fun getMemo(memoId: String): StudyMemo? {
        return dao.get(memoId)
    }
    fun getFromParams(studentId: String, mainId: Int, subId: Int, case: StudyMemoCase): StudyMemo? {
        return dao.getFromParams(
            studentId = studentId,
            mainId = mainId,
            subId = subId,
            memoCase = case,
            os = StudyMemoOS.ANDROID)
    }
    fun countMemo (studentId: String, assignId: Int): Int {
        return dao.countMemo(studentId = studentId, assignId = assignId)
    }
    fun getMaxId(): Int {
        return dao.getMaxId()
    }
//    fun getLatestTimestamp (studentId: String): Long? {
//        return dao.getLatestTimestamp(studentId = studentId)
//    }
    suspend fun upsert(memo: StudyMemo) {
        dao.upsertOne(memo)
//        val prevMemo = dao.getFromParams(
//            studentId = studentId,
//            mainId = assignId,
//            subId = problemId,
//            os = StudyMemoOS.ANDROID,
//            memoCase = case
//        )

//        if (prevMemo == null) {
//            println("aspasp 같은 메모가 없다!")
//            dao.upsertOne(memo)
//        } else {
//            println("aspasp 같은 메모가 있다!!!!")
//            dao.delete(prevMemo)
//            dao.upsertOne(memo)
//        }
    }
//    fun upsertOne(memo: StudyMemo) {
//        dao.upsertOne(memo)
//    }
    fun delete(memo: StudyMemo) {
        dao.delete(memo = memo)
    }
}