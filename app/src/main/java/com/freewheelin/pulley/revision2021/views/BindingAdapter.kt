package com.freewheelin.pulley.revision2021.views

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.LCWrongNoteActivity
import com.freewheelin.pulley.revision2021.activity.fragments.AffiliatedSolveSolutionFragment
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCCookingFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCPatternMapFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCPriorConceptFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCWrongNoteMapFragment
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.model.response.AffiliatedSolution
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.ui.adapter.ConceptCourseSmallAdapter
import com.freewheelin.pulley.utils.*

object BindingAdapter {
    @JvmStatic
    @BindingAdapter("bind_pattern_map_table")
    fun bindPatternMapTableRecyclerView(recyclerView: RecyclerView, item: List<LCPatternCard>?) {
        Log.d("bind_pattern_map_table", "list=$item")
        item?.let { contentList ->
            val adapter = recyclerView.adapter as LCPatternMapFragment.PatternCardListAdapter
            adapter.submitList(contentList)
        }
    }

    @JvmStatic
    @BindingAdapter("bind_note_selector")
    fun bindNoteSelectorRecyclerView(recyclerView: RecyclerView, item: List<LCWrongNoteMapCard>?){
        Log.d("bind_note_selector", "list=$item")
        item?.let { cardList ->
            val adapter = recyclerView.adapter as LCWrongNoteActivity.NoteNumberListAdapter
            adapter.submitList(cardList)
        }
    }

    @JvmStatic
    @BindingAdapter("layout_margin_top_dimen")
    fun setLayoutMarginTop(view: View, dimen: Float) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.topMargin = dimen.toInt()
            println("layout_margin_top_dimen, dimen :${dimen.toInt()}")
        }
    }
    @JvmStatic
    @BindingAdapter("layout_margin_start_dimen")
    fun setLayoutMarginBottom(view: View, dimen: Float) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.marginStart = dimen.toInt()
        }
    }

    @JvmStatic
    @BindingAdapter("layout_margin_end_dimen")
    fun setLayoutMarginEnd(view: View, dimen: Float) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.marginEnd = dimen.toInt()
        }
    }

    @JvmStatic
    @BindingAdapter("imageview_tint")
    fun ImageView.setImageTint(@ColorInt color: Int?) {
        color?.let {
            setColorFilter(it)
        }
    }

    @JvmStatic
    @BindingAdapter("imagebtn_tint")
    fun ImageButton.setImageTint(@ColorInt color: Int?) {
        color?.let {
            setColorFilter(it)
        }
    }

    @JvmStatic
    @BindingAdapter("layout_margin_end_dimen_on_text_length")
    fun setLayoutMarginEndOnTextLength(view: View, length: Int) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            when (length) {
                3 -> { this.marginEnd = 43.toPx() }
                4 -> { this.marginEnd = 47.toPx() }
                5 -> { this.marginEnd = 51.toPx() }
                6 -> { this.marginEnd = 55.toPx() }
                7 -> { this.marginEnd = 59.toPx() }
                else -> { this.marginEnd = 59.toPx() }
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bind_img_url")
    fun setImageUrl(v: ImageView, url: String) {
        Glide.with(v.context)
            .load(url)
            .apply(RequestOptions().centerCrop())
            .into(v)
    }

    @JvmStatic
    @BindingAdapter("bind_study_chapter")
    fun bindStudyChapterRecyclerView(recyclerView: RecyclerView, item: List<StudyChapter>?){
        Log.d("bind_study_chapter", "list=$item")
        item?.let { chapterList ->
            val adapter = recyclerView.adapter as? ConceptCourseFragment.ChapterAdapter
            adapter?.submitList(chapterList)
        }
    }

    @JvmStatic
    @BindingAdapter("bind_small_chapter")
    fun bindSmallChapterRv(rv: RecyclerView, item: List<StudyChapter>?) {
        Log.d("bind_small_chapter", "list=$item")
        item?.let { chapterList ->
            val adapter = rv.adapter as? ConceptCourseSmallAdapter
            adapter?.submitList(chapterList)
        }
    }

    @JvmStatic
    @BindingAdapter("progress_layout_width")
    fun setLayoutWidth(view: View, rate: Double) {
        println("progress_layout_width , view: ${view.id} , rate: ${rate}")
        val layoutParams = view.layoutParams
        layoutParams.width = (rate * 170).toInt().dpToPx()
        view.layoutParams = layoutParams
    }

    @JvmStatic
    @BindingAdapter("bind_cooking_list")
    fun bindCookingRecyclerView(recyclerView: RecyclerView, item: List<CookingInfoItem>?) {
        println("bind_cooking_list, size=${item?.size}")
        item?.let { itemList ->
            if (recyclerView.adapter == null) return
            val adapter = recyclerView.adapter as LCCookingFragment.CookingAdapter
            adapter.submitList(itemList)
        }
    }
    @JvmStatic
    @BindingAdapter("bind_cooking_selection_image")
    fun bindCookingSelectionImageRecyclerView(recyclerView: RecyclerView, item: List<CookingQuizSelection>?) {
        Log.d("bind_cooking_selection_image", "list=$item")
        item?.let { contentList ->
            if (recyclerView.adapter == null) { return }
            val adapter = recyclerView.adapter as LCCookingFragment.SelectionListAdapter
            adapter.submitList(contentList)
        }
    }

    @JvmStatic
    @BindingAdapter("bind_prior_concept_card")
    fun bindPriorConceptCardRecyclerView(recyclerView: RecyclerView, item: List<LCPriorConceptInfo>?){
        Log.d("bind_prior_concept_card", "list=$item")
        item?.let { priorConceptList ->
            val adapter = recyclerView.adapter as LCPriorConceptFragment.PriorConceptCardListAdapter
            adapter.submitList(priorConceptList)
        }
    }

    @JvmStatic
    @BindingAdapter("floatingSheetStepBtnText")
    fun setFloatingBtnText(sheet: FloatingAnswerSheet, value: String) {
        if(sheet.binding.scoringBtn.text.toString() != value) {
            sheet.binding.scoringBtn.setText(value)
        }
    }

    @JvmStatic
    @BindingAdapter("floatingSheetAnswerType")
    fun setFloatingBtnAnswerType(sheet: FloatingAnswerSheet, isShortFormat: Boolean) {
        sheet.binding.apply {
            if (isShortFormat) {
                shortAnswerView.visibility = View.VISIBLE
                selectionAnswerView.visibility = View.INVISIBLE
            } else {
                shortAnswerView.visibility = View.INVISIBLE
                selectionAnswerView.visibility = View.VISIBLE
            }
        }
    }

    @JvmStatic
    @BindingAdapter("floatingStepBtnBackground")
    fun setFloatingStepBtnBackground(sheet: FloatingAnswerSheet, isAnswerEntered: Boolean) {
        sheet.binding.apply {
            if (isAnswerEntered) {
                scoringBtn.background = ContextCompat.getDrawable(sheet.context, R.drawable.bg_purple_6d6dff_round_40_disabled)
            } else {
                scoringBtn.background = ContextCompat.getDrawable(sheet.context, R.drawable.bg_purple_6d6dff_round_40)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("answerAreaEnabled")
    fun setFloatingBtnAnswerAreaEnabled(sheet: FloatingAnswerSheet, isEnabled: Boolean) {
        sheet.binding.apply {
            selectionAnswerView.isEnabled = isEnabled
            shortAnswerView.isEnabled = isEnabled
        }
    }

    @JvmStatic
    @BindingAdapter("bind_solution_list")
    fun bindSolutionRecyclerView(recyclerView: RecyclerView, item: List<AffiliatedSolution>?) {
//    Log.d("bind_solution_video_response", " size=${item?.size}")
        item?.let { workbookList ->
            val adapter = recyclerView.adapter as AffiliatedSolveSolutionFragment.VideoSolutionAdapter
            adapter.submitList(null)
            adapter.submitList(workbookList)
            adapter.notifyDataSetChanged()
        }
    }

    @JvmStatic
    @BindingAdapter("bind_lc_wrong_note_card")
    fun bindLCWrongNoteCardRecyclerView(recyclerView: RecyclerView, item: List<LCWrongNoteMapCard>?){
        Log.d("bind_lc_wrong_note_card", "list=$item")
        item?.let { reviewList ->
            val adapter = recyclerView.adapter as LCWrongNoteMapFragment.WrongNoteAdapter
            adapter.submitList(reviewList)
        }
    }

    @JvmStatic
    @BindingAdapter("cookingImgRes")
    fun loadImage(view: ImageView, imageUrl: String?) {
        imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
        if (imageUrl?.isEmpty() == true) return
        imageUrl?.let {
            view.setImageUrlGlide(it)
        }
    }
    @JvmStatic
    @BindingAdapter("cookingImgResOnPicasso")
    fun loadImagePicasso(view: ImageView, imageUrl: String?) {
        imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
        if (imageUrl?.isEmpty() == true) return
        imageUrl?.let {
            view.setImageUrlPicasso(it)
        }
    }
    @JvmStatic
    @BindingAdapter("cookingImgResOnPicassoDownScale")
    fun loadImagePicassoDownScale(view: ImageView, imageUrl: String?) {
        imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
        if (imageUrl?.isEmpty() == true) return
        imageUrl?.let {
            view.setImageUrlPicassoDownScale(it)
        }
    }

    @JvmStatic
    @BindingAdapter("imgResAtQuiz")
    fun loadImage2(view: ImageView, imageUrl: String?) {
        imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
        if (imageUrl?.isEmpty() == true) return
        imageUrl?.let {
            view.setCookingImageURL(it)
        }
    }

}