package com.freewheelin.pulley.legacy.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogDaebakBinding
import com.freewheelin.pulley.databinding.DialogDaebakTitleOnlyBinding
import com.freewheelin.pulley.legacy.activities.SplashActivity
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2023.ui.dialogs.CommonDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.DaebakDialogV2

enum class DialogType {
    default,
    alert
}


class DialogUtils {
    companion object {
        fun showDialog(context: Context,
                       title: String,
                       content: String,
                       leftBtnText: String = "취소",
                       rightBtnText: String = "완료") {
            confirmV2(context,title,content,leftBtnText,rightBtnText)

        }

        fun confirmDialog(context: Context,
                       title: String,
                       content: String,
                       leftBtnText: String = "취소",
                       rightBtnText: String = "확인",
                       leftBtnCB: () -> Unit = {},
                       rightBtnCB: () -> Unit = {}) {
            confirmV2(
                context = context,
                title = title,
                contents = content,
                leftBtnText = leftBtnText,
                rightBtnText = rightBtnText,
                isOneBtn = false,
                cancelCb = leftBtnCB,
                successCb = rightBtnCB)
        }

        fun showNetworkErr(context: Context) {
            val dialog = networkErrDialog(context)
            showCommonDialog(context, dialog, "showNetworkErr")
        }

        fun networkErrDialog(context: Context): CommonDialog {
            val title = "네트워크 연결이 필요합니다."
            val content = "네트워크 연결에 실패했습니다.\n와이파이 설정을 확인해 주세요."

            val dialog = CommonDialog.newInstance(
                title = title,
                contents = content,
                isOneBtn = true,
                successText = "확인"
            )
            return dialog
        }

        fun lockAccountDialog(context: Context, callback: () -> Unit) {
            confirmV2(
                context = context,
                title = "계정 잠금 알림",
                contents = "로그인 정보 10회 불일치로 계정이 잠겼습니다.\n비밀번호를 재설정해주세요.",
                isOneBtn = true,
                rightBtnText = "비밀번호 재설정하기",
                type = CommonDialog.DialogType.Alert,
                successCb = callback
            )
        }

        fun serverErrDialog(context: Context): CommonDialog {
            val dialog = CommonDialog.newInstance(
                title = "데이터를 가져올 수 없습니다",
                contents = "인터넷 연결을 확인하고 다시 시도해주세요.\n문제가 지속되면\n카카오톡(@풀리는수학)으로 문의 바랍니다.",
                isOneBtn = true,
                successText = "확인"
            )
            return dialog
        }

        fun showServerErr(context: Context) {
            if((context as? Activity)?.isFinishing == false) {

                val dialog = CommonDialog.newInstance(
                    title = "데이터를 가져올 수 없습니다",
                    contents = "인터넷 연결을 확인하고 다시 시도해주세요.\n문제가 지속되면\n카카오톡(@풀리는수학)으로 문의 바랍니다.",
                    successText = "확인",
                    isOneBtn = true
                )
                showCommonDialog(context, dialog, "showServerErr")
            }
        }

        fun showCommonDialog(context: Context, dialog: CommonDialog, tag: String) {
            if (context is AppCompatActivity && !context.isFinishing) {
                val fm = context.supportFragmentManager
                if (!fm.isDestroyed) {
                    try {
                        fm.let { dialog.show(it, tag) }
                    } catch (e: IllegalStateException) {
                        fm.beginTransaction().add(dialog, tag).commitAllowingStateLoss()
                    }
                }
            }
        }
        fun showExamSubmitDialog(context: Context, notSolvedProblemCnt: Int, onSubmitClicked:(() -> Unit)) {
            val message = """
                        풀지 않은 문제 : ${notSolvedProblemCnt}개
                        추후 이어풀기가 가능합니다.
                    """.trimIndent()

            val dialog = CommonDialog.newInstance(
                title = "제출하시겠습니까",
                contents = message,
                cancelText = "취소",
                successText = "제출하기"
            )

            dialog.successCallback = onSubmitClicked
            showCommonDialog(context, dialog, "showExamSubmitDialog")
        }

        fun showExamExpiredDialog(context: Context, notSolvedProblemCnt: Int, onSolveClicked: (() -> Unit), onSubmitClicked: (() -> Unit)) {
            val message = """
                    풀지 않은 문제 : ${notSolvedProblemCnt}개
                    [계속 풀기] 선택 시 남은 문제를 계속 풀 수 있고,
                    [결과 보기]를 선택 시 보고서로 이동합니다.
                """.trimIndent()

            val dialog = CommonDialog.newInstance(
                title = "수고하셨습니다",
                contents = message,
                type = CommonDialog.DialogType.Alert,
                cancelText = "계속 풀기",
                successText = "결과 보기"
            )

            dialog.cancelCallback = onSolveClicked
            dialog.successCallback = onSubmitClicked
            dialog.isCancelable = false

            val fm = (context as AppCompatActivity).supportFragmentManager
            fm.let { dialog.show(it, "showExamExpiredDialog")}
        }

        fun showProblemSolveExitDialog(context: Context, notSolvedProblemCnt: Int, onExitClicked: (() -> Unit), cancelCallback: (() -> Unit)) {
            val dialog = CommonDialog.newInstance(
                title = "채점하지 않은 기록은 없어져요",
                contents = "채점하지 않은 문항 : ${notSolvedProblemCnt}개" + "\n[종료하기]를 누르면,\n채점하지 않은 기록은 삭제됩니다.",
                type = CommonDialog.DialogType.Alert,
                cancelText = "취소",
                successText = "종료하기"
            )

            dialog.cancelCallback = cancelCallback
            dialog.successCallback = onExitClicked
            showCommonDialog(context, dialog, "showProblemSolveExitDialog")

        }

        fun needAppUpgradeDialog(activity: Activity) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "업그레이드필요", "업그레이드")
            val dialog = CommonDialog.newInstance(
                title = "앱 업데이트",
                contents = "최신 업데이트가 있습니다.\n앱을 다시 시작 해야 합니다!",
                isOneBtn = true,
                successText = "확인"
            )

            dialog.isCancelable = false
            dialog.successCallback = {
                activity.finishAffinity()
                activity.startActivity(Intent(activity, SplashActivity::class.java))
            }
            showCommonDialog(activity, dialog, "needAppUpgradeDialog")
        }

        fun expiredSessionDialog(activity: Activity, callback:(()->Unit) = {}) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "세션만료", "세션만료")

            val dialog = CommonDialog.newInstance(
                title = "로그아웃",
                contents = "다른 기기에서 로그인이 되거나\n로그인 세션이 만료되어 로그아웃 되었습니다.",
                isOneBtn = true,
                type = CommonDialog.DialogType.Alert,
                successText = "확인"
            )

            dialog.successCallback = {
                activity.finishAffinity()
                activity.startActivity(Intent(activity, SplashActivity::class.java))
                callback.run { this() }
            }
            showCommonDialog(activity, dialog, "expiredSessionDialog")
        }

        fun toLoginDialog(activity:Activity, rightCallback: (() -> Unit), leftCallback:()->Unit) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "마이페이지", "비밀번호변경")
            val dialog = DaebakDialog(activity)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "완료"
            dialog.binding.contentTv.text = "비밀번호가 변경되었습니다."
            dialog.binding.rightBtn.text = "다시 로그인 하기"
            dialog.binding.leftBtn.text = "닫기"
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                rightCallback()
            }
            dialog.binding.leftBtn.setOnClickListener {
                dialog.dismiss()
                leftCallback()
            }
            if(!activity.isFinishing)
                dialog.show()
        }

        fun showReluctanceDialog(context: Context, leftBtnCB: () -> Unit, rightBtnCB: (() -> Unit) = {}) {
            val unicode = 0x270B
            val uniStr = String(Character.toChars(unicode))
            val title = "잠깐만요? ${uniStr}"
            val contents = "가입 없이 바로 풀리수학을 구경해보세요!"

            val dialog = CommonDialog.newInstance(
                title = title,
                contents = contents,
                cancelText = "나가기",
                successText = "시작하기"
            )

            dialog.cancelCallback = leftBtnCB
            dialog.successCallback = rightBtnCB

            showCommonDialog(context, dialog, "showReluctanceDialog")
        }

        fun showMainDontExitDialog(context: Context, leftBtnCB: () -> Unit, rightBtnCB: (() -> Unit) = {}) {
            val unicode = 0x270B
            val uniStr = String(Character.toChars(unicode))
            val title = "잠깐만요? ${uniStr}"

            val contents = "풀리수학만 있으면 수학공부 한번에 끝낼수 있어요! 조금 더 둘러볼까요?"

            val dialog = CommonDialog.newInstance(
                title = title,
                contents = contents,
                cancelText = "나가기",
                successText = "둘러보기"
            )

            dialog.cancelCallback = leftBtnCB
            dialog.successCallback = rightBtnCB

            showCommonDialog(context, dialog, "showMainDontExitDialog")
        }
        fun showTutorialEndDialog(context: Context, leaveCallback: (() -> Unit), stayCallback: (() -> Unit)) {
            val title = "튜토리얼이 아직 남아있어요!"
            val contents = "1분안에 끝나는 풀리수학 사용법,\n정말 유용한 꿀팁들이 있으니 끝까지 함께해요 ♥"

            val dialog = DaebakDialogV2(context, successCallback = leaveCallback, cancelCallback = stayCallback)
            dialog.title = title
            dialog.contents = contents
            dialog.binding.leftBtn.text = "이어보기"
            dialog.binding.rightBtn.text = "종료하기"
//            val dialog = makeDialog(context, title, content, "종료하기", "이어보기")


            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            val flag = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            dialog.window?.decorView?.systemUiVisibility = flag

            dialog.show()

            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        }

        fun confirmLogoutDevice(activity: Activity, deviceName:String, callback:()->Unit) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "다이얼로그", "특정 디바이스 삭제")
            val dialog = DaebakTitleOnlyDialog(activity)
            dialog.type = DialogType.alert
            dialog.binding.titleTv.text = "$deviceName\n기기를 삭제하시겠습니까?"
            dialog.binding.leftBtn.text = "아니요"
            dialog.binding.rightBtn.text = "삭제할게요"
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                callback()
            }
            if(!activity.isFinishing)
                dialog.show()
        }

        fun confirmDeleteAllStudy(activity: Activity, callback:()->Unit) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "다이얼로그", "학습내역 전체삭제")
            val dialog = CommonDialog.newInstance(
                title = "학습내역을\n전체 삭제하시겠습니까?",
                contents = "삭제된 내용은 복구가 불가능합니다.",
                cancelText = "아니요",
                successText = "삭제할게요"
            )
            dialog.isCancelable = false
            dialog.successCallback = callback

            showCommonDialog(activity, dialog, "confirmDeleteAllStudy")
        }

        fun confirmV2(context: Context,
                      title: String,
                      contents: String,
                      leftBtnText: String = "취소",
                      rightBtnText: String = "확인",
                      isOneBtn: Boolean = false,
                      type: CommonDialog.DialogType = CommonDialog.DialogType.Common,
                      isCancelable: Boolean = true,
                      cancelCb: ()-> Unit = {},
                      successCb: () -> Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = title,
                contents = contents,
                isOneBtn = isOneBtn,
                type = type,
                cancelText = leftBtnText,
                successText = rightBtnText
            )
            dialog.isCancelable = isCancelable
            dialog.cancelCallback = cancelCb
            dialog.successCallback = successCb
            showCommonDialog(context, dialog, "confirmV2${title}${contents}")
        }

        fun confirmBuyPulleyBooks(context: Context, bookName:String?, callback:()->Unit = {}) {
            confirmV2(
                context = context,
                title = "문제집 구매 후 풀이가 가능합니다.",
                contents = "${bookName}\n풀리북스를 구매하시겠습니까?",
                leftBtnText = "더 고민해볼래요",
                rightBtnText = "구매하기",
                successCb = callback
            )
        }

        fun v2SubmitDialog (context: Context, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "검토까지 끝났나요?",
                contents = "제출하시면 시험은 종료됩니다.",
                cancelText = "취소",
                successText = "제출하기"
            )
            dialog.successCallback = callback
            dialog.isCancelable = false
            showCommonDialog(context, dialog, "v2SubmitDialog")
        }
        fun v2SubmitUnCompletedDialog (context: Context, remainingCount: Int, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "풀지 않은 문제: ${remainingCount}개",
                contents = "제출하시면 풀지 않은 문제는 모두 오답처리되며 시험이 종료됩니다.",
                cancelText = "취소",
                successText = "제출하기"
            )
            dialog.isCancelable = false
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "v2SubmitUnCompletedDialog")
        }

        fun v2GetOutSolveViewDialog (context: Context, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "화면을 나가도 시험은 진행됩니다.",
                contents = "푼 문제는 시험시간이 끝나거나\n제출할 때까지 채점되지 않습니다.",
                type = CommonDialog.DialogType.Alert,
                cancelText = "계속 응시하기",
                successText = "나가기"
            )
            dialog.isCancelable = false
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "v2GetOutSolveViewDialog")
        }
        fun v2FinishTestDialog (context: Context, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "테스트가 종료되었습니다.",
                contents = "수고하셨습니다 :)\n보고서로 시험결과를 확인해볼까요?",
                isOneBtn = true,
                successText = "보고서 보기"
            )
            dialog.isCancelable = false
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "v2FinishTestDialog")
        }
        fun v2AffiliatedTestStartWarningDialog (context: Context, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "시험을 시작하시겠습니까?",
                contents = "시험이 시작된 후에는 시험을 중단할 수 없습니다.",
                cancelText = "취소",
                successText = "시험 시작하기"
            )
            dialog.isCancelable = false
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "v2AffiliatedTestStartWarningDialog")
        }
        fun v2LoginErrDialog (context: Context, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "로그인에 실패했습니다.",
                contents = "문제가 지속될 경우\n카카오톡(@풀리는수학)으로 문의 바랍니다.",
                isOneBtn = true,
                successText = "확인"
            )
            dialog.isCancelable = true
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "v2LoginErrDialog")
        }
        fun v2NotFoundErrDialog (context: Context, callback:()->Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "잘못된 API요청입니다.",
                contents = "문제가 지속될 경우\n카카오톡(@풀리는수학)으로 문의 바랍니다.",
                isOneBtn = true,
                successText = "확인"
            )
            dialog.isCancelable = true
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "v2NotFoundErrDialog")
        }
        fun purchaseConfirmDialog (context: Context, message: String, cancelCallback: () -> Unit = {}, callback: () -> Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "결제 안내",
                contents = message,
                cancelText = "취소",
                successText = "확인"
            )
            dialog.isCancelable = true
            dialog.cancelCallback = cancelCallback
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "purchaseConfirmDialog")
        }
        fun purchaseAlertDialog (context: Context, message: String, cancelCallback: () -> Unit = {}, callback: () -> Unit = {}) {
            val dialog = CommonDialog.newInstance(
                title = "결제 안내",
                contents = message,
                isOneBtn = true,
                successText = "확인"
            )
            dialog.isCancelable = true
            dialog.cancelCallback = cancelCallback
            dialog.successCallback = callback
            showCommonDialog(context, dialog, "purchaseAlertDialog")
        }
    }
    class DaebakTitleOnlyDialog(context: Context) : Dialog(context) {
        val binding: DialogDaebakTitleOnlyBinding by lazy {
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_daebak_title_only, null, false)
        }
        var type: DialogType = DialogType.default
            set(value) {
                field = value
                when(type) {
                    DialogType.default -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
                    DialogType.alert -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.red_300))
                }
            }

        var title: String?
            get() = binding.titleTv.text.toString()
            set(value) {
                binding.titleTv.text = value
            }

//        var contents: String?
//            get() = binding.contentTv.text.toString()
//            set(value) {
//                binding.contentTv.text = value
//            }

        init {

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(binding.root)


            binding.leftBtn.setOnClickListener { cancel() }
            binding.rightBtn.setOnClickListener { dismiss() }
        }
    }
    class DaebakDialog(context: Context) : Dialog(context) {
        val binding: DialogDaebakBinding by lazy {
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_daebak, null, false)
        }
        var type: DialogType = DialogType.default
            set(value) {
                field = value
                when(type) {
                    DialogType.default -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
                    DialogType.alert -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.red_300))
                }
            }

        var title: String?
            get() = binding.titleTv.text.toString()
            set(value) {
                binding.titleTv.text = value
            }

        var contents: String?
            get() = binding.contentTv.text.toString()
            set(value) {
                binding.contentTv.text = value
            }

        init {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(binding.root)

            binding.leftBtn.setOnClickListener { cancel() }
            binding.rightBtn.setOnClickListener { dismiss() }
        }
    }
}
