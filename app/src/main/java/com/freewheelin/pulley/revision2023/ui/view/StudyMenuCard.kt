package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.LifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewStudyMenuCardBinding
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.activity.PdfListActivity
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.ui.activity.MockListActivity
import com.freewheelin.pulley.revision2023.ui.activity.PulleyMathBooksActivity
import com.freewheelin.pulley.revision2023.ui.activity.TestActivity
import com.freewheelin.pulley.revision2023.ui.activity.WorkbookListActivity
import com.freewheelin.pulley.revision2023.ui.activity.WrongNoteActivity
import com.freewheelin.pulley.revision2023.viewmodel.PatternStudyViewModel
import com.pulleymath.android.pdf.utils.onThrottleClick

class StudyMenuCard(context: Context, val getResult: ActivityResultLauncher<Intent>) : CardView(context) {

    companion object {
        fun getMenuList(schoolType: SchoolType): List<Type> {
            return when(schoolType) {
                SchoolType.ELEMENTARY -> listOf(
                    Type.PulleyMath,
                    Type.Workbook,
                    Type.WrongNote,
                    Type.Test,
                    Type.CommercialBook,
                )
                SchoolType.MIDDLE -> listOf(
                    Type.PulleyMath,
                    Type.Workbook,
                    Type.WrongNote,
                    Type.Test,
                    Type.CommercialBook,
                )
                else -> listOf(
                    Type.PulleyMath,
                    Type.Workbook,
                    Type.Mock,
                    Type.WrongNote,
                    Type.Test,
                    Type.CommercialBook,
                )
            }
        }
    }
    enum class Type {
        PulleyMath, Workbook, Mock, WrongNote, Test, CommercialBook
    }

    val binding = ViewStudyMenuCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {

        radius = 10f.toPx()
        elevation = 0f

        binding.iconIv.layoutParams.width = 64.toPx()
        binding.iconIv.layoutParams.height = 64.toPx()
    }


    fun setParams(schoolType: SchoolType) {
        val param = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f), GridLayout.spec(
                GridLayout.UNDEFINED, GridLayout.FILL, 1f),)
        param.width = 0
//        param.height = 132.toPx()
        setMarginByType(param, schoolType)
        layoutParams = param
    }
    private fun setMarginByType (param: GridLayout.LayoutParams, schoolType: SchoolType) {
        val marginOnDevice8And16 = if (context.isMobile) 8.toPx() else 16.toPx()
        val marginOnDevice16And32 = if (context.isMobile) 16.toPx() else 32.toPx()
        when (type) {
            Type.PulleyMath -> {
                param.setMargins(8.toPx(), 0, marginOnDevice8And16, 0)
            }
            Type.Workbook -> {
                param.setMargins(marginOnDevice8And16, 0, marginOnDevice8And16, 0)
            }
            Type.Mock -> {
                param.setMargins(marginOnDevice8And16, 0, 8.toPx(), 0)
            }
            Type.WrongNote -> {
                if (schoolType.isHigh) {
                    param.setMargins(8.toPx(), marginOnDevice16And32, marginOnDevice8And16, 0)
                } else {
                    param.setMargins(marginOnDevice8And16, 0, 8.toPx(), 0)
                }
            }
            Type.Test -> {
                if (!schoolType.isHigh) {
                    param.setMargins(8.toPx(), marginOnDevice16And32, marginOnDevice8And16, 0)
                }
            }
            Type.CommercialBook -> { }
        }
    }
    var type: Type = Type.PulleyMath
        set(value) {
            field = value
            when (value) {
                Type.PulleyMath -> {
                    setComponent(
                        imgRes = R.drawable.ic_study_pencil_calendar,
                        title = if (context.isMobile) "풀리수학\n문제집" else "풀리수학 문제집",
                        showStamp = true,
                        action = {
                            PulleyMathBooksActivity.getIntent(context, false).let {
                                getResult.launch(it)
                            }
                        }
                    )
                }
                Type.Workbook -> {
                    setComponent(
                        imgRes = R.drawable.ic_study_book_check,
                        title = "워크북",
                        showStamp = true,
                        action = {
                            WorkbookListActivity.getIntent(context).let {
                                getResult.launch(it)
                            }
                        }
                    )
                }
                Type.Mock -> {
                    setComponent(
                        imgRes = R.drawable.ic_study_ox_panel,
                        title = "모의고사",
                        showStamp = false,
                        action = {
                            MockListActivity.getIntent(context).let {
                                getResult.launch(it)
                            }
                        }
                    )
                }
                Type.WrongNote -> {
                    setComponent(
                        imgRes = R.drawable.ic_study_calendar_x,
                        title = "오답노트",
                        showStamp = false,
                        action = {
                            WrongNoteActivity.getIntent(context).let {
                                getResult.launch(it)
                            }
                        }
                    )
                }
                Type.Test -> {
                    setComponent(
                        imgRes = R.drawable.ic_study_check_panel,
                        title = "테스트",
                        showStamp = false,
                        action = {
                            TestActivity.getIntent(context).let {
                                getResult.launch(it)
                            }
                        }
                    )
                }
                Type.CommercialBook -> {
                    setComponent(
                        imgRes = R.drawable.ic_study_book_normal,
                        title = "풀리북스",
                        showStamp = true,
                        action = {
                            Intent(context, PdfListActivity::class.java).let {
                                getResult.launch(it)
                            }
                        }
                    )
                }
            }
        }

    private fun setComponent(imgRes: Int, title: String, showStamp: Boolean, action: () -> Unit) {
        binding.iconIv.setImageResource(imgRes)
        binding.titleTv.text = title
        binding.challengeStamp.visibleIf(showStamp)
        this.onThrottleClick {
            action()
        }
    }

    fun setViewModel(vm: PatternStudyViewModel, owner: LifecycleOwner) {
        binding.vm = vm
        binding.lifecycleOwner = owner
        vm.apply {
            binding.showStamp = when (type) {
                Type.PulleyMath -> showPulleyMathChallengeStamp
                Type.Workbook -> showWorkbooksChallengeStamp
                Type.CommercialBook -> showCommercialBooksChallengeStamp
                else -> null
            }
        }
    }

}