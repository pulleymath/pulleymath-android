package com.freewheelin.pulley.revision2021.ui.viewholder

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ItemSmallChapterBinding
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*

class ConceptCourseSmallChapterViewHolder(private val binding: ItemSmallChapterBinding, private val viewModel: ConceptCourseViewModel, val getResult: ActivityResultLauncher<Intent>): RecyclerView.ViewHolder(binding.root) {
    val context: Context = binding.root.context
    fun bind(item: StudyChapter) {
        binding.apply {
            this.item = item
            isNextItemExist = item.hasNextItem
            lifecycleOwner = binding.root.findViewTreeLifecycleOwner()

            smallChapterTitleTv.text = item.name.trim()

            setFirstItemMarginStart(rootCl, item.isFirstSmallItem)
            setFirstItemMarginStart(challengeStampIv, item.isFirstSmallItem)
            setLastItemMarginEnd(rootCl, item.isLastSmallItem)
            setLastItemMarginEnd(challengeStampIv, item.isLastSmallItem)
            setExerciseTvTextColor(item)
            setViewMarginEnd(backgroundExerciseProgressBarIv, item)
            setViewMarginEnd(backgroundPatternProgressBarIv, item)
            setExerciseProgressBarIv(item)
            setPatternCorrectProgressBarIv(item)
            setExerciseSolveTv(item)
            setExerciseTotalTv(item)
            setPatternTv(item)
            setPatternWrongProgressBarIv(item)
            setBetweenWhiteBar(item)
            setPatternSolveTv(item)
            setPatternTotalTv(item)
            setLastStudyDateTv(item)
            setLastStudyDateIv(item)
            setDoneStampIv(item)
            setRightArrowIv(item)
            setSmallChapterRootCl(item)
            setChallengeStampIv(item)
            setLockIv(item)
        }
    }
    private fun setSmallChapterRootCl(item: StudyChapter) {
        binding.smallChapterRootCl.apply {
            setOnTouchListener(BoongthEffect())
            setOnClickListener {
                if (user?.serviceType?.isGuestUser == true) {
                    LogUtils.logEvent(context, user!!, PulleyEvent.LEARNING_CARD_CLICK, "개념", item.parentName, item.name)
                    viewModel.guestException()
                    return@setOnClickListener
                }
                if (item.isLocked) {
                    LogUtils.logEvent(itemView.context, user, PulleyEvent.BUTTON_CLICK, "개념카드", "결제유도", "잠금버튼")
                    val dialog = PurchaseGuideDialog()
                    val fm = (context as AppCompatActivity).supportFragmentManager
                    fm.let { dialog.show(it, "purchaseGuideDialog")}
                    return@setOnClickListener
                }
                if (item.sequence == StudyChapter.TUTORIAL_SEQUENCE) {
                    getResult.launch(LCTutorialActivity.getIntentAddFlags(context))
                    return@setOnClickListener
                }
                val isUnderBasicC = MyApplication.user?.serviceType?.isUnderBasicC() == true
                val isStartChallengeRewardCard = item.isStartChallengeRewardCard()
                if (isUnderBasicC && !isStartChallengeRewardCard) {
                    DialogUtils.confirmDialog(context,
                        "이 학습지로 선택하실 건가요?",
                        "학습지 1개만 무료로 이용 가능해요 :)",
                        "아니오",
                        "네, 선택할래요",
                        rightBtnCB = {
                            createLearningCourse(item)
                        })
                    return@setOnClickListener
                }

                createLearningCourse(item)
            }
        }
    }
    private fun createLearningCourse(item: StudyChapter) {
        viewModel.createLearningCourseOnStudentId(item.id) {
            val chapterId = item.id
            val name = item.name
            val subjectId = viewModel.selectedSubjectId.value ?: LCSubject.SubjectIndicator.MathSang.rawValue
            getResult.launch(LearningCourseActivity.getIntent(context, subjectId, chapterId, name))
        }
    }
    private fun setLockIv(item: StudyChapter) {
        binding.lockIv.apply {
            visibility = if (item.isLocked) View.VISIBLE else View.GONE
        }
    }
    private fun setChallengeStampIv(item: StudyChapter) {
        val isConceptChallengeInProgress = viewModel.joinedChallengeList.value?.find {
            it.startChallenge?.isConceptCourseInProgress == true
        } != null
        val isStartCategory = item.category == "START"
        binding.challengeStampIv.apply {
            visibility = if (isConceptChallengeInProgress && isStartCategory) View.VISIBLE else View.GONE
        }
    }
    private fun setRightArrowIv(item: StudyChapter) {
        binding.rightArrowIv.apply {
            visibility = if (item.hasNextItem) View.VISIBLE else View.GONE
        }
    }
    private fun setDoneStampIv(item: StudyChapter) {
        binding.stampIv.apply {
            visibility = if(item.isChapterDone) View.VISIBLE else View.GONE
        }
    }
    private fun setLastStudyDateIv(item: StudyChapter) {
        binding.lastStudyDateIv.apply {
            val color = if (item.isChapterDone) R.color.purple_gray_300_opa_50 else R.color.purple_gray_300
            setColorFilter(ContextCompat.getColor(context, color))
        }
    }
    private fun setLastStudyDateTv(item: StudyChapter) {
        binding.lastStudyDateTv.apply {
            text = item.lastStudiedFormatting
            val color = when {
                item.isChapterDone -> R.color.purple_gray_300_opa_50
                else -> R.color.purple_gray_300
            }
            setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setPatternTotalTv(item: StudyChapter) {
        binding.patternTotalTv.apply {
            text = item.patternTotalText
            val color = when {
                item.progress?.pattern?.userSolvedCount == 0 -> R.color.purple_gray_300
                item.progress?.pattern?.isDone == true -> R.color.purple_250_opa_50
                else -> R.color.purple_gray_300
            }
            setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setPatternSolveTv(item: StudyChapter) {
        binding.patternSolvedTv.apply {
            text = item.patternSolvedText
            val color = when {
                item.progress?.pattern?.userSolvedCount == 0 -> R.color.purple_gray_300
                item.progress?.pattern?.isDone == true -> R.color.purple_250_opa_50
                else -> R.color.purple_250
            }
            setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setBetweenWhiteBar(item: StudyChapter) {
        binding.whiteBarView.apply {
            visibility = if (item.progress?.pattern?.userWrongCount == 0) View.GONE else View.VISIBLE
        }
    }
    private fun setPatternWrongProgressBarIv(item: StudyChapter) {
        binding.patternWrongProgressBarIv.apply {
            setLayoutWidth(this, item.patternWrongProgressRate)
            visibility = if (item.patternWrongProgressRate == 0.0) View.GONE else View.VISIBLE
        }
    }
    private fun setPatternTv(item: StudyChapter) {
        binding.patternTv.apply {
            val color = when {
                item.progress?.pattern?.userSolvedCount == 0 -> R.color.purple_gray_300
                item.progress?.pattern?.isDone == true -> R.color.purple_250_opa_50
                else -> R.color.purple_250
            }
            setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setExerciseTotalTv(item: StudyChapter) {
        binding.exerciseTotalTv.apply {
            text = item.exerciseTotalText
            val color = when {
                item.progress?.exercise?.userSolvedCount == 0 -> R.color.purple_gray_300
                item.progress?.exercise?.isDone == true -> R.color.purple_200_opa_50
                else -> R.color.purple_gray_300
            }
            setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setExerciseSolveTv(item: StudyChapter) {
        binding.exerciseSolvedTv.apply {
            text = item.exerciseSolvedText
            val color = when {
                item.progress?.exercise?.userSolvedCount == 0 -> R.color.purple_gray_300
                item.progress?.exercise?.isDone == true -> R.color.purple_200_opa_50
                else -> R.color.purple_200
            }
            setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setExerciseProgressBarIv(item: StudyChapter) {
        binding.exerciseProgressBarIv.apply {
            setLayoutWidth(this, item.exerciseProgressRate)
            visibility = if (item.exerciseProgressRate == 0.0) View.GONE else View.VISIBLE
            val src = if (item.progress?.exercise?.isDone == true) R.drawable.bg_progress_purple_200_opa_50 else R.drawable.bg_progress_purple_200
            setImageResource(src)
        }
    }
    private fun setPatternCorrectProgressBarIv(item: StudyChapter) {
        binding.patternCorrectProgressBarIv.apply {
            setLayoutWidth(this, item.patternCorrectProgressRate)
            visibility = if (item.patternCorrectProgressRate == 0.0) View.GONE else View.VISIBLE
            val src = if (item.progress?.pattern?.isDone == true) {
                R.drawable.bg_progress_purple_250_opa_50
            } else if (item.progress?.pattern?.userWrongCount != 0) {
                R.drawable.bg_progress_purple_250_left_corner
            } else {
                R.drawable.bg_progress_purple_250
            }
            setImageResource(src)
        }
    }
    private fun setLayoutWidth(view: View, rate: Double) {
        val layoutParams = view.layoutParams
        layoutParams.width = (rate * 170).toInt().dpToPx()
        view.layoutParams = layoutParams
    }
    private fun setExerciseTvTextColor(item: StudyChapter) {
        binding.apply {
            val color = when {
                item.progress?.exercise?.userSolvedCount == 0 -> R.color.purple_gray_300
                item.progress?.exercise?.isDone == true -> R.color.purple_200_opa_50
                else -> R.color.purple_200
            }
            exerciseTv.setTextColor(ContextCompat.getColor(context, color))
        }
    }
    private fun setViewMarginEnd(view: View, item: StudyChapter) {
        val value = when (item.progressTextLength) {
            3 -> 43
            4 -> 47
            5 -> 51
            6 -> 55
            7 -> 59
            else -> 59
        }
        view.setMarginEnd(value)
    }
    private fun setFirstItemMarginStart(view: View, isFirstItem: Boolean) {
        view.setMarginStart(if (isFirstItem) 48 else 0)
    }
    private fun setLastItemMarginEnd(view: View, isLastItem: Boolean) {
        view.setMarginEnd(if (isLastItem) 48 else 0)
    }
}