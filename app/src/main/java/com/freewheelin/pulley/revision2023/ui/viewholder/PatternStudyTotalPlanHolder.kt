package com.freewheelin.pulley.revision2023.ui.viewholder

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.activities.learning.tabFragment.book.PlanHolder
import com.freewheelin.pulley.databinding.ItemBookTotalPlanBinding
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.contents.ClientBookType
import com.freewheelin.pulley.utils.scrollToView

class PatternStudyTotalPlanHolder(val binding: ItemBookTotalPlanBinding) : PlanHolder(binding.root) {

    override fun set(book: Book) {
        super.set(book)

        binding.apply {
            val tags = listOf(tag1, tag2)
            book.clientBookType = ClientBookType.ALL

            setTag(tags) {
                studyListener?.filterFromTagOnCard(it)
            }
            solveCntTv.text = "${book.markedNumber}/${book.totalNumber}"
            problemCntTv.text = book.totalNumber.toString() + "문제"
            correctRateTv.text = "${book.score}%"

            if (book.markedNumber == 0) {
                solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            } else {
                solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            }

            guideTv.text = book.description
        }


    }
}