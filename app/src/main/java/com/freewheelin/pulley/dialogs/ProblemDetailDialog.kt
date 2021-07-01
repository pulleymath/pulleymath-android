package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
//import com.facebook.drawee.backends.pipeline.Fresco
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.utils.*
import kotlinx.android.synthetic.main.dialog_problem_detail.*

//class ProblemDetailDialog: Dialog {
//    val problem: Problem
//    private val dialogWidth = DisplayUtils.getScreenWidth(context) - 64.toPx()
//    private val dialogHeight = DisplayUtils.getScrenHeight(context) - 88.toPx()
//
//    constructor(context: Context, problem: Problem): super(context) {
//        this.problem = problem
//        configureUI()
//    }
//
//
//    init {
//        setContentView(R.layout.dialog_problem_detail)
//        window?.setLayout(dialogWidth, dialogHeight)
//        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        xBtn.setOnClickListener {
//            dismiss()
//        }
//
//        problemSdv.maxWidth = (dialogWidth * 0.276).toInt()
//        solutionSdv.maxWidth = dialogWidth - (problemSdv.maxWidth) - 72.toPx()
//    }
//
//    private fun configureUI() {
////        val problemController = Fresco.newDraweeControllerBuilder()
////                .setControllerListener(ProblemImageController(problemSdv))
////                .setUri(problem.getProblemUrl()).build()
////        problemSdv.controller = problemController
////
////        solutionSdv.setSolutionImage(problem.getSolutionUrl())
//    }
//}