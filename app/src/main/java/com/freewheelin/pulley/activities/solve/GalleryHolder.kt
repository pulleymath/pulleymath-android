package com.freewheelin.pulley.activities.solve

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemHeaderGalleryBinding
import com.freewheelin.pulley.databinding.ItemProblemGalleryBinding
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemErrorStatus
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.utils.setImageURL

class GalleryHeaderHolder(val headerBinding: ItemHeaderGalleryBinding):RecyclerView.ViewHolder(headerBinding.root) {
    val chapterMiddleTv = headerBinding.chapterMiddleTv
    val pageNameTv = headerBinding.pageNameTv

}
class GalleryHolder(val itemBinding: ItemProblemGalleryBinding): RecyclerView.ViewHolder(itemBinding.root) {
    lateinit var problem: Problem

    val bridgeView = itemBinding.bridgeView
    val triangleView = itemBinding.triangleIv
    val selectView = itemBinding.selectView
    val bgView = itemBinding.bgView
    val resultIv = itemBinding.resultIv

    val imageView = itemBinding.imageView
    val numberTv = itemBinding.numberTv
    val tagView = itemBinding.tagIv
    val clearView = itemBinding.clearCoverCl
    val coverView = itemBinding.coverView
    val statusIv = itemBinding.statusIv

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
        triangleView.setColorFilter(ContextCompat.getColor(itemBinding.root.context, R.color.purple_6D6DFF))
        selectView.visibility = View.VISIBLE
    }

    fun unselect() {
        triangleView.setColorFilter(ContextCompat.getColor(itemBinding.root.context, R.color.grey_c0c0c0))
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
            Result.correct -> ContextCompat.getDrawable(itemBinding.root.context, R.drawable.ic_correct_new)
            Result.incorrect -> ContextCompat.getDrawable(itemBinding.root.context, R.drawable.ic_incorrect_new)
            else -> null
        }
    }
}