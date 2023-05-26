package com.freewheelin.pulley.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx

interface ArduousSpinnerListener {
    fun onItemClicked(view: ArduousSpinner, position: Int)
    fun onListShown() {}
}

class ArduousSpinner : ConstraintLayout, View.OnClickListener {


    var items: List<String> = ArrayList()
    var defaultStr: String? = null
        set(value) {
            field = value
            mainBtn.text = value
        }
    var popupWindow: PopupWindow? = null
    var listener: ArduousSpinnerListener? = null
    var position: Int? = null
    var listMaxheight = 316.toPx()
    var itemHeight = 48.toPx()
    lateinit var arduousList: ArduousList

    constructor(context: Context, defaultStr: String) : super(context) {
        this.defaultStr = defaultStr
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
        arduousList = ArduousList(context)
    }

    constructor(context: Context, defaultStr: String, items: ArrayList<String>) : super(context) {
        this.defaultStr = defaultStr
        this.items = items
        arduousList = ArduousList(context)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ArduousSpinner)
        val rawValueForDefaultStr = array.getString(R.styleable.ArduousSpinner_defaultStr)
        defaultStr = rawValueForDefaultStr
        itemHeight = array.getDimensionPixelOffset(R.styleable.ArduousSpinner_Arduous_itemHeight, itemHeight)
        listMaxheight = array.getDimensionPixelOffset(R.styleable.ArduousSpinner_Arduous_listMaxHeight, listMaxheight)
        array.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if(enabled) {
            mainBtn.setTextColor(ContextCompat.getColor(context,R.color.gray_800))
            mainBtn.setOnClickListener(this)
            arrowIv.setColorFilter(ContextCompat.getColor(context, R.color.gray_800))
            this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round)
        } else {
            mainBtn.setTextColor(ContextCompat.getColor(context,R.color.gray_400))
            mainBtn.setOnClickListener(null)
            arrowIv.setColorFilter(ContextCompat.getColor(context, R.color.gray_400))
            this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_400_round)
        }
    }

    private fun getProperHeight(): Int {
        return minOf(listMaxheight, (itemHeight * items.size))
    }

    var mainBtn: Button
    var arrowIv: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.spinner_arduous, this, true)
        this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round)
        isFocusableInTouchMode = true

        mainBtn = findViewById(R.id.mainBtn)
        arrowIv = findViewById(R.id.arrowIv)

        mainBtn.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        if(items.isNotEmpty()) {
            Log.d("팝업", "몇번불리냐")
            val locationInts = IntArray(2)
            getLocationOnScreen(locationInts)

            popupWindow = PopupWindow(arduousList, this@ArduousSpinner.width, getProperHeight())

            popupWindow?.windowLayoutType = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW
            arduousList.setItem()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, android.R.color.transparent)))

            if(popupWindow?.isShowing() == true) {
                popupWindow?.dismiss()
            }

            popupWindow?.showAtLocation(arduousList.rootView, Gravity.NO_GRAVITY, locationInts[0], locationInts[1] - DisplayUtils.getStatusbarHeight(context))
            arduousList.show()

            arduousList.layoutParams.height = getProperHeight()
//            arduousList.requestLayout()
            this.requestFocus()
            listener?.onListShown()
        }
    }

    inner class ArduousList : LinearLayout {

        constructor(context: Context) : super(context) {
            setItem()
        }

        var listRv:RecyclerView

        init {
            LayoutInflater.from(context).inflate(R.layout.view_arduous_list, this, true)
            this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round)
            orientation = VERTICAL

            listRv = findViewById(R.id.listRv)
        }

        fun setItem() {
            listRv.adapter = ArduousAdapter()
            listRv.layoutManager = LinearLayoutManager(context)
        }

        fun show() {
            var height = getProperHeight()
            val showAnimator = ValueAnimator.ofInt(this@ArduousSpinner.measuredHeight, height)
            showAnimator.addUpdateListener {
                listRv.post {
                    var layoutParam = layoutParams
                    layoutParam.height = it.animatedValue as Int
                    this.requestLayout()
                }
            }
            showAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                }
            })
            showAnimator.duration = 150
            showAnimator.start()
        }

        inner class ArduousAdapter : RecyclerView.Adapter<ViewHolder> {
            constructor()

            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
                val holder =  ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_arduous_spinner, p0, false))
                holder.itemView.findViewById<ConstraintLayout>(R.id.containerCl).layoutParams.height = itemHeight
                return holder
            }

            override fun getItemCount(): Int {
                return items.size
            }

            override fun onBindViewHolder(vh: ViewHolder, position: Int) {
                vh.button.text = items[position]
                vh.button.setOnClickListener {
                    this@ArduousSpinner.position = position
                    this@ArduousSpinner.mainBtn.text = items[position]
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        this@ArduousSpinner.popupWindow?.dismiss()
                    }, 350)

                    listener?.onItemClicked(this@ArduousSpinner, position)
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var button = view.findViewById<Button>(R.id.listItem)
        val containerCl = view.findViewById<ConstraintLayout>(R.id.containerCl)
    }
}