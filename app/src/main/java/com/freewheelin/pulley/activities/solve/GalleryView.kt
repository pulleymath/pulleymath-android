package com.freewheelin.pulley.activities.solve

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ItemHeaderGalleryBinding
import com.freewheelin.pulley.databinding.ItemProblemGalleryBinding
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.GridMarginDecoration
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type

interface GalleryViewDelegate {
    fun onFoldBtnClicked()
    fun onProblemSelected(problem: Problem?, autoFocus: Boolean = true)
    fun onFilterCheckChanged(isChecked: Boolean)
}

class GalleryView : ConstraintLayout {
    var delegate: GalleryViewDelegate? = null
    var adapter: ContentAdapter? = null
    var itemValue: String = ""

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    companion object {
        fun getGalleryViewWidth(context: Context): Int {
            return (DisplayUtils.getScreenWidth(context) * 0.693f).toInt()
        }
    }

    var foldBtn: ImageButton
    var filterSwitch: Switch
    var filterLabel: TextView
    var recyclerView: RecyclerView
    var galleryHeaderTv: TextView
    var emptyFilterContainer: ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_gallery, this)

        foldBtn = findViewById(R.id.foldBtn)
        filterSwitch = findViewById(R.id.filterSwitch)
        filterLabel = findViewById(R.id.filterLabel)
        recyclerView = findViewById(R.id.recyclerView)
        galleryHeaderTv = findViewById(R.id.galleryHeaderTv)
        emptyFilterContainer = findViewById(R.id.emptyFilterContainer)

        foldBtn.setOnClickListener {
            LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤-갤러리꺽쇠닫기", itemValue)
            delegate?.onFoldBtnClicked()
        }
        filterSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { button, isChecked ->
            adapter?.filter(isChecked)
            delegate?.onFilterCheckChanged(isChecked)
        })

        val decoration = GridMarginDecoration(rowSpace = 8, columnCnt = 4, columnSpace = 0)
        recyclerView.addItemDecoration(decoration)
        recyclerView.layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤-썸네일스크롤", itemValue)
                    }
                }
            }

        })
    }

    fun set(content: Content) {
        galleryHeaderTv.text = getHeaderText(content)
        when (content) {

            is Book -> {
                val contentAdapter = if (content.bookPage == null || content.bookPage!!.isEmpty()) {
                    ContentAdapter(content, delegate)
                } else {
                    BookAdapter(content, delegate)
                }

                this.adapter = contentAdapter
                if (content.bookPage?.isNotEmpty() == true && content.bookPage!![contentAdapter.selectedIndexPath.section]?.problems?.isNotEmpty() == true) {
                    val selectedProblem = if (adapter is BookAdapter)
                        content.bookPage!![contentAdapter.selectedIndexPath.section].problems[contentAdapter.selectedIndexPath.row]
                    else
                        content.problems[contentAdapter.selectedIndexPath.row]

                    delegate?.onProblemSelected(selectedProblem)
                } else {
                    // Toast 문제가 없습니다.
                    if(content.problems.isNotEmpty()) {
                        val selectedProblem = content.problems[contentAdapter.selectedIndexPath.row]
                        delegate?.onProblemSelected(selectedProblem)
                    }
                }
            }
            else -> {
                try {
                    val contentAdapter = ContentAdapter(content, delegate)
                    this.adapter = contentAdapter
                    val selectedProblem = content.problems[contentAdapter.selectedIndexPath.row]
                    delegate?.onProblemSelected(selectedProblem)
                }catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }

        recyclerView.layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false).also {
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val indexPath = adapter!!.getIndexPath(position)
                    return if (indexPath.type == Type.header) {
                        4
                    } else {
                        1
                    }
                }
            }
        }
        val decoration = GridMarginDecoration(rowSpace = 8, columnCnt = 4, columnSpace = 0)
        recyclerView.addItemDecoration(decoration)
        recyclerView.adapter = this.adapter
    }

    fun hideFilter() {
        filterLabel.visibility = View.INVISIBLE
        filterSwitch.visibility = View.INVISIBLE
    }

    fun showFilter() {
        filterLabel.visibility = View.VISIBLE
        filterSwitch.visibility = View.VISIBLE
    }

    fun getHeaderText(content: Content): String {
        return when (content) {
            is Book -> {
                "${content.bookName}"
            }
            is Test, is Piece -> {
                "${content.subject}"
            }
            is MockExam -> {
                content.getMockTitle()
            }
            else -> {
                ""
            }
        }
    }

    fun prev() {
        adapter?.prev()
    }

    fun next() {
        adapter?.next()
    }

    fun updateAll() {
        adapter?.notifyDataSetChanged()
    }

    fun update(problem: Problem) {
        adapter?.reload(problem)
    }

    fun select(problem: Problem) {
        adapter?.select(problem)
    }

    fun add(problem: Problem) {
        adapter?.add(problem)
    }

    fun change(origin: Problem, target: Problem) {
        adapter?.change(origin, target)
    }

    fun scrollTo(problem: Problem) {
        adapter?.scroll(problem)
    }

    fun getBookPage(): BookPage {
        val bookAdapter = adapter as BookAdapter
        val book = bookAdapter.book
        val indexPath = bookAdapter.selectedIndexPath
        return book.bookPage!![indexPath.section]
    }

    open inner class ContentAdapter(var content: Content, open var delegate: GalleryViewDelegate?) : SectionAdapter<RecyclerView.ViewHolder>() {

        var selectedIndexPath: IndexPath
        var filteredProblem: MutableList<Problem>? = null

        init {
            this.selectedIndexPath = getInitProblemIndexPath()
            this.sectionType = SectionType.none
        }

        override fun getItemViewType(indexPath: IndexPath): Int {
            return 0
        }

        override fun numberOfRows(section: Int): Int {
            return getProblems().size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            val problem = getProblems()[indexPath.row]
            val holder = holder as GalleryHolder
            val selectedProblem = getProblems()[selectedIndexPath.row]
            holder.setProblem(problem, selectedProblem)
            holder.bgView.setOnClickListener {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤-썸네일터치", itemValue)
                val beforeIndexPath = selectedIndexPath
                selectedIndexPath = indexPath
                if (beforeIndexPath != null && beforeIndexPath != selectedIndexPath)
                    notifyItemChanged(beforeIndexPath.row, "UNSELECT")

                holder.select()
                delegate?.onProblemSelected(problem, false)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
            if (payloads.isEmpty())
                this.onBindViewHolder(holder, position)
            else {
                for (payload in payloads) {
                    when (payload) {
                        "UNSELECT" -> {
                            (holder as? GalleryHolder)?.unselect()
                        }
                        "RELOAD" -> this.onBindViewHolder(holder, position)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_problem_gallery, parent, false)
            val itemBinding: ItemProblemGalleryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_problem_gallery, parent, false)
            return GalleryHolder(itemBinding)
        }

        open fun reload(problem: Problem) {
            val indexPath = getProblemIndexPath(problem) ?: return
            notifyItemChanged(indexPath.row, "RELOAD")
        }

        fun select(problem: Problem) {
            val indexPath = getProblemIndexPath(problem) ?: return
            select(indexPath)
        }

        open fun change(origin: Problem, target: Problem) {
            val indexPath = getProblemIndexPath(target) ?: return
            selectedIndexPath = indexPath
            notifyDataSetChanged()
            delegate?.onProblemSelected(target)
        }

        open fun add(problem: Problem) {
            val indexPath = getProblemIndexPath(problem) ?: return
            selectedIndexPath = indexPath
            notifyDataSetChanged()
            delegate?.onProblemSelected(problem)
        }

        open fun select(indexPath: IndexPath) {
            val beforeIndexPath = selectedIndexPath
            selectedIndexPath = indexPath

            notifyItemChanged(beforeIndexPath.row, "UNSELECT")
            notifyItemChanged(selectedIndexPath.row, "RELOAD")
            val problem = getProblems()[indexPath.row]
            delegate?.onProblemSelected(problem)
        }

        fun prev() {
            val prevIndexPath = getPrevIndexPath() ?: return
            select(prevIndexPath)
        }

        fun next() {
            val nextIndexPath = getNextIndexPath() ?: return
            select(nextIndexPath)
        }

        open fun scroll(problem: Problem) {
            val indexPath = getProblemIndexPath(problem) ?: return
            recyclerView.scrollToPosition(indexPath.row)
        }

        open fun filter(isChecked: Boolean) {
            if (isChecked) {
                this.filteredProblem = content.problems.filter { it.getResultByScoring() == Result.incorrect }.toMutableList()
                val currentSelectedProblem = content.problems[selectedIndexPath.row]
                if (currentSelectedProblem.getResultByScoring() == Result.incorrect) {
                    val indexPath = getProblemIndexPath(currentSelectedProblem) ?: return
                    selectedIndexPath = indexPath
                } else {
                    val index = content.problems.indexOf(currentSelectedProblem)

                    for (i in index - 1 downTo 0) {
                        val problem = content.problems[i]
                        if (problem.getResultByScoring() == Result.incorrect) {
                            val indexProblem = getProblemIndexPath(problem) ?: return
                            selectedIndexPath = indexProblem
                            notifyDataSetChanged()
                            delegate?.onProblemSelected(problem)
                            return
                        }
                    }

                    for (i in index + 1 until content.problems.size) {
                        val problem = content.problems[i]
                        if (problem.getResultByScoring() == Result.incorrect) {
                            val indexPath = getProblemIndexPath(problem) ?: return
                            selectedIndexPath = indexPath
                            notifyDataSetChanged()
                            delegate?.onProblemSelected(problem)
                            return
                        }
                    }

                    val problem = getProblems().getOrNull(0)
                    selectedIndexPath = IndexPath(0, 0, Type.row)
                    delegate?.onProblemSelected(problem)
                }

            } else {
                val currentSelectedProblem = filteredProblem?.getOrNull(selectedIndexPath.row)
                this.filteredProblem = null

                if (currentSelectedProblem != null) {
                    val indexPath = getProblemIndexPath(currentSelectedProblem)
                    if (indexPath != null) {
                        selectedIndexPath = indexPath
                        delegate?.onProblemSelected(currentSelectedProblem)
                    }
                } else {
                    selectedIndexPath = getInitProblemIndexPath()
                    val problem = getProblems()[selectedIndexPath.row]
                    delegate?.onProblemSelected(problem)
                }
            }
            notifyDataSetChanged()

            if (getProblems().isEmpty()) {
                emptyFilterContainer.visibility = View.VISIBLE
            } else {
                emptyFilterContainer.visibility = View.GONE
            }
        }

        open fun getPrevIndexPath(): IndexPath? {
            val indexPath = selectedIndexPath ?: return null
            val row = indexPath.row

            return if (row > 0)
                IndexPath(row - 1, 0, Type.row)
            else
                null
        }

        open fun getNextIndexPath(): IndexPath? {
            val indexPath = selectedIndexPath ?: return null
            val row = indexPath.row
            return if (row + 1 < getProblems().size)
                IndexPath(row + 1, 0, Type.row)
            else
                null
        }

        open fun getProblemIndexPath(target: Problem): IndexPath? {
            val row = getProblems().indexOf(target)
            return if (row >= 0)
                IndexPath(row, 0, Type.row)
            else
                null
        }

        open fun getInitProblemIndexPath(): IndexPath {
            var row = 0
            for (problem in content.problems) {
                if (problem.getResultByScoring() == Result.yet)
                    return IndexPath(row, 0, Type.row)
                else
                    row += 1
            }

            return IndexPath(0, 0, Type.row)
        }

        private fun getProblems(): MutableList<Problem> {
            return filteredProblem ?: content.problems.toMutableList()
        }
    }

    inner class BookAdapter : ContentAdapter {
        val book: Book
            get() = content as Book

        var filteredPage: List<BookPage>? = null

        constructor(book: Book, delegate: GalleryViewDelegate?) : super(book, delegate) {
            sectionType = SectionType.header
        }

        override fun getItemViewType(indexPath: IndexPath): Int {
            return when (indexPath.type) {
                Type.header -> 0
                else -> 1
            }
        }

        override fun numberOfSection(): Int {
            return getPages().size
        }

        override fun numberOfRows(section: Int): Int {
            return getPages()[section].getPageProblems().size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            when (indexPath.type) {
                Type.header -> {
                    val holder = holder as GalleryHeaderHolder
                    val bookPage = getPages()[indexPath.section]
                    holder.pageNameTv.text = bookPage.pageName
                    holder.chapterMiddleTv.text = bookPage.chapterMiddleName

                    val prevPage = getPages().getOrNull(indexPath.section - 1)
                    if (prevPage?.chapterMiddleName == bookPage.chapterMiddleName) {
                        holder.chapterMiddleTv.visibility = View.GONE
                    } else
                        holder.chapterMiddleTv.visibility = View.VISIBLE
                }
                Type.row -> {
                    val problem = getPages()[indexPath.section].getPageProblems()[indexPath.row]

                    val holder = holder as GalleryHolder
                    val selectedProblem = getPages()[selectedIndexPath.section].getPageProblems()[selectedIndexPath.row]

                    holder.setProblem(problem, selectedProblem)
                    holder.bgView.setOnClickListener {
                        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤-썸네일터치", itemValue)
                        val beforeIndexPath = selectedIndexPath
                        selectedIndexPath = indexPath
                        if (beforeIndexPath != null && beforeIndexPath != selectedIndexPath)
                            notifyItemChanged(getRawPosition(beforeIndexPath), "UNSELECT")

                        holder.select()
                        delegate?.onProblemSelected(problem)
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
            if (payloads.isEmpty())
                this.onBindViewHolder(holder, position)
            else {
                for (payload in payloads) {
                    when (payload) {
                        "UNSELECT" -> {
                            (holder as? GalleryHolder)?.unselect()
                        }
                        "RELOAD" -> this.onBindViewHolder(holder, position)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == 1) {
                val itemBinding: ItemProblemGalleryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_problem_gallery, parent, false)
                return GalleryHolder(itemBinding)
            } else {
                val itemHeaderBinding: ItemHeaderGalleryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_header_gallery, parent, false)
                return GalleryHeaderHolder(itemHeaderBinding)
            }
        }

        override fun reload(problem: Problem) {
            val indexPath = getProblemIndexPath(problem) ?: return
            notifyItemChanged(getRawPosition(indexPath), "RELOAD")
        }

        override fun add(problem: Problem) {
            val rootIndexPath = getProblemIndexPath(problem.rootProblem!!)
            val page = getPages()[rootIndexPath!!.section]

            val rootIndex = page.problems.indexOf(problem.rootProblem!!)
            val index = problem.rootProblem!!.similarProblems.indexOf(problem)
            page.problems.add(rootIndex + index + 1, problem)

            if (page.filteredProblems != null) {
                val rootIndex = page.filteredProblems!!.indexOf(problem.rootProblem!!)
                val index = problem.rootProblem!!.similarProblems.filter {
                    it === problem || page.filteredProblems!!.toSet().contains(it)
                }.indexOf(problem)
                page.filteredProblems!!.add(rootIndex + index + 1, problem)
            }

            val indexPath = getProblemIndexPath(problem) ?: return
            selectedIndexPath = indexPath
            notifyDataSetChanged()
            delegate?.onProblemSelected(problem)
        }

        override fun change(origin: Problem, target: Problem) {
            val rootIndexPath = getProblemIndexPath(target.rootProblem!!)
            val page = getPages()[rootIndexPath!!.section]
            val index = page.problems.indexOf(origin)
            page.problems[index] = target

            if (page.filteredProblems != null) {
                val index = page.filteredProblems!!.indexOf(origin)
                page.filteredProblems?.set(index, target)
            }

            val indexPath = getProblemIndexPath(target) ?: return
            selectedIndexPath = indexPath
            notifyDataSetChanged()
            delegate?.onProblemSelected(target)
        }

        override fun select(indexPath: IndexPath) {
            val beforeIndexPath = selectedIndexPath
            selectedIndexPath = indexPath

            notifyItemChanged(getRawPosition(beforeIndexPath), "UNSELECT")
            notifyItemChanged(getRawPosition(selectedIndexPath), "RELOAD")
            val problem = getPages()[indexPath.section].getPageProblems()[indexPath.row]
            delegate?.onProblemSelected(problem)
        }

        override fun getPrevIndexPath(): IndexPath? {
            val indexPath = selectedIndexPath ?: return null

            val section = indexPath.section
            val row = indexPath.row - 1

            if (row >= 0) {
                return IndexPath(row, section, Type.row)
            } else if (section > 0) {
                for (i in section - 1 downTo 0) {
                    val problemSize = getPages()[i].getPageProblems().size
                    if (problemSize > 0) {
                        return IndexPath(problemSize - 1, i, Type.row)
                    }
                }
            }
            return null
        }

        override fun getNextIndexPath(): IndexPath? {
            val indexPath = selectedIndexPath ?: return null
            val section = indexPath.section
            var row = indexPath.row + 1

            val problemSize = getPages()[section].getPageProblems().size

            if (row < problemSize) {
                return IndexPath(row, section, Type.row)
            } else {
                row = 0
                for (i in section + 1 until getPages().size) {

                    if (getPages()[i].getPageProblems().isNotEmpty()) {
                        return IndexPath(row, i, Type.row)
                    }
                }
            }

            return null
        }

        override fun getProblemIndexPath(target: Problem): IndexPath? {
            var section = 0
            var row: Int
            for (page in getPages()) {
                row = 0
                for (problem in page.getPageProblems()) {
                    if (problem == target)
                        return IndexPath(row, section, Type.row)
                    else
                        row += 1
                }

                section += 1
            }

            return null
        }

        override fun getInitProblemIndexPath(): IndexPath {
            var section = 0
            var row = 0
            for (page in getPages()) {
                row = 0
                for (problem in page.getPageProblems()) {
                    if (problem.getResultByScoring() == Result.yet)
                        return IndexPath(row, section, Type.row)
                    else
                        row += 1
                }

                section += 1
            }

            return IndexPath(0, 0, Type.row)
        }

        override fun filter(isChecked: Boolean) {
            if (isChecked) {
                this.filteredPage = book.getFilteredPage()
                val currentSelectedProblem = book.bookPage!![selectedIndexPath.section].problems[selectedIndexPath.row]
                if (currentSelectedProblem.getResultByScoring() == Result.incorrect) {
                    val indexPath = getProblemIndexPath(currentSelectedProblem) ?: return
                    selectedIndexPath = indexPath
                } else {
                    val index = book.problems.indexOf(currentSelectedProblem)

                    for (i in index - 1 downTo 0) {
                        val problem = book.problems[i]
                        if (problem.getResultByScoring() == Result.incorrect) {
                            val indexPath = getProblemIndexPath(problem) ?: return
                            selectedIndexPath = indexPath
                            notifyDataSetChanged()
                            delegate?.onProblemSelected(problem)
                            return
                        }
                    }

                    for (i in index + 1 until book.problems.size) {
                        val problem = book.problems[i]
                        if (problem.getResultByScoring() == Result.incorrect) {
                            val indexPath = getProblemIndexPath(problem) ?: return
                            selectedIndexPath = indexPath
                            notifyDataSetChanged()
                            delegate?.onProblemSelected(problem)
                            return
                        }
                    }
                    val problem = getPages().getOrNull(0)?.getPageProblems()?.getOrNull(0)
                    selectedIndexPath = IndexPath(0, 0, Type.row)
                    delegate?.onProblemSelected(problem)
                }

            } else {
                val currentSelectedProblem = filteredPage?.getOrNull(selectedIndexPath.section)?.filteredProblems?.getOrNull(selectedIndexPath.row)
                book.bookPage?.forEach { it.filteredProblems = null }
                this.filteredPage = null

                if (currentSelectedProblem != null) {
                    val indexPath = getProblemIndexPath(currentSelectedProblem)
                    if (indexPath != null) {
                        selectedIndexPath = indexPath
                        delegate?.onProblemSelected(currentSelectedProblem)
                    }
                } else {
                    selectedIndexPath = getInitProblemIndexPath()
                    val problem = getPages()[selectedIndexPath.section].problems[selectedIndexPath.row]
                    delegate?.onProblemSelected(problem)
                }
            }
            notifyDataSetChanged()

            if (getPages().isEmpty()) {
                emptyFilterContainer.visibility = View.VISIBLE
            } else {
                emptyFilterContainer.visibility = View.GONE
            }
        }

        override fun scroll(problem: Problem) {
            val indexPath = getProblemIndexPath(problem) ?: return
            recyclerView.scrollToPosition(getRawPosition(indexPath))
        }

        fun getPages(): List<BookPage> {
            return filteredPage ?: book.bookPage?.filter { it.problems.size != 0 } ?: emptyList()
        }

    }
}
