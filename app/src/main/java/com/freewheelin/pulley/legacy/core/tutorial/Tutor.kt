package com.freewheelin.pulley.legacy.core.tutorial

import android.view.View
import com.freewheelin.pulley.legacy.utils.APPreference
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.FocusedDimView
import com.freewheelin.pulley.legacy.views.tooltip.TutorWindow
import com.freewheelin.pulley.legacy.views.balloonWindow.BalloonWindow

class Tutor {
    enum class TooltipType {

        takeNoteScroll,
        addSimilar,
        addSimilarOfStartChallenge,
        changeSimilar,
        additionalStudyInWrongNote,
        additionalStudyInAnalysis,
        mailInUnitStudy,
        mailInMockExam,
        mailInMyStudy,
        recommendPlan,
        analysisMain,
        middleIntroduceOpening;

        fun isNeedToShow(): Boolean {
            val maximumCnt = 1
            return this.pref.get() < maximumCnt
        }

        val position: BalloonWindow.Position
            get() {
                return when (this) {
                    takeNoteScroll -> BalloonWindow.Position.left
                    addSimilar -> BalloonWindow.Position.below
                    addSimilarOfStartChallenge -> BalloonWindow.Position.below
                    changeSimilar -> BalloonWindow.Position.below
                    additionalStudyInWrongNote -> BalloonWindow.Position.right
                    additionalStudyInAnalysis -> BalloonWindow.Position.above
                    mailInUnitStudy -> BalloonWindow.Position.below
                    mailInMyStudy, mailInMockExam -> BalloonWindow.Position.above
                    analysisMain -> BalloonWindow.Position.below
                    recommendPlan -> BalloonWindow.Position.above
                    middleIntroduceOpening -> BalloonWindow.Position.below
                }
            }

        val pref : APPreference<Int>
            get() {
                return when (this) {
                    takeNoteScroll -> Preferences.tooltipShowingCntTakeNoteScroll
                    addSimilar -> Preferences.tooltipShowingCntAddSimilar
                    addSimilarOfStartChallenge -> Preferences.tooltipShowingCntAddSimilarOfStartChallenge
                    changeSimilar -> Preferences.tooltipShowingCntChangeSimilar
                    additionalStudyInWrongNote -> Preferences.tooltipShowingCntAdditionalStudyInWrongNote
                    additionalStudyInAnalysis -> Preferences.tooltipShowingCntAdditionalStudyInAnalysis
                    mailInUnitStudy,
                    mailInMyStudy,
                    mailInMockExam -> Preferences.tooltipShowingCntMail
                    analysisMain -> Preferences.tooltipShowingCntAnalysisMain
                    recommendPlan -> Preferences.tooltipShowingCntRecommendPlan
                    middleIntroduceOpening -> Preferences.tooltipShowingCntMiddleOpening
                }
            }


        fun addShowingCnt() {
            val cnt = pref.get()
            pref.set(cnt + 1)
        }

    }

    companion object {
        fun showToolTipIfNeed(view: View?, tooltipType: TooltipType) {
            if (!tooltipType.isNeedToShow()) return
            if (view == null) return

            tooltipType.addShowingCnt()

            val window = TutorWindow(view.context, view, tooltipType.position)

            if (tooltipType == TooltipType.additionalStudyInAnalysis)
                window.offset = 100

            if(tooltipType == TooltipType.additionalStudyInWrongNote)
                window.margin = -30

            if(tooltipType == TooltipType.mailInMyStudy)
                window.offset = 100

            if(tooltipType == TooltipType.recommendPlan)
                window.offset = 100

            if(tooltipType == TooltipType.middleIntroduceOpening) {
                window.margin = -12
            }

            window.show(tooltipType)
        }
    }
}