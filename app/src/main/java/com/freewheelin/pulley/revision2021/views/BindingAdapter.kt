package com.freewheelin.pulley.revision2021.views

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.revision2021.activity.LCWrongNoteActivity
import com.freewheelin.pulley.revision2021.activity.fragments.AffiliatedSolveSolutionFragment
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCCookingFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCWrongNoteMapFragment
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.model.response.AffiliatedSolution
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.ui.adapter.ConceptCourseSmallAdapter
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.view.MainUserStatusChip
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.model.AffiliatedUniv
import com.freewheelin.pulley.revision2023.ui.view.MainTab

object BindingAdapter {

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
        }
    }
    @JvmStatic
    @BindingAdapter("layout_margin_start_dimen")
    fun setLayoutMarginStart(view: View, dimen: Float) {
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
    @BindingAdapter("layout_margin_bottom_dimen")
    fun setLayoutMarginBottom(view: View, dimen: Float) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.bottomMargin = dimen.toInt()
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
    @BindingAdapter("commonBackgroundIf")
    fun commonBackgroundIf(view: View, show: Boolean?) {
        view.setBackgroundResource(if (show == true) R.drawable.bg_purple_300_round_28_ripple else R.drawable.bg_white_round_28_ripple_gray200)
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
    @BindingAdapter("bind_img_url_")
    fun setImageUrl_(v: ImageView, url: String?) {
        url?.let {
            v.setImageURL(it)
        }
    }

    @JvmStatic
    @BindingAdapter("bind_study_chapter")
    fun bindStudyChapterRecyclerView(recyclerView: RecyclerView, item: List<StudyChapter>?){
        Log.d("bind_study_chapter", "list=$item")
        item?.let { chapterList ->
            val adapter = recyclerView.adapter as? ConceptCourseFragment.ChapterAdapter
            println("bind_study_chapter size : ${chapterList.size} , adapter :${adapter == null}")
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
                scoringBtn.background = ContextCompat.getDrawable(sheet.context, R.drawable.bg_purple_300_round_40_disabled)
            } else {
                scoringBtn.background = ContextCompat.getDrawable(sheet.context, R.drawable.bg_purple_300_round_40)
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
    @JvmStatic
    @BindingAdapter("visibleIf")
    fun visibleIf(view: View, show: Boolean?) {
        view.visibility = if (show == true) View.VISIBLE else View.GONE
    }
    @JvmStatic
    @BindingAdapter("visibleOrInvisibleIf")
    fun visibleOrInvisibleIf(view: View, show: Boolean?) {
        view.visibility = if (show == true) View.VISIBLE else View.INVISIBLE
    }

    @JvmStatic
    @BindingAdapter("visibleAnimIf")
    fun visibleAnimIf(view: View, show: Boolean?) {
        if (show == true) {
            view.show(600)
        } else {
            view.hide(200)
        }
    }
    @JvmStatic
    @BindingAdapter("visibleGoneAnimIf")
    fun visibleGoneAnimIf(view: View, show: Boolean?) {
        if (show == true) {
            view.show(400)
        } else {
            view.hideToGone(400)
        }
    }
    @JvmStatic
    @BindingAdapter("setUserServiceType")
    fun makeUserStatusChip(view: MainUserStatusChip, type: PaidServiceType?) {
        view.type = type
    }

    @JvmStatic
    @BindingAdapter("planV2Cover")
    fun loadPlanV2Cover(view: ImageView, id: Int?) {
        id?.let {
            val imgRes = when (it) {
                1 -> R.drawable.book_plan_v2_cover_1
                2 -> R.drawable.book_plan_v2_cover_2
                3 -> R.drawable.book_plan_v2_cover_3
                4 -> R.drawable.book_plan_v2_cover_4
                5 -> R.drawable.book_plan_v2_cover_5
                6 -> R.drawable.book_plan_v2_cover_6
                7 -> R.drawable.book_plan_v2_cover_7
                8 -> R.drawable.book_plan_v2_cover_8
                9 -> R.drawable.book_plan_v2_cover_9
                10 -> R.drawable.book_plan_v2_cover_10
                11 -> R.drawable.book_plan_v2_cover_11
                12 -> R.drawable.book_plan_v2_cover_12
                13 -> R.drawable.book_plan_v2_cover_13
                14 -> R.drawable.book_plan_v2_cover_14
                15 -> R.drawable.book_plan_v2_cover_15
                16 -> R.drawable.book_plan_v2_cover_16
                17 -> R.drawable.book_plan_v2_cover_17
                18 -> R.drawable.book_plan_v2_cover_18
                19 -> R.drawable.book_plan_v2_cover_19
                20 -> R.drawable.book_plan_v2_cover_20
                21 -> R.drawable.book_plan_v2_cover_21
                22 -> R.drawable.book_plan_v2_cover_22
                23 -> R.drawable.book_plan_v2_cover_23
                24 -> R.drawable.book_plan_v2_cover_24
                25 -> R.drawable.book_plan_v2_cover_25
                26 -> R.drawable.book_plan_v2_cover_26
                27 -> R.drawable.book_plan_v2_cover_27
                28 -> R.drawable.book_plan_v2_cover_28
                29 -> R.drawable.book_plan_v2_cover_29
                30 -> R.drawable.book_plan_v2_cover_30
                else -> R.drawable.book_plan_v2_cover_8
            }
            view.setImageResource(imgRes)
        }
    }

    @JvmStatic
    @BindingAdapter("purchase_guide_badge_background")
    fun setBadgeBackground(view: LinearLayout, type: PaidServiceType?) {
        type?.let {
            val imgRes = when (it) {
                PaidServiceType.BASIC_C, PaidServiceType.BASIC_P -> R.drawable.bg_bronze_round_13
                PaidServiceType.STANDARD -> R.drawable.bg_gray_600_round_13
                PaidServiceType.PREMIUM -> R.drawable.bg_yellow_300_round_13
                else -> R.drawable.bg_gray_600_round_13
            }
            view.setBackgroundResource(imgRes)
        }
    }

    @JvmStatic
    @BindingAdapter("affiliated_card_background")
    fun setAffiliatedCardBackground(view: View, univ: AffiliatedUniv?) {
        univ?.let {
            val imgRes = when (it) {
                AffiliatedUniv.Konkuk -> R.drawable.bg_konkuk_primary_round
                AffiliatedUniv.Soongsil -> R.drawable.bg_soongsil_primary_round
            }
            view.setBackgroundResource(imgRes)
        }
    }
    @JvmStatic
    @BindingAdapter("affiliated_card_character")
    fun setAffiliatedCardCharactor(view: ImageView, univ: AffiliatedUniv?) {
        univ?.let {
            val imgRes = when (it) {
                AffiliatedUniv.Konkuk -> R.mipmap.kudoctor
                AffiliatedUniv.Soongsil -> R.drawable.soongsoong_disabled
            }
            view.setImageResource(imgRes)
        }
    }
    @JvmStatic
    @BindingAdapter("affiliated_test_completed_character")
    fun setAffiliatedTestCompletedCharactor(view: ImageView, univ: AffiliatedUniv?) {
        univ?.let {
            val imgRes = when (it) {
                AffiliatedUniv.Konkuk -> R.drawable.box_colorful_ku
                AffiliatedUniv.Soongsil -> R.drawable.soongsoong_wink
            }
            view.setImageResource(imgRes)
        }
    }

    @JvmStatic
    @BindingAdapter("searchview_hint_size")
    fun setSearchViewHindSize(v: SearchView, dimen: Float) {
        ((((v.getChildAt(0) as LinearLayout
            ).getChildAt(2) as LinearLayout
            ).getChildAt(1) as LinearLayout
            ).getChildAt(0) as AutoCompleteTextView
            ).setTextSize(dimen, dimen)
    }

    @JvmStatic
    @BindingAdapter("mainTabTextColor")
    fun setMainTabTextColor(view: TextView, type: MainTab?) {
        type?.let {
            val tabName = view.text.toString()
            val color = if (tabName in it.names) {
                if (schoolType.isMiddle) R.color.gray_800 else R.color.white
            } else {
                R.color.gray_700
            }
            view.setTextColor(ContextCompat.getColor(view.context, color))
        }
    }
}