package com.freewheelin.pulley.activities.learning.tabFragment

import android.animation.Animator
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.BookManager
import com.freewheelin.pulley.core.manage.BookManager.ARG_BOOK
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.FragmentUnitStudyBinding
import com.freewheelin.pulley.databinding.ItemPieceLearnedInFourBinding
import com.freewheelin.pulley.dialogs.EmailInputDialog
import com.freewheelin.pulley.dialogs.EmailInputDialogListener
import com.freewheelin.pulley.dialogs.UnitPlanAddDialog
import com.freewheelin.pulley.dialogs.UnitPlanAddDialogListener
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.getSerializable
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.*
import com.freewheelin.pulley.views.tooltip.TutorWindow
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow
import com.freewheelin.pulley.views.balloonWindow.BalloonWindowListener
import java.lang.Math.PI
import java.util.*
import kotlin.math.sin

//230209 안쓰는거같음
class UnitStudyFragment : LearningTabFragment(), ArduousSpinnerListener, StudyPlanTemplateInteface, UnitPlanAddDialogListener, EmailInputDialogListener {

    override var screenName = "유형학습"
    var books: ArrayList<Book>? = null
    var filteredBooks: List<Book> = listOf()

    val filterList = listOf("문제집 전체", "학습중인 문제집", "완료한 문제집")

    var isStartWithInitTest = false
    var isNeedLeading = false

    val cntPerPage: Int
        get() {
            val config = requireActivity().resources.configuration
            return if (config.screenWidthDp >= 1280)
                4
            else
                3
        }

    lateinit var clearReceiver: BroadcastReceiver
    lateinit var scoreReceiver: BroadcastReceiver

    companion object {
        @JvmStatic
        fun newInstance() = UnitStudyFragment()
    }

    lateinit var binding: FragmentUnitStudyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clearReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                this@UnitStudyFragment.initUI()
            }
        }

        scoreReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val book = getSerializable(requireActivity(), ARG_BOOK, Book::class.java)

                books?.filter { it.pieceID == book.pieceID }?.forEach {
                    it.markingState = book.markingState
                    it.markedNumber = book.markedNumber
                    it.updateDateTime = Date()
                }
                filteredBooks.filter { it.pieceID == book.pieceID }.forEach {
                    it.markingState = book.markingState
                    it.markedNumber = book.markedNumber
                    it.updateDateTime = Date()
                }

                books?.sortByDescending { it.updateDateTime }
                filteredBooks.sortedByDescending { it.updateDateTime }
                onItemClicked(binding.viewTypeSpinner, binding.viewTypeSpinner.position ?: 0)

            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(clearReceiver, IntentFilter(BookManager.EVENT_BOOK_CLEAR))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(scoreReceiver, IntentFilter(BookManager.EVENT_BOOK_SCORING))

        if(isStartWithInitTest) {
            LogUtils.logEvent(requireContext(), user, PulleyEvent.INDUCE, "페이지뷰", "유형학습")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isStartWithInitTest) {
            initUI()
            wasInitUI = true
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(clearReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(scoreReceiver)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_unit_study, container, false)

        return binding.root
    }

    override fun initUI() {
        if (!::binding.isInitialized) return
        with(binding) {
            guideTv.text = "학습중인 문제집이 없습니다.\n우측 상단의 <문제집 추가하기>로 문제집을 추가해보세요."
            addPieceBtn.setOnClickListener {
                onAddBookBtnClicked()
            }
            pieceVp.adapter = Adapter()
            pieceVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {}

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

                override fun onPageSelected(position: Int) {
                    setPageText(position + 1)
                }
            })
            viewTypeSpinner.items = filterList
            viewTypeSpinner.listener = this@UnitStudyFragment
            configureUI()

            BookManager.getMyBookList(user!!) {
//            this.books = ArrayList(it)
//            this.filteredBooks = it
//            configureUI()
//            pieceVp.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun configureUI() {
        if(books?.isEmpty() == true) {
            binding.guideView.visibility = View.VISIBLE
        } else {
            binding.guideView.visibility = View.GONE
            setPageText(1)
        }
    }

    override fun onItemClicked(view: ArduousSpinner, position: Int) {
        filteredBooks = when(position) {
            0 -> this.books ?: listOf()
            1 -> this.books?.filter { !it.isCompleted() } ?: listOf()
            2 -> this.books?.filter { it.isCompleted() } ?: listOf()
            else -> {
                LogUtils.assert(false, "불가능한 선택 in ${UnitStudyFragment}")
                listOf()
            }
        }

        binding.pieceVp.adapter?.notifyDataSetChanged()
        binding.pieceVp.setCurrentItem(0, false)
        setPageText(1)
    }

    override fun onBookAdded(book: Book) {
        books?.add(0, book)
        configureUI()
        onItemClicked(binding.viewTypeSpinner, binding.viewTypeSpinner.position ?: 0)
    }

    fun onAddBookBtnClicked() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK,"유형학습","새로풀기")
        val dialog = UnitPlanAddDialog(requireContext(), user!!)
        dialog.listener = this
        dialog.show()
    }

    private fun setPageText(page: Int) {
        val remainder = filteredBooks.size % cntPerPage
        binding.pageTv.text = "${page} / ${(filteredBooks.size / cntPerPage) + if (remainder > 0) 1 else 0}"
    }

    override fun onHidden(view: StudyPlanTemplateView, position: Int) {}


    override fun onSolveBtnClicked(book: Book) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "문제집풀기")
        val intent = SolveActivity.getIntent(requireContext(), book)
        startActivity(intent)
    }

    override fun onMailBtnClicked(book: Book) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일")
        val dialog = EmailInputDialog(requireContext(), listOf(book), user!!, this)
        dialog.show()
    }

    override fun onReviewBtnClicked(book: Book) {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "리뷰하기")
        val intent = SolveActivity.getReviewIntent(requireContext(), book)
        startActivity(intent)
    }

    override fun onSendEmailBtnClicked() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "유형학습", "메일 보내기")
    }
    override fun onSentEmail() {
        DaebakToast.show(requireContext(), "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
    }

    inner class Adapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
//            val layout = LayoutInflater.from(context).inflate(R.layout.item_piece_learned_in_four, container, false)
            val itemBinding: ItemPieceLearnedInFourBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.fragment_init_setting_personal, container, false)
//            val layout = itemBinding.root
            itemBinding.root.tag = "page"
            container.addView(itemBinding.root)
            val books = filteredBooks

            itemBinding.template1.set(books[(position * cntPerPage)])
            itemBinding.template1.listener = this@UnitStudyFragment
            itemBinding.template1.position = (position * cntPerPage)
            (itemBinding.root as ViewGroup).layoutTransition = null

            if (position == 0 && isNeedLeading == false) {
                Tutor.showToolTipIfNeed(itemBinding.template1.binding.mailBtn, Tutor.TooltipType.mailInUnitStudy)
            }

            if(isNeedLeading) {
                step7(itemBinding.template1)
                isNeedLeading = false
            }

            val secondPlan = books.getOrNull((position * cntPerPage) + 1)
            val thirdPlan = books.getOrNull((position * cntPerPage) + 2)
            if (secondPlan != null) {
                itemBinding.template2.set(secondPlan)
                itemBinding.template2.visibility = View.VISIBLE
                itemBinding.template2.listener = this@UnitStudyFragment
                itemBinding.template2.position = (position * cntPerPage) + 1

            } else {
                itemBinding.template2.visibility = if (books.size <= 4) View.GONE else View.INVISIBLE
            }

            if (thirdPlan != null) {
                itemBinding.template3.set(thirdPlan)
                itemBinding.template3.visibility = View.VISIBLE
                itemBinding.template3.listener = this@UnitStudyFragment
                itemBinding.template3.position = (position * cntPerPage) + 2
            } else {
                itemBinding.template3.visibility = if (books.size <= 4) View.GONE else View.INVISIBLE
            }


            if(cntPerPage == 4) {
                val fourthPlan = books.getOrNull((position * cntPerPage) + 3)

                if (fourthPlan != null) {
                    itemBinding.template4?.set(fourthPlan)
                    itemBinding.template4?.visibility = View.VISIBLE
                    itemBinding.template4?.listener = this@UnitStudyFragment
                    itemBinding.template4?.position = (position * cntPerPage) + 3
                } else {
                    itemBinding.template4?.visibility = if (books.size <= 4) View.GONE else View.INVISIBLE
                }

            }
            (itemBinding.root as ViewGroup).layoutTransition = LayoutTransition()
            return itemBinding.root
        }

        override fun getCount(): Int {
            val remainder = filteredBooks.size % cntPerPage
            return (filteredBooks.size / cntPerPage) + if (remainder > 0) 1 else 0
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView((view as View))
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

    }

    fun step7(targetView: View) {
        val window = TutorWindow(requireContext(), targetView, BalloonWindow.Position.right)
        window.balloonColor = ContextCompat.getColor(requireContext(), R.color.yellow_f79b00)
        window.setPadding(
                resources.getDimensionPixelSize(R.dimen.dp32),
                resources.getDimensionPixelSize(R.dimen.dp24),
                resources.getDimensionPixelSize(R.dimen.dp32),
                resources.getDimensionPixelSize(R.dimen.dp24)
        )

        val textView = TextView(requireContext())
        textView.text = "${user!!.fullName}님께\n" +
                "딱 맞는 문제집을 추가했어요 :)"
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_ffffff))
        textView.typeface = Theme.extraBold(requireContext())
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
        textView.setLineSpacing(8f.toPx(),1f)

        val anim = ValueAnimator.ofFloat(0f, PI.toFloat())
        anim.duration = 1200
        anim.repeatCount = 3
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {

                Handler(Looper.getMainLooper()).postDelayed({
                    window.dismiss()
                }, 1000)
            }
            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }
        })
        window.setBalloonListener(object: BalloonWindowListener {
            override fun didAppear(window: BalloonWindow) {
                anim.addUpdateListener {
                    val value = it.animatedValue as Float
                    val sign = sin(value)
                    val position = window.position

                    val x = if (position == BalloonWindow.Position.right || position == BalloonWindow.Position.left) window.x + (20 * sign).toInt() else window.x
                    val y = if (position == BalloonWindow.Position.above || position == BalloonWindow.Position.below) window.y + (20 * sign).toInt() else window.y
                    window.update(x, y, window.width, window.height)
                }
                anim.start()
            }

            override fun didDisappear(window: BalloonWindow) {
            }
        })
        window.show(textView)
    }
}




