package com.freewheelin.pulley.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.facebook.appevents.AppEventsLogger
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.SplashActivity
import com.freewheelin.pulley.activities.auth.InitTestActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.assets.Grade
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.DialogDaebakBinding
import com.freewheelin.pulley.databinding.DialogDaebakTitleOnlyBinding
import com.freewheelin.pulley.databinding.DialogDaebakV2ConfirmBinding
import com.freewheelin.pulley.dialogs.BannerDialog
import com.freewheelin.pulley.dialogs.MockGuideDialog
import com.freewheelin.pulley.dialogs.MockGuideDialogListener
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.views.WrongManageView
import java.util.*

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
            var dialog = makeDialog(context, title, content, leftBtnText, rightBtnText)

            dialog.show()
        }

        fun confirmDialog(context: Context,
                       title: String,
                       content: String,
                       leftBtnText: String = "취소",
                       rightBtnText: String = "확인",
                       leftBtnCB: (()->Unit)? = null,
                       rightBtnCB: (() -> Unit)? = null) {
            makeDialog(context,
                    title,
                    content,
                    leftBtnText,
                    rightBtnText).apply {
                binding.leftBtn.setOnClickListener {
                    dismiss()
                    leftBtnCB?.let{ it() }
                }
                binding.rightBtn.setOnClickListener {
                    dismiss()
                    rightBtnCB?.let{ it() }
                }
            }.show()
        }

        fun makeDialog(context: Context,
                       title: String,
                       content: String,
                       leftBtnText: String,
                       rightBtnText: String): DaebakDialog {
            var dialog = DaebakDialog(context)
            dialog.binding.titleTv.text = title
            dialog.binding.contentTv.text = content
            dialog.binding.leftBtn.text = leftBtnText
            dialog.binding.rightBtn.text = rightBtnText
            return dialog
        }

        fun showNetworkErr(context: Context) {
            val dialog = networkErrDialog(context)
            if (context is Activity && !context.isFinishing)
                dialog.show()
        }

        fun networkErrDialog(context: Context): Dialog {
            val dialog = DaebakDialog(context)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "네트워크 연결이 필요합니다."
            dialog.binding.contentTv.text = "네트워크 연결에 실패했습니다.\n와이파이 설정을 확인해 주세요."
            dialog.binding.rightBtn.text = "확인"
            dialog.binding.leftBtn.visibility = View.GONE
            return dialog
        }

        fun lockAccountDialog(context: Context, callback: () -> Unit): Dialog {
            val dialog = DaebakDialog(context)
            dialog.type = DialogType.alert
            dialog.binding.titleTv.text = "계정 잠금 알림"
            dialog.binding.contentTv.text = "로그인 정보 10회 불일치로 계정이 잠겼습니다.\n비밀번호를 재설정해주세요."
            dialog.binding.rightBtn.text = "비밀번호 재설정하기"
            dialog.binding.leftBtn.visibility = View.GONE
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                callback()
            }
            return dialog
        }

        fun showIndevelopingErr(context: Context) {
            val dialog = DaebakDialog(context)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "현재 개발 진행중입니다 :)"

            val rand = NumberUtils.rand(0,2)
            if(rand == 0) {
                dialog.binding.contentTv.text = "버튼을 선택해도 바뀌는 것이 없어요!"
                dialog.binding.rightBtn.text = "기대하고 있을게요 +_+"
            } else {
                dialog.binding.contentTv.text = "풀리수학팀은 오늘도 열일 중!"
                dialog.binding.rightBtn.text = "으쌰으쌰"
            }

            dialog.binding.leftBtn.visibility = View.GONE
            dialog.show()
        }

        fun serverErrDialog(context: Context): Dialog {
            val dialog = DaebakDialog(context)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "데이터를 가져올 수 없습니다"
            dialog.binding.contentTv.text = "인터넷 연결을 확인하고 다시 시도해주세요.\n문제가 지속되면\n카카오톡(@풀리는수학)으로 문의 바랍니다."
            dialog.binding.rightBtn.text = "확인"
            dialog.binding.leftBtn.visibility = View.GONE
            return dialog
        }

        fun showServerErr(context: Context) {
            if((context as? Activity)?.isFinishing == false) {
                val dialog = serverErrDialog(context)
                dialog.show()
            }
        }

        fun showExamSubmitDialog(context: Context, notSolvedProblemCnt: Int, onSubmitClicked:(() -> Unit)) {
            val message = """
                        풀지 않은 문제 : ${notSolvedProblemCnt}개
                        추후 이어풀기가 가능합니다.
                    """.trimIndent()

            DialogUtils.makeDialog(context,
                    "제출하시겠습니까?",
                    message,
                    "취소",
                    "제출하기").apply {
                binding.leftBtn.setOnClickListener {
                    dismiss()
                }
                binding.rightBtn.setOnClickListener {
                    dismiss()
                    onSubmitClicked()
                }
            }.show()
        }

        fun showExamExpiredDialog(context: Context, notSolvedProblemCnt: Int, onSolveClicked: (() -> Unit), onSubmitClicked: (() -> Unit)) {
            val message = """
                    풀지 않은 문제 : ${notSolvedProblemCnt}개
                    [계속 풀기] 선택 시 남은 문제를 계속 풀 수 있고,
                    [결과 보기]를 선택 시 보고서로 이동합니다.
                """.trimIndent()

            val dialog = DialogUtils.makeDialog(context,
                    "수고하셨습니다!",
                    message,
                    "계속 풀기",
                    "결과 보기")
            dialog.binding.leftBtn.setOnClickListener {
                dialog.dismiss()
                onSolveClicked()
            }

            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                onSubmitClicked()
            }
            dialog.setCancelable(false)
            dialog.show()
        }

        fun showProblemSolveExitDialog(context: Context, notSolvedProblemCnt: Int, onExitClicked:(() -> Unit), cancelListener: DialogInterface.OnCancelListener ?= null) {
            val dialog = makeDialog(context, "채점하지 않은 기록은 없어져요",
                    "채점하지 않은 문항 : ${notSolvedProblemCnt}개" + "\n[종료하기]를 누르면," +
                            "\n채점하지 않은 기록은 삭제됩니다.", "취소", "종료하기")
            dialog.type = DialogType.alert
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                onExitClicked()
            }
            dialog.setOnCancelListener(cancelListener)
            dialog.show()
        }

        fun showExpiredDialog(context: Context) {
            val dialog = makeDialog(context, "이용 중인 서비스가 없습니다.",
                    "풀리수학을 이용하고 싶다면\n서비스 이용권을 구매해주세요!", "닫기", "구입하러가기")
            dialog.type = DialogType.alert
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                FacebookEvent.log(context, FacebookEvent.SUBSCRIBE_STARTED)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(URL.홈페이지)
                context.startActivity(intent)
            }
            dialog.show()
        }

        fun showFreeStartDialog(context: Context, user: User, cb:() -> Unit) {
            val dialog = BannerDialog(context, "지금부터\n무료체험이 시작됩니다!")
            dialog.binding.rightBtn.text = "START"
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                user.startFreeMembership(context) {
                    dialog.dismiss()
                    cb()
                }
            }
            dialog.show()
        }

        fun showRushForPayDialog(context: Context, clickAction:() -> Unit) {
            val dialog = BannerDialog(context, "한달에 만구천원으로\n풀리수학과 공부해요!")
            dialog.binding.rightBtn.text = "START"
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                clickAction()

            }
            dialog.show()
        }

        fun showNeedInitTestDialog(activity: Activity) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "초기테스트유도", "테스트시작팝업")
            val dialog = DaebakDialog(activity)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "2분 진단 테스트"
            dialog.binding.contentTv.text = "나의 수학 공부 타입과\n딱 맞는 공부법을 확인하세요!"
            dialog.binding.rightBtn.text = "테스트하기"
            dialog.binding.leftBtn.visibility = View.GONE
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                activity.finish()
                val intent = InitTestActivity.getIntent(activity)
                activity.startActivity(intent)
            }
            dialog.show()
        }

        fun needAppUpgradeDialog(activity: Activity) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "업그레이드필요", "업그레이드")
            val dialog = DaebakDialog(activity)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "앱 업그레이드"
            dialog.binding.contentTv.text = "최신 업데이트가 있습니다.\n앱을 다시 시작 해야 합니다!"
            dialog.binding.rightBtn.text = "확인"
            dialog.binding.leftBtn.visibility = View.GONE
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                activity.finishAffinity()
                activity.startActivity(Intent(activity, SplashActivity::class.java))
            }
            if(!activity.isFinishing)
                dialog.show()
        }

        fun expiredSessionDialog(activity: Activity, callback:(()->Unit)?=null) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "세션만료", "세션만료")
            val dialog = DaebakDialog(activity)
            dialog.type = DialogType.alert
            dialog.binding.titleTv.text = "로그아웃"
            dialog.binding.contentTv.text = "다른 기기에서 로그인이 되거나\n로그인 세션이 만료되어 로그아웃 되었습니다."
            dialog.binding.rightBtn.text = "확인"
            dialog.binding.leftBtn.visibility = View.GONE
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                activity.finishAffinity()
                activity.startActivity(Intent(activity, SplashActivity::class.java))
                callback?.run { this() }
            }
            if(!activity.isFinishing)
                dialog.show()
//            Log.d(javaClass.simpleName, "expiredSessionDialog opened!")
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

        // deprecated
//        fun showExpiredDDayDialog(context: Context, dday: Int, memberType: String) {
//            val title = if(dday != 0) {
//                if(memberType == User.TYPE_FREE_ING)
//                    "${dday}일 뒤 풀리수학 체험이 끝나요!"
//                else
//                    "${dday}일 뒤 풀리수학 이용기간이 만료됩니다!"
//            } else {
//                if(memberType == User.TYPE_FREE_ING)
//                    "오늘 풀리수학 체험이 끝나요!"
//                else
//                    "오늘 풀리수학 이용기간이 만료됩니다!"
//            }
//
//            val content = "풀리수학을 더 이용하고 싶다면\n서비스 이용권을 구매해주세요!"
//
//            val dialog = makeDialog(context, title, content, "닫기", "구입하러가기")
//            dialog.rightBtn.setOnClickListener {
//                dialog.dismiss()
//                FacebookEvent.log(context, FacebookEvent.TUTORIAL_FINISHED)
//
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.data = Uri.parse(URL.홈페이지)
//                context.startActivity(intent)
//            }
//            dialog.show()
//        }

        fun showReluctanceDialog(context: Context, leftBtnCB: () -> Unit, rightBtnCB: (() -> Unit)? = null) {
            val title = "그냥 종료하시는 거예요?\uD83D\uDE22"
            val contents = "풀리수학이 준비한 테스트\n" +
                    "한 번 풀어보지 않을래요?\n" +
                    "열심히 준비했는데…"

            val dialog = makeDialog(context, title, contents, "그냥 종료할래요", "좀 더 살펴볼까?")
            dialog.binding.leftBtn.setOnClickListener {
                dialog.dismiss()
                leftBtnCB()
            }
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                if(rightBtnCB != null) rightBtnCB()
            }
            dialog.show()

        }

        fun updateGradeDialog(context: Context, user: User): Dialog {
            val gradeText: String = when {
                user.grade.nextGrade == Grade.High_1 -> "고1이"
                user.grade.nextGrade == Grade.High_2 -> "고2가"
                else -> "고3이"
            }

            val title = "${Date().year()}년도에 ${gradeText} 맞나요? "
            val contents = "${user.fullName}님에게 딱 맞춘\n학습을 제공해드릴게요 :)"
            return makeDialog(context, title, contents, "아니요", "맞아요")
        }

        fun updateGradeNoDialog(context: Context): Dialog {
            val dialog = DaebakDialog(context)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "학습 정보가\n수정되지 않았어요!"
            dialog.binding.contentTv.text = "설정 > 학습정보에서 수정이 가능합니다 :)"
            dialog.binding.rightBtn.text = "확인"
            dialog.binding.leftBtn.visibility = View.GONE
            return dialog
        }

        fun confirmLogoutForAllDevices(activity: Activity, callback:()->Unit) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "기기관리", "모든기기 로그아웃")
            val dialog = DaebakTitleOnlyDialog(activity)
            dialog.type = DialogType.alert
            dialog.binding.titleTv.text = "모든 기기에서\n로그아웃 하시겠습니까?"
            dialog.binding.leftBtn.text = "취소"
            dialog.binding.rightBtn.text = "로그아웃할게요"
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                callback()
            }
            dialog.show()
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
            val dialog = DaebakDialog(activity)
            dialog.type = DialogType.alert
            dialog.binding.titleTv.text = "학습내역을\n전체 삭제하시겠습니까?"
            dialog.binding.contentTv.text = "삭제된 내용은 복구가 불가능합니다."
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

        fun confirmExceedDevice(activity: Activity, callback:()->Unit) {
            LogUtils.logEvent(activity, user!!, PulleyEvent.DIALOG, "다이얼로그", "기기초과:신규기기 등록")
            val dialog = DaebakDialog(activity)
            dialog.type = DialogType.default
            dialog.binding.titleTv.text = "이미 3개의 기기가\n등록되어 있습니다."
            dialog.binding.contentTv.text = "등록된 기기를 삭제하고 해당 기기를 등록하시겠습니까?"
            dialog.binding.leftBtn.text = "아니요"
            dialog.binding.rightBtn.text = "등록할게요"
            dialog.setCancelable(false)
            dialog.binding.rightBtn.setOnClickListener {
                dialog.dismiss()
                callback()
            }
            if(!activity.isFinishing)
                dialog.show()
        }

        fun confirmHasPulleyPlus(context: Context, callback:(()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.setCancelable(false)
            dialog.show(context)
        }

        fun confirmBuyPulleyBooks(context: Context, bookName:String?, callback:(()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "문제집 구매 후 풀이가 가능합니다."
            dialog.binding.contentTv.text = "${bookName}\n출판사 문제집을 구매하시겠습니까?"
            dialog.binding.leftBtn.text = "더 고민해볼래요"
            dialog.binding.rightBtn.text = "구매하기"
            dialog.setCancelable(false)
            dialog.show(context)
        }

        fun v2SubmitDialog (context: Context, callback: (()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "검토까지 끝났나요?"
            dialog.binding.contentTv.text = "제출하시면 시험은 종료됩니다."
            dialog.binding.leftBtn.text = "취소"
            dialog.binding.rightBtn.text = "제출하기"
            dialog.setCancelable(false)
            dialog.show(context)
        }
        fun v2SubmitUnCompletedDialog (context: Context, remainingCount: Int, callback:(()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "풀지 않은 문제: ${remainingCount}개"
            dialog.binding.contentTv.text = "제출하시면 풀지 않은 문제는 모두 오답처리되며 시험이 종료됩니다."
            dialog.binding.leftBtn.text = "취소"
            dialog.binding.rightBtn.text = "제출하기"
            dialog.setCancelable(false)
            dialog.show(context)
        }

        fun v2GetOutSolveViewDialog (context: Context, callback:(()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "화면을 나가도 시험은 진행됩니다."
            dialog.binding.contentTv.text = "푼 문제는 시험시간이 끝나거나\n제출할 때까지 채점되지 않습니다."
            dialog.binding.leftBtn.text = "계속 응시하기"
            dialog.binding.rightBtn.text = "나가기"
            dialog.isConfirmBtnRed = true
            dialog.setCancelable(false)
            dialog.show(context)
        }
        fun v2FinishTestDialog (context: Context, callback:(()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "테스트가 종료되었습니다."
            dialog.binding.contentTv.text = "수고하셨습니다 :)\n보고서로 시험결과를 확인해볼까요?"
            dialog.binding.leftBtn.text = ""
            dialog.binding.rightBtn.text = "보고서 보기"
            dialog.isOneBtn = true
            dialog.setCancelable(false)
            dialog.show(context)
        }
        fun v2AffiliatedTestStartWarningDialog (context: Context, callback:(()->Unit)?) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "시험을 시작하시겠습니까?"
            dialog.binding.contentTv.text = "시험이 시작된 후에는 시험을 중단할 수 없습니다."
            dialog.binding.leftBtn.text = "취소"
            dialog.binding.rightBtn.text = "시험 시작하기"
            dialog.setCancelable(true)
            dialog.show(context)
        }
        fun v2LoginErrDialog (context: Context, callback:(()->Unit)? = {}) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "로그인에 실패했습니다."
            dialog.binding.contentTv.text = "문제가 지속될 경우\n카카오톡(@풀리는수학)으로 문의 바랍니다."
            dialog.binding.leftBtn.text = ""
            dialog.binding.rightBtn.text = "확인"
            dialog.isOneBtn = true
            dialog.setCancelable(true)
            dialog.show(context)
        }
        fun v2NotFoundErrDialog (context: Context, callback:(()->Unit)? = {}) {
            val dialog = DaebakDialogV2Confirm(context, callback)
            dialog.binding.titleTv.text = "잘못된 API요청입니다."
            dialog.binding.contentTv.text = "문제가 지속될 경우\n카카오톡(@풀리는수학)으로 문의 바랍니다."
            dialog.binding.leftBtn.text = ""
            dialog.binding.rightBtn.text = "확인"
            dialog.isOneBtn = true
            dialog.setCancelable(true)
            dialog.show(context)
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
                    DialogType.default -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                    DialogType.alert -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.red_fe7b67))
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
                    DialogType.default -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                    DialogType.alert -> binding.rightBtn.setTextColor(ContextCompat.getColor(context, R.color.red_fe7b67))
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

    class DaebakDialogV2Confirm(context: Context, val callback: (() -> Unit)? = null) : Dialog(context) {
        val binding: DialogDaebakV2ConfirmBinding by lazy {
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_daebak_v2_confirm, null, false)
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
        var isConfirmBtnRed: Boolean = false
            set(value) {
                if (value) {
                    binding.rightBtn.setBackgroundResource(R.drawable.bg_red_fe7b67_round_ripple)
                    binding.leftBtn.setTextColor(context.getColor(R.color.gray_800))

                } else {
                    binding.rightBtn.setBackgroundResource(R.drawable.bg_purple_6d6dff_round)
                    binding.leftBtn.setTextColor(context.getColor(R.color.purple_300))
                }
                field = value
            }

        var isOneBtn: Boolean = false
            set(value) {
                if (value) {
                    binding.leftBtn.visibility = View.GONE
                    val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36F, context.resources.displayMetrics).toInt()
                    val params = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, height, 10f)
                    binding.rightBtn.layoutParams = params
                } else {
                    binding.leftBtn.visibility = View.VISIBLE
                }
                field = value
            }

        init {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(binding.root)
            binding.leftBtn.setOnClickListener { dismiss() }
            binding.rightBtn.setOnClickListener {
                dismiss()
                callback?.run { this() }
            }
        }

        fun show(context: Context) {
            if(context is Activity && !context.isFinishing) super.show()
        }
    }
}

