package com.freewheelin.pulley.revision2021.cookingmemo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.views.CookingPencilcase
import com.freewheelin.pulley.revision2021.views.CookingPencilcaseView

interface CookingMemoViewListener {
    fun onMemorizing(ev: MotionEvent?)
}

class CookingMemoView: FreeDrawView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var pencilcase: CookingPencilcase? = null
    var listener: CookingMemoViewListener? = null

    var isBlocked: Boolean = false

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

        println("xjcl,  -  -  - memoview onTouch, ${motionEvent?.pointerCount}, pencilcase?.editType: ${pencilcase?.editType}")

        val pencilcaseViewBlack = (pencilcase as? CookingPencilcaseView)
        pencilcaseViewBlack?.pencilOptionLl?.isSelected = false
        pencilcaseViewBlack?.pencilOptionLl?.visibility = View.GONE
        pencilcaseViewBlack?.clearAllBtn?.isSelected = false
        pencilcaseViewBlack?.clearAllBtn?.visibility = View.GONE

        listener?.onMemorizing(motionEvent)

        val buttonType = motionEvent?.buttonState

        if(motionEvent?.action == MotionEvent.ACTION_UP) {
            super.onTouch(view, motionEvent)
            saveDrawing()
            return true
        }

        if(pencilcase?.editType == CookingPencilcase.EditType.pencil && buttonType != MotionEvent.BUTTON_STYLUS_PRIMARY) {
            if (motionEvent?.pointerCount == 2) {
                parent.requestDisallowInterceptTouchEvent(false)
                mPoints.clear()
                return true
            }
            if (motionEvent?.pointerCount == 3) {
                parent.requestDisallowInterceptTouchEvent(false)
                mPoints.clear()
                return false
            }

            if (motionEvent?.action == MotionEvent.ACTION_DOWN) {
                println("xjcl2,  -  -  - pencil mode,  memoview action down ")
                super.onTouch(view, motionEvent)
                return true
            }
            println("xjcl2,  -  -  - pencil mode,  memoview action not down ")
            super.onTouch(view, motionEvent)
            return false

        } else if(pencilcase?.editType == CookingPencilcase.EditType.eraser || buttonType == MotionEvent.BUTTON_STYLUS_PRIMARY) {

            saveHistoryPathFromPoints()
            if (motionEvent?.pointerCount in 2..3 ) {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }

            parent.requestDisallowInterceptTouchEvent(true)
            if (motionEvent?.action == MotionEvent.ACTION_DOWN)
                return true
            super.onTouch(view, motionEvent)
        } else {
            return true
        }

        return false
    }

    private var isWaitingExecutionSignal = false
    private var drawingSaveHandler: Handler? = null

    fun saveDrawing() {
        if (isWaitingExecutionSignal) {
            drawingSaveHandler?.removeCallbacksAndMessages(null)
            drawingSaveHandler = null
        }
        else isWaitingExecutionSignal = true
        addSaveHandler()
    }
    private fun addSaveHandler() {
        drawingSaveHandler = Handler(Looper.getMainLooper())
        drawingSaveHandler!!.postDelayed({
            isWaitingExecutionSignal = false
            println("memoview:: addSaveHandler")
            saveImaged()
        }, 1000)
    }

    var memoId: String = ""
    fun setMemoSavedName(id: Int, subId: Int, memoTag: String) {
        val studentId = user?.studentID ?: return
        memoId = "${memoTag}&&${studentId}&&${id}&&${subId}"
    }
    fun setPatternMemoId(patternId: Int, patternQuizId: Int) {
        val studentId = user?.studentID ?: return
        memoId = "patternmemo&&${studentId}&&${patternId}&&${patternQuizId}"
    }
    fun setCookingMemoId(chapterId: Int, cookingId: Int) {
        val studentId = user?.studentID ?: return
        memoId = "cookingmemo&&${studentId}&&${chapterId}&&${cookingId}"
    }
    fun saveImaged() {
        FileHelper.saveImagedMemo(context, memoId, this)
    }

    fun load() {
        FileHelper.loadMemo(context, memoId, {
            Handler(Looper.getMainLooper()).post {
                clearMemoState()
                loadedBitmap = it
            }
        }, { error ->
            Handler(Looper.getMainLooper()).post {
                undoAll()
            }
        })
    }
    fun erase() {
        FileHelper.eraseMemo(context, memoId)
    }
    fun clearBitmap() {
        loadedBitmap = null
    }
    fun clearMemoState() {
        undoAll()
    }

    override fun restoreStateFromSerializable(state: FreeDrawSerializableState) {
        super.restoreStateFromSerializable(state)
        setPaintWidthDp(pencilcase!!.thickness.width)
        paintColor = pencilcase!!.penColor.value
        paintAlpha = pencilcase!!.penColor.alpha
    }

    fun set(pencilcase: CookingPencilcase) {
        this.pencilcase = pencilcase
        paintColor = pencilcase.penColor.value
        paintAlpha = pencilcase.penColor.alpha
        setPaintWidthDp(pencilcase.thickness.width)
        if (!pencilcase.memoViews.contains(this)) {
            pencilcase.memoViews.add(this)
        }
    }

    fun save(fileName: String) {
        FileHelper.saveStateIntoFile(context, currentViewStateAsSerializable, fileName, null)
    }

    fun load(fileName: String) {
        FileHelper.getSavedStoreFromFile(context, fileName, object : FileHelper.StateExtractorInterface {
            override fun onStateExtracted(state: FreeDrawSerializableState) {
                restoreStateFromSerializable(state)
            }

            override fun onStateExtractionError() {
                undoAll()
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.pointerCount == 1)
            return true

        return super.onTouchEvent(event)
    }
}