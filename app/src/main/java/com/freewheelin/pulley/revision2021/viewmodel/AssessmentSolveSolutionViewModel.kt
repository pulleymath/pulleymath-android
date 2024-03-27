package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AssessmentSolveSolutionViewModel private constructor(): BaseViewModel(), LifecycleObserver {

    companion object {
        val instance: AssessmentSolveSolutionViewModel by lazy { AssessmentSolveSolutionViewModel() }
    }
    val assessmentRepository: AssessmentRepository by lazy { AssessmentRepository.instance }

    val currentProblem by lazy { assessmentRepository.currentProblem }
    val showEmptyView by lazy { MutableLiveData(false) }

    // 전체 비디오자료 따로 들고있어야함
    val totalVideoHeaderAndItemList by lazy { ArrayList<AssessmentSolution>() }
    val totalPdfList by lazy { ArrayList<AssessmentSolution>() }
    val filteredSolutionList by lazy { MutableLiveData<List<AssessmentSolution>>() }

    val solutionOrgList = mutableListOf<AssessmentSolution>()

    @SuppressLint("CheckResult")
    fun fetchMedia(problemId: Int?, cb: ((Boolean)-> Unit)) {
        val problemId = problemId ?: return
        filteredSolutionList.postValue(listOf())
        expendedVideoGroupNoList = mutableListOf()
        assessmentRepository.fetchMedia(problemId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "group list=>${res.data}")

                res.data.let { it ->
                    totalPdfList.clear()
                    totalPdfList.addAll(
                        it.filter { solution -> solution.type == "pdf" }
                            .map { solution ->
                                solution.itemType = AssessmentSolution.ItemType.pdfItem
                                solution.listOrder = "A${solution.seq}"
                                solution
                            }
                    )

                    val pdfTextHeaderList = totalPdfList.let {
                        if (it.isEmpty()) listOf()
                        else {
                            val pdfHeader = AssessmentSolution(AssessmentSolution.ItemType.textHeader, "강의 자료", 10000)
                            pdfHeader.listOrder = "A"
                            listOf(pdfHeader)
                        }
                    }

                    val videoHeaderList1 = it.filter { solution -> solution.type == "video" }
                                            .groupBy { it.group_no }
                                            .map {
                                                val videoHeader = AssessmentSolution(AssessmentSolution.ItemType.videoGroupHeader, it.value[0])
                                                videoHeader.listOrder = "C${videoHeader.group_no}"
                                                videoHeader
                                            }

                    var mutableVideoHeaderAndItemList = listOf<AssessmentSolution>()

                    videoHeaderList1.forEach { header ->
                        val videoItemList = it.filter { it.group_no == header.group_no }
                                              .map {
                                                  it.parentId = header.id
                                                  val paddedSeq = "${it.seq}".padStart(3, '0')
                                                  it.listOrder = "C${it.group_no}${paddedSeq}"
                                                  it
                                              }

                        val videoTotalPlayTime = videoItemList.map { it.length }.reduce { total, length -> total + length }
                        val videoTotalPlayTimeString = makeVideoTotalPlayTimeString(videoTotalPlayTime)
                        header.videoGroupDurationString = videoTotalPlayTimeString

                        val lastSeq = videoItemList.last().seq + 1
                        val paddedSeq = "$lastSeq".padStart(3, '0')

                        val footer = AssessmentSolution(AssessmentSolution.ItemType.videoFooter, header)
                        footer.listOrder = "C${footer.group_no}$paddedSeq"

                        val footerList = listOf(footer)
                        mutableVideoHeaderAndItemList = mutableVideoHeaderAndItemList + listOf(header) + videoItemList + footerList
                    }

                    totalVideoHeaderAndItemList.clear()
                    totalVideoHeaderAndItemList.addAll(mutableVideoHeaderAndItemList)

                    val videoTextHeaderList = totalVideoHeaderAndItemList.let {
                        if (it.isEmpty()) listOf()
                        else {
                            val videoTextHeader = AssessmentSolution(AssessmentSolution.ItemType.videoTextHeader, "개념 영상", 10001)
                            videoTextHeader.listOrder = "B"
                            listOf(videoTextHeader)
                        }
                    }


                    val allList = pdfTextHeaderList + totalPdfList + videoTextHeaderList + totalVideoHeaderAndItemList
                    allList.sortedBy { it.listOrder }

                    solutionOrgList.clear()
                    solutionOrgList.addAll(allList)

                    cb(allList.isEmpty())

                    solutionFilter()

                }
            }, { error ->
                Log.e(javaClass.simpleName, "group error=${error.localizedMessage}")
            })
    }

    private var resultList = mutableListOf<AssessmentSolution>()

    var expendedVideoGroupNoList = mutableListOf<Int>()

    fun solutionFilter (selectedVideoHeaderId: Int = -9999, selectedGroupNo: Int = -9999) {
//        println("tpehf. solutionOrgList size : ${solutionOrgList.size}")

        if (expendedVideoGroupNoList.contains(selectedGroupNo)) {
            expendedVideoGroupNoList.remove(selectedGroupNo)
        } else {
            expendedVideoGroupNoList.add(selectedGroupNo)
        }

        val result = solutionOrgList.filter {
            expendedVideoGroupNoList.contains(it.group_no) || it.isNotVideoItemAndFooter()
        }

        resultList.clear()
        resultList.addAll(result)
        filteredSolutionList.postValue(resultList)
    }

    private fun makeVideoTotalPlayTimeString(totalPlayTime: Int): String {
        val hour = (totalPlayTime / 3600)
        val hourToSec = hour * 3600
        val min = ((totalPlayTime - hourToSec) / 60)
        val minToSec = min * 60
        val sec = ((totalPlayTime - hourToSec - minToSec) % 60)
        return "${addTimeStringPadding(hour)}:${addTimeStringPadding(min)}:${addTimeStringPadding(sec)}"
    }
    private fun addTimeStringPadding(time: Int): String {
        if (time < 10) {
            return "0$time"
        }
        return "$time"
    }

    @SuppressLint("CheckResult")
    fun makeMediaLog(item: AssessmentSolution) {
        val problemId = currentProblem.value?.id ?: return
        val studentId = user?.studentID ?: return

        val mediaLog = AssessmentMediaLog(problemId, item.id, item.media_file_id, studentId)

        assessmentRepository.makeMediaLog(mediaLog)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "makeMediaLog =>${res.data}")

                res.data.let { it ->
                    println("solution vidwmodel response : $it")

                }
            }, { error ->
                Log.e(javaClass.simpleName, "makeMediaLog error=${error.localizedMessage}")
            })
    }
}