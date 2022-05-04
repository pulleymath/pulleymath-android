package com.pulleymath.android.pdf.memo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.BUTTON_STYLUS_PRIMARY
import android.view.View
import com.pulleymath.android.pdf.memo.storage.FileHelper


interface MemoViewListener {
    fun onMemorizing(ev: MotionEvent?)
}

class MemoView: FreeDrawView {
    constructor(context: Context): super(context) { }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var memoId: String = ""

    var pencilcase: Pencilcase? = null
    var listener: MemoViewListener? = null

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

        val pencilcaseView2 = (pencilcase as? PencilcaseView)
        pencilcaseView2?.pencilOptionLl?.isSelected = false
        pencilcaseView2?.pencilOptionLl?.visibility = View.GONE
        pencilcaseView2?.clearAllBtn?.isSelected = false
        pencilcaseView2?.clearAllBtn?.visibility = View.GONE

        listener?.onMemorizing(motionEvent)

        val buttonType = motionEvent?.buttonState

        if(motionEvent?.action == MotionEvent.ACTION_UP) {
            super.onTouch(view, motionEvent)
            saveDrawing()
            return true
        } else if (motionEvent?.pointerCount == 2) {
            parent.parent.requestDisallowInterceptTouchEvent(false)
            mPoints.clear()
            return false
        } else if(pencilcase?.editType == Pencilcase.EditType.pencil && buttonType != BUTTON_STYLUS_PRIMARY) {
            if (motionEvent?.action == MotionEvent.ACTION_DOWN)
                return true
            super.onTouch(view, motionEvent)
        }else if(pencilcase?.editType == Pencilcase.EditType.eraser || buttonType == BUTTON_STYLUS_PRIMARY) {
            if (motionEvent?.action == MotionEvent.ACTION_DOWN)
                return true
            super.onTouch(view, motionEvent)
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
            saveImaged()
        }, 1000)
    }

    override fun restoreStateFromSerializable(state: FreeDrawSerializableState) {
        super.restoreStateFromSerializable(state)
        applyPencilMode()
//        setPaintWidthDp(pencilcase!!.thickness.width)
//        paintColor = pencilcase!!.penColor.value
//        paintAlpha = pencilcase!!.penColor.alpha
    }
    fun clearMemoState() {
        undoAll()
    }

    fun applyPencilMode() {
        pencilcase?.apply {
            paintColor = penColor.value
            paintAlpha = penColor.alpha
            setPaintWidthDp(thickness.width)
            when (editType) {
                Pencilcase.EditType.pencil -> setPencil(paintColor, paintAlpha, thickness.width)
                else -> setEraser(ERASE_THICK)
            }
        }
    }

    fun set(pencilcase: Pencilcase) {
        this.pencilcase = pencilcase
        paintColor = pencilcase.penColor.value
        paintAlpha = pencilcase.penColor.alpha
        setPaintWidthDp(pencilcase.thickness.width)
        pencilcase.memoViews.add(this)
    }

    fun save() {
        FileHelper.saveMemo(context, currentViewStateAsSerializable, memoId)
    }

    fun saveImaged() {
        FileHelper.saveImagedMemo(context, memoId, this)
    }

    fun load() {
        FileHelper.loadMemo(context, memoId, { state ->
            Handler(Looper.getMainLooper()).post {
                loadedBitmap = null
                restoreStateFromSerializable(state)
            }
        },{
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.pointerCount?:1 > 1)
            return false
        return super.onTouchEvent(event)
    }
}