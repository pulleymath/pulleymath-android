package com.freewheelin.pulley.revision2021.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.AppUsageMonitor
import com.freewheelin.pulley.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.databinding.ActivityLcWrongNoteBinding
import com.freewheelin.pulley.databinding.ItemLcWrongNoteSelectorBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCWrongNoteFragment
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.viewmodel.LCWrongNoteAViewModel
import com.freewheelin.pulley.revision2021.views.CookingPencilcase
import kotlinx.coroutines.*

class LCWrongNoteActivity : AppCompatActivity() {
    companion object {
        val NOTE_CARD_LIST = "NOTE_CARD_LIST"
        val NOTE_CARD_ITEM = "NOTE_CARD_ITEM"
        val HEADER_TITLE = "HEADER_TITLE"
        val CURR_CHAPTER = "CURR_CHAPTER"
        fun getIntent(context: Context, list: ArrayList<LCWrongNoteMapCard>?, item: LCWrongNoteMapCard, headerTitle: String?, chapterId: Int?) : Intent {
            return Intent(context, LCWrongNoteActivity::class.java).apply {
                putExtra(NOTE_CARD_LIST, list)
                putExtra(NOTE_CARD_ITEM, item)
                putExtra(CURR_CHAPTER, chapterId)
                putExtra(HEADER_TITLE, headerTitle)

            }
        }
    }

    val binding: ActivityLcWrongNoteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_lc_wrong_note,null,false)
    }
    val viewModel: LCWrongNoteAViewModel by viewModels()

    private var tabFragments: MutableList<Fragment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideSystemUI()
        ConceptLearningUsageMonitor.startConceptLearningUsage()

        val noteCardList: ArrayList<LCWrongNoteMapCard> = intent.getSerializableExtra(NOTE_CARD_LIST) as ArrayList<LCWrongNoteMapCard>
        val noteCardItem = intent.getSerializableExtra(NOTE_CARD_ITEM) as LCWrongNoteMapCard
        val chapterId = intent.getIntExtra(CURR_CHAPTER, -1)
        val title = intent.getStringExtra(HEADER_TITLE) ?: ""

        binding.apply {
            vm = viewModel
            lifecycleOwner = this@LCWrongNoteActivity

            viewModel.init(noteCardList, noteCardItem, title, chapterId)
            val frags = noteCardList.map {
                return@map LCWrongNoteFragment.newInstance(it)
            }

            tabFragments.addAll(frags)

            pagerWrapper.pager.adapter = WrongNotePagerAdapter(tabFragments, supportFragmentManager, lifecycle)
            pagerWrapper.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    moveSelectorOnPosition(position)
                    setPagerIndexes(position)
                }

                private fun setPagerIndexes(position: Int) {

                    viewModel.apply {
                        val pagerLastIndex = filteredNoteCardList.value?.lastIndex
                        isPagerFirstIndex.postValue(position == 0)
                        isPagerLastIndex.postValue(position == pagerLastIndex)
                    }
                }
            })

            noteSelectorRv.adapter = NoteNumberListAdapter()
            noteSelectorRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val isEndPosition = !recyclerView.canScrollHorizontally(1)
                    viewModel.isNoteSelectorScrollPositionEnd.postValue(isEndPosition)
                }
            })

            conceptSolutionToggleBtn.setOnClickListener {
                val children = supportFragmentManager.fragments.filter { it.tag.equals("f" + pagerWrapper.pager.adapter?.getItemId(pagerWrapper.pager.currentItem)) }
                children.forEach {
                    (it as LCWrongNoteFragment).toggleDrawer()
                }
//                val children = supportFragmentManager.fragments.filter { it.tag.equals("f" + newPager.adapter?.getItemId(newPager.currentItem)) }
//                children.forEach {
//                    (it as LCWrongNoteFragment).toggleDrawer()
//                }
            }

            appendHintBtn.setOnClickListener {
                viewModel.remainingHintSize.value?.let { hintSize ->
                    val nextHintSize = hintSize - 1
                    viewModel.setHintBtnText(nextHintSize)

                    val children = getChildrenPage()

                    children.forEach {
                        val quizFrag = (it as LCWrongNoteFragment)
                        if (quizFrag.hasMoreHint()) {
                            quizFrag.setNextHint(nextHintSize)
                        }
                    }
                }
            }

            resetHintBtnLl.setOnClickListener {
                viewModel.resetHint()

                val children = getChildrenPage()
                children.forEach {
                    val quizFrag = (it as LCWrongNoteFragment)
                    quizFrag.viewModel.resetQuizImage()
                }
            }

            backBtn.setOnClickListener {
                onBackPressed()
            }
            headerCl.setOnClickListener {
                hidePencilcasePanel()
            }
            subHeaderCl.setOnClickListener {
                hidePencilcasePanel()
            }

            navPrevBtn.setOnClickListener {
                val pagerIndex = binding.pagerWrapper.pager.currentItem
                if (pagerIndex == 0) {
                    Toast.makeText(this@LCWrongNoteActivity, "첫 페이지입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.pagerWrapper.pager.currentItem = binding.pagerWrapper.pager.currentItem - 1
                }
            }

            navNextBtn.setOnClickListener {
                val pagerIndex = binding.pagerWrapper.pager.currentItem
                viewModel.filteredNoteCardList.value?.let {
                    if (it.lastIndex == pagerIndex) {
                        Toast.makeText(this@LCWrongNoteActivity, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.pagerWrapper.pager.currentItem = binding.pagerWrapper.pager.currentItem + 1
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ConceptLearningUsageMonitor.startConceptLearning(viewModel.selectedChapterId)

        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            withContext(Dispatchers.Main) {
                goInitialPosition()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ConceptLearningUsageMonitor.pauseConceptLearning()
    }
    fun goInitialPosition() {
        val list = viewModel.filteredNoteCardList.value
        val noteItem = viewModel.currNoteCard.value
        list?.forEachIndexed { index, card ->
            if (card.getId() == noteItem?.getId()) {
                moveSelectorOnPosition(index)
                binding.pagerWrapper.pager.currentItem = index
                return@forEachIndexed
            }
        }
    }


    fun moveSelectorOnPosition (pos: Int) {
        binding.apply {
            viewModel.currentCardIndex.postValue(pos)
            noteSelectorRv.scrollToPosition(pos)

            val isEndPosition = viewModel.filteredNoteCardList.value?.lastIndex == pos
            viewModel.isNoteSelectorScrollPositionEnd.postValue(isEndPosition)

            when (pos) {
                0 -> {
                    pagerWrapper.setStartIndex()
                }
                tabFragments.lastIndex -> {
                    pagerWrapper.setEndIndex()
                }
                else -> {
                    pagerWrapper.setMiddleIndex()
                }
            }
        }
    }
    fun getChildrenPage() = supportFragmentManager.fragments.filter {
        it.tag.equals(
            "f" + binding.pagerWrapper.pager.adapter?.getItemId(binding.pagerWrapper.pager.currentItem)
        )
//        it.tag.equals(
//            "f" + binding.newPager.adapter?.getItemId(binding.newPager.currentItem)
//        )
    }

    fun scoringPatternQuiz(scoring: LCPatternScoring) {
        viewModel.filteredNoteCardList.value?.forEach { quiz ->
            if (quiz.refPatternQuizId == scoring.patternQuizId) {
                quiz.isCorrect = scoring.isCorrect
                quiz.isAnswerSubmitted.set(true)
                viewModel.forceUpdateNoteCardList()
            }
        }
    }
    fun hidePencilcasePanel() {
        binding.pencilcaseView.pencilOptionLl.isSelected = false
        binding.pencilcaseView.pencilOptionLl.visibility = View.GONE
        binding.pencilcaseView.clearAllBtn.isSelected = false
        binding.pencilcaseView.clearAllBtn.visibility = View.GONE
    }

    fun setQuizImageScale(scale: Float) {
        binding.pagerWrapper.scaleFactor = scale
    }
    fun setPagerUserInputEnabled(enabled: Boolean) {
        binding.pagerWrapper.pager.isUserInputEnabled = enabled
    }
    fun setHintBtn(flag: Boolean?, size: Int?) {
        if (flag != null && size != null) {
            viewModel.isHintBtnDisabled.postValue(flag)
            viewModel.setHintBtnText(size)
        }
    }

    fun savePencilcaseType(type: CookingPencilcase.EditType?) {
        viewModel.pencilcaseType = type
    }
    fun getPencilcaseType(): CookingPencilcase.EditType? {
        return viewModel.pencilcaseType
    }
    fun savePencilcaseColor(color: CookingPencilcase.PenColor) {
        viewModel.pencilcaseColor = color
    }
    fun getPencilcaseColor(): CookingPencilcase.PenColor? {
        return viewModel.pencilcaseColor
    }

    fun savePencilcaseThicknesss(thickness: CookingPencilcase.Thickness) {
        viewModel.pencilcaseThickness = thickness
    }
    fun getPencilcaseThickness(): CookingPencilcase.Thickness? {
        return viewModel.pencilcaseThickness
    }
    fun savePencilcaseMode(isFixedMode: Boolean) {
        viewModel.pencilcaseModeFixed = isFixedMode
    }
    fun getPencilcaseMode(): Boolean {
        return viewModel.pencilcaseModeFixed
    }

    fun setPagerSwipeBlocked(blocked: Boolean) {
        binding.pagerWrapper.isPagerSwipeBlocked = blocked
    }
    fun setConceptSolutionToggleBtnText(isOpened: Boolean) {
        binding.conceptSolutionToggleBtn.text = if (isOpened) "개념 | 정답 닫기" else "개념 | 정답 보기"
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
        }
    }
    inner class WrongNotePagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

    inner class NoteNumberListAdapter(): ListAdapter<LCWrongNoteMapCard, WrongNoteSelectorViewHolder>(
        DiffCallback<LCWrongNoteMapCard>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WrongNoteSelectorViewHolder {
            return WrongNoteSelectorViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_lc_wrong_note_selector,
                    parent,
                    false
                )
            )


        }

        override fun onBindViewHolder(holder: WrongNoteSelectorViewHolder, position: Int) {
            holder.bind(getItem(position), position)
        }
    }

    inner class WrongNoteSelectorViewHolder(private val itemBinding: ItemLcWrongNoteSelectorBinding): RecyclerView.ViewHolder(itemBinding.root),
        WrongNoteSelectorItemClickListener {

        fun bind(item: LCWrongNoteMapCard, position: Int) {
            itemBinding.apply {
                listener = this@WrongNoteSelectorViewHolder
                lifecycleOwner = this@LCWrongNoteActivity
                this.item = item
                vm = viewModel
                this.position = position
                isLastPosition = viewModel.filteredNoteCardList.value?.lastIndex == position
            }
        }

        override fun onItemClick(position: Int) {
            binding.pagerWrapper.pager.currentItem = position
//            binding.newPager.currentItem = position
            viewModel.currentCardIndex.postValue(position)
        }
    }
    interface WrongNoteSelectorItemClickListener {
        fun onItemClick(position: Int)
    }

}


@BindingAdapter("bind_note_selector")
fun bindNoteSelectorRecyclerView(recyclerView: RecyclerView, item: List<LCWrongNoteMapCard>?){
    Log.d("bind_note_selector", "list=$item")
    item?.let { cardList ->
        val adapter = recyclerView.adapter as LCWrongNoteActivity.NoteNumberListAdapter
        adapter.submitList(cardList)
    }
}
