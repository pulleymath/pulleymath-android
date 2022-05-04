package com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component

import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.FragmentNoteFilterBinding
import com.freewheelin.pulley.databinding.ItemNoteFilterCalendarBinding
import com.freewheelin.pulley.databinding.ItemNoteFilterHeaderBinding
import com.freewheelin.pulley.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.dialogs.DateRangePickerDialogListener
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.toPx
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import org.joda.time.LocalDate
import java.io.Serializable

interface NoteFilterFragmentListener : Serializable {
    fun onFilterTypeChanged(fragment: NoteFilterFragment, filters: Set<FilterType>)
    fun onDateSet(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type)
}

class NoteFilterFragment : Fragment() {
    var selectedFilterTypes: HashSet<FilterType> = HashSet()
    lateinit var filters: List<Pair<String, List<FilterType>>>
    var listener: NoteFilterFragmentListener? = null

    var from: LocalDate = LocalDate.now().minusDays(6)
    var to: LocalDate = LocalDate.now()
    var type = DateRangePickerDialog.Type.RECENT7
    lateinit var binding: FragmentNoteFilterBinding

    val dialog: DateRangePickerDialog by lazy {
        val pickerDialog = DateRangePickerDialog(requireContext(), from, to, LocalDate(user!!.firstDate))
        pickerDialog.listener = object: DateRangePickerDialogListener {
            override fun onUpdateClicked(picker: DateRangePickerDialog, from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type) {
                this@NoteFilterFragment.from = from
                this@NoteFilterFragment.to = to
                this@NoteFilterFragment.type = type

                listener?.onDateSet(from, to, type)
                binding.filterRv.adapter?.notifyDataSetChanged()
            }
        }
        pickerDialog
    }

    var filterAdapter: FilterAdapter? = null

    companion object {
        const val ARG_FILTERS = "FILTER_TYPES"
        const val ARG_SELECTED_FILTERS = "SELECTED FILTERS"
        const val ARG_FROM = "ARG_FROM"
        const val ARG_TO = "ARG_TO"
        @JvmStatic
        fun newInstance(filters: List<Pair<String, List<FilterType>>>): NoteFilterFragment {
            val fragment = NoteFilterFragment()
            val args = Bundle()
            args.putSerializable(ARG_FILTERS, ArrayList(filters))

            val selectedSet = HashSet<FilterType>()
            filters.forEach { selectedSet.add(it.second[0]) }

            args.putSerializable(ARG_SELECTED_FILTERS, selectedSet)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.filters = arguments?.getSerializable(ARG_FILTERS) as? List<Pair<String, List<FilterType>>>
                ?: ArrayList()
        this.selectedFilterTypes = arguments?.getSerializable(ARG_SELECTED_FILTERS) as? HashSet<FilterType>
                ?: HashSet()

        if(savedInstanceState != null) {
            this.filters = savedInstanceState.getSerializable(ARG_FILTERS) as? List<Pair<String, List<FilterType>>>
                    ?: ArrayList()
            this.selectedFilterTypes = savedInstanceState.getSerializable(ARG_SELECTED_FILTERS) as? HashSet<FilterType>
                    ?: HashSet()
            this.from = savedInstanceState.getSerializable(ARG_FROM) as? LocalDate
                    ?: LocalDate.now().minusDays(6)
            this.to = savedInstanceState.getSerializable(ARG_TO) as? LocalDate ?: LocalDate.now()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ARG_FILTERS, ArrayList(filters))
        outState.putSerializable(ARG_SELECTED_FILTERS, this.selectedFilterTypes)
        outState.putSerializable(ARG_FROM, from)
        outState.putSerializable(ARG_TO, to)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_filter, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    private fun initUI() {
        filterAdapter = FilterAdapter()
        filterAdapter?.sectionType = SectionType.header
        binding.filterRv.adapter = filterAdapter
        binding.filterRv.addItemDecoration(SpaceItemDecoration())
        binding.filterRv.layoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false).also {
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val indexPath = filterAdapter?.getIndexPath(position)
                    if(indexPath != null) {
                        return if (indexPath.type == Type.header) {
                            6
                        } else {
                            val filter = filters[indexPath.section - 1].second[indexPath.row]
                            return if (filter == FilterType.클리어_미포함 || filter == FilterType.클리어_포함)
                                3
                            else
                                2
                        }
                    } else {
                        return 0
                    }
                }
            }
        }
    }

    // 한쪽 필터(ex 오답노트) 컨트롤 중인 경우 어떤 클릭이 다른쪽 필터(ex 즐겨찾기)에도 적용되도록 하는 코드임
    // 다른쪽 필터에 적용되면서도 보기설정은 서로 다르기때문에 그부분만 차이를 두도록 함
    fun setFiltersStatus(filters:HashSet<FilterType>) {
        // 현재 필터 조건에서 공통은 삭제
        selectedFilterTypes.removeAll(selectCommonFilters())
        // 현재 필터 조건에 새로운 공통을 추가
        selectedFilterTypes.addAll(filters.filter { it.commonSet.contains(it) })

        filterAdapter?.notifyDataSetChanged()
    }

    fun selectCommonFilters() : List<FilterType> {
        return selectedFilterTypes.filter { it.commonSet.contains(it) }
    }

    inner class FilterAdapter : SectionAdapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(indexPath: IndexPath): Int {
            return if(indexPath.type == Type.header) {
                if(indexPath.section == 0)
                    2
                else
                    0

            } else {
                1
            }
        }

        override fun numberOfSection(): Int {
            return filters.size + 1
        }

        override fun numberOfRows(section: Int): Int {
            if(section == 0)
                return 0
            return filters[section - 1].second.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            (holder as? CalendarHolder)?.apply {
                set(from, to, type)
                monthContainerCl.setOnClickListener { dialog.show() }
            }

            (holder as? HeaderHolder)?.apply {
                titleTv.text = filters[indexPath.section - 1].first
            }


            (holder as? FilterButtonHolder)?.apply {
                val filter = filters[indexPath.section - 1].second[indexPath.row]
                filterBtn.text = filter.text
                this.isSelected = selectedFilterTypes.contains(filter)
                filterBtn.setOnClickListener {
                    if (isSelected) {
                        selectedFilterTypes.remove(filter)
                    } else {
                        selectedFilterTypes.add(filter)
                        selectedFilterTypes.removeAll(filter.exclusiveSet)
                    }

                    changeSelectedSetAfterRemoved(filter, indexPath.section - 1)
                    listener?.onFilterTypeChanged(this@NoteFilterFragment, selectedFilterTypes)
                    notifyDataSetChanged()

                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                HeaderHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_note_filter_header, parent, false))
            } else if (viewType == 1) {
                FilterButtonHolder(Button(parent.context))
            } else {
                CalendarHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_note_filter_calendar, parent, false))
            }
        }
    }

    private fun changeSelectedSetAfterRemoved(filter: FilterType, section: Int) {
        val siblingFilters = filters[section].second.filter { it != filter }

        for (siblingFilter in siblingFilters) {
            if (selectedFilterTypes.contains(siblingFilter))
                return
        }

        selectedFilterTypes.add(filter)
    }

    inner class SpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val sectionAdapter = parent.adapter as SectionAdapter
            val indexPath = sectionAdapter.getIndexPath(parent.getChildLayoutPosition(view))
            if(indexPath.section == 0)
                return

            if (indexPath.type == Type.header) {
                if (indexPath.section == 0)
                    outRect.top = 24.toPx()
                else
                    outRect.top = 26.toPx()

                outRect.bottom = 6.toPx()
            } else if (indexPath.type == Type.row) {
                outRect.top = 6.toPx()
                outRect.bottom = 6.toPx()

                val filter = filters[indexPath.section - 1].second[indexPath.row]
                if (filter == FilterType.클리어_포함) {
                    outRect.left = 4
                } else if (filter == FilterType.클리어_미포함) {
                    outRect.right = 4
                } else {
                    when {
                        indexPath.row % 3 == 0 -> outRect.right = 4.toPx()
                        indexPath.row % 3 == 1 -> {
                            outRect.left = 2.toPx()
                            outRect.right = 2.toPx()
                        }
                        else -> outRect.left = 4.toPx()
                    }
                }
            }
        }
    }
}

class CalendarHolder(val itemBinding: ItemNoteFilterCalendarBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    val monthContainerCl = itemBinding.monthContainerCl
    val calendarRangeTv = itemBinding.calendarRangeTv

    fun set(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type) {
        when(type) {
            DateRangePickerDialog.Type.RECENT7 -> { calendarRangeTv.text = "최근 7일" }
            DateRangePickerDialog.Type.RECENT14 -> { calendarRangeTv.text = "최근 14일" }
            DateRangePickerDialog.Type.RECENT30 -> { calendarRangeTv.text = "최근 30일" }
            else -> {
                calendarRangeTv.text = "${DateTimeUtils.yyyyMMddFormat.format(from.toDate())}" +
                    " - " +
                    "${DateTimeUtils.yyyyMMddFormat.format(to.toDate())}"
            }
        }
    }
}

class HeaderHolder(val headerBinding: ItemNoteFilterHeaderBinding) : RecyclerView.ViewHolder(headerBinding.root) {
    var titleTv = headerBinding.titleTv
}
