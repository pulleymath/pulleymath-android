package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.*
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import com.freewheelin.pulley.bases.isSPYMode
import com.freewheelin.pulley.databinding.ActivityInitTestBinding
import com.freewheelin.pulley.databinding.ViewSelectorInitTestBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InitTestActivity : AppCompatActivity() {
    var problemURLs = emptyList<String>()

    var stage: Int = 1
    val maxStage: Int = 12
    var selected: HashMap<Int, Int> = hashMapOf()

    val score = hashMapOf<Int, List<Int>>(
            1 to listOf(3,2,0),
            2 to listOf(3,2,0),
            3 to listOf(1,3,0),
            4 to listOf(3,1,0),
            5 to listOf(3,2,1),
            6 to listOf(3,2),
            7 to listOf(3,2,1),
            8 to listOf(3,2),
            9 to listOf(3,1,2),
            10 to listOf(3,3,1),
            11 to listOf(3,2,1),
            12 to listOf(2,1,1)
    )

    val knowledgeScore: Int
    get() {
        var value = 0
        value += score[1]?.getOrNull(selected[1] ?: -1) ?: 0
        value += score[2]?.getOrNull(selected[2] ?: -1) ?: 0
        value += score[3]?.getOrNull(selected[3] ?: -1) ?: 0
        value += score[4]?.getOrNull(selected[4] ?: -1) ?: 0

        return value
    }

    val technicalScore: Int
    get() {
        var value = 0
        value += score[6]?.getOrNull(selected[6] ?: -1) ?: 0
        value += score[7]?.getOrNull(selected[7] ?: -1) ?: 0
        value *= score[5]?.getOrNull(selected[5] ?: -1) ?: 0

        return value
    }

    val attitudePlanScore: Int
    get() {
        var value = 0
        value += score[8]?.getOrNull(selected[8] ?: -1) ?: 0
        value += score[9]?.getOrNull(selected[9] ?: -1) ?: 0

        return value
    }

    val attitudeMentalScore: Int
    get() {
        var value = 0
        value += score[10]?.getOrNull(selected[10] ?: -1) ?: 0
        value += score[11]?.getOrNull(selected[11] ?: -1) ?: 0
        value += score[12]?.getOrNull(selected[12] ?: -1) ?: 0

        return value
    }

    val gson by lazy { Gson() }

    companion object {
        const val COMPLETED_SNACK_TEST = 21001

        fun getIntent(context: Context): Intent {
            return Intent(context, InitTestActivity::class.java)
        }
    }

    override fun onBackPressed() {
        if(stage > 1)
            onPrevBtnClicked()
        else {
            LogUtils.logEvent(this, user, PulleyEvent.DIALOG,"이탈방지", "가지마팝업", "초기테스트")
            DialogUtils.showReluctanceDialog(this, leftBtnCB =  {
                finish()
            })
        }
    }
    private val binding: ActivityInitTestBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_init_test, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUI()
        API_V2.getTestProblems(user!!.studentID).enqueue(object : retrofit2.Callback<Map<String, Any>> {
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                responseFailed(this@InitTestActivity, t)
            }

            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                val problems = response.body()?.get("problemUrl")

                if (response.code() == 200 && problems != null) {
                    problemURLs = problems as List<String>

                    Picasso.get().load(problems.getOrNull(0) + "3x.png").fetch(object: com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            binding.apply {
                                loadingLottie.cancelAnimation()
                                loadingLottie.visibility = View.INVISIBLE
                                configure(stage)
                                scrollView.scrollTo(0,0)
                                pageTv.show()
                                pageBar.show()
                                titleTv.show()
                                contentLl.show()
                                btnLl.show()
                            }

                        }

                        override fun onError(e: Exception?) {}

                    })
                    Picasso.get().load(problems.getOrNull(1) + "3x.png").fetch()
                    Picasso.get().load(problems.getOrNull(2) + "3x.png").fetch()
                    Picasso.get().load(problems.getOrNull(3) + "3x.png").fetch()
                } else {

                }

            }
        })
    }

    fun initUI() {
        binding.apply {
            LogUtils.logEvent(this@InitTestActivity, user, PulleyEvent.INIT_TEST, "초기테스트", "진단시작")
            prevBtn.setOnClickListener { onPrevBtnClicked() }
            nextBtn.setOnClickListener { onNextBtnClicked() }

            firstSelector.checkView = firstCheck
            secondSelector.checkView = secondCheck
            thirdSelector.checkView = thirdCheck

            firstSelector.setOnClickListener {
                selected[stage] = 0
                configSelectorUI()
                configBtnUI()
                knowledgeScore
                showScore()
            }

            secondSelector.setOnClickListener{
                selected[stage] = 1
                configSelectorUI()
                configBtnUI()
                showScore()
            }

            thirdSelector.setOnClickListener {
                selected[stage] = 2
                configSelectorUI()
                configBtnUI()
                knowledgeScore
                showScore()
            }
            pageTv.visibility = View.INVISIBLE
            pageBar.visibility = View.INVISIBLE
            titleTv.visibility = View.INVISIBLE
            contentLl.visibility = View.INVISIBLE
            btnLl.visibility = View.INVISIBLE

            setSelectedData()
        }
    }

    private fun setSelectedData() {
        val prefStr = Preferences.initTestData.get()
        if (prefStr.isNullOrEmpty()) return

        val type = object : TypeToken<HashMap<Int, Int>>(){}.type
        val savedData = gson.fromJson<HashMap<Int, Int>>(prefStr, type)
        selected = savedData
        stage = selected.keys.maxOrNull()?.let { if (it == 12) it else it + 1 } ?: 1
        configure(stage)
    }

    override fun onStop() {
        super.onStop()
        Preferences.initTestData.set(gson.toJson(selected))
    }

    fun configure(stage: Int) {
        binding.apply {
            pageTv.text = "${stage}/${maxStage}"
            pageBar.set(stage.toFloat() / maxStage, false)
            titleTv.text = getHeaderText()

            val problemURL = problemURLs.getOrNull(stage - 1)
            if (problemURL != null) {
                ivCl.visibility = View.VISIBLE
                Picasso.get().load(problemURL + "3x.png").into(object: com.squareup.picasso.Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}

                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {

                        problemIv.layoutParams.width = (bitmap.width / 3f).toInt().toPx()
                        problemIv.layoutParams.height = (bitmap.height / 3f).toInt().toPx()
                        problemIv.setImageBitmap(bitmap)
                    }
                })
            } else {
                ivCl.visibility = View.GONE
            }

            configSelectorUI()
            configBtnUI()
        }
    }

    fun configSelectorUI() {
        binding.apply {
            firstSelector.stage = stage
            secondSelector.stage = stage
            thirdSelector.stage = stage

            setSelectorMargin()
            setSpaceIvAndLabel()
            when (stage) {
                1, 2 -> selectorRUSolve()
                3, 4 -> selectorHowScoreIs()
                5 -> selectorHowOftenWrongNote()
                6 -> selectorTypePrefer()
                7 -> selectorHowToSolve()
                8 -> selectorStudyStyle()
                9 -> selectorHowToStudy()
                10 -> selectorManageHardProblem()
                11 -> selectorPreferBook()
                12 -> selectorThinkingResult()
            }
        }
    }

    fun configBtnUI() {
        binding.apply {
            if (stage == 1) {
                prevBtn.visibility = View.GONE
                nextBtn.visibility = View.VISIBLE
            } else {
                prevBtn.visibility = View.VISIBLE
                nextBtn.visibility = View.VISIBLE
            }
            if (stage == 12)
                nextBtn.text = "제출하기"
            else
                nextBtn.text = "다음"

            val selectedIndex = selected[stage]

            if(selectedIndex == null) {
                nextBtn.toDisableUI()
            } else {
                nextBtn.toEnableUI()
            }
        }
    }

    fun selectorRUSolve() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val lp = secondSelector.layoutParams as? ViewGroup.MarginLayoutParams
            lp?.marginStart = 24.toPx()
            lp?.marginEnd = 24.toPx()

            val selectedIndex = selected.get(stage)
            firstSelector.set("당연하죠!", "ic_circle_", selectedIndex == 0)
            secondSelector.set("접근은 가능해요.", "ic_triangle_", selectedIndex == 1)
            thirdSelector.set("아직 안 배웠어요.", "x_", selectedIndex == 2)
        }
    }

    fun selectorHowScoreIs() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set(if (stage == 3) 2 else 3, selectedIndex == 0)
            secondSelector.set(if (stage == 3) 3 else 4, selectedIndex == 1)

            if (stage == 3)
                thirdSelector.set(4, selectedIndex == 2)
            else
                thirdSelector.set("이건 킬러예요!", selectedIndex == 2)
        }
    }

    fun selectorHowOftenWrongNote() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set("정기적으로 해요.", "ic_frequent_regular_", selectedIndex == 0)
            secondSelector.set("생각나면 해요.", "ic_frequent_often_", selectedIndex == 1)
            thirdSelector.set("안해요.", "x_", selectedIndex == 2)
        }
    }

    fun selectorTypePrefer() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.GONE
            thirdCheck.visibility = View.GONE

            val selectedIndex = selected.get(stage)
            firstSelector.set("한 문제집 & 문제\n여러 번 반복", "ic_solve_one_book_multiple_", selectedIndex == 0)
            secondSelector.set("여러 권의\n문제집 & 문제", "ic_solve_multi_book_problem_", selectedIndex == 1)
        }
    }

    fun selectorHowToSolve() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set("대부분 끝까지\n풀어요.", "solve_to_end_", selectedIndex == 0)
            secondSelector.set("일부 교재 & 단원만\n골라 풀어요.", "solve_to_partial_", selectedIndex == 1)
            thirdSelector.set("앞의 몇 장에\n열정을 쏟아요!", "solve_first_", selectedIndex == 2)
        }
    }

    fun selectorStudyStyle() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.GONE
            thirdCheck.visibility = View.GONE

            val selectedIndex = selected.get(stage)
            firstSelector.set("매일 나누어서", "daily_portion_", selectedIndex == 0)
            secondSelector.set("집중될 때 확! 몰아서", "binge_", selectedIndex == 1)
        }
    }

    fun selectorHowToStudy() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set("직접 고른 문제집\n& 강의로 자습", "book_tablet_", selectedIndex == 0)
            secondSelector.set("학원, 과외 등\n숙제 위주로", "ic_solve_multi_book_problem_", selectedIndex == 1)
            thirdSelector.set("직접 고른 문제집 &\n학원/과외 숙제 병행", "book_piece_", selectedIndex == 2)
        }
    }

    fun selectorManageHardProblem() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set("일단\n도전해봐요.", "do_it_", selectedIndex == 0)
            secondSelector.set("해설지를 보며\n접근 방법을 찾아요.", "find_solution_", selectedIndex == 1)
            thirdSelector.set("다음에 봐야지!\n하고 넘어가요.", "skip_", selectedIndex == 2)
        }
    }

    fun selectorPreferBook() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set("도전정신 뿜뿜!\n고난도 문제집", "face_question_", selectedIndex == 0)
            secondSelector.set("풀기 쉬운 난이도의\n문제집", "face_exclamation_mark_", selectedIndex == 1)
            thirdSelector.set("주변에서 많이 풀거나\n유명한 문제집", "faces_discussion_", selectedIndex == 2)
        }
    }

    fun selectorThinkingResult() {
        binding.apply {
            firstSelector.visibility = View.VISIBLE
            secondSelector.visibility = View.VISIBLE
            thirdSelector.visibility = View.VISIBLE
            thirdCheck.visibility = View.VISIBLE

            val selectedIndex = selected.get(stage)
            firstSelector.set("내가\n열심히 했지!", "scary_face_", selectedIndex == 0)
            secondSelector.set("시험문제가\n쉽게 나왔지 :)", "long_note_", selectedIndex == 1)
            thirdSelector.set("운이 좋다!\n(찍은게 맞았다)", "book_fork_", selectedIndex == 2)
        }
    }

    fun setSelectorMargin() {
        val (left, right) = when (stage) {
            5, 7, 9, 10, 11, 12 -> Pair(28, 28)
            else -> Pair(24, 24)
        }
        val lp = binding.secondSelector.layoutParams as? ViewGroup.MarginLayoutParams
        lp?.marginStart = left.toPx()
        lp?.marginEnd = right.toPx()
    }

    fun setSpaceIvAndLabel() {
        binding.apply {
            val lp = thirdSelector.binding.guideIv.layoutParams as? ViewGroup.MarginLayoutParams
            lp?.topMargin = 0

            when (stage) {
                5 -> {
                    firstSelector.setSpaceBetweenImageAndLabel(32.toPx())
                    secondSelector.setSpaceBetweenImageAndLabel(32.toPx())
                    thirdSelector.setSpaceBetweenImageAndLabel(32.toPx())
                }
                10 -> {
                    firstSelector.setSpaceBetweenImageAndLabel(16.toPx())
                    secondSelector.setSpaceBetweenImageAndLabel(16.toPx())
                    thirdSelector.setSpaceBetweenImageAndLabel(39.toPx())
                    lp?.topMargin = 14.toPx()
                }
                else -> {
                    firstSelector.setSpaceBetweenImageAndLabel(16.toPx())
                    secondSelector.setSpaceBetweenImageAndLabel(16.toPx())
                    thirdSelector.setSpaceBetweenImageAndLabel(16.toPx())
                }
            }
        }
    }
    fun onPrevBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.INIT_TEST, "초기테스트", "이전 버튼", null)
        if (stage > 1) {
            stage = stage - 1
        }

        configure(stage)
    }

    fun onNextBtnClicked() {
        binding.apply {
            if(nextBtn.isEnableUI() == false) return

            LogUtils.logEvent(this@InitTestActivity, user, PulleyEvent.INIT_TEST, "초기테스트", "다음 버튼", "${stage}:${selected[stage]!! + 1}")
            if (stage < maxStage) {
                stage = stage + 1
                configure(stage)
                scrollView.scrollTo(0,0)
                titleTv.show()
                contentLl.show()
                btnLl.show()
            } else if (stage == maxStage) {
                LogUtils.logEvent(this@InitTestActivity, user, PulleyEvent.INIT_TEST, "초기테스트", "제출하기")
                submitType()
            }
        }
    }

    fun getHeaderText(): String {
        return when (stage) {
            1, 2 -> "이 문제 풀 수 있나요?"
            3, 4 -> "이 문제 배점은 몇 점일까요?";
            5 -> "오답 노트 얼마나 사용하세요?"
            6 -> "어떤 걸 더 좋아해요?"
            7 -> "문제집 어떻게 풀어요?";
            8 -> "수학 공부할 때 어떤 스타일이에요?";
            9 -> "수학 공부 어떻게 하나요?";
            10 -> "어려운 문제는 어떻게 하나요?";
            11 -> "어떤 문제집을 선호하나요?";
            12 -> "시험 결과가 잘 나왔을 때 당신의 생각은?";
            else -> "이 문제 풀 수 있나요?";
        }
    }

    fun showScore() {
        if(isSPYMode) {
            Toast.makeText(this, "지식: ${knowledgeScore}\n"
                    +"기술: ${technicalScore}\n"
                    +"태도-계획: ${attitudePlanScore}\n"
                    +"태도-학습: ${attitudeMentalScore}\n"
                    +"selected: ${selected}\n"
                    +"최종타입: ${DessertType.getType(
                            knowledgeScore,
                            technicalScore,
                            attitudePlanScore,
                            attitudeMentalScore
                            )}", Toast.LENGTH_LONG)
        }
    }

    fun submitType() {
        val type = DessertType.getType(
                knowledgeScore,
                technicalScore,
                attitudePlanScore,
                attitudeMentalScore
        )
        user?.setUserType(this,
                type,
                knowledgeScore,
                technicalScore,
                attitudePlanScore,
                attitudeMentalScore) {
            val intent = StudyReportActivity.getIntent(this, true)
            startActivity(intent)
            finish()
        }
    }
}


class SelectorView : ConstraintLayout {
    lateinit var binding: ViewSelectorInitTestBinding
    lateinit var checkView: View
    var stage: Int = 1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_selector_init_test, this, true)
    }

    fun set(text: String, imageUrl: String, isSelected: Boolean) {
        binding.apply {
            bigTv.visibility = View.GONE
            smallTv.visibility = View.GONE
            onlyTv.visibility = View.GONE
            guideIv.visibility = View.VISIBLE
            guideLabel.visibility = View.VISIBLE

            size(stage)
            guideLabel.text = text
            val resId = resources.getIdentifier(imageUrl + if (isSelected) "check" else "uncheck", "drawable", context.packageName)
            guideIv.setImageResource(resId)

            configUI(isSelected)
        }
    }

    fun set(score: Int, isSelected: Boolean) {
        binding.apply {
            bigTv.visibility = View.VISIBLE
            smallTv.visibility = View.VISIBLE
            onlyTv.visibility = View.GONE
            guideIv.visibility = View.GONE
            guideLabel.visibility = View.GONE

            size(stage)
            bigTv.text = "$score"
            configUI(isSelected)
        }
    }

    fun set(text: String, isSelected: Boolean) {
        binding.apply {
            bigTv.visibility = View.GONE
            smallTv.visibility = View.GONE
            onlyTv.visibility = View.VISIBLE
            guideIv.visibility = View.GONE
            guideLabel.visibility = View.GONE

            size(stage)
            onlyTv.text = text
            configUI(isSelected)
        }
    }

    fun setSpaceBetweenImageAndLabel(space: Int) {
        val lp = binding.guideLabel.layoutParams as? MarginLayoutParams
        lp?.topMargin = space
    }

    fun size(stage: Int) {
        when (stage) {
            1, 2, 3, 4 -> {
                layoutParams.width = 168.toPx()
                layoutParams.height = 108.toPx()
            }
            5 -> {
                layoutParams.width = 168.toPx()
                layoutParams.height = 184.toPx()
            }
            6, 8 -> {
                layoutParams.width = 268.toPx()
                layoutParams.height = 184.toPx()
            }
            else -> {
                layoutParams.width = 168.toPx()
                layoutParams.height = 184.toPx()
            }
        }
        requestLayout()

    }

    fun configUI(isSelected: Boolean) {
        binding.apply {
            if (isSelected) {
                checkView.visibility = View.VISIBLE
                setBackgroundResource(R.drawable.bg_white_stroke_purple_300_round)
                onlyTv.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
                bigTv.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
                smallTv.setTextColor(ContextCompat.getColor(context, R.color.purple_300))
                guideLabel.setTextColor(ContextCompat.getColor(context, R.color.purple_300))

                onlyTv.typeface = Theme.extraBold(context)
                guideLabel.typeface = Theme.extraBold(context)
            } else {
                checkView.visibility = View.GONE
                setBackgroundResource(R.drawable.bg_white_stroke_gray_500_round)
                onlyTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                bigTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                smallTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
                guideLabel.setTextColor(ContextCompat.getColor(context, R.color.gray_500))

                onlyTv.typeface = Theme.bold(context)
                guideLabel.typeface = Theme.bold(context)
            }
        }
    }


}