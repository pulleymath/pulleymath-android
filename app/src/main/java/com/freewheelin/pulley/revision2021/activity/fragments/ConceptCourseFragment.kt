package com.freewheelin.pulley.revision2021.activity.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.*
//import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.utils.Preferences
import com.freewheelin.pulley.utils.dpToPx
import com.freewheelin.pulley.databinding.FragmentConceptCourseBinding
import com.freewheelin.pulley.databinding.ItemStudyChapterBinding
import com.freewheelin.pulley.databinding.ItemLargeChapterBinding
import com.freewheelin.pulley.databinding.ItemMediumChapterBinding
import com.freewheelin.pulley.databinding.ItemSmallChapterBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.utils.AnimUtils
import com.freewheelin.pulley.utils.BoongthEffect

class ConceptCourseFragment : LearningTabFragment() {
    companion object {
        fun newInstance() = ConceptCourseFragment()
    }

    lateinit var binding: FragmentConceptCourseBinding

    private lateinit var viewModel: ConceptCourseViewModel

    override var screenName = "개념"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchAvailableSubjects()
        viewModel.fetch()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_concept_course, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ConceptCourseViewModel::class.java)

    }


    override fun initUI() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.showProgress.postValue(true)
            viewModel.subjectIndex.observe(this@ConceptCourseFragment) { selectedSubjectIndex ->

                // TODO vm.subjectIndex 바인딩이 작동하지 않음 왤까
                rvHeaderBinding?.apply {
                    listOf(
                        mathSangBtn,
                        mathHaBtn,
                        math1Btn,
                        math2Btn,
                        mathProbabilityAndStatisticsBtn,
                        mathCalculusBtn,
                        mathKihaBtn
                    )
                        .forEachIndexed { index, button ->
                            when (index) {
                                selectedSubjectIndex -> {
                                    button.let {
                                        it.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.white
                                            )
                                        )
                                        it.setBackgroundResource(R.drawable.bg_study_frag_header_btn_selected_custom_shadow)
//                                        it.elevation = resources.getDimension(R.dimen.dp0)
                                    }

                                }
                                else -> {
                                    button.let {
                                        it.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.gray_600
                                            )
                                        )
                                        it.setBackgroundResource(R.drawable.bg_study_frag_header_btn_common_custom_shadow)
//                                        it.elevation = resources.getDimension(R.dimen.dp5)
                                    }

                                }
                            }
                        }

                }
            }
            studyRv.adapter = StudyChapterAdapter()
            viewModel.fetch()

        }
    }

    inner class StudyChapterAdapter(): ListAdapter<StudyChapter, RecyclerView.ViewHolder>(
        DiffCallback<StudyChapter>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return StudyChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_chapter, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as StudyChapterViewHolder).bind(getItem(position))
        }
    }

    var rvHeaderBinding: ItemStudyChapterBinding ? = null
    inner class StudyChapterViewHolder(private val itemBinding: ItemStudyChapterBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: StudyChapter) {
            if (item.id == -1) rvHeaderBinding = itemBinding
            itemBinding.apply {
                vm = viewModel
                this.item = item
//                lifecycleOwner = viewLifecycleOwner

                val lastChapterIndex = viewModel.chapterList.value?.lastIndex?.minus(1)
                isLastItem = lastChapterIndex?.let { viewModel.chapterList.value?.get(it)?.id } == item.id

                when {
                    item.isHeader -> {
                        headerCl.visibility = View.VISIBLE
                        footerCl.visibility = View.GONE
                        chapterCl.visibility = View.GONE

                    }
                    item.isFooter -> {
                        headerCl.visibility = View.GONE
                        footerCl.visibility = View.VISIBLE
                        chapterCl.visibility = View.GONE
                    }
                    else -> {
                        headerCl.visibility = View.GONE
                        footerCl.visibility = View.GONE
                        chapterCl.visibility = View.VISIBLE
                    }
                }
                val mediumChapterList = listOf(largeChapter.mediumChapter1, largeChapter.mediumChapter2, largeChapter.mediumChapter3, largeChapter.mediumChapter4, largeChapter.mediumChapter5)

                mediumChapterList.forEachIndexed { index, mediumBinding ->
                    if (item.isChildExist(index)) {
                        mediumBinding.smallChapterRv.adapter = SmallChapterListAdapter(item.children[index])
                    }
                }

            }
        }
    }

    inner class SmallChapterListAdapter(private val parentItem: StudyChapter): ListAdapter<StudyChapter, RecyclerView.ViewHolder>(DiffCallback<StudyChapter>()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return SmallChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_small_chapter, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SmallChapterViewHolder).bind(getItem(position), parentItem)
        }
    }

    inner class SmallChapterViewHolder(private val smallItemBinding: ItemSmallChapterBinding): RecyclerView.ViewHolder(smallItemBinding.root), ChapterItemClickListener  {
        fun bind(item: StudyChapter, parent: StudyChapter) {
            smallItemBinding.apply {
//                vm = viewModel
                this.item = item
                lifecycleOwner = viewLifecycleOwner

                listener = this@SmallChapterViewHolder
                isNextItemExist = item.isNextItemExist(parent)
                partIndex = item.getItemPosition(parent)
                smallChapterRootCl.setOnTouchListener(BoongthEffect())
            }
        }

        override fun onItemClick(sc: StudyChapter, partIndex: Int) {
            viewModel.createLearningCourseOnStudentId(sc.id) {
                val chapterId = sc.id
                val name = sc.name
                startActivity(LearningCourseActivity.getIntent(requireContext(), chapterId, name))
            }
        }
    }

    interface ChapterItemClickListener {
        fun onItemClick(sc: StudyChapter, partIndex: Int)
    }
}

@BindingAdapter("bind_study_chapter")
fun bindStudyChapterRecyclerView(recyclerView: RecyclerView, item: List<StudyChapter>?){
    Log.d("bind_study_chapter", "list=$item")
    item?.let { chapterList ->
        val adapter = recyclerView.adapter as? ConceptCourseFragment.StudyChapterAdapter
        adapter?.submitList(chapterList)
    }
}

@BindingAdapter("bind_small_chapter")
fun bindSmallChapterRv(rv: RecyclerView, item: List<StudyChapter>?) {
    Log.d("bind_small_chapter", "list=$item")
    item?.let { chapterList ->
        val adapter = rv.adapter as? ConceptCourseFragment.SmallChapterListAdapter
        adapter?.submitList(chapterList)
    }
}

@BindingAdapter("progress_layout_width")
fun setLayoutWidth(view: View, rate: Double) {
    println("progress_layout_width , view: ${view.id} , rate: ${rate}")
    val layoutParams = view.layoutParams
    layoutParams.width = (rate * 170).toInt().dpToPx()
    view.layoutParams = layoutParams
}