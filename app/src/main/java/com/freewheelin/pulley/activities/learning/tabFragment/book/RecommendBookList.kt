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
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.MarginDecoration

class RecommendBookList: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var index = 0
    var books: MutableList<Book>? = null
    var planListener: PlanListener? = null

    var recyclerView: RecyclerView
    var indexLabel: TextView
    var guideTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_recommend_book_list, this)

        recyclerView = findViewById(R.id.recyclerView)
        indexLabel = findViewById(R.id.indexLabel)
        guideTv = findViewById(R.id.guideTv)

        recyclerView.adapter = PlanAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val itemSpace = resources.getDimension(R.dimen.dp16).toInt()
        recyclerView.addItemDecoration(MarginDecoration(itemSpace))
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "스와이프", "추천플랜")
                    }
                }
            }
        })
    }

    inner class PlanAdapter: RecyclerView.Adapter<RecommendPlanHolder>()  {
        override fun getItemCount(): Int {
            return books?.size ?: 0
        }

        override fun onBindViewHolder(holder: RecommendPlanHolder, position: Int) {
            val book = books?.getOrNull(position) ?: return
            holder.set(book)
            holder.listener = planListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendPlanHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_book_recommend_plan, parent, false)
            return RecommendPlanHolder(view)
        }
    }

    fun set(books: MutableList<Book>?, title: String, index: Int, planListener: PlanListener? = null) {
        this.planListener = planListener
        indexLabel.text = "#${index}"
        guideTv.text = "$title"
        this.books = books?: mutableListOf()
        visibility = if (books == null) View.GONE else View.VISIBLE
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun set(books: List<Book>) {
        this.books?.clear()
        this.books?.addAll(books)
        visibility = if(books == null) View.GONE else View.VISIBLE

        recyclerView.adapter?.notifyDataSetChanged()
    }
}

