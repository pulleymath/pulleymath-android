package com.freewheelin.pulley.revision2023.ui.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.solve.AnswerShadowBuilder
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.toDp
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.revision2021.utils.getBottomNavigationBarHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ClickableViewAccessibility")
class ChatBotFloatingBtn: ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    companion object {
        var x: Float? = null
        var y: Float? = null
        var initialBottomYPosition: Float? = null
        var buttonLongClickDescCount: Int = 0
    }
    val screenWidth by lazy { DisplayUtils.getScreenWidth(context) }
    val screenHeight by lazy { DisplayUtils.getScreenHeight(context) }
    var floatingButton: CardView
    var chatBotIntroduceTv: TextView
    var chatBotLottie: LottieAnimationView
    init {
        LayoutInflater.from(context).inflate(R.layout.view_chat_bot_floating_btn, this)

        floatingButton = findViewById(R.id.floatingButton)
        chatBotIntroduceTv = findViewById(R.id.chatBotIntroduceTv)
        chatBotLottie = findViewById(R.id.chatBotLottie)
        playLottie()
//        setInitPosition()
        this.setOnDragListener { view, dragEvent ->
            println("qwpqwp chatBotDragListener on solve dragEvent: ${dragEvent.action} / ${dragEvent.x} ${dragEvent.y} ")
            val dragState = (dragEvent.localState as? View)?.id ?: -1
            if (this.id != dragState) return@setOnDragListener true
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (initialBottomYPosition == null) {
                        initialBottomYPosition = this.y
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    dropBtn(dragEvent)
                }
                DragEvent.ACTION_DROP -> {
                    dropBtn(dragEvent)
                }
                else -> {  }
            }

            true
        }
        this.setOnLongClickListener {
            val data = ClipData.newPlainText("chatbot", "chatbot")
            val shadowBuilder = AnswerShadowBuilder(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(data, shadowBuilder, this, 0)
            } else {
                it.startDrag(data, shadowBuilder, this, 0)
            }
            true
        }
    }

    private fun dropBtn (dragEvent: DragEvent) {
        if (dragEvent.x == 0f && dragEvent.y == 0f) return
        val viewHeight = this.height
        val viewWidth = this.width
        val x = dragEvent.x - viewWidth / 2f
        val y = dragEvent.y - viewHeight / 2f - 106.toDp()

        val screenLeft = x < screenWidth / 2f
        val screenTop = y < screenHeight / 2f
        val bottomPositionY = screenHeight - floatingButton.height - 48f.toPx()


        when {
            screenLeft -> {
                if (screenTop) {
                    setFloatingPosition(48f.toPx(), 48f.toPx())
                } else {
                    setFloatingPosition(48f.toPx(), bottomPositionY)
                }
            }

            !screenLeft -> {
                println("qwpqwp 2 screenWidth.toFloat(): ${screenWidth.toFloat()}, viewWidth :${viewWidth}, 48f.toPx() :${48f.toPx()}")
                val xPosition = screenWidth.toFloat() - floatingButton.width - 48f.toPx()
                if (screenTop) {
                    setFloatingPosition(xPosition, 48f.toPx())
                } else {
                    setFloatingPosition(xPosition, bottomPositionY)
                }
            }
        }
    }

    fun playLottie() {
        chatBotLottie.playAnimation()
    }
    fun startLongClickDescAnim() {
        if (buttonLongClickDescCount > 0) return
        buttonLongClickDescCount += 1
        CoroutineScope(Dispatchers.Main).launch {
            chatBotIntroduceTv.text = "화면이 가려지면 나를 길게 눌러서 다른 곳으로 이동 시켜봐!"
            delay(3000)
            val isLeftPosition = true
            val anim = if (isLeftPosition) R.anim.slide_from_left_with_opacity else R.anim.slide_from_right_with_opacity
            val animUtil = AnimationUtils.loadAnimation(context, anim)
            chatBotIntroduceTv.startAnimation(animUtil)
            chatBotIntroduceTv.visibility = View.VISIBLE
            animUtil.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val finishAnim = if (isLeftPosition) R.anim.slide_to_left_with_opacity else R.anim.slide_to_right_with_opacity
                        val finishAnimUtil = AnimationUtils.loadAnimation(context, finishAnim)
                        finishAnimUtil.setAnimationListener(object: Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {}
                            override fun onAnimationRepeat(p0: Animation?) {}
                            override fun onAnimationEnd(p0: Animation?) {
                                chatBotIntroduceTv.visibility = View.GONE
                            }
                        })
                        chatBotIntroduceTv.startAnimation(finishAnimUtil)
                    }, 4000)
                }
            })
        }
    }
    fun startDescriptionAnim() {
        CoroutineScope(Dispatchers.Main).launch {
            chatBotIntroduceTv.text = "문제를 풀다가 모르는 게 있으면 언제든지 물어봐!"
            delay(3000)
            val isLeftPosition = true
            val anim = if (isLeftPosition) R.anim.slide_from_left_with_opacity else R.anim.slide_from_right_with_opacity
            val animUtil = AnimationUtils.loadAnimation(context, anim)
            chatBotIntroduceTv.startAnimation(animUtil)
            chatBotIntroduceTv.visibility = View.VISIBLE
            animUtil.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val finishAnim = if (isLeftPosition) R.anim.slide_to_left_with_opacity else R.anim.slide_to_right_with_opacity
                        val finishAnimUtil = AnimationUtils.loadAnimation(context, finishAnim)
                        finishAnimUtil.setAnimationListener(object: Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {}
                            override fun onAnimationRepeat(p0: Animation?) {}
                            override fun onAnimationEnd(p0: Animation?) {
                                chatBotIntroduceTv.visibility = View.GONE
                            }
                        })
                        chatBotIntroduceTv.startAnimation(finishAnimUtil)
                    }, 2500)
                }


            })
        }

    }
    fun setInitPosition() {
        if (ChatBotFloatingBtn.x != null && ChatBotFloatingBtn.y != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                this.x = ChatBotFloatingBtn.x!!
                this.y = ChatBotFloatingBtn.y!!
            }, 200)
        }
    }

    fun setFloatingPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
        ChatBotFloatingBtn.x = x
        ChatBotFloatingBtn.y = y
    }
    fun removeDragListener() {
        this.setOnDragListener(null)
    }
    fun addDragListenerFromYoutubeWebView(dragEvent: DragEvent) {
        if (dragEvent.action == DragEvent.ACTION_DROP) {
            val viewWidth = this.width
            val xPosition = screenWidth.toFloat() - viewWidth - 48f.toPx()
            setFloatingPosition(xPosition, 48f.toPx())
        }
    }
    fun addDragListener(dragEvent: DragEvent): Boolean {
        val dragState = (dragEvent.localState as? View)?.id ?: -1
        if (dragState != this.id) return true
        when (dragEvent.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (initialBottomYPosition == null) {
                    initialBottomYPosition = this.y
                }
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                dropBtn(dragEvent)
            }
            DragEvent.ACTION_DROP -> {
                dropBtn(dragEvent)

//                val viewHeight = this.height
//                val viewWidth = this.width
//                val x = dragEvent.x - viewWidth / 2f
//                val y = dragEvent.y - viewHeight / 2f - 106.toDp()
//                val screenLeft = x < screenWidth / 2f
//                val screenTop = y < screenHeight / 2f
//
//                val bottomPositionY = screenHeight - floatingButton.height - 48f.toPx()
//                when {
//                    screenLeft -> {
//                        if (screenTop) {
//                            setFloatingPosition(48f.toPx(), 48f.toPx())
//                        } else {
//                            setFloatingPosition(48f.toPx(), bottomPositionY)
//                        }
//                    }
//
//                    !screenLeft -> {
//                        println("qwpqwp screenWidth.toFloat(): ${screenWidth.toFloat()}, viewWidth :${viewWidth}, 48f.toPx() :${48f.toPx()}, floatingButton.width :${floatingButton.width}")
//
//                        val xPosition = screenWidth.toFloat() - floatingButton.width - 48f.toPx()
//                        if (screenTop) {
//                            setFloatingPosition(xPosition, 48f.toPx())
//                        } else {
//                            setFloatingPosition(xPosition, bottomPositionY)
//                        }
//                    }
//                }
            }
        }
        return true
    }
}