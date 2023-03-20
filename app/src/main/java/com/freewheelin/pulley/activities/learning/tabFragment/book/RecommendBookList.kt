package com.freewheelin.pulley.activities.learning.tabFragment.book

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PatternStudyMyPlanAdapter.OriginType
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.MarginDecoration

class RecommendBookList: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var index = 0
    var books: MutableList<Book>? = null
    lateinit var planListener: PlanListenerV2

    var recyclerView: RecyclerView
    var indexLabel: TextView
    var guideTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_recommend_book_list, this)

        recyclerView = findViewById(R.id.recyclerView)
        indexLabel = findViewById(R.id.indexLabel)
        guideTv = findViewById(R.id.guideTv)
    }
    fun initRv(planListener: PlanListenerV2) {
        recyclerView.adapter = PatternStudyMyPlanAdapter(planListener, listOf(ActionType.pin), OriginType.Recommend, isGridLayout = false)
        val itemSpace = resources.getDimension(R.dimen.dp16).toInt()
        val sideItemSpace = resources.getDimension(R.dimen.dp48).toInt()
        recyclerView.addItemDecoration(MarginDecoration(itemSpace, sideItemSpace, sideItemSpace))
    }

    fun set(books: MutableList<Book>?, title: String, index: Int, planListener: PlanListenerV2) {
        this.planListener = planListener
        initRv(planListener)
        indexLabel.text = "#${index}"
        guideTv.text = "$title"
        this.books = books?: mutableListOf()
        visibility = if (books == null) View.GONE else View.VISIBLE
        (recyclerView.adapter as PatternStudyMyPlanAdapter).submitList(this.books)
    }

    fun set(books: List<Book>) {
        this.books?.clear()
        this.books?.addAll(books)
        visibility = if(books == null) View.GONE else View.VISIBLE
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

