package com.freewheelin.pulley.legacy.views.memoView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.BUTTON_STYLUS_PRIMARY
import android.view.View
import com.freewheelin.pulley.legacy.views.Pencilcase
import com.freewheelin.pulley.legacy.views.Pencilcase.EditType.eraser
import com.freewheelin.pulley.legacy.views.Pencilcase.EditType.pencil
import com.freewheelin.pulley.legacy.views.PencilcaseView


interface MemoViewListener {
    fun onMemorizing(ev: MotionEvent?)
}

class MemoView: FreeDrawView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var pencilcase: Pencilcase? = null
    var listener: MemoViewListener? = null

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {

//        Log.d("펜체크", "event=${motionEvent}")

        val pencilcaseView2 = (pencilcase as? PencilcaseView)
        pencilcaseView2?.pencilOptionLl?.isSelected = false
        pencilcaseView2?.pencilOptionLl?.visibility = View.GONE
        pencilcaseView2?.clearAllBtn?.isSelected = false
        pencilcaseView2?.clearAllBtn?.visibility = View.GONE

        listener?.onMemorizing(motionEvent)

        val buttonType = motionEvent?.buttonState

        if(pencilcase?.editType == pencil && buttonType != BUTTON_STYLUS_PRIMARY) {
            if (motionEvent?.pointerCount == 2) {
                parent.requestDisallowInterceptTouchEvent(false)
                mPoints.clear()
                return false
            }

            if (motionEvent?.action == MotionEvent.ACTION_DOWN)
                return true

            super.onTouch(view, motionEvent)

            return false

        }else if(pencilcase?.editType == eraser || buttonType == BUTTON_STYLUS_PRIMARY) {

            saveHistoryPathFromPoints()

            if (motionEvent?.pointerCount == 2) {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }

            val iterator = mPaths.iterator()
            parent.requestDisallowInterceptTouchEvent(true)
            while(iterator.hasNext()) {
                val e = iterator.next()

                for (i in 0 until (motionEvent?.historySize ?: 0)) {
                    val point = Point()

                    if (motionEvent?.getHistoricalX(i) == null)
                        continue

                    point.x = motionEvent.getHistoricalX(i)
                    point.y = motionEvent.getHistoricalY(i)

                    if (e.isIn(point)) {
                        iterator.remove()
                        invalidate()
                        break
                    }
                }
            }
            return true
        } else {
            return true
        }

    }


    override fun restoreStateFromSerializable(state: FreeDrawSerializableState) {
        super.restoreStateFromSerializable(state)
        setPaintWidthDp(pencilcase!!.thickness.width)
        paintColor = pencilcase!!.penColor.value
        paintAlpha = pencilcase!!.penColor.alpha
    }

    fun set(pencilcase: Pencilcase) {
        this.pencilcase = pencilcase
        paintColor = pencilcase.penColor.value
        paintAlpha = pencilcase.penColor.alpha
        setPaintWidthDp(pencilcase.thickness.width)
        pencilcase.memoViews.add(this)
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