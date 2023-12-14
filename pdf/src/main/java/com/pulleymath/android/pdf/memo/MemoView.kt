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

    var pencilPanel: IPencilPanel? = null
    var listener: MemoViewListener? = null
    var fingerDrawMode = false

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

        (pencilPanel as? PencilPanel)?.run {
            penOptionPanelCl.visibility = View.GONE
            eraserPanelCl.visibility = View.GONE
            figurePanelCl.visibility = View.GONE
            transparencyMainPanel()
        }

        val pp = MotionEvent.PointerProperties()
        motionEvent?.getPointerProperties(0, pp)
        if (!fingerDrawMode && pp.toolType == MotionEvent.TOOL_TYPE_FINGER) {
            parent.parent.requestDisallowInterceptTouchEvent(false)
            return false
        }

        listener?.onMemorizing(motionEvent)

        val buttonType = motionEvent?.buttonState

        if(motionEvent?.action == MotionEvent.ACTION_UP) {
            super.onTouch(view, motionEvent)
            saveDrawing()
            return true

        }

        drawType = null

        if(pencilPanel?.drawType == DrawType.Pencil && buttonType != BUTTON_STYLUS_PRIMARY) {
            drawType = DrawType.Pencil
            if (pencilPanel?.pathType == DrawPathType.Circle) {
                pathType = DrawPathType.Circle
            } else if (pencilPanel?.pathType == DrawPathType.Line) {
                pathType = DrawPathType.Line
            } else if (pencilPanel?.pathType == DrawPathType.Arrow) {
                pathType = DrawPathType.Arrow
            } else {
                pathType = DrawPathType.Curve
            }
            if (motionEvent?.pointerCount == 2) {
                parent.requestDisallowInterceptTouchEvent(false)
                mPoints.clear()
                return false
            }

            if (motionEvent?.action == MotionEvent.ACTION_DOWN)
                return true

            super.onTouch(view, motionEvent)

            return false

        } else if(pencilPanel?.drawType == DrawType.Figure && buttonType != BUTTON_STYLUS_PRIMARY) {
            drawType = DrawType.Figure
            pathType = pencilPanel?.pathType ?: DrawPathType.Curve
            super.onTouch(view, motionEvent)
            return true
        } else if(pencilPanel?.drawType == DrawType.Eraser || buttonType == BUTTON_STYLUS_PRIMARY) {
            pathType = DrawPathType.Curve
            drawType = DrawType.Eraser
            parent.requestDisallowInterceptTouchEvent(true)
            if (motionEvent?.action == MotionEvent.ACTION_DOWN) {
                super.onTouch(view, motionEvent)
                return true
            }
            super.onTouch(view, motionEvent)
            return false
        } else {
            return true
        }
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
        pencilPanel?.apply {
            paintColor = penColorType.value
            paintAlpha = penAlphaType.value
            setPaintWidthDp(thickness)
//            when (drawType) {
//                DrawType.Pencil -> setPencil(paintColor, paintAlpha, thickness.width)
//                else -> setEraser(ERASE_THICK)
//            }
        }
    }

    fun set(pencilcase: PencilPanel) {
        this.pencilPanel = pencilcase
        paintColor = pencilcase.penColorType.value
        paintAlpha = pencilcase.penAlphaType.value
        setPaintWidthDp(pencilcase.thickness)
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
                clearMemoState()
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