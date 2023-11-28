package com.freewheelin.pulley.legacy.views.memoView

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.BUTTON_STYLUS_PRIMARY
import android.view.View
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.revision2023.ui.view.DrawPathType
import com.freewheelin.pulley.revision2023.ui.view.DrawType
import com.freewheelin.pulley.revision2023.ui.view.IPencilPanel
import com.freewheelin.pulley.revision2023.ui.view.PencilPanel


class MemoView: FreeDrawView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var pencilPanel: IPencilPanel? = null
    var fingerDrawMode = false

//    private fun isPencilPanelVisible(): Boolean {
//        return (pencilPanel as? PencilcaseView)?.pencilOptionLl?.visibility == View.VISIBLE
//    }
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

        Log.d("MemoView-onTouch", "event=${motionEvent}")
//        if (motionEvent?.action == MotionEvent.ACTION_DOWN) {
//            isPencilcaseVisibleBeforeOnTouchDraw = isPencilPanelVisible()
//        }

        (pencilPanel as? PencilPanel)?.run {
            penOptionPanelCl.visibility = View.GONE
            eraserPanelCl.visibility = View.GONE
            figurePanelCl.visibility = View.GONE
            transparencyMainPanel()
        }

        val pp = MotionEvent.PointerProperties()
        motionEvent?.getPointerProperties(0, pp)
        if (!fingerDrawMode && pp.toolType != MotionEvent.TOOL_TYPE_STYLUS) {
            parent.requestDisallowInterceptTouchEvent(false)
            return false
        }

        val buttonType = motionEvent?.buttonState
        if(isCookingMemo && motionEvent?.action == MotionEvent.ACTION_UP) {
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

    override fun restoreStateFromSerializable(state: FreeDrawSerializableState) {
        super.restoreStateFromSerializable(state)
        setPaintWidthDp(pencilPanel!!.thickness)
        paintColor = pencilPanel!!.penColorType.value
        paintAlpha = pencilPanel!!.penAlphaType.value
    }

    fun set(penPanel: IPencilPanel) {
        this.pencilPanel = penPanel
        paintColor = penPanel.penColorType.value
        paintAlpha = penPanel.penAlphaType.value
        setPaintWidthDp(penPanel.thickness)
        isCookingMemo = penPanel.isCookingMemo
        if (!penPanel.memoViews.contains(this)) {
            penPanel.memoViews.add(this)
        }
    }

    fun save(fileName: String) {
        println("aspasp save filename: $fileName")
        FileHelper.saveStateIntoFile(context, currentViewStateAsSerializable, fileName, null)
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
        }, 500)
    }

    var memoId: String = ""
    fun setMemoSavedName(id: Int, subId: Int, memoTag: String) {
        val studentId = MyApplication.user?.studentID ?: return
        memoId = "${memoTag}&&${studentId}&&${id}&&${subId}"
    }
    fun setPatternMemoId(patternId: Int, patternQuizId: Int) {
        val studentId = MyApplication.user?.studentID ?: return
        memoId = "patternmemo&&${studentId}&&${patternId}&&${patternQuizId}"
    }
    fun setCookingMemoId(chapterId: Int, cookingId: Int) {
        val studentId = MyApplication.user?.studentID ?: return
        memoId = "cookingmemo&&${studentId}&&${chapterId}&&${cookingId}"
    }
    fun saveImaged() {
        FileHelper.saveImagedMemo(context, memoId, this)
    }

    fun load(fileName: String) {
        println("aspasp load filename: $fileName")
        FileHelper.getSavedStoreFromFile(context, fileName, object : FileHelper.StateExtractorInterface {
            override fun onStateExtracted(state: FreeDrawSerializableState) {
                restoreStateFromSerializable(state)
            }

            override fun onStateExtractionError() {
                undoAll()
            }
        })
    }
    fun load() {
        FileHelper.loadMemo(context, memoId, {
            Handler(Looper.getMainLooper()).post {
                clearMemoState()
                loadedBitmap = it
                notifyRedoUndoCountSetting()
            }
        }, { error ->
            Handler(Looper.getMainLooper()).post {
                println("CookingMemoView Load Error!")
                undoAll()
            }
        })
    }
    fun clearMemoState() {
        undoAll()
    }
    fun clearBitmap() {
        loadedBitmap = null
        loadedBitmapAtWillRedo = null
        notifyRedoUndoCountSetting()
    }
    fun erase() {
        FileHelper.eraseMemo(context, memoId)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.pointerCount == 1)
            return true

        return super.onTouchEvent(event)
    }
}