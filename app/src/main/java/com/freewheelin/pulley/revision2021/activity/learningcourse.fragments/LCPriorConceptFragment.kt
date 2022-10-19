package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCPriorConceptViewModel
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.analysis.component.StudyListViewHolder
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.LCPriorConceptInfo
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.views.LabelFlowView
//import com.freewheelin.pulley.revision2021.views.LCPatternDetailDialog
import com.freewheelin.pulley.utils.Preferences
import com.zoyi.channel.plugin.android.ChannelIO
import kotlinx.coroutines.*

class LCPriorConceptFragment : Fragment() {

    companion object {
        val CHAPTER_NAME = "CHAPTER_NAME"
        fun newInstance(chapterName: String): LCPriorConceptFragment {
            return LCPriorConceptFragment().apply {
                arguments = Bundle().apply {
                    putString(CHAPTER_NAME, chapterName)
                }
            }
        }
    }

    val binding: FragmentLearnCoursePriorConceptBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_learn_course_prior_concept, null, false)
    }

    override fun onResume() {
        super.onResume()

    }

    private lateinit var viewModel: LCPriorConceptViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LCPriorConceptViewModel::class.java)
        arguments?.let {
            val chapterName = it.getString(CHAPTER_NAME) ?: ""
            val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId
            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner

                viewModel.fetchPriorConcept(chapterName, chapterId)

                priorConceptRv.adapter = PriorConceptCardListAdapter()

                priorConceptBottomNextBtnWrapperLl.setOnClickListener {
                    (activity as LearningCourseActivity).setPagerToCookingFirstPage()
                }

                viewModel.priorConceptCount.observe(viewLifecycleOwner) {
//                    if (it == 0) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            delay(2000)
//                            withContext(Dispatchers.Main) {
//                                (activity as LearningCourseActivity).setPagerToCookingFirstPage()
//                            }
//                        }
//                    }
                }

            }
        }
    }

    inner class PriorConceptCardListAdapter(): ListAdapter<LCPriorConceptInfo, PriorConceptCardViewHolder>(
        DiffCallback<LCPriorConceptInfo>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriorConceptCardViewHolder {
            return PriorConceptCardViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_prior_concept_card,
                    parent,
                    false
                )
            )


        }

        override fun onBindViewHolder(holder: PriorConceptCardViewHolder, position: Int) {
            holder.bind(getItem(position), position)
        }
    }

    inner class PriorConceptCardViewHolder(private val binding: ItemPriorConceptCardBinding): RecyclerView.ViewHolder(binding.root),
        PriorConceptItemClickListener {

        fun bind(item: LCPriorConceptInfo, position: Int) {
            binding.apply {
                listener = this@PriorConceptCardViewHolder
                this.item = item
                vm = viewModel

                item.tags.forEach {
                    if (labelFlowLayout.childCount < item.tags.size) {
                        val label = LabelFlowView(requireContext(), it, "c0c0c0")
                        label.load()
                        labelFlowLayout.addView(label)
                    }
                }
            }
        }

        override fun onItemClick(info: LCPriorConceptInfo) {
//            Toast.makeText(requireContext(), "사전개념 학습", Toast.LENGTH_SHORT).show()


            val chapterId = info.priorConceptChapterId
            viewModel.createLearningCourseOnStudentId(chapterId) {
                startActivity(LearningCourseActivity.getIntent(requireContext(), info))

            }
        }
    }
    interface PriorConceptItemClickListener {
        fun onItemClick(info: LCPriorConceptInfo)
    }
}

@BindingAdapter("bind_prior_concept_card")
fun bindPriorConceptCardRecyclerView(recyclerView: RecyclerView, item: List<LCPriorConceptInfo>?){
    Log.d("bind_prior_concept_card", "list=$item")
    item?.let { priorConceptList ->
        val adapter = recyclerView.adapter as LCPriorConceptFragment.PriorConceptCardListAdapter
        adapter.submitList(priorConceptList)
    }
}
