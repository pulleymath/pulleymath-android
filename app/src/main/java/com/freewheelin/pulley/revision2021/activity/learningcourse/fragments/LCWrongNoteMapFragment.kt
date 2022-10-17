package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentLcWrongNoteMapBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity.Companion.STUDY_CHAPTER_FLAG
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCWrongNoteMapViewModel
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.core.manage.ServerStatusManager
import com.freewheelin.pulley.databinding.ItemLcWrongNoteBinding
import com.freewheelin.pulley.revision2021.activity.LCWrongNoteActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.activity.dialog.LCCourseEndDialog
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.utils.AnimUtils
import com.freewheelin.pulley.utils.BoongthEffect
import kotlinx.coroutines.*

class LCWrongNoteMapFragment : Fragment() {
    companion object {
        val COURSE_DESC = "COURSE_DESC"
        fun newInstance() : LCWrongNoteMapFragment {
            return LCWrongNoteMapFragment().apply {
                arguments = Bundle().apply {
//                    putSerializable(STUDY_CHAPTER_FLAG, chapter)
                }
            }
        }
    }

    val binding: FragmentLcWrongNoteMapBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_lc_wrong_note_map, null, false)
    }
    lateinit var viewModel: LCWrongNoteMapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LCWrongNoteMapViewModel::class.java)
        arguments?.let {


            val headerTitle = (activity as LearningCourseActivity).viewModel.headerTitle.value
            viewModel.setChapterHeaderTitle(headerTitle)

            viewModel.showProgress.postValue(true)

            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner

                wrongNoteRv.adapter = WrongNoteAdapter()

                wrongNoteRv.layoutManager = GridLayoutManager(requireContext(), 5, GridLayoutManager.VERTICAL, false).also {
                    it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            val result = when (position) {
                                0 -> 5
                                viewModel.filteredNoteCardList.value?.lastIndex -> 5
                                else -> 1
                            }
                            return result
                        }
                    }
                }


                val exitCallback: () -> Unit = {
                    (activity as LearningCourseActivity).finish()
                }
                val moreStudyCallback: () -> Unit = {
                    (activity as LearningCourseActivity).finish()
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(200)

                        withContext(Dispatchers.Main) {
                            val intent = Intent(PieceManager.EVENT_MOVE_TAB)
                            intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 2)
                            intent.putExtra(PieceManager.EVENT_SCROLL, true)
                            intent.putExtra(PieceManager.EVENT_SCROLL_UNIT_TOTAL_LABEL, true)
                            intent.putExtra(PieceManager.EVENT_FILTER, "확률과 통계")
                            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                        }
                    }
                }

                val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId
                nextStepBtnCl.setOnClickListener {
                    val dialog =
                        LCCourseEndDialog(requireContext(), chapterId, exitCallback, moreStudyCallback)
                    childFragmentManager.let { dialog.show(it, "LCPatternEndDialog") }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val currChapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId
        viewModel.fetchLCWrongNoteInfo(currChapterId)
    }

    inner class WrongNoteAdapter(): ListAdapter<LCWrongNoteMapCard, RecyclerView.ViewHolder>(
        DiffCallback<LCWrongNoteMapCard>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return WrongNoteViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_lc_wrong_note, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as WrongNoteViewHolder).bind(getItem(position))
        }
    }


    inner class WrongNoteViewHolder(private val itemBinding: ItemLcWrongNoteBinding): RecyclerView.ViewHolder(itemBinding.root)
        , NoteCardItemClickListener {


        fun bind(item: LCWrongNoteMapCard) {
            itemBinding.apply {
                vm = viewModel
                this.item = item
                listener = this@WrongNoteViewHolder
                lifecycleOwner = viewLifecycleOwner

                when (item.cardType) {
                    LCWrongNoteMapCard.CardType.Header -> {
                        headerCl.visibility = View.VISIBLE
                        cardCl.visibility = View.GONE
                        footerCl.visibility = View.GONE
                    }
                    LCWrongNoteMapCard.CardType.Card -> {
                        headerCl.visibility = View.GONE
                        cardCl.visibility = View.VISIBLE
                        footerCl.visibility = View.GONE
                        cardCl.setOnTouchListener(BoongthEffect())

                    }
                    LCWrongNoteMapCard.CardType.Footer -> {
                        headerCl.visibility = View.GONE
                        cardCl.visibility = View.GONE
                        footerCl.visibility = View.VISIBLE
                    }
                }
            }
        }

        override fun onItemClick(noteCard: LCWrongNoteMapCard) {
            val filteredNoteCardList = viewModel.filteredNoteCardList.value?.filter {
                it.cardType == LCWrongNoteMapCard.CardType.Card
            }?.let { return@let ArrayList(it) }
            val headerTitle = (activity as LearningCourseActivity).viewModel.headerTitle.value
            val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId
            startActivity(LCWrongNoteActivity.getIntent(requireContext(), filteredNoteCardList, noteCard, headerTitle, chapterId))

        }
    }

    interface NoteCardItemClickListener {
        fun onItemClick(item: LCWrongNoteMapCard)
    }
}

@BindingAdapter("bind_lc_wrong_note_card")
fun bindLCWrongNoteCardRecyclerView(recyclerView: RecyclerView, item: List<LCWrongNoteMapCard>?){
    Log.d("bind_lc_wrong_note_card", "list=$item")
    item?.let { reviewList ->
        val adapter = recyclerView.adapter as LCWrongNoteMapFragment.WrongNoteAdapter
        adapter.submitList(reviewList)
    }
}
