package com.freewheelin.pulley.legacy.activities.learning.tabFragment.wrongNote.component

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.FragmentNoteFilterBinding
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialog
import com.freewheelin.pulley.legacy.dialogs.DateRangePickerDialogListener
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.revision2023.ui.activity.WrongNoteActivity
import com.freewheelin.pulley.revision2023.ui.adapter.BookFilterAdapter
import com.freewheelin.pulley.revision2023.ui.fragment.WrongNoteStudyFragment
import com.freewheelin.pulley.revision2023.utils.listeners.BookFilterItemListener
import com.freewheelin.pulley.revision2023.viewmodel.WrongNoteFragViewModel
import org.joda.time.LocalDate
import java.io.Serializable

interface NoteFilterFragmentListener : Serializable {
    fun onFilterTypeChanged(fragment: NoteFilterFragment, filters: Set<LearningFilterType>)
    fun onDateSet(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type)
}

interface NoteFilterChangeListener : Serializable {
    fun onFilterTypeChanged(fragment: NoteFilterFragment, filters: Set<LearningFilterType>)
    fun onDateChanged(from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type)
}

class NoteFilterFragment : Fragment() {
    var listener: NoteFilterFragmentListener? = null
    var changeListener: NoteFilterChangeListener? = null
    val viewModel: WrongNoteFragViewModel by viewModels()

    lateinit var binding: FragmentNoteFilterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_filter, container, false)
        return binding.root
    }

    lateinit var filterAdapter: BookFilterAdapter
    var noteType: NoteFilterType = NoteFilterType.Wrong

    companion object {
        const val ARG_FILTERS = "FILTER_TYPES"
        const val ARG_SELECTED_FILTERS = "SELECTED FILTERS"
        const val ARG_FROM = "ARG_FROM"
        const val ARG_TO = "ARG_TO"
        enum class NoteFilterType {
            Wrong, Scrap
        }
        @JvmStatic
        fun newWrongInstance(): NoteFilterFragment {
            return NoteFilterFragment().also {
                it.noteType = NoteFilterType.Wrong
            }
        }
        @JvmStatic
        fun newScrapInstance(): NoteFilterFragment {
            return NoteFilterFragment().also {
                it.noteType = NoteFilterType.Scrap
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBookFilterParent()
        updateBookFilter()
    }

    fun initBookFilterParent() {
        val filterParent = when (noteType) {
            NoteFilterType.Wrong -> BookFilterParent.WRONG_NOTE
            NoteFilterType.Scrap -> BookFilterParent.SCRAP_NOTE
        }

        viewModel.bookFilterParent = filterParent
    }

    fun updateBookFilter() {
        viewModel.fetchBookFilter()
    }

    var isViewCreated = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        initUI()
        initObserve()
    }

    fun updateParentFilters() {
        if (isViewCreated) {
            (activity as? WrongNoteActivity)?.updateFilter(viewModel.selectedFilterTypes)
        }
    }
    private fun initUI() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            updateParentFilters()
            filterAdapter = BookFilterAdapter (object : BookFilterItemListener {
                override fun onToggle(isChecked: Boolean) {}

                override fun onFilterItemClick(item: BookFilterElement) {
                    viewModel.onFilterItemClick(item) { filters ->
                        changeListener?.onFilterTypeChanged(this@NoteFilterFragment, filters)
                    }
                }

                override fun onCalendar() {
                    dialog.show()
                }
            })
            filterRv.layoutManager = GridLayoutManager(requireContext(), 6).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        viewModel.filterElements.value?.let { list ->
                            return when (list[position].type) {
                                BookFilterElement.Type.Item -> {
                                    if (list[position].filterType == LearningFilterType.보기설정_클리어_미포함 ||
                                        list[position].filterType == LearningFilterType.보기설정_클리어_포함) {
                                        3
                                    } else {
                                        2
                                    }
                                }
                                else -> 6
                            }
                        }
                        return 1
                    }
                }
            }
            filterRv.adapter = filterAdapter
        }
    }

    private fun initObserve() {
        viewModel.apply {
            filterElements.observe(viewLifecycleOwner) {
                filterAdapter.submitList(it)
            }

        }

    }

    val dialog: DateRangePickerDialog by lazy {
        val pickerDialog = DateRangePickerDialog(requireContext(), viewModel.from, viewModel.to)
        pickerDialog.listener = object: DateRangePickerDialogListener {
            override fun onUpdateClicked(picker: DateRangePickerDialog, from: LocalDate, to: LocalDate, type: DateRangePickerDialog.Type) {
                val fromDate = when(type) {
                    DateRangePickerDialog.Type.RECENT7 -> LocalDate.now().minusDays(6)
                    DateRangePickerDialog.Type.RECENT14 -> LocalDate.now().minusDays(13)
                    DateRangePickerDialog.Type.RECENT30 -> LocalDate.now().minusDays(29)
                    else -> from
                }
                viewModel.from = fromDate

                val toDate = when(type) {
                    DateRangePickerDialog.Type.RECENT7, DateRangePickerDialog.Type.RECENT14, DateRangePickerDialog.Type.RECENT30 -> LocalDate.now()
                    else -> to
                }
                viewModel.to = toDate
                viewModel.datePickerType = type

                viewModel.updateCalendarElement()
                changeListener?.onDateChanged(fromDate, toDate, type)
            }
        }
        pickerDialog
    }
}