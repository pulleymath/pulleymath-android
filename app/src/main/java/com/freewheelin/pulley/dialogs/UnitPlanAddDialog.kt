package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.BookCategory
import com.freewheelin.pulley.core.manage.BookManager
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.MarginDecoration
import com.freewheelin.pulley.views.TextViews.TagTextView
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import kotlinx.android.synthetic.main.dialog_add_plan.*
import kotlinx.android.synthetic.main.item_cell.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_study_plan_add.view.*

interface UnitPlanAddDialogListener {
    fun onBookAdded(book: Book)
}

class UnitPlanAddDialog: Dialog {

    var selectedIndexPath = IndexPath(0,0,Type.row)
    var listener: UnitPlanAddDialogListener? = null
    var categories: List<Pair<String,List<BookCategory>>>? = null
    var books: List<Book>? = null
    var selectedBooksByGroup: List<Book> = listOf()
    val user: User

    constructor(context: Context, user: User): super(context) {
        setContentView(R.layout.dialog_add_plan)
        this.user = user
        initUI()
        BookManager.getNewPlanList(context, user) { books, categories ->
            this.categories = categories
            this.books = books
            this.selectedBooksByGroup = books.filter {
                val selectedGroupID = categories.first().second.first().bookCategoryListID
                it.bookCategoryListID ==  selectedGroupID
            }
            leftRv.adapter?.notifyDataSetChanged()
            rightRv.adapter?.notifyDataSetChanged()
        }
    }

    private fun initUI() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        xBtn.extensionTouchArea(32.toPx())
        xBtn.setOnClickListener {
            dismiss()
        }
        rightRv.adapter = RightAdapter()
        val decoration = MarginDecoration(28.toPx(), 28.toPx(), 28.toPx())
        rightRv.addItemDecoration(decoration)
        rightRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val adapter = LeftAdapter()
        adapter.sectionType = SectionType.header
        leftRv.adapter = adapter
        leftRv.layoutManager = LinearLayoutManager(context)
    }

    fun onCategorySelected(id: Int) {
        LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK,"유형학습","좌측메뉴클릭")
        this.selectedBooksByGroup = this.books?.filter { it.bookCategoryListID == id } ?: listOf()
        this.leftRv.adapter?.notifyDataSetChanged()
        this.rightRv.adapter?.notifyDataSetChanged()
    }

    inner class RightAdapter: RecyclerView.Adapter<StudyPlanViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StudyPlanViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_study_plan_add, viewGroup, false)
            return StudyPlanViewHolder(view)
        }

        override fun getItemCount(): Int {
            return selectedBooksByGroup.size
        }

        override fun onBindViewHolder(holder: StudyPlanViewHolder, position: Int) {
            val book = selectedBooksByGroup[position]
            holder.addBtn.setOnClickListener {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "유형학습", "추가하기")
                BookManager.assign(context, book, this@UnitPlanAddDialog.user) {
                    book.assignID = it.assignID
                    book.createDateTime = it.createDateTime
                    book.addNewAssignPlan = true
                    holder.set(book)
                    listener?.onBookAdded(book)
                }

            }
            holder.set(book)
        }
    }

    inner class LeftAdapter: SectionAdapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(indexPath: IndexPath): Int {
            when(indexPath.type) {
                Type.row -> return 0
                else -> return 1
            }
        }

        override fun numberOfRows(section: Int): Int {
            return categories!![section].second.size
        }

        override fun numberOfSection(): Int {
            return categories?.size ?: 0
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            (holder as? HeaderHolder)?.apply {
                itemView.headerTv.text = categories!![indexPath.section].first
                val bookCategories = categories!![indexPath.section].second
                val scheduleCategories = bookCategories.filter { it.scheduled }

                if(scheduleCategories.isEmpty() && bookCategories.isNotEmpty()) {
                    itemView.headerTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                } else {
                    itemView.headerTv.text = "${categories!![indexPath.section].first}(예정)"
                    itemView.headerTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                }
            }

            (holder as? CellHolder)?.apply {
                val bookCategory = categories!![indexPath.section].second[indexPath.row]

                itemView.itemTv.text = bookCategory.bookCategory

                if (indexPath == selectedIndexPath)
                    itemView.containerCl.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_f2f2f2))
                else
                    itemView.containerCl.setBackgroundColor(ContextCompat.getColor(context, R.color.white_ffffff))

                if(bookCategory.scheduled) {
                    itemView.itemTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                    itemView.itemTv.text = "${bookCategory.bookCategory}(예정)"
                } else {
                    itemView.itemTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                }

                if(bookCategory.containUploadNewPlan) {
                    itemView.updateTag.visibility = View.VISIBLE
                } else {
                    itemView.updateTag.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    if (bookCategory.scheduled == false) {
                        this@UnitPlanAddDialog.selectedIndexPath = indexPath
                        onCategorySelected(bookCategory.bookCategoryListID)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> {
                    val view = LayoutInflater.from(context).inflate(R.layout.item_cell, parent, false)
                    CellHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false)
                    HeaderHolder(view)
                }
            }
        }
    }
}
class CellHolder(view: View): RecyclerView.ViewHolder(view)
class HeaderHolder(view: View): RecyclerView.ViewHolder(view)

class StudyPlanViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    var hider = view.hider
    var bookNameTv = view.bookNameTv
    val subjectTagTv = view.subjectTagTv
    val chapterTv = view.chapterTv
    var problemCntTv = view.problemCntTv
    var addBtn = view.addBtn
    var addedDateTv = view.addedDateTv
    val tagContainerLl = view.tagContainerLl
    val updateTag = view.cardUpdateTag
    val activeTag = view.activeTag

    fun set(book: Book) {
        bookNameTv.text = book.bookName
        subjectTagTv.text = book.subject
        chapterTv.text = book.chapter

        if(book.assignID == null) {
            addedDateTv.visibility = View.GONE
            hider.visibility = View.GONE
            addBtn.visibility = View.VISIBLE
        } else {
            addedDateTv.visibility = View.VISIBLE
            hider.visibility = View.VISIBLE
            addBtn.visibility = View.GONE

            addedDateTv.text = DateTimeUtils.yyyyMMddFormat.format(book.createDateTime) + " 추가됨"
        }

        problemCntTv.text = "${book.totalNumber} 문제"

        tagContainerLl.removeAllViewsInLayout()
        for (i in 0 until book.tag.size) {
            val tagLabel = TagTextView(view.context, book.tag[i], view.context.resources.getDimension(R.dimen.sp12)).apply {
                setPadding(6.toPx(),4.toPx(),6.toPx(),4.toPx())
            }
            tagContainerLl.addView(tagLabel)
            if(i != 0)
                (tagLabel.layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart = 8.toPx()
        }

        if(book.activeRecommendTag == null) {
            activeTag.visibility = View.GONE
        } else {
            activeTag.text = "${book.activeRecommendTag}"
            activeTag.visibility = View.VISIBLE
        }

        if(book.uploadNewPlan) {
            updateTag.visibility = View.VISIBLE
        } else {
            updateTag.visibility = View.INVISIBLE
        }
    }
}


