package com.freewheelin.pulley.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.freewheelin.pulley.core.manage.AndroidID
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


@SuppressLint("StaticFieldLeak")
object APHelper {

    private const val NAME_BASE_PREFERENCE = "base_preferences"
    private const val KEY_DEVICE_ID = "key_device_id"

    private var AppContext: Context? = null
    private var handler: Handler? = null
    private lateinit var sharedPreferences:SharedPreferences

    fun init(appContext: Context) {
        this.AppContext = appContext
        this.handler = Handler(Looper.getMainLooper())
        this.sharedPreferences = appContext.getSharedPreferences(NAME_BASE_PREFERENCE, Context.MODE_PRIVATE)
    }

    fun clear() {
        AppContext = null
        handler = null
    }

    fun px2dp(pixel: Int): Float = pixel / Resources.getSystem().displayMetrics.density

    fun toast(message: String) = Toast.makeText(AppContext, message, Toast.LENGTH_SHORT).show()

    fun toastLong(message: String) = Toast.makeText(AppContext, message, Toast.LENGTH_LONG).show()

    fun post(pRun: Runnable) = handler!!.post(pRun)

    fun post(pRun: Runnable, delayMillis: Long) = handler!!.postDelayed(pRun, delayMillis)

    fun postCancel(pRun: Runnable) = handler!!.removeCallbacks(pRun)

    fun resizeBitmapImage(source: Bitmap, maxResolution: Int): Bitmap {
        val width = source.width
        val height = source.height
        var newWidth = width
        var newHeight = height
        val rate: Float

        if (width > height) {
            if (maxResolution < width) {
                rate = maxResolution / width.toFloat()
                newHeight = (height * rate).toInt()
                newWidth = maxResolution
            }
        } else {
            if (maxResolution < height) {
                rate = maxResolution / height.toFloat()
                newWidth = (width * rate).toInt()
                newHeight = maxResolution
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }

    private val nextGeneratedId = AtomicInteger(1)

    fun generateID(): Int {
        while (true) {
            val result = nextGeneratedId.get()
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue = result + 1
            if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
            if (nextGeneratedId.compareAndSet(result, newValue)) {
                return result
            }
        }
    }

    val deviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    fun deviceId() :String {
        var uid = sharedPreferences.getString(KEY_DEVICE_ID, null) ?: null

        if (uid == null) {
            uid = AndroidID.getUUID()
            sharedPreferences.edit().putString(KEY_DEVICE_ID, uid).commit()
        }
        val nameHash = deviceName.hashCode().toString()
        val fullUuid = "$nameHash-$uid"

        return fullUuid
    }

    val osVersion: String
        get() = Build.VERSION.RELEASE


    private fun capitalize(s: String?): String {
        return if (s == null || s.length == 0) "" else Character.toUpperCase(s[0]) + s.substring(1)
    }


    object Files {

        fun closeStream(stream: OutputStream?) {
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: IOException) {
                }

            }
        }


        fun findFileRecursive(dir: File): List<File> {
            val willReturn = ArrayList<File>()

            val files = dir.listFiles()

            if (files == null || files.size == 0)
                return willReturn

            for (file in dir.listFiles()!!) {
                if (file.isDirectory)
                    willReturn.addAll(findFileRecursive(file))
                else
                    willReturn.add(file)
            }

            return willReturn
        }

    }

}