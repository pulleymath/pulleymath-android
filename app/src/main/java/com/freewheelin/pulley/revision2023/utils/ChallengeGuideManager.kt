package com.freewheelin.pulley.revision2023.utils

import androidx.fragment.app.DialogFragment
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeGuideDialog

class ChallengeGuideManager {
    companion object {
        fun getStartGuideMission1(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "안녕~ 나는 너의 수학 메이트 풀링이라고 해 :)\n풀리수학을 체험하는 스타트 챌린지를 준비했어!\n먼저 개념 학습부터 살펴볼까? 빨간 도장을 따라가봐!",
                nextText = "미션 01. 개념으로 이동하기",
                highlightText = "빨간 도장",
                showBottomButtons = true,
                pullingIvSrc = ChallengeGuideDialog.PullingImage.StampNormal,
                canDismissOutSide = false,
                nextEvent = nextEvent,
                exitEvent = exitEvent
            )
        }
        fun getFinishGuideFromMission1(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "와~ 정말 잘했어!\n개념 학습지 하나를 선물로 줄게!\n챌린지 끝나고 원하는 소단원을 골라서 풀어봐~",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.Happy,
//                nextEvent = nextEvent,
                dismissEvent = nextEvent
            )
        }

        fun getStartGuideMission2(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "스타트 챌린지 문제집을 선택해서\n문제를 풀고, 유사문제를 만들어봐!",
                highlightText = "스타트 챌린지",
                nextEvent = nextEvent
            )
        }
        fun getFinishGuideFromMission2(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "와~ 정말 잘했어!\n풀리수학 문제집 하나를 선물로 줄게!\n챌린지 끝나고 원하는 문제집을 골라서 풀어봐~",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.Happy,
//                nextEvent = nextEvent,
                dismissEvent = nextEvent
            )
        }

        fun getStartGuideMission3(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "원하는 EBS 문제집 하나를 열어봐 :)",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.RightHandUp,
//                highlightText = "※ 처음 선택한 문제집으로 미션 진행 시 인정됨",
//                nextEvent = nextEvent,
                dismissEvent = nextEvent

            )
        }
        fun getStartGuideMission4(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "오른쪽 워크북 만들기 버튼을 클릭해\n문제집에서 원하는 부분만 골라 워크북을 만들어봐!",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.LeftHandUp,
//                nextEvent = nextEvent,
                dismissEvent = nextEvent

            )
        }
        fun getFinishGuideFromAllMission(nextEvent: () -> Unit = {}, exitEvent: () -> Unit = {}): DialogFragment {
            return ChallengeGuideDialog(
                guideText = "와, 정말 대단해! 모든 미션을 클리어 했어 :)\n지금 바로 쿠폰을 받아봐!",
                pullingIvSrc = ChallengeGuideDialog.PullingImage.Happy,
//                nextEvent = nextEvent,
                dismissEvent = nextEvent,
                showSprinkle = true
            )
        }
    }
}