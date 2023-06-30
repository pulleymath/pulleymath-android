package com.freewheelin.pulley.legacy.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.BookCategory
import com.freewheelin.pulley.legacy.core.manage.BookManager
import com.freewheelin.pulley.databinding.ItemCellBinding
import com.freewheelin.pulley.databinding.ItemHeaderBinding
import com.freewheelin.pulley.databinding.ItemStudyPlanAddBinding
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.extensionTouchArea
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.MarginDecoration
import com.freewheelin.pulley.legacy.views.textViews.TagTextView
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type


interface UnitPlanAddDialogListener {
    fun onBookAdded(book: Book)
}

class UnitPlanAddDialog(context: Context, val user: User) : Dialog(context) {

    var selectedIndexPath = IndexPath(0,0,Type.row)
    var listener: UnitPlanAddDialogListener? = null
    var categories: List<Pair<String,List<BookCategory>>>? = null
    var books: List<Book>? = null
    var selectedBooksByGroup: List<Book> = listOf()

    lateinit var xBtn: ImageView
    lateinit var leftRv: RecyclerView
    lateinit var rightRv: RecyclerView

    init {
        setContentView(R.layout.dialog_add_plan)
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
        xBtn = findViewById(R.id.xBtn)
        leftRv = findViewById(R.id.leftRv)
        rightRv = findViewById(R.id.rightRv)

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
            val itemBinding: ItemStudyPlanAddBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_study_plan_add, viewGroup, false)
            return StudyPlanViewHolder(itemBinding)
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
                itemBinding.headerTv.text = categories!![indexPath.section].first
                val bookCategories = categories!![indexPath.section].second
                val scheduleCategories = bookCategories.filter { it.scheduled }

                if(scheduleCategories.isEmpty() && bookCategories.isNotEmpty()) {
                    itemBinding.headerTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
                } else {
                    itemBinding.headerTv.text = "${categories!![indexPath.section].first}(예정)"
                    itemBinding.headerTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                }
            }

            (holder as? CellHolder)?.apply {
                val bookCategory = categories!![indexPath.section].second[indexPath.row]

                itemBinding.itemTv.text = bookCategory.bookCategory

                if (indexPath == selectedIndexPath)
                    itemBinding.containerCl.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_200))
                else
                    itemBinding.containerCl.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

                if(bookCategory.scheduled) {
                    itemBinding.itemTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                    itemBinding.itemTv.text = "${bookCategory.bookCategory}(예정)"
                } else {
                    itemBinding.itemTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
                }

                if(bookCategory.containUploadNewPlan) {
                    itemBinding.updateTag.visibility = View.VISIBLE
                } else {
                    itemBinding.updateTag.visibility = View.GONE
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
                    val itemBinding: ItemCellBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_cell, parent, false)
                    CellHolder(itemBinding)
                }
                else -> {
                    val itemBinding: ItemHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_header, parent, false)
                    HeaderHolder(itemBinding)
                }
            }
        }
    }
}
class CellHolder(val itemBinding: ItemCellBinding): RecyclerView.ViewHolder(itemBinding.root)
class HeaderHolder(val itemBinding: ItemHeaderBinding): RecyclerView.ViewHolder(itemBinding.root)

class StudyPlanViewHolder(val itemBinding: ItemStudyPlanAddBinding): RecyclerView.ViewHolder(itemBinding.root) {
    var hider = itemBinding.hider
    var bookNameTv = itemBinding.bookNameTv
    val subjectTagTv = itemBinding.subjectTagTv
    val chapterTv = itemBinding.chapterTv
    var problemCntTv = itemBinding.problemCntTv
    var addBtn = itemBinding.addBtn
    var addedDateTv = itemBinding.addedDateTv
    val tagContainerLl = itemBinding.tagContainerLl
    val updateTag = itemBinding.cardUpdateTag
    val activeTag = itemBinding.activeTag
    val viewContext = itemBinding.root.context

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
            val tagLabel = TagTextView(viewContext, book.tag[i], viewContext.resources.getDimension(R.dimen.sp12)).apply {
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


