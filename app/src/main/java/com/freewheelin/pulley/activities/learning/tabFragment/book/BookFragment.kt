package com.freewheelin.pulley.activities.learning.tabFragment.book


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.core.manage.BookManager
import com.freewheelin.pulley.core.manage.ServerStatusManager
import com.freewheelin.pulley.databinding.FragmentBookBinding
import com.freewheelin.pulley.databinding.TooltipAnalysisBinding
import com.freewheelin.pulley.dialogs.*
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.contents.ClientBookType
import com.freewheelin.pulley.revision2021.activity.PdfListActivity
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.GridMarginDecoration
import com.freewheelin.pulley.views.MarginDecoration
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow
import kotlinx.coroutines.*

class BookFragment : LearningTabFragment(), PlanListener, EmailInputDialogListener, BookFilterListener, CustomizeBookDialogListener {

    lateinit var binding: FragmentBookBinding

    var myBooks: MyBookList? = null
    var totalBooks: MutableList<Book>? = null
    lateinit var recommendBookListViews: List<RecommendBookList>
//        get() = listOf(firstRecommendList, secondRecommendList, thirdRecommendList, fourthRecommendList)

    var isStartWithInitTest = false

    companion object {
        fun newInstance() = BookFragment()
    }

    override var screenName = "유형학습"
    lateinit var scoreReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scoreReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                getMyPlanList {
                    getRecommendList {
                        getTotalList()
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(scoreReceiver, IntentFilter(BookManager.EVENT_BOOK_SCORING))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(scoreReceiver)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isStartWithInitTest) {
            initUI()
            wasInitUI = true
        }
    }

    override fun initUI() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner

            myPlanRv.adapter = MyPlanAdapter()
            myPlanRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            commercialBookLayout.setOnClickListener {
                Intent(requireContext(), PdfListActivity::class.java).let {
                    startActivity(it)
                }
            }

            iconLock.visibility = if (user!!.hasPulleyPlus) {
                View.GONE
            } else {
                View.VISIBLE
            }
            workbookBtn.setOnClickListener {
                LogUtils.logEvent(
                    requireContext(),
                    user!!,
                    PulleyEvent.BUTTON_CLICK,
                    "유형학습",
                    "전체-워크북만들기"
                )
                if (user!!.hasPulleyPlus) {
                    CustomizeBookDialog(requireContext(), this@BookFragment).show()
                } else {
                    DialogUtils.confirmHasPulleyPlus(requireContext()) {
                        PulleyPlusPriceDialog(requireContext()).show()
                    }
                }
            }

            totalRv.layoutManager = GridLayoutManager(context, 3)
            totalRv.adapter = TotalPlanAdapter()
            val totalRvAnimationController = AnimationUtils.loadLayoutAnimation(
                requireContext(),
                R.anim.recyclerview_grid_layout_animation
            )
            totalRv.layoutAnimation = totalRvAnimationController
            totalRv.addItemDecoration(GridMarginDecoration(16.toPx(), 0, 3))
            totalRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            LogUtils.logEvent(
                                requireContext(),
                                user!!,
                                PulleyEvent.BUTTON_CLICK,
                                "유형학습",
                                "스크롤",
                                "전체플랜"
                            )
                        }
                    }
                }
            })

            val itemSpace = resources.getDimension(R.dimen.dp16).toInt()
            myPlanRv.addItemDecoration(MarginDecoration(itemSpace))
            myPlanRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            LogUtils.logEvent(
                                requireContext(),
                                user!!,
                                PulleyEvent.BUTTON_CLICK,
                                "유형학습",
                                "스와이프",
                                "나의플랜"
                            )
                        }
                    }
                }
            })
            myPlanDeleteGuideTv.extensionTouchArea(12.toPx())
            myPlanDeleteGuideTv.setOnClickListener {
                LogUtils.logEvent(
                    requireContext(),
                    user!!,
                    PulleyEvent.BUTTON_CLICK,
                    "유형학습",
                    "삭제기준보기"
                )
                val window =
                    BalloonWindow(requireContext(), it, BalloonWindow.Position.below, 16.toPx())
                window.balloonColor = ContextCompat.getColor(requireContext(), R.color.purple_ACACFF)
                window.offset = if (context?.is10InchUI == true) -240 else -190
                window.setPadding(if (context?.is10InchUI == true) 32.toPx() else 24.toPx())
                val tooltipBinding: TooltipAnalysisBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.tooltip_analysis, null, false)
//                val view = LayoutInflater.from(requireContext()).inflate(R.layout.tooltip_analysis, null)
                tooltipBinding.chartTopTv.text = "삭제 기준"
                tooltipBinding.chartContentTv.text = "- 최근 30일 동안 학습하지 않은 플랜은 [나의플랜]에서 자동으로 빠집니다.\n" +
                    "   그렇게 빠진 플랜은 [전체플랜]에서 다시 볼 수 있습니다.\n" +
                    "\n" +
                    "- 워크북 플랜의 경우,\n" +
                    "   채점한 문제가 총 2문제 이하이고 최근 30일 동안 학습하지 않았다면 \n   영구 삭제됩니다.\n" +
                    "\n" +
                    "- 핀을 꽂아둔 모든 플랜은 빠지거나 삭제되지 않습니다."

                window.show(tooltipBinding.root)
            }

            planLoadingView.playAnimation()
            Handler(Looper.getMainLooper()).postDelayed({
                getMyPlanList {
                    getRecommendList {
//                    Tutor.showToolTipIfNeed(recommendLabel, Tutor.TooltipType.recommendPlan)
                        getTotalList()
                    }
                }
            }, 200)

            recommendBookListViews = listOf(firstRecommendList, secondRecommendList, thirdRecommendList, fourthRecommendList)
            recommendBookListViews.forEach { it.visibility = View.GONE }
            recommendLabel.visibility = View.INVISIBLE
            totalPlanContainer.layoutParams.height = DisplayUtils.getScreenHeight(requireContext())
            filterView.listener = this@BookFragment
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("유형학습", "나의 플랜 onResume()")
        getMyPlanList {  }
    }

    fun getMyPlanList(cb: () -> Unit) {
        binding.apply {
            BookManager.getMyBookList(requireContext(), user!!) {
                myBooks = it

                myPlanCntTv?.text = "총 ${it?.myPieceStorageList?.size ?: 0}개 "
                pinCntTv?.text = "핀 설정 ${it?.pinBookPlanCount ?: 0}개 "
                if (it == null || it.myPieceStorageList.size == 0) {
                    myBookEmptyContainer?.visibility = View.VISIBLE
                    myPlanRv?.visibility = View.INVISIBLE
                } else {
                    myBookEmptyContainer?.visibility = View.INVISIBLE
                    myPlanRv?.visibility = View.VISIBLE
                    myPlanRv?.adapter?.notifyDataSetChanged()
                }

                cb()
            }
        }
    }

    fun getRecommendList(cb: () -> Unit) {
        BookManager.getRecommendBookList(requireContext(), user!!) {
            if (it != null) {
                for (i in 0 until it.size) {
                    val recommend = it[i]
                    val title = recommend.title
                    val bookList = recommend.targetBookPlanList
                    val view = recommendBookListViews.getOrNull(i)
                    view?.set(bookList.toMutableList(), title, i + 1, this@BookFragment)
                    view?.show { }
                }
                binding.recommendLabel.showIfNeed()
                binding.planLoadingView.cancelAnimation()
                binding.planLoadingView.visibility = View.INVISIBLE
                cb()
            }
        }
    }

    private fun getRecommendListWithoutRefresh(cb: () -> Unit) {
        BookManager.getRecommendBookList(requireContext(), user!!) {
            if (it != null) {
                for (i in 0 until it.size) {
                    val recommend = it[i]
                    val title = recommend.title
                    val bookList = recommend.targetBookPlanList
                    val view = recommendBookListViews.getOrNull(i)
                    view?.set(bookList.toList())
                }
                binding.recommendLabel.showIfNeed()
                binding.planLoadingView.cancelAnimation()
                binding.planLoadingView.visibility = View.INVISIBLE
                cb()
            }
        }
    }

    fun getTotalList() {
        binding.apply {
            totalEmptyContainer.visibility = View.INVISIBLE
            totalRv.visibility = View.INVISIBLE
            totalRv.scrollTo(0, 0)
            totalRv.adapter = null
            totalLoadingView.visibility = View.VISIBLE
            totalLoadingView.playAnimation()

            val filters = filterView.selectedFilterTypes.toSet()

            BookManager.getBooks(requireContext(), user!!, filters, cb = { books, filter ->
                if (filter == filterView.selectedFilterTypes) {
                    totalBooks = books.toMutableList()
                    totalRv.visibility = View.VISIBLE
                    totalRv.adapter = TotalPlanAdapter()

                    if (books.isEmpty()) {
                        totalEmptyContainer.show(300)
                        totalRv.adapter?.notifyDataSetChanged()
                    } else {
                        val totalRvAnimationController = AnimationUtils.loadLayoutAnimation(
                            requireContext(),
                            R.anim.recyclerview_grid_layout_animation
                        )
                        totalRv.layoutAnimation = totalRvAnimationController
                        totalRv.adapter?.notifyDataSetChanged()
                        totalRv.scheduleLayoutAnimation()
                    }
                }
                totalLoadingView.visibility = View.INVISIBLE
                totalLoadingView.cancelAnimation()
            })
        }
    }

    private fun getTotalListWithoutRefresh() {
        binding.apply {
            totalPlanCover.visibility = View.VISIBLE
            totalLoadingView.visibility = View.VISIBLE
            totalLoadingView.playAnimation()
            val filters = filterView.selectedFilterTypes.toSet()

            BookManager.getBooks(requireContext(), user!!, filters, cb = { books, filter ->
                if (filter == filterView.selectedFilterTypes) {
                    totalBooks?.clear()
                    totalBooks?.addAll(books.toMutableList())

                    if (books.isEmpty()) {
                        totalEmptyContainer.show(300)
                    }
                    totalRv.adapter?.notifyDataSetChanged()
                }
                totalLoadingView.visibility = View.INVISIBLE
                totalLoadingView.cancelAnimation()
                totalPlanCover.visibility = View.GONE
            })
        }
    }

    fun syncRecommendBook(book: Book) {
        recommendBookListViews.forEach {
            val books = it.books
            val index = books?.indexOfFirst {
                it.id == book.id
            }
            if (index != null && index >= 0) {
                books[index] = book
                it.set(books)
            }
        }
    }

    fun syncTotalBook(book: Book) {
        val books = totalBooks
        val index = books?.indexOfFirst {
            if(book.assignID != null)
                book.assignID == it.assignID
            else
                it.id == book.id
        }

        if(index != null && index >= 0) {
            books[index] = book
        }

        if(binding.filterView.selectedFilterTypes.contains(FilterType.핀_미포함) && book.pin == true) {
            books?.remove(book)
        }

        totalBooks = books
        binding.totalRv.adapter?.notifyDataSetChanged()
    }

    inner class MyPlanAdapter : RecyclerView.Adapter<MyPlanHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlanHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_book_my_plan, parent, false)
            return MyPlanHolder(view)
        }

        override fun getItemCount(): Int {
            return myBooks?.myPieceStorageList?.size ?: 0
        }

        override fun onBindViewHolder(holder: MyPlanHolder, position: Int) {
            val book = myBooks?.myPieceStorageList!![position]
            holder.listener = this@BookFragment
            holder.set(book)
//            if (position == 0) {
//                Tutor.showToolTipIfNeed(holder.actionBtn, Tutor.TooltipType.mailInUnitStudy)
//            }
        }
    }

    inner class TotalPlanAdapter: RecyclerView.Adapter<TotalPlanHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalPlanHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_book_total_plan, parent, false)
            return TotalPlanHolder(view)
        }

        override fun getItemCount(): Int {
            return totalBooks?.size ?: 0
        }

        override fun onBindViewHolder(holder: TotalPlanHolder, position: Int) {
            val book = totalBooks!![position]
            holder.listener = this@BookFragment
            holder.set(book)
        }
    }

    inner class TotalPlanHolder(override var view: View) : PlanHolder(view) {
        val solveCntTv = view.findViewById<TextView>(R.id.solveCntTv)
        val problemCntTv = view.findViewById<TextView>(R.id.problemCntTv)
        val correctRateTv = view.findViewById<TextView>(R.id.correctRateTv)
        val tags = listOf(view.findViewById<TextView>(R.id.tag1), view.findViewById<TextView>(R.id.tag2))
        val guideTv = view.findViewById<TextView>(R.id.guideTv)

        override fun set(book: Book) {
            book.clientBookType = ClientBookType.ALL
            super.set(book)

            setTag(tags) {
                filterFromTagOnCard(it)
            }
            solveCntTv.text = "${book.markedNumber}/${book.totalNumber}"
            problemCntTv.text = book.totalNumber.toString() + "문제"
            correctRateTv.text = "${book.score}%"

            if (book.markedNumber == 0) {
                solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            } else {
                solveCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            }

            guideTv.text = book.description
        }
    }

    override fun onActionBtnClicked(action: ActionType, book: Book, holder: PlanHolder) {
        when (action) {
            ActionType.mail -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일보내기")
                val dialog = EmailInputDialog(requireContext(), listOf(book), user!!, this)
                dialog.show()
            }
            ActionType.pin -> {
                val itemName = if(book.pin) "핀해제하기" else "핀설정하기"
                val itemValue = if(holder is MyPlanHolder) "나의플랜" else if(holder is RecommendPlanHolder) "추천플랜" else "전체플랜"
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", itemName, itemValue)
                BookManager.togglePin(requireContext(), book, user!!) {
                    getMyPlanList {
                        getRecommendListWithoutRefresh {
                            getTotalListWithoutRefresh()
                        }
                    }
                }
            }
            ActionType.delete -> {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "나의플랜빼기")
                BookManager.deleteBook(requireContext(), user!!, book) {
                    myBooks?.removeBook(book) {
                        binding.apply {
                            myPlanRv.adapter?.notifyItemRemoved(it)
                            myPlanCntTv.text = "총 ${myBooks?.myPieceStorageList?.size ?: 0}개 "
                            pinCntTv.text = "핀 설정 ${myBooks?.pinBookPlanCount ?: 0}개 "

                            if (myBooks == null || myBooks!!.myPieceStorageList.size == 0) {
                                myBookEmptyContainer.visibility = View.VISIBLE
                                myPlanRv.visibility = View.INVISIBLE
                            } else {
                                myBookEmptyContainer.visibility = View.INVISIBLE
                                myPlanRv.visibility = View.VISIBLE
                                myPlanRv.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onReviewBtnClikced(holder: PlanHolder, book: Book) {
        val itemValue = if(holder is MyPlanHolder) "나의플랜" else if(holder is RecommendPlanHolder) "추천플랜" else "전체플랜"
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "리뷰하기", itemValue)
        val intent = SolveActivity.getReviewIntent(requireContext(), book)
        startActivity(intent)

    }

    override fun onSolveClicked(holder: PlanHolder, book: Book) {
        val intent = SolveActivity.getIntent(requireContext(), book)
        startActivity(intent)
    }

    override fun onSendEmailBtnClicked() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일발송버튼")
    }

    override fun onSentEmail() {
        DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }

    override fun onFilterTypeChanged(view: BookFilterView, filters: Set<FilterType>) {
        getTotalList()
    }

    override fun onMakeCustomBookClicked(holder: PlanHolder, book: Book) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "문제집선택버튼")
        CustomizeBookDialog(requireContext(), this, book).show()
    }

    override fun onMadeCustomBook(dialog: CustomizeBookDialog, book: Book) {
        getMyPlanList {
            (activity as LearningTabActivity).showSnackBar("${book.bookName}으로 워크북이 만들어졌습니다.", "나의플랜으로 이동", action = {
                val index = myBooks?.myPieceStorageList?.indexOfFirst { it.assignID == book.assignID }
                binding.rootView.smoothScrollTo(0, 0)

                if(index != null) {
                    val scroller = object: LinearSmoothScroller(context) {
                        override fun getHorizontalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }
                    scroller.targetPosition = index
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            binding.myPlanRv.layoutManager?.startSmoothScroll(scroller)
                        } catch (e:Exception) {
                            Log.e("워크북생성", "error===>${e.localizedMessage}, position=>$index")
                            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "워크북생성", "나의플랜으로 이동")
                        }
                    }, 300)
                }
            })
            getTotalList()
        }
    }
    fun setFilterType(subject: String): HashSet<FilterType> {
        return when (subject) {
            "미적분" -> hashSetOf(
                FilterType.워크북_미포함,
                FilterType.핀_포함,
                FilterType.계열_전체,
                FilterType.과목_미적분,
                FilterType.과목_수학2,
                FilterType.유형_전체,
                FilterType.추천_2_3등급
            )
            "확률과 통계" -> hashSetOf(
                FilterType.워크북_미포함,
                FilterType.핀_포함,
                FilterType.계열_전체,
                FilterType.과목_확통,
                FilterType.유형_전체,
                FilterType.추천_2_3등급
            )
            else -> hashSetOf(
                FilterType.워크북_미포함,
                FilterType.핀_포함,
                FilterType.계열_전체,
                FilterType.과목_확통,
                FilterType.유형_전체,
                FilterType.추천_2_3등급
            )
        }
    }
    fun scrollToTotalLabel(subject: String) {
        val targetHashSet = setFilterType(subject)

        binding.filterView.selectedFilterTypes = targetHashSet
        binding.filterView.adapter?.notifyDataSetChanged()

        getTotalList()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.rootView.scrollToView(binding.totalLabel)

        }, 1000)
    }
    fun filterFromTagOnCard(type: FilterType) {

        binding.filterView.selectedFilterTypes.add(type)
        binding.filterView.selectedFilterTypes.removeAll(type.exclusiveSet)
        binding.filterView.adapter?.notifyDataSetChanged()

        getTotalListWithoutRefresh()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.rootView.scrollToView(binding.totalLabel)

        }, 1000)
    }
}