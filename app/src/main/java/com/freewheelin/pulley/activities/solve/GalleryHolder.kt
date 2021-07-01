package com.freewheelin.pulley.activities.solve

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemErrorStatus
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.utils.setImageURL
import kotlinx.android.synthetic.main.item_header_gallery.view.*
import kotlinx.android.synthetic.main.item_problem_gallery.view.*

class GalleryHeaderHolder(val view: View):RecyclerView.ViewHolder(view) {
    val chapterMiddleTv = view.chapterMiddleTv
    val pageNameTv = view.pageNameTv

}
class GalleryHolder(val view: View): RecyclerView.ViewHolder(view) {
    lateinit var problem: Problem

    val bridgeView = view.bridgeView
    val triangleView = view.triangleIv
    val selectView = view.selectView
    val bgView = view.bgView
    val resultIv = view.resultIv

    val imageView = view.imageView
    val numberTv = view.numberTv
    val tagView = view.tagIv
    val clearView = view.clearCoverCl
    val coverView = view.coverView
    val statusIv = view.statusIv

    fun setProblem(problem: Problem, selectedProblem: Problem?) {
        this.problem = problem
        numberTv.text = problem.getNumberText()
        bridgeView.visibility = getBridgeViewVisibility()
        tagView.visibility = getTabVisibility()
        clearView.visibility = getClearVisibility()
        coverView.visibility = getBgViewVisibility()
        resultIv.setImageDrawable(getResultDrawable())
        imageView.setImageURL(problem.getThumbnailUrl())
        triangleView.visibility = getTriangleVisibility()
        setErrorStateUI()
        if(problem == selectedProblem)
            select()
        else
            unselect()
    }

    fun select() {
        triangleView.setColorFilter(ContextCompat.getColor(view.context, R.color.purple_6D6DFF))
        selectView.visibility = View.VISIBLE
    }

    fun unselect() {
        triangleView.setColorFilter(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
        selectView.visibility = View.INVISIBLE
    }


    fun getTabVisibility(): Int {
        return if(problem.isScrap)
            View.VISIBLE
        else
            View.GONE
    }

    fun getClearVisibility(): Int {
        return if(problem.isClear)
            View.VISIBLE
        else
            View.GONE
    }

    fun getBridgeViewVisibility(): Int {
        if(problem.isSimilarProblem() && problem.rootProblem != null)
            return View.VISIBLE
        else
            return View.INVISIBLE
    }

    fun getBgViewVisibility(): Int {
        if(!problem.isUserAnswerInput() || problem.problemErrorStatus != ProblemErrorStatus.NONE)
            return View.VISIBLE
        else
            return View.INVISIBLE
    }

    fun setErrorStateUI() {
        when(problem.problemErrorStatus) {
            ProblemErrorStatus.NONE -> {
                statusIv.visibility = View.GONE
                coverView.setBackgroundColor(Color.parseColor("#409f9f9f"))
                imageView.visibility = View.VISIBLE
            }
            ProblemErrorStatus.REPORT -> {
                statusIv.visibility = View.VISIBLE
                statusIv.setImageResource(R.drawable.ic_siren_white)
                coverView.setBackgroundColor(Color.parseColor("#809f9f9f"))
                imageView.visibility = View.VISIBLE
            }
            ProblemErrorStatus.ERROR -> {
                statusIv.visibility = View.VISIBLE
                statusIv.setImageResource(R.drawable.ic_error_white)
                coverView.setBackgroundColor(Color.parseColor("#809f9f9f"))
                imageView.visibility = View.GONE
                tagView.visibility = View.GONE
                clearView.visibility = View.GONE
            }
        }
    }

    fun getTriangleVisibility(): Int {
        if (problem.rootProblem == null)
            return View.GONE
        else
            return View.VISIBLE
    }


    fun getResultDrawable(): Drawable? {
        return when(problem.getResultByScoring()) {
            Result.correct -> ContextCompat.getDrawable(view.context, R.drawable.ic_correct_new)
            Result.incorrect -> ContextCompat.getDrawable(view.context, R.drawable.ic_incorrect_new)
            else -> null
        }
    }
}