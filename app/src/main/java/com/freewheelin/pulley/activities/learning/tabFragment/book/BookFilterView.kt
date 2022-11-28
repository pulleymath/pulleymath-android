package com.freewheelin.pulley.activities.learning.tabFragment.book

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.R.*
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.Type
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterType.*
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component.FilterButtonHolder
import com.freewheelin.pulley.activities.learning.tabFragment.wrongNote.component.HeaderHolder
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ItemFilterSwitchBinding
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx
import com.ht.RecyclerAdapters.SectionAdapter.SectionType


enum class FilterType {
    계열_전체,
    계열_공통,
    계열_가형,
    계열_나형,

    과목_전체,
    과목_수학_상,
    과목_수학_하,
    과목_수학1,
    과목_수학2,
    과목_확통,
    과목_미적분,
    과목_기하,

    유형_전체,
    유형_유형서,
    유형_내신서,
    유형_기출서,

    추천_전체,
    추천_1등급,
    추천_2_3등급,
    추천_3_4등급,
    추천_4등급이하,

    핀_포함,
    핀_미포함,

    워크북_포함,
    워크북_미포함;

    val text: String
    get() {
        return when(this) {
            계열_전체, 과목_전체, 추천_전체, 유형_전체 -> "전체"
            계열_공통 -> "공통"
            계열_가형 -> "가형"
            계열_나형 -> "나형"

            과목_수학_상 -> "수학(상)"
            과목_수학_하 -> "수학(하)"
            과목_수학1 -> "수학1"
            과목_수학2 -> "수학2"
            과목_확통 -> "확률과 통계"
            과목_미적분 -> "미적분"
            과목_기하 -> "기하"

            유형_유형서 -> "유형서"
            유형_내신서 -> "내신서"
            유형_기출서 -> "기출서"

            추천_1등급 -> "1등급"
            추천_2_3등급 -> "2-3등급"
            추천_3_4등급 -> "3-4등급"
            추천_4등급이하 -> "4등급 이하"

            핀_포함 -> "핀 설정한 문제집 포함"
            핀_미포함 -> "핀 설정한 문제집 미포함"

            워크북_포함 -> "워크북 포함"
            워크북_미포함 -> "워크북 미포함"
        }
    }

    val exclusiveSet: Set<FilterType>
        get() {
            return when(this) {
                계열_전체 -> return setOf(계열_가형, 계열_나형, 계열_공통)
                계열_가형 -> return setOf(계열_전체)
                계열_나형 -> return setOf(계열_전체)
                계열_공통 -> return setOf(계열_전체)

                과목_전체 -> return setOf(과목_수학_상, 과목_수학_하, 과목_수학1, 과목_수학2, 과목_확통, 과목_미적분, 과목_기하)
                과목_수학_상 -> return setOf(과목_전체)
                과목_수학_하 -> return setOf(과목_전체)
                과목_수학1 -> return setOf(과목_전체)
                과목_수학2 -> return setOf(과목_전체)
                과목_확통 -> return setOf(과목_전체)
                과목_미적분 -> return setOf(과목_전체)
                과목_기하 -> return setOf(과목_전체)

                유형_전체 -> setOf(유형_유형서, 유형_내신서, 유형_기출서)
                유형_유형서 -> setOf(유형_전체)
                유형_내신서 -> setOf(유형_전체)
                유형_기출서 -> setOf(유형_전체)

                추천_전체 -> return setOf(추천_1등급, 추천_2_3등급, 추천_3_4등급, 추천_4등급이하)
                추천_1등급 -> return setOf(추천_전체)
                추천_2_3등급 -> return setOf(추천_전체)
                추천_3_4등급 -> return setOf(추천_전체)
                추천_4등급이하 -> return setOf(추천_전체)

                핀_포함 -> return setOf(핀_미포함)
                핀_미포함 -> return setOf(핀_포함)

                워크북_포함 -> return setOf(워크북_미포함)
                워크북_미포함 -> return setOf(워크북_포함)
            }
        }

    val eventValue: String
        get() {
            return when(this) {
                과목_확통 -> "확률과통계"
                핀_포함 -> "핀포함"
                핀_미포함 -> "핀미포함"
                워크북_포함 -> "워크북 포함"
                워크북_미포함 -> "워크북 미포함"
                else -> text
        }
    }
}

enum class FilterOrder {
    SUBJECT,
    LEVEL,
    SERIES,
    LAST,
    PAST,
    DEFAULT;

    val text: String
        get() {
            return this.toString()
        }
}
enum class FilterCategory {
    BASIC,
    MO, // 모의고사
    BOOK,   // 유형학습
    TEST,   // 테스트
    NOTE,   // 오답학습
    REFERENCE,
    COMMERCIAL, // 시중교재
    CUSTOM_BOOK,    // 워크북
    RECOMMEND;   // 풀리에서만든 추천 학습지
    val text: String
        get() {
            return this.toString()
        }
}
interface BookFilterListener {
    fun onFilterTypeChanged(view: BookFilterView, filters: Set<FilterType>)
}

class BookFilterView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    val filters = listOf(
            Pair("보기설정" , listOf(핀_미포함)), // 워크북은 유료화때문에 book filter에서 노출되지 않음
            Pair("과목" , listOf(과목_전체, 과목_수학_상, 과목_수학_하, 과목_수학1, 과목_수학2, 과목_확통, 과목_미적분, 과목_기하)),
            Pair("문제집 유형" , listOf(유형_전체, 유형_유형서, 유형_내신서, 유형_기출서)),
            Pair("추천등급" , listOf(추천_전체, 추천_1등급, 추천_2_3등급, 추천_3_4등급, 추천_4등급이하))
    )

    var selectedFilterTypes: HashSet<FilterType> = hashSetOf(
            워크북_미포함, 핀_미포함, 계열_전체, 과목_전체, 유형_전체, 추천_전체
    )

    var listener: BookFilterListener? = null

    init {
        val filterAdatper = FilterAdapter()
        filterAdatper.sectionType = SectionType.header

        adapter = filterAdatper
        addItemDecoration(SpaceItemDecoration())
        layoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false).also {
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val indexPath = filterAdatper.getIndexPath(position)
                    return if (indexPath.type == Type.header || indexPath.section == 0) {
                        6
                    } else {
                        2
                    }
                }
            }
        }
    }

    inner class FilterAdapter: SectionAdapter<ViewHolder>() {

        override fun getItemViewType(indexPath: IndexPath): Int {
            return if (indexPath.type == Type.header) {
                0
            } else if(indexPath.section == 0){
                1
            } else {
                2
            }
        }

        override fun numberOfSection(): Int {
            return filters.size
        }

        override fun numberOfRows(section: Int): Int {
            return filters[section].second.size
        }

        override fun onBindViewHolder(holder: ViewHolder, indexPath: IndexPath) {
            (holder as? HeaderHolder)?.apply {
                if(indexPath.section == 0) {
                    titleTv.height = 0
                } else
                    titleTv.text = filters[indexPath.section].first
            }
            (holder as? FilterButtonHolder)?.apply {
                val filter = filters[indexPath.section].second[indexPath.row]
                filterBtn.text = filter.text
                this.isSelected = selectedFilterTypes.contains(filter)

                filterBtn.setOnClickListener {
                    if(isSelected)
                        selectedFilterTypes.remove(filter)
                    else {
                        selectedFilterTypes.add(filter)
                        selectedFilterTypes.removeAll(filter.exclusiveSet)
                    }
                    changeSelectedSetAfterRemoved(filter, indexPath.section)
                    notifyDataSetChanged()
                    val itemName = when(indexPath.section) {
                        0 -> "필터-보기설정"
                        1 -> "필터-과목"
                        2 -> "필터-문제집 유형"
                        3 -> "필터-추천등급"
                        else -> ""
//                        else -> "필터-보기설정"
                    }
                    LogUtils.logEvent(context!!, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", itemName, filter.eventValue)
                    listener?.onFilterTypeChanged(this@BookFilterView, selectedFilterTypes)
                }
            }
            (holder as? FilterSwitchHolder)?.apply {
                val row = indexPath.row

                if(row == 0)
                    holder.text = "핀 설정한 문제집 포함"
                else
                    holder.text = "워크북 포함"

                itemBinding.filterSwitch.setOnCheckedChangeListener(null)

                if(row == 0)
                    itemBinding.filterSwitch.isChecked = selectedFilterTypes.contains(핀_포함)
                else
                    itemBinding.filterSwitch.isChecked = selectedFilterTypes.contains(워크북_포함)

                itemBinding.filterSwitch.setOnCheckedChangeListener { button, isChecked ->
                    val willAddFilter = if(isChecked) {
                        if(row == 0) 핀_포함
                        else 워크북_포함
                    } else {
                        if(row == 0)핀_미포함
                        else 워크북_미포함
                    }

                    val willRemoveFilter = willAddFilter.exclusiveSet

                    selectedFilterTypes.add(willAddFilter)
                    selectedFilterTypes.removeAll(willRemoveFilter)

                    LogUtils.logEvent(context!!, user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "필터-보기설정", willAddFilter.eventValue)

                    listener?.onFilterTypeChanged(this@BookFilterView, selectedFilterTypes)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            if (viewType == 0) {
                return HeaderHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_note_filter_header, parent, false))
            } else if(viewType == 2) {
                return FilterButtonHolder(Button(parent.context))
            } else {
                return FilterSwitchHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_filter_switch, parent, false))
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

    }

    inner class SpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val sectionAdapter = parent.adapter as SectionAdapter
            val indexPath = sectionAdapter.getIndexPath(parent.getChildLayoutPosition(view))

            if (indexPath.type == Type.header) {
                if (indexPath.section == 0)
                    outRect.top = 24.toPx()
                else
                    outRect.top = 26.toPx()

                outRect.bottom = 4.toPx()
            } else if (indexPath.type == Type.row) {
                outRect.top = 4.toPx()
                outRect.bottom = 4.toPx()

                val filter = filters[indexPath.section].second[indexPath.row]
                if (filter == 핀_미포함) {
                    outRect.left = 2.toPx()
                } else if (filter == 핀_포함) {
                    outRect.left = 2.toPx()
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

                if(indexPath.section == 0) {
                    outRect.top = 0
                    outRect.bottom = 0
                }
            }
        }
    }
}

class FilterSwitchHolder(val itemBinding: ItemFilterSwitchBinding): RecyclerView.ViewHolder(itemBinding.root) {
    var text: String = "필터"
        set(value) {
            field = value
            itemBinding.textView.text = field
        }
} 