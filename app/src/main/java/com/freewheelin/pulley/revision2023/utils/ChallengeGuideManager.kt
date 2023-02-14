package com.freewheelin.pulley.revision2023.utils

import androidx.fragment.app.DialogFragment
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeGuideDialog

class ChallengeGuideManager {
    companion object {
        fun getStartGuideMission1(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "안녕~ 풀리수학에 온 걸 정말 환영해!\n나는 너의 수학 메이트 풀링이라고 해 :)\n미션 1번부터 내 빨간 도장을 따라가 진행해봐!",
                nextText = "미션 01. 개념으로 이동하기",
                exitText = "다른 미션 둘러보기",
                highlightText = "빨간 도장",
                showBottomButtons = true,
                pullingIvSrc = ChallengeGuideDialog.PullingImage.StampNormal,
                nextEvent = nextEvent,
                exitEvent = exitEvent
            )
        }
        fun getFinishGuideFromMission1(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "와~ 정말 잘했어!\n챌린지 끝나고 개념학습지 하나를 골라서 풀어봐~",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.Happy,
                nextEvent = nextEvent,
                dismissEvent = nextEvent
            )
        }

        fun getStartGuideMission2(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "챌린지 풀리수학 문제집을 선택해서\n문제를 풀고, 유사문제를 만들어봐!",
            )
        }
        fun getFinishGuideFromMission2(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "와~ 정말 잘했어!\n챌린지 끝나고 유형 학습지 하나를 골라서 풀어봐~",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.Happy,
                nextEvent = nextEvent,
            )
        }
    }
}