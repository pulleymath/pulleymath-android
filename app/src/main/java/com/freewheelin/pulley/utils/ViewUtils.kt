package com.freewheelin.pulley.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Editable
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.dialogs.PulleyPlusPriceDialog
import com.freewheelin.pulley.views.TooltipWindow
import com.freewheelin.pulley.views.balloonWindow.BalloonWindow
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs


object ViewUtils {
    val sNextGeneratedId = AtomicInteger(1)

    fun generateViewId() : Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return generateViewIdSdk17Under()
        } else {
            return View.generateViewId()
        }
    }

    fun generateViewIdSdk17Under() : Int{
        while (true) {
            val result = sNextGeneratedId.get()
            var newValue = result + 1
            if (newValue > 0x00FFFFFF) newValue = 1
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result
            }
        }
    }
}

fun View.extensionTouchArea(space: Int) {
    val parent = parent as View
    parent.post {
        val touchableArea = Rect()
        getHitRect(touchableArea)

        touchableArea.top -= space
        touchableArea.bottom += space
        touchableArea.right += space
        touchableArea.left -= space
        parent.touchDelegate = TouchDelegate(touchableArea, this)
    }
}

fun View.extensionTouchArea(left: Int, top: Int, right: Int, bottom: Int) {
    val parent = parent as View

    parent.post {
        val touchableArea = Rect()
        getHitRect(touchableArea)
        touchableArea.top -= top
        touchableArea.bottom += bottom
        touchableArea.right += right
        touchableArea.left -= left
        parent.touchDelegate = TouchDelegate(touchableArea, this)
    }
}

fun View.show(duration:Long = 500, cb:((view: View) -> Unit)? = null) {
    visibility = View.VISIBLE
    val anim = AlphaAnimation(0f, 1f)
    anim.duration = duration
    anim.setAnimationListener(object: Animation.AnimationListener{
        override fun onAnimationRepeat(p0: Animation?) {
        }

        override fun onAnimationEnd(p0: Animation?) {
            if (cb == null) return else cb(this@show)
        }

        override fun onAnimationStart(p0: Animation?) {
        }
    })
    startAnimation(anim)
}

enum class ViewTransition {
    Instant,
    SlideFromDown,
    SlideFromUp,
    SlideFromLeft,
    SlideFromRight
}

fun View.showTransition(duration: Long = 500, transition: ViewTransition, cb:((view: View) -> Unit)? = null) {
    // https://stackoverflow.com/questions/5151591/android-left-to-right-slide-animation
    visibility = View.VISIBLE

    val anim = when(transition) {
        ViewTransition.Instant -> TranslateAnimation(0f, 0f, 0f, 0f)
        ViewTransition.SlideFromDown -> TranslateAnimation(0f, 0f, height.toFloat(), 0f)
        ViewTransition.SlideFromUp -> TranslateAnimation(0f, 0f, -height.toFloat(), 0f)
        ViewTransition.SlideFromLeft -> TranslateAnimation(-width.toFloat(), 0f, 0f, 0f)
        ViewTransition.SlideFromRight -> TranslateAnimation(width.toFloat(), 0f, 0f, 0f)
    }
    anim.duration = duration
    anim.fillAfter = true
    anim.setAnimationListener(object: Animation.AnimationListener{
        override fun onAnimationRepeat(p0: Animation?) {}
        override fun onAnimationStart(p0: Animation?) {}
        override fun onAnimationEnd(p0: Animation?) {
            cb?.let { it(this@showTransition) }
        }
    })
    startAnimation(anim)

}

fun View.showIfNeed(duration: Long = 500, cb:((view: View) -> Unit)? = null) {
    if(visibility == View.VISIBLE)
        return
    else
        show(duration, cb)
}

fun View.hide(duration:Long = 500, cb:(() -> Unit)? = null) {
    if(this.visibility == View.INVISIBLE)
        return

    val anim = AlphaAnimation(1f, 0f)
    anim.duration = duration
    anim.setAnimationListener(object: Animation.AnimationListener{
        override fun onAnimationRepeat(p0: Animation?) {
        }

        override fun onAnimationEnd(p0: Animation?) {
            visibility = View.INVISIBLE
            if(cb == null) return else cb()
        }

        override fun onAnimationStart(p0: Animation?) {
        }
    })
    startAnimation(anim)
}

fun View.hideIfNeed(duration: Long = 500, cb:(() -> Unit)? = null) {
    if(visibility == View.INVISIBLE || visibility == View.GONE)
        return
    else
        hide(duration, cb)
}

fun View.setPermissionClickListener(cb: (view: View) -> Unit) {
    this.setOnClickListener {
        if(user?.hasPulleyPlus == true) {
            cb(it)
    //            DialogUtils.showExpiredDialog(context)
        } else {
            DialogUtils.confirmHasPulleyPlus(context) {
                PulleyPlusPriceDialog(context).show()
            }
        }
    }
}

fun View.setPaddingLeft(padding: Int) {
    setPadding(
            padding,
            paddingTop,
            paddingRight,
            paddingBottom
    )
}

fun View.setPaddingRight(padding:Int) {
    setPadding(
            paddingLeft,
            paddingTop,
            padding,
            paddingBottom
    )
}

fun View.setPaddingBottom(padding: Int) {
    setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            padding
    )
}

fun View.setPaddingTop(padding: Int) {
    setPadding(
            paddingLeft,
            padding,
            paddingRight,
            paddingBottom
    )
}

fun View.getTargetAbsolutePosition(isExistStatusBar: Boolean = true): Pair<Float, Float> {
    val locations = IntArray(2)
    getLocationInWindow(locations)
    val xPos = locations[0].toFloat()
    var yPos = locations[1].toFloat()
    if(isExistStatusBar)
        yPos = yPos - DisplayUtils.getStatusbarHeight(context)
    return Pair(xPos, yPos)
}

fun View.showBalloon(text: String) {
    val location = IntArray(2).apply {
        getLocationOnScreen(this)
    }

    val position = if(location[1] + (measuredHeight * 0.5) < DisplayUtils.getScreenHeight(context) * 0.5)
        BalloonWindow.Position.below
    else
        BalloonWindow.Position.above
    
    val textView = TextView(context)
    textView.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
    textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp8),  resources.displayMetrics),1f)
    textView.text = text
    textView.typeface = Theme.bold(context)


    val balloon = TooltipWindow(context, this, position)
    val padding = resources.getDimension(R.dimen.dp24).toInt()
    balloon.setPadding(padding, padding, padding, padding)
    balloon.balloonColor = ContextCompat.getColor(context, R.color.purple_ACACFF)
    balloon.show(textView)
}

fun View.getBitmap(scaledWidth: Int? = null, scaledHeight: Int? = null): Bitmap {

    val returnedBitmap = Bitmap.createBitmap(this.width, this.height,Bitmap.Config.ARGB_8888);
    //Bind a canvas to it
    val canvas = Canvas(returnedBitmap)
    //Get the view's background
    val bgDrawable = this.getBackground()
    if (bgDrawable!=null)
        bgDrawable.draw(canvas)
    else
        canvas.drawColor(Color.WHITE);
    // draw the view on the canvas
    this.draw(canvas)
    //return the bitmap

    if(scaledWidth != null || scaledHeight != null) {
        val scaledWidth = (scaledWidth ?: width).toInt()
        val scaledHeight = (scaledHeight ?: height).toInt()
        return Bitmap.createScaledBitmap(returnedBitmap, scaledWidth, scaledHeight, true)
    } else {
        return returnedBitmap
    }

}

fun EditText.toKoreanKeyboard() {
    privateImeOptions = "defaultInputmode=korean"
}

fun EditText.removeKeyboard() {
    this.setOnTouchListener{ v, event ->
        v.onTouchEvent(event)
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
        true
    }


    this.customSelectionActionModeCallback = object : ActionMode.Callback {

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {}

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }
    }
}

fun View.isAbove():Boolean {
    val location = IntArray(2).apply {
        getLocationOnScreen(this)
    }

    return location[1] + (measuredHeight * 0.5) < DisplayUtils.getScreenHeight(context) * 0.5
}



fun EditText.setTextSize(textSize: Float, hintTextSize: Float) {
    this.addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
            if(sequence.isEmpty()) {
                this@setTextSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintTextSize)
            } else {
                this@setTextSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
        }
    })
}

fun ImageView.setImageResourceWithSpecificHeight(res: Int, height: Int) {
    Picasso.get().load(res).resize(0, height).into(this)
}

fun ImageView.setImageResourceWithSpecificSize(res: Int, width: Int, height: Int) {
    Picasso.get().load(res).resize(width, height).into(this)
}

fun ImageView.setImageURL(url: String) {
    if (!this.adjustViewBounds)
        this.adjustViewBounds = true
    Picasso.get()
            .load(url)
            .fit()
            .centerInside()
            .into(this)
}

fun ImageView.setImageURLBackground(url: String) {
    Picasso.get()
            .load(url)
            .into(this)
}

fun ImageView.setProblemImageURL(url: String) {
    if (url.isEmpty()) return

//    val resizeVal = (maxWidth * 1.5).toInt()
    val resizeVal = maxWidth

//    Log.d("이미지", "url=$url")

//    Picasso.get().load(url).into(this)

//    CoroutineScope(Dispatchers.IO).launch {
//        val problemImage = GlideApp.with(context).asBitmap().format(DecodeFormat.PREFER_ARGB_8888).load(url).submit().get()
//        withContext(Dispatchers.Main) {
//            setImageBitmap(problemImage)
//        }
//    }

    Picasso.get()
            .load(url)
            .resize(resizeVal,8000)
            .onlyScaleDown()
            .centerInside()
            .into(this, object: Callback {
                override fun onSuccess() {
                    val bitmap = (drawable as BitmapDrawable).bitmap ?: return

                    var scale = bitmap.width / resizeVal.toFloat()
                    val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                    if (scale > 1 || bitmap.height > bitmap.width)
                        scale = 1f

                    layoutParams.width = ((maxWidth - paddingLeft - paddingRight) * scale).toInt() + paddingLeft + paddingRight
                    layoutParams.height = ((maxWidth - paddingLeft - paddingRight) * scale * ratio + paddingBottom + paddingTop).toInt()

                    requestLayout()
                }

                override fun onError(e: Exception?) {
                }
            })
}

fun ImageView.setSolutionImage(url: String) {
    val widthMaximum = 650f
    Picasso.get()
            .load(url)
            .resize(widthMaximum.toInt(),8000)
            .onlyScaleDown()
            .centerInside()
            .into(this, object: Callback {
                override fun onSuccess() {
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    if(bitmap == null)
                        return


                    var scale = bitmap.width / widthMaximum
                    val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                    if (scale > 1 || bitmap.height > bitmap.width)
                        scale = 1f

                    layoutParams.width = ((maxWidth - paddingLeft - paddingRight) * scale).toInt() + paddingLeft + paddingRight
                    layoutParams.height = ((maxWidth - paddingLeft - paddingRight) * scale * ratio + paddingBottom + paddingTop).toInt()

                    requestLayout()

                }

                override fun onError(e: Exception?) {
                }

            })
}

class CustomTypefaceSpan(family: String, private val newType: Typeface) : TypefaceSpan(family) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.getTypeface()
        if (old == null) {
            oldStyle = 0
        } else {
            oldStyle = old!!.getStyle()
        }

        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.setFakeBoldText(true)
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.setTextSkewX(-0.25f)
        }

        paint.setTypeface(tf)
    }
}

fun ScrollView.scrollToView(view: View) {
    val y = computeDistanceToView(view)
    this.scrollTo(0, y)
}

fun ScrollView.computeDistanceToView(view: View): Int {
    return abs(view.calculateRectOnScreen().top - (this.scrollY + view.calculateRectOnScreen().top))
}

fun View.calculateRectOnScreen(): Rect {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return Rect(
        location[0],
        location[1],
        location[0] + this.measuredWidth,
        location[1] + this.measuredHeight
    )
}
fun ImageView.setImageUrlGlide(url: String) {

    Glide.with(this.context)
//        .load("${url}?time=${Date().time}")
        .load(url)
        .into(this)
}

fun ImageView.setImageUrlPicasso(url: String) {
    Picasso.get()
        .load(url)
//        .load("${url}?time=${Date().time}")
        .into(this)
}

fun ImageView.setImageUrlPicassoDownScale(url: String) {
    CoroutineScope(Dispatchers.IO).launch {

        val requestCreator = Picasso.get()
            .load(url)
//            .load("${url}?time=${Date().time}")

        val width = requestCreator.get().width
        val height = requestCreator.get().height

        withContext(Dispatchers.Main) {
            requestCreator
                .resize(if (height > 5000) 3000 else width, 0)
                .onlyScaleDown()
                .into(this@setImageUrlPicassoDownScale)
        }
    }
}


fun ImageView.setCookingImageURL(url: String) {
    if (url.isEmpty()) return
    val screenWidth by lazy { DisplayUtils.getScreenWidth(this.context) }

    CoroutineScope(Dispatchers.IO).launch {
//        val downloadedImage: Bitmap = Picasso.get().load(url).get()
        // 피카소 쓰는거보다 글라이드가좀더 빠름
        val downloadedImage: Bitmap = Glide.with(this@setCookingImageURL.context)
            .asBitmap()
            .load(url)
//            .load("${url}?time=${Date().time}")
            .submit().get()


        val originalWidth = downloadedImage.width
        val originalHeight = downloadedImage.height

//        val ratio = originalWidth.toFloat() / screenWidth.toFloat()

        // tab s6 lite의 landscape일때 screenWidth는 2000 이고 tab s7의 width 는 2560이다.
        // originalWidth는 문제일때 약 900, 힌트가 포함되었을때 최대 1800 이다.
        // screenWidth 대비 originalWidth 의 사이즈가 tab s6lite 에서 너무 크기 때문에

        // screenWidth가 2200 이하일땐 20%정도 문제사이즈를 줄여서 표현하도록 하였다.
        // 추후에 다른 기기를 대응해야 할때 더 복잡하게 들어가야 할 수도 있다.
        val maxWidth = if(screenWidth < 2200) (originalWidth * 0.8).toInt() else originalWidth


        withContext(Dispatchers.Main) {
            Glide.with(this@setCookingImageURL.context)
                .load(downloadedImage)
                .apply(RequestOptions().override(maxWidth, originalHeight))
                .into(this@setCookingImageURL)
        }
    }
}
fun View.setMarginTop(dp: Int) {
    this.layoutParams = (this.layoutParams as ViewGroup.MarginLayoutParams).apply {
        setMarginTop(dp.toPx())
    }
}
fun View.setMarginBottom(dp: Int) {
    this.layoutParams = (this.layoutParams as ViewGroup.MarginLayoutParams).apply {
        setMarginBottom(dp.toPx())
    }
}
fun View.setMarginStart(dp: Int) {
    this.layoutParams = (this.layoutParams as ViewGroup.MarginLayoutParams).apply {
        marginStart = dp.toPx()
    }
}
fun View.setMarginEnd(dp: Int) {
    this.layoutParams = (this.layoutParams as ViewGroup.MarginLayoutParams).apply {
        marginEnd = dp.toPx()
    }
}